package org.cl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    // ─── FileEntry ────────────────────────────────────────────────────────────

    @Test void fileEntry_file() {
        var e = new Main.FileEntry("readme.txt", false, 42L);
        assertEquals("readme.txt", e.name());
        assertFalse(e.isDirectory());
        assertEquals(42L, e.size());
    }

    @Test void fileEntry_directory() {
        var e = new Main.FileEntry("subdir", true, 0L);
        assertTrue(e.isDirectory());
        assertEquals(0L, e.size());
    }

    @Test void fileEntry_zeroSizeFile() {
        var e = new Main.FileEntry("empty.txt", false, 0L);
        assertFalse(e.isDirectory());
        assertEquals(0L, e.size());
    }

    // ─── Panel – cursor movement ──────────────────────────────────────────────

    @Test void panel_upDown_basic(@TempDir Path dir) throws IOException {
        createFiles(dir, "a.txt", "b.txt", "c.txt");
        var p = new Main.Panel(dir);
        // entries: ["..", "a.txt", "b.txt", "c.txt"]  (4 entries)
        assertEquals(0, p.cursor);
        p.down(1); assertEquals(1, p.cursor);
        p.down(1); assertEquals(2, p.cursor);
        p.up(1);   assertEquals(1, p.cursor);
    }

    @Test void panel_up_clampAtZero(@TempDir Path dir) throws IOException {
        createFiles(dir, "x.txt");
        var p = new Main.Panel(dir);
        p.up(100);
        assertEquals(0, p.cursor);
    }

    @Test void panel_down_clampAtLast(@TempDir Path dir) throws IOException {
        createFiles(dir, "a.txt", "b.txt");
        var p = new Main.Panel(dir);
        // entries: ["..", "a.txt", "b.txt"]  → max index 2
        p.down(999);
        assertEquals(p.entries.size() - 1, p.cursor);
    }

    @Test void panel_upByZero_noChange(@TempDir Path dir) throws IOException {
        createFiles(dir, "a.txt");
        var p = new Main.Panel(dir);
        p.down(1);
        int before = p.cursor;
        p.up(0);
        assertEquals(before, p.cursor);
    }

    @Test void panel_downByZero_noChange(@TempDir Path dir) throws IOException {
        var p = new Main.Panel(dir);
        p.down(0);
        assertEquals(0, p.cursor);
    }

    // ─── Panel – load() ───────────────────────────────────────────────────────

    @Test void panel_load_alwaysHasDotDotFirst(@TempDir Path dir) {
        var p = new Main.Panel(dir);
        assertEquals("..", p.entries.get(0).name());
        assertTrue(p.entries.get(0).isDirectory());
    }

    @Test void panel_load_emptyDirectory(@TempDir Path dir) {
        var p = new Main.Panel(dir);
        // Only ".." should be present
        assertEquals(1, p.entries.size());
    }

    @Test void panel_load_dirsSortedBeforeFiles(@TempDir Path dir) throws IOException {
        Files.createDirectory(dir.resolve("zeta"));
        Files.createDirectory(dir.resolve("alpha"));
        createFiles(dir, "m.txt", "a.txt");
        var p = new Main.Panel(dir);
        // ["..", "alpha/", "zeta/", "a.txt", "m.txt"]
        assertEquals("..",    p.entries.get(0).name());
        assertEquals("alpha", p.entries.get(1).name());
        assertEquals("zeta",  p.entries.get(2).name());
        assertEquals("a.txt", p.entries.get(3).name());
        assertEquals("m.txt", p.entries.get(4).name());
    }

    @Test void panel_load_dirsHaveZeroSize(@TempDir Path dir) throws IOException {
        Files.createDirectory(dir.resolve("subdir"));
        var p = new Main.Panel(dir);
        var subdir = p.entries.stream().filter(e -> e.name().equals("subdir")).findFirst().orElseThrow();
        assertEquals(0L, subdir.size());
    }

    @Test void panel_load_filesSizeReported(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("data.txt"), "hello");
        var p = new Main.Panel(dir);
        var file = p.entries.stream().filter(e -> e.name().equals("data.txt")).findFirst().orElseThrow();
        assertEquals(5L, file.size());
    }

    @Test void panel_load_cursorClampedOnReload(@TempDir Path dir) throws IOException {
        createFiles(dir, "a.txt", "b.txt", "c.txt");
        var p = new Main.Panel(dir);
        p.down(3); // cursor at last entry (index 3)
        // Remove all files so only ".." remains after reload
        Files.delete(dir.resolve("a.txt"));
        Files.delete(dir.resolve("b.txt"));
        Files.delete(dir.resolve("c.txt"));
        p.load();
        assertEquals(0, p.cursor); // clamped to valid range
    }

    // ─── Panel – adjustScroll ─────────────────────────────────────────────────

    @Test void panel_adjustScroll_cursorAboveScroll(@TempDir Path dir) throws IOException {
        createFiles(dir, "a.txt", "b.txt", "c.txt", "d.txt", "e.txt");
        var p = new Main.Panel(dir);
        p.scroll = 3;
        p.cursor = 1;
        p.adjustScroll(3);
        assertEquals(1, p.scroll); // scroll pulled down to cursor
    }

    @Test void panel_adjustScroll_cursorBelowViewport(@TempDir Path dir) throws IOException {
        createFiles(dir, "a.txt", "b.txt", "c.txt", "d.txt", "e.txt");
        var p = new Main.Panel(dir);
        p.cursor = 5;
        p.scroll = 0;
        p.adjustScroll(3); // viewport = rows 0..2
        assertEquals(3, p.scroll); // cursor 5, visibleRows 3 → scroll = 5-3+1 = 3
    }

    @Test void panel_adjustScroll_cursorInsideViewport_noChange(@TempDir Path dir) throws IOException {
        createFiles(dir, "a.txt", "b.txt", "c.txt");
        var p = new Main.Panel(dir);
        p.scroll = 1;
        p.cursor = 2;
        p.adjustScroll(5);
        assertEquals(1, p.scroll); // cursor is within viewport [1..5], no change
    }

    @Test void panel_adjustScroll_zeroVisibleRows_noChange(@TempDir Path dir) {
        var p = new Main.Panel(dir);
        p.scroll = 0;
        p.cursor = 0;
        p.adjustScroll(0); // should not modify scroll
        assertEquals(0, p.scroll);
    }

    @Test void panel_adjustScroll_negativeVisibleRows_noChange(@TempDir Path dir) {
        var p = new Main.Panel(dir);
        p.adjustScroll(-5);
        assertEquals(0, p.scroll);
    }

    @Test void panel_adjustScroll_scrollNeverNegative(@TempDir Path dir) {
        var p = new Main.Panel(dir);
        p.scroll = 0;
        p.cursor = 0;
        p.adjustScroll(10);
        assertTrue(p.scroll >= 0);
    }

    // ─── Panel – selected() ───────────────────────────────────────────────────

    @Test void panel_selected_returnsCursorEntry(@TempDir Path dir) throws IOException {
        createFiles(dir, "foo.txt");
        var p = new Main.Panel(dir);
        p.down(1); // cursor on "foo.txt"
        assertNotNull(p.selected());
        assertEquals("foo.txt", p.selected().name());
    }

    @Test void panel_selected_atDotDot(@TempDir Path dir) {
        var p = new Main.Panel(dir);
        assertEquals("..", p.selected().name());
    }

    @Test void panel_selected_emptyEntries(@TempDir Path dir) {
        var p = new Main.Panel(dir);
        p.entries.clear();
        p.cursor = 0;
        assertNull(p.selected());
    }

    @Test void panel_selected_negativeCursor(@TempDir Path dir) {
        var p = new Main.Panel(dir);
        p.cursor = -1;
        assertNull(p.selected());
    }

    // ─── Panel – enter() ─────────────────────────────────────────────────────

    @Test void panel_enter_intoDirectory(@TempDir Path dir) throws IOException {
        Path sub = Files.createDirectory(dir.resolve("subdir"));
        var p = new Main.Panel(dir);
        // Move cursor to "subdir" entry
        int idx = indexOfEntry(p, "subdir");
        p.cursor = idx;
        p.enter();
        assertEquals(sub.toRealPath(), p.path);
        assertEquals(0, p.cursor);
        assertEquals(0, p.scroll);
    }

    @Test void panel_enter_onFile_noChange(@TempDir Path dir) throws IOException {
        createFiles(dir, "file.txt");
        var p = new Main.Panel(dir);
        p.cursor = indexOfEntry(p, "file.txt");
        Path before = p.path;
        p.enter();
        assertEquals(before, p.path); // path unchanged
    }

    @Test void panel_enter_dotDot_goesUp(@TempDir Path dir) throws IOException {
        Path sub = Files.createDirectory(dir.resolve("child"));
        var p = new Main.Panel(sub);
        p.cursor = 0; // ".."
        p.enter();
        assertEquals(dir.toRealPath(), p.path);
    }

    @Test void panel_enter_outOfBounds_noChange(@TempDir Path dir) {
        var p = new Main.Panel(dir);
        p.cursor = 999; // beyond entries
        Path before = p.path;
        p.enter(); // should return early
        assertEquals(before, p.path);
    }

    @Test void panel_enter_negativeCursor_noChange(@TempDir Path dir) {
        var p = new Main.Panel(dir);
        p.cursor = -1;
        Path before = p.path;
        p.enter();
        assertEquals(before, p.path);
    }

    // ─── copyDirectory ────────────────────────────────────────────────────────

    @Test void copyDirectory_flatFiles(@TempDir Path tmp) throws IOException {
        Path src = Files.createDirectory(tmp.resolve("src"));
        Path dst = Files.createDirectory(tmp.resolve("dst"));
        Files.writeString(src.resolve("a.txt"), "alpha");
        Files.writeString(src.resolve("b.txt"), "beta");

        Main.copyDirectory(src, dst);

        assertEquals("alpha", Files.readString(dst.resolve("a.txt")));
        assertEquals("beta",  Files.readString(dst.resolve("b.txt")));
    }

    @Test void copyDirectory_nestedDirectories(@TempDir Path tmp) throws IOException {
        Path src   = Files.createDirectory(tmp.resolve("src"));
        Path sub   = Files.createDirectory(src.resolve("sub"));
        Path deep  = Files.createDirectory(sub.resolve("deep"));
        Files.writeString(deep.resolve("leaf.txt"), "leaf");

        Path dst = tmp.resolve("dst");
        Main.copyDirectory(src, dst);

        assertTrue(Files.isDirectory(dst.resolve("sub").resolve("deep")));
        assertEquals("leaf", Files.readString(dst.resolve("sub").resolve("deep").resolve("leaf.txt")));
    }

    @Test void copyDirectory_overwritesExistingFiles(@TempDir Path tmp) throws IOException {
        Path src = Files.createDirectory(tmp.resolve("src"));
        Path dst = Files.createDirectory(tmp.resolve("dst"));
        Files.writeString(src.resolve("f.txt"), "new");
        Files.writeString(dst.resolve("f.txt"), "old");

        Main.copyDirectory(src, dst);

        assertEquals("new", Files.readString(dst.resolve("f.txt")));
    }

    @Test void copyDirectory_preservesFileContents(@TempDir Path tmp) throws IOException {
        Path src = Files.createDirectory(tmp.resolve("src"));
        String content = "line1\nline2\nline3";
        Files.writeString(src.resolve("multi.txt"), content);

        Path dst = tmp.resolve("dst");
        Main.copyDirectory(src, dst);

        assertEquals(content, Files.readString(dst.resolve("multi.txt")));
    }

    @Test void copyDirectory_emptySource(@TempDir Path tmp) throws IOException {
        Path src = Files.createDirectory(tmp.resolve("src"));
        Path dst = tmp.resolve("dst");
        Main.copyDirectory(src, dst);
        assertTrue(Files.isDirectory(dst));
        assertEquals(0, Files.list(dst).count());
    }

    // ─── deleteRecursive ─────────────────────────────────────────────────────

    @Test void deleteRecursive_singleFile(@TempDir Path dir) throws IOException {
        Path f = Files.writeString(dir.resolve("todel.txt"), "bye");
        Main.deleteRecursive(f);
        assertFalse(Files.exists(f));
    }

    @Test void deleteRecursive_emptyDirectory(@TempDir Path dir) throws IOException {
        Path sub = Files.createDirectory(dir.resolve("empty"));
        Main.deleteRecursive(sub);
        assertFalse(Files.exists(sub));
    }

    @Test void deleteRecursive_directoryWithFiles(@TempDir Path dir) throws IOException {
        Path sub = Files.createDirectory(dir.resolve("sub"));
        createFiles(sub, "x.txt", "y.txt");
        Main.deleteRecursive(sub);
        assertFalse(Files.exists(sub));
    }

    @Test void deleteRecursive_nestedTree(@TempDir Path dir) throws IOException {
        Path a = Files.createDirectory(dir.resolve("a"));
        Path b = Files.createDirectory(a.resolve("b"));
        Path c = Files.createDirectory(b.resolve("c"));
        Files.writeString(c.resolve("leaf.txt"), "data");
        Main.deleteRecursive(a);
        assertFalse(Files.exists(a));
    }

    @Test void deleteRecursive_nonExistentPath_throws(@TempDir Path dir) {
        Path ghost = dir.resolve("ghost");
        assertThrows(IOException.class, () -> Main.deleteRecursive(ghost));
    }

    // ─── fmtSize ─────────────────────────────────────────────────────────────

    @Test void fmtSize_zero()            { assertEquals("   0B", Main.fmtSize(0)); }
    @Test void fmtSize_oneB()            { assertEquals("   1B", Main.fmtSize(1)); }
    @Test void fmtSize_maxBytes()        { assertEquals("1023B", Main.fmtSize(1_023)); }
    @Test void fmtSize_exactlyOneK()     { assertEquals("   1K", Main.fmtSize(1_024)); }
    @Test void fmtSize_justAboveOneK()   { assertEquals("   1K", Main.fmtSize(1_025)); }
    @Test void fmtSize_maxKilobytes()    { assertEquals("1023K", Main.fmtSize(1_048_575)); }
    @Test void fmtSize_exactlyOneMeg()   { assertEquals("   1M", Main.fmtSize(1_048_576)); }
    @Test void fmtSize_maxMegabytes()    { assertEquals("1023M", Main.fmtSize(1_073_741_823L)); }
    @Test void fmtSize_exactlyOneGig()   { assertEquals("   1G", Main.fmtSize(1_073_741_824L)); }
    @Test void fmtSize_largeGigabytes()  { assertEquals("   8G", Main.fmtSize(8L * 1_073_741_824L)); }

    // ─── pad ─────────────────────────────────────────────────────────────────

    @Test void pad_zeroLen()             { assertEquals("",    Main.pad("hi",   0)); }
    @Test void pad_negativeLenIsEmpty()  { assertEquals("",    Main.pad("hi",  -1)); }
    @Test void pad_exactFit()            { assertEquals("abc", Main.pad("abc",  3)); }
    @Test void pad_tooShort_padded()     { assertEquals("ab ", Main.pad("ab",   3)); }
    @Test void pad_tooLong_truncated()   { assertEquals("abc", Main.pad("abcd", 3)); }
    @Test void pad_emptyString()         { assertEquals("   ", Main.pad("",     3)); }
    @Test void pad_singleChar()          { assertEquals("a  ", Main.pad("a",    3)); }
    @Test void pad_len1_exact()          { assertEquals("x",   Main.pad("x",    1)); }
    @Test void pad_len1_truncates()      { assertEquals("a",   Main.pad("ab",   1)); }

    // ─── trunc ───────────────────────────────────────────────────────────────

    @Test void trunc_zeroMax()           { assertEquals("",     Main.trunc("hello", 0)); }
    @Test void trunc_negativeMax()       { assertEquals("",     Main.trunc("hello", -1)); }
    @Test void trunc_exactFit()          { assertEquals("abc",  Main.trunc("abc",   3)); }
    @Test void trunc_shorterThanMax()    { assertEquals("ab",   Main.trunc("ab",    5)); }
    @Test void trunc_longerThanMax()     { assertEquals("abcd~",Main.trunc("abcdef",5)); }
    @Test void trunc_max1_anyString()    { assertEquals("~",    Main.trunc("ab",    1)); }
    @Test void trunc_max1_singleChar()   { assertEquals("a",    Main.trunc("a",     1)); }
    @Test void trunc_emptyString()       { assertEquals("",     Main.trunc("",      5)); }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static void createFiles(Path dir, String... names) throws IOException {
        for (String name : names) Files.createFile(dir.resolve(name));
    }

    private static int indexOfEntry(Main.Panel p, String name) {
        for (int i = 0; i < p.entries.size(); i++)
            if (p.entries.get(i).name().equals(name)) return i;
        throw new AssertionError("Entry not found: " + name);
    }
}
