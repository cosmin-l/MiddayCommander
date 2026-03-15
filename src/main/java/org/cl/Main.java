package org.cl;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {

    record FileEntry(String name, boolean isDirectory, long size, long modified) {}

    static class Panel {
        Path path;
        List<FileEntry> entries = new ArrayList<>();
        int cursor = 0;
        int scroll = 0;

        Panel(Path path) {
            this.path = path;
            load();
        }

        void load() {
            entries.clear();
            entries.add(new FileEntry("..", true, 0, 0));
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                List<FileEntry> dirs  = new ArrayList<>();
                List<FileEntry> files = new ArrayList<>();
                for (Path p : ds) {
                    try {
                        BasicFileAttributes a = Files.readAttributes(p, BasicFileAttributes.class);
                        String name = p.getFileName().toString();
                        long   mod  = a.lastModifiedTime().toMillis();
                        if (a.isDirectory()) dirs.add(new FileEntry(name, true, 0, mod));
                        else                 files.add(new FileEntry(name, false, a.size(), mod));
                    } catch (IOException ignored) {}
                }
                dirs.sort(Comparator.comparing(FileEntry::name));
                files.sort(Comparator.comparing(FileEntry::name));
                entries.addAll(dirs);
                entries.addAll(files);
            } catch (IOException ignored) {}
            cursor = Math.max(0, Math.min(cursor, entries.size() - 1));
        }

        void up(int n)   { cursor = Math.max(0, cursor - n); }
        void down(int n) { cursor = Math.min(entries.size() - 1, cursor + n); }

        void enter() {
            if (cursor < 0 || cursor >= entries.size()) return;
            FileEntry e = entries.get(cursor);
            if (!e.isDirectory()) return;
            Path next = e.name().equals("..") ? path.getParent() : path.resolve(e.name());
            if (next == null) return;
            try {
                Path real = next.toRealPath();
                if (Files.isDirectory(real)) {
                    path   = real;
                    cursor = 0;
                    scroll = 0;
                    load();
                }
            } catch (IOException ignored) {}
        }

        void adjustScroll(int visibleRows) {
            if (visibleRows <= 0) return;
            if (cursor < scroll) scroll = cursor;
            if (cursor >= scroll + visibleRows) scroll = cursor - visibleRows + 1;
            scroll = Math.max(0, scroll);
        }

        FileEntry selected() {
            return (cursor >= 0 && cursor < entries.size()) ? entries.get(cursor) : null;
        }
    }

    // ─── Themes ──────────────────────────────────────────────────────────────────

    record Theme(String name, TextColor accent, TextColor accentFg, TextColor border, TextColor dirFg) {}

    static final Theme[] THEMES = {
        new Theme("Norton Commander",TextColor.ANSI.CYAN,    TextColor.ANSI.BLACK, TextColor.ANSI.CYAN,    TextColor.ANSI.CYAN),
        new Theme("Midnight Blue",  TextColor.ANSI.BLUE,    TextColor.ANSI.WHITE, TextColor.ANSI.WHITE,   TextColor.ANSI.CYAN),
        new Theme("Classic MC",     TextColor.ANSI.CYAN,    TextColor.ANSI.BLACK, TextColor.ANSI.WHITE,   TextColor.ANSI.CYAN),
        new Theme("Forest Green",   TextColor.ANSI.GREEN,   TextColor.ANSI.BLACK, TextColor.ANSI.WHITE,   TextColor.ANSI.GREEN),
        new Theme("Crimson Night",  TextColor.ANSI.RED,     TextColor.ANSI.WHITE, TextColor.ANSI.WHITE,   TextColor.ANSI.YELLOW),
        new Theme("Magenta Dream",  TextColor.ANSI.MAGENTA, TextColor.ANSI.BLACK, TextColor.ANSI.WHITE,   TextColor.ANSI.CYAN),
        new Theme("Solar Flare",    TextColor.ANSI.YELLOW,  TextColor.ANSI.BLACK, TextColor.ANSI.WHITE,   TextColor.ANSI.GREEN),
        new Theme("Neon Matrix",    TextColor.ANSI.GREEN,   TextColor.ANSI.BLACK, TextColor.ANSI.GREEN,   TextColor.ANSI.GREEN),
        new Theme("Deep Ocean",     TextColor.ANSI.BLUE,    TextColor.ANSI.WHITE, TextColor.ANSI.CYAN,    TextColor.ANSI.CYAN),
        new Theme("Rose Garden",    TextColor.ANSI.RED,     TextColor.ANSI.WHITE, TextColor.ANSI.MAGENTA, TextColor.ANSI.MAGENTA),
        new Theme("Arcane",         TextColor.ANSI.MAGENTA, TextColor.ANSI.WHITE, TextColor.ANSI.CYAN,    TextColor.ANSI.CYAN),
        new Theme("Arctic Ice",     TextColor.ANSI.WHITE,   TextColor.ANSI.BLACK, TextColor.ANSI.CYAN,    TextColor.ANSI.CYAN),
        new Theme("Autumn Fire",    TextColor.ANSI.YELLOW,  TextColor.ANSI.BLACK, TextColor.ANSI.RED,     TextColor.ANSI.RED),
        new Theme("Obsidian",       TextColor.ANSI.WHITE,   TextColor.ANSI.BLACK, TextColor.ANSI.WHITE,   TextColor.ANSI.WHITE),
        new Theme("Electric Storm", TextColor.ANSI.CYAN,    TextColor.ANSI.BLACK, TextColor.ANSI.MAGENTA, TextColor.ANSI.MAGENTA),
        new Theme("Cobalt",         TextColor.ANSI.BLUE,    TextColor.ANSI.WHITE, TextColor.ANSI.CYAN,    TextColor.ANSI.WHITE),
        new Theme("Desert Wind",    TextColor.ANSI.YELLOW,  TextColor.ANSI.BLACK, TextColor.ANSI.GREEN,   TextColor.ANSI.GREEN),
        new Theme("Toxic Green",    TextColor.ANSI.GREEN,   TextColor.ANSI.BLACK, TextColor.ANSI.YELLOW,  TextColor.ANSI.YELLOW),
        new Theme("Violet Haze",    TextColor.ANSI.MAGENTA, TextColor.ANSI.WHITE, TextColor.ANSI.BLUE,    TextColor.ANSI.CYAN),
        new Theme("Steel Blue",     TextColor.ANSI.WHITE,   TextColor.ANSI.BLACK, TextColor.ANSI.BLUE,    TextColor.ANSI.BLUE),
        new Theme("Ember",          TextColor.ANSI.RED,     TextColor.ANSI.WHITE, TextColor.ANSI.YELLOW,  TextColor.ANSI.YELLOW),
    };
    static int currentTheme = 0; // Norton Commander

    // ─── Menu data ──────────────────────────────────────────────────────────────

    static final String[]   MENU_TITLES = { "Configure" };
    static final String[][] MENU_ITEMS  = { { "Preferences...", "Themes \u25b8" } };
    static final String[] THEME_NAMES;
    static {
        THEME_NAMES = new String[THEMES.length];
        for (int i = 0; i < THEMES.length; i++) THEME_NAMES[i] = THEMES[i].name();
    }
    // SUBMENUS[menuIdx][itemIdx] = submenu String[] or null (no submenu)
    static final String[][][] SUBMENUS = { { null, THEME_NAMES } };

    // ─── Entry point ────────────────────────────────────────────────────────────

    public static void main(String[] args) throws IOException {
        Terminal terminal = new DefaultTerminalFactory().setTerminalEmulatorTitle("Midday Commander").createTerminal();
        if (terminal instanceof SwingTerminalFrame f) {
            java.awt.Dimension sz = f.getSize();
            f.setSize((int)(sz.width * 1.3), (int)(sz.height * 1.3));
            f.setLocationRelativeTo(null);
        }
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        screen.setCursorPosition(null);

        Path  start  = Paths.get(System.getProperty("user.home"));
        Panel left   = new Panel(start);
        Panel right  = new Panel(start);
        int   active = 0;   // 0 = left, 1 = right

        boolean menuActive   = false;
        int     menuIdx      = 0;
        boolean dropdownOpen = false;
        int     dropdownIdx  = 0;
        boolean subMenuOpen  = false;
        int     subMenuIdx   = 0;

        try {
            while (true) {
                screen.doResizeIfNecessary();
                TerminalSize sz          = screen.getTerminalSize();
                int          W           = sz.getColumns();
                int          H           = sz.getRows();
                int          visibleRows = Math.max(0, H - 7); // menu bar + box chrome + header row

                left.adjustScroll(visibleRows);
                right.adjustScroll(visibleRows);
                draw(screen, left, right, active, W, H, visibleRows,
                        menuActive, menuIdx, dropdownOpen, dropdownIdx, subMenuOpen, subMenuIdx);

                KeyStroke k = null;
                while (k == null) {
                    k = screen.pollInput();
                    if (k == null) {
                        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
                        if (screen.doResizeIfNecessary() != null) break; // resize → redraw
                    }
                }
                if (k == null) continue;

                // ── Menu navigation ──────────────────────────────────────────
                if (menuActive) {
                    if (subMenuOpen) {
                        String[] sub = SUBMENUS[menuIdx][dropdownIdx];
                        switch (k.getKeyType()) {
                            case ArrowUp   -> subMenuIdx = Math.max(0, subMenuIdx - 1);
                            case ArrowDown -> subMenuIdx = Math.min(sub.length - 1, subMenuIdx + 1);
                            case Enter, ArrowRight -> {
                                currentTheme = subMenuIdx;
                                menuActive = false; dropdownOpen = false; subMenuOpen = false;
                            }
                            case ArrowLeft, Escape, F9 -> subMenuOpen = false;
                            default -> {}
                        }
                    } else {
                        switch (k.getKeyType()) {
                            case F9, Escape -> { menuActive = false; dropdownOpen = false; }
                            case ArrowLeft  -> { menuIdx = (menuIdx - 1 + MENU_TITLES.length) % MENU_TITLES.length; dropdownIdx = 0; dropdownOpen = false; }
                            case ArrowRight -> {
                                if (dropdownOpen && SUBMENUS[menuIdx][dropdownIdx] != null) {
                                    subMenuOpen = true; subMenuIdx = 0;
                                } else {
                                    menuIdx = (menuIdx + 1) % MENU_TITLES.length; dropdownIdx = 0; dropdownOpen = false;
                                }
                            }
                            case ArrowDown -> {
                                if (!dropdownOpen) { dropdownOpen = true; dropdownIdx = 0; }
                                else dropdownIdx = Math.min(dropdownIdx + 1, MENU_ITEMS[menuIdx].length - 1);
                            }
                            case ArrowUp -> {
                                if (dropdownOpen && dropdownIdx > 0) dropdownIdx--;
                                else dropdownOpen = false;
                            }
                            case Enter -> {
                                if (!dropdownOpen) { dropdownOpen = true; dropdownIdx = 0; }
                                else if (SUBMENUS[menuIdx][dropdownIdx] != null) {
                                    subMenuOpen = true; subMenuIdx = 0;
                                } else {
                                    menuActive = false; dropdownOpen = false;
                                    runMenuItem(screen, menuIdx, dropdownIdx, W, H);
                                }
                            }
                            default -> {}
                        }
                    }
                    continue;
                }

                // ── Normal key handling ──────────────────────────────────────
                Panel p     = (active == 0) ? left : right;
                Panel other = (active == 0) ? right : left;
                switch (k.getKeyType()) {
                    case ArrowUp    -> p.up(1);
                    case ArrowDown  -> p.down(1);
                    case ArrowRight -> { FileEntry re = p.selected(); if (re != null && re.isDirectory()) p.enter(); }
                    case ArrowLeft  -> { p.cursor = 0; p.enter(); } // cursor=0 selects "..", enter goes up
                    case PageUp     -> p.up(visibleRows);
                    case PageDown   -> p.down(visibleRows);
                    case Home       -> p.cursor = 0;
                    case End        -> p.cursor = p.entries.size() - 1;
                    case Enter      -> p.enter();
                    case Tab       -> active = 1 - active;
                    case F3        -> viewFile(screen, p, W, H);
                    case F4        -> editFile(screen, p);
                    case F5        -> copyFile(screen, p, other, W, H);
                    case F6        -> moveFile(screen, p, other, W, H);
                    case F7        -> mkDir(screen, p, W, H);
                    case F8        -> deleteFile(screen, p, W, H);
                    case F9        -> { menuActive = true; menuIdx = 0; dropdownOpen = false; dropdownIdx = 0; }
                    case Character -> { if (k.getCharacter() == 'q' || k.getCharacter() == 'Q') return; }
                    case Escape    -> { return; }
                    default        -> {}
                }
            }
        } finally {
            screen.stopScreen();
        }
    }

    // ─── Copy logic ─────────────────────────────────────────────────────────────

    static void copyFile(Screen screen, Panel src, Panel dst, int W, int H) throws IOException {
        FileEntry e = src.selected();
        if (e == null || e.name().equals("..")) return;

        if (src.path.equals(dst.path)) {
            showMessageDialog(screen, " Notice ", "Source and destination are the same directory.", W, H);
            return;
        }

        boolean confirmed = showCopyDialog(screen, e, src.path, dst.path, W, H);
        if (!confirmed) return;

        Path srcPath = src.path.resolve(e.name());
        Path dstPath = dst.path.resolve(e.name());
        try {
            if (e.isDirectory()) {
                copyDirectory(srcPath, dstPath);
            } else {
                Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
            }
            dst.load();
        } catch (IOException ex) {
            showMessageDialog(screen, " Error ", ex.getMessage(), W, H);
        }
    }

    static void copyDirectory(Path src, Path dst) throws IOException {
        Files.walkFileTree(src, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(dst.resolve(src.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, dst.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // ─── Move logic ─────────────────────────────────────────────────────────────

    static void moveFile(Screen screen, Panel src, Panel dst, int W, int H) throws IOException {
        FileEntry e = src.selected();
        if (e == null || e.name().equals("..")) return;

        if (src.path.equals(dst.path)) {
            showMessageDialog(screen, " Notice ", "Source and destination are the same directory.", W, H);
            return;
        }

        boolean confirmed = showOpDialog(screen, " Move ", e, src.path, dst.path, W, H);
        if (!confirmed) return;

        Path srcPath = src.path.resolve(e.name());
        Path dstPath = dst.path.resolve(e.name());
        try {
            Files.move(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
            src.load();
            dst.load();
        } catch (AtomicMoveNotSupportedException ex) {
            // Cross-device move: copy then delete
            try {
                if (e.isDirectory()) copyDirectory(srcPath, dstPath);
                else Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
                deleteRecursive(srcPath);
                src.load();
                dst.load();
            } catch (IOException ex2) {
                showMessageDialog(screen, " Error ", ex2.getMessage(), W, H);
            }
        } catch (IOException ex) {
            showMessageDialog(screen, " Error ", ex.getMessage(), W, H);
        }
    }

    // ─── Delete logic ────────────────────────────────────────────────────────────

    static void deleteFile(Screen screen, Panel panel, int W, int H) throws IOException {
        FileEntry e = panel.selected();
        if (e == null || e.name().equals("..")) return;

        boolean confirmed = showDeleteDialog(screen, e, panel.path, W, H);
        if (!confirmed) return;

        Path target = panel.path.resolve(e.name());
        try {
            deleteRecursive(target);
            panel.cursor = Math.max(0, panel.cursor - 1);
            panel.load();
        } catch (IOException ex) {
            showMessageDialog(screen, " Error ", ex.getMessage(), W, H);
        }
    }

    static void deleteRecursive(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
                if (ex != null) throw ex;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // ─── View logic ─────────────────────────────────────────────────────────────

    static void viewFile(Screen screen, Panel panel, int W, int H) throws IOException {
        FileEntry e = panel.selected();
        if (e == null || e.isDirectory()) return;

        Path file = panel.path.resolve(e.name());

        // Read lines (fall back to hex-ish dump on binary/read error)
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (Exception ex) {
            showMessageDialog(screen, " Error ", ex.getMessage(), W, H);
            return;
        }
        if (lines.isEmpty()) lines = List.of("(empty file)");

        int scrollRow  = 0;
        int scrollCol  = 0;
        int viewRows   = H - 2; // header + footer
        int viewCols   = W - 7; // 6-char line-number gutter + 1 space

        while (true) {
            // ── Draw ──
            screen.clear();
            TextGraphics g = screen.newTextGraphics();

            // Header
            g.setForegroundColor(TextColor.ANSI.BLACK);
            g.setBackgroundColor(TextColor.ANSI.CYAN);
            g.putString(0, 0, pad(" " + e.name() + "  [" + lines.size() + " lines]", W));

            // Lines
            for (int i = 0; i < viewRows; i++) {
                int lineIdx = scrollRow + i;
                g.setForegroundColor(TextColor.ANSI.DEFAULT);
                g.setBackgroundColor(TextColor.ANSI.DEFAULT);

                // Line number gutter
                if (lineIdx < lines.size()) {
                    g.setForegroundColor(TextColor.ANSI.YELLOW);
                    g.putString(0, i + 1, String.format("%5d ", lineIdx + 1));
                } else {
                    g.putString(0, i + 1, "      ");
                }

                // Content
                g.setForegroundColor(TextColor.ANSI.WHITE);
                String content = "";
                if (lineIdx < lines.size()) {
                    String raw = lines.get(lineIdx);
                    // expand tabs
                    raw = raw.replace("\t", "    ");
                    if (scrollCol < raw.length()) content = raw.substring(scrollCol);
                }
                g.putString(6, i + 1, pad(trunc(content, viewCols), viewCols));
            }

            // Footer
            g.setForegroundColor(TextColor.ANSI.BLACK);
            g.setBackgroundColor(TextColor.ANSI.CYAN);
            String pos = String.format(" Line %d/%d  Col %d ", scrollRow + 1, lines.size(), scrollCol + 1);
            String hint = "  Arrows/PgUp/PgDn:Scroll  Home/End  F3/Q/Esc:Close";
            g.putString(0, H - 1, pad(pos + hint, W));

            g.setForegroundColor(TextColor.ANSI.DEFAULT);
            g.setBackgroundColor(TextColor.ANSI.DEFAULT);
            screen.refresh();

            // ── Input ──
            KeyStroke k = screen.readInput();
            int maxRow = Math.max(0, lines.size() - 1);
            int maxCol = lines.stream().mapToInt(String::length).max().orElse(0);
            switch (k.getKeyType()) {
                case ArrowUp    -> scrollRow = Math.max(0, scrollRow - 1);
                case ArrowDown  -> scrollRow = Math.min(maxRow, scrollRow + 1);
                case ArrowLeft  -> scrollCol = Math.max(0, scrollCol - 4);
                case ArrowRight -> scrollCol = Math.min(maxCol, scrollCol + 4);
                case PageUp     -> scrollRow = Math.max(0, scrollRow - viewRows);
                case PageDown   -> scrollRow = Math.min(maxRow, scrollRow + viewRows);
                case Home       -> scrollCol = 0;
                case End        -> scrollCol = maxCol;
                case F3, Escape -> { return; }
                case Character  -> { if (k.getCharacter() == 'q' || k.getCharacter() == 'Q') return; }
                default         -> {}
            }
        }
    }

    // ─── Edit logic ─────────────────────────────────────────────────────────────

    static void editFile(Screen screen, Panel panel) throws IOException {
        FileEntry e = panel.selected();
        if (e == null || e.isDirectory()) return;

        Path file = panel.path.resolve(e.name());

        List<StringBuilder> lines = new ArrayList<>();
        try {
            for (String s : Files.readAllLines(file)) lines.add(new StringBuilder(s));
        } catch (Exception ex) {
            TerminalSize sz = screen.getTerminalSize();
            showMessageDialog(screen, " Error ", ex.getMessage(), sz.getColumns(), sz.getRows());
            return;
        }
        if (lines.isEmpty()) lines.add(new StringBuilder());

        int     curRow    = 0, curCol = 0;
        int     scrollRow = 0, scrollCol = 0;
        boolean modified  = false;
        final int GUTTER  = 6; // "99999 "

        screen.setCursorPosition(new TerminalPosition(GUTTER, 1));

        while (true) {
            screen.doResizeIfNecessary();
            TerminalSize sz    = screen.getTerminalSize();
            int          W     = sz.getColumns();
            int          H     = sz.getRows();
            int          vRows = H - 2;
            int          vCols = W - GUTTER;

            // Scroll to keep cursor visible
            if (curRow < scrollRow) scrollRow = curRow;
            if (curRow >= scrollRow + vRows) scrollRow = curRow - vRows + 1;
            if (curCol < scrollCol) scrollCol = curCol;
            if (curCol >= scrollCol + vCols) scrollCol = curCol - vCols + 1;
            scrollRow = Math.max(0, scrollRow);
            scrollCol = Math.max(0, scrollCol);

            // Draw
            screen.clear();
            TextGraphics g = screen.newTextGraphics();

            // Header
            g.setForegroundColor(TextColor.ANSI.BLACK);
            g.setBackgroundColor(TextColor.ANSI.CYAN);
            g.putString(0, 0, pad(" " + e.name() + (modified ? " [+]" : "") + "  [" + lines.size() + " lines]", W));

            // Lines
            for (int i = 0; i < vRows; i++) {
                int li = scrollRow + i;
                if (li < lines.size()) {
                    g.setForegroundColor(TextColor.ANSI.YELLOW);
                    g.setBackgroundColor(TextColor.ANSI.DEFAULT);
                    g.putString(0, i + 1, String.format("%5d ", li + 1));

                    g.setForegroundColor(TextColor.ANSI.WHITE);
                    String content = lines.get(li).toString().replace("\t", "    ");
                    content = scrollCol < content.length() ? content.substring(scrollCol) : "";
                    g.putString(GUTTER, i + 1, pad(trunc(content, vCols), vCols));
                } else {
                    g.setForegroundColor(TextColor.ANSI.DEFAULT);
                    g.setBackgroundColor(TextColor.ANSI.DEFAULT);
                    g.putString(0, i + 1, pad("", W));
                }
            }

            // Footer
            g.setForegroundColor(TextColor.ANSI.BLACK);
            g.setBackgroundColor(TextColor.ANSI.CYAN);
            String pos  = String.format(" Ln %d/%d  Col %d ", curRow + 1, lines.size(), curCol + 1);
            String hint = "  Arrows:Move  F2:Save  F4/Esc:Close";
            g.putString(0, H - 1, pad(pos + hint, W));

            screen.setCursorPosition(new TerminalPosition(GUTTER + curCol - scrollCol, curRow - scrollRow + 1));
            screen.refresh();

            KeyStroke      k    = screen.readInput();
            StringBuilder  line = lines.get(curRow);
            switch (k.getKeyType()) {
                case ArrowUp -> {
                    if (curRow > 0) { curRow--; curCol = Math.min(curCol, lines.get(curRow).length()); }
                }
                case ArrowDown -> {
                    if (curRow < lines.size() - 1) { curRow++; curCol = Math.min(curCol, lines.get(curRow).length()); }
                }
                case ArrowLeft -> {
                    if (curCol > 0) { curCol--; }
                    else if (curRow > 0) { curRow--; curCol = lines.get(curRow).length(); }
                }
                case ArrowRight -> {
                    if (curCol < line.length()) { curCol++; }
                    else if (curRow < lines.size() - 1) { curRow++; curCol = 0; }
                }
                case PageUp -> {
                    curRow = Math.max(0, curRow - vRows);
                    curCol = Math.min(curCol, lines.get(curRow).length());
                }
                case PageDown -> {
                    curRow = Math.min(lines.size() - 1, curRow + vRows);
                    curCol = Math.min(curCol, lines.get(curRow).length());
                }
                case Home -> curCol = 0;
                case End  -> curCol = line.length();
                case Backspace -> {
                    if (curCol > 0) {
                        line.deleteCharAt(--curCol);
                        modified = true;
                    } else if (curRow > 0) {
                        int prevLen = lines.get(curRow - 1).length();
                        lines.get(curRow - 1).append(line);
                        lines.remove(curRow--);
                        curCol   = prevLen;
                        modified = true;
                    }
                }
                case Delete -> {
                    if (curCol < line.length()) {
                        line.deleteCharAt(curCol);
                        modified = true;
                    } else if (curRow < lines.size() - 1) {
                        line.append(lines.remove(curRow + 1));
                        modified = true;
                    }
                }
                case Enter -> {
                    String rest = line.substring(curCol);
                    line.delete(curCol, line.length());
                    lines.add(++curRow, new StringBuilder(rest));
                    curCol   = 0;
                    modified = true;
                }
                case F2 -> {
                    try {
                        List<String> out = new ArrayList<>();
                        for (StringBuilder sb : lines) out.add(sb.toString());
                        Files.write(file, out);
                        modified = false;
                    } catch (IOException ex) {
                        showMessageDialog(screen, " Error ", ex.getMessage(), W, H);
                    }
                }
                case F4, Escape -> {
                    if (modified) {
                        if (!showDiscardDialog(screen, e.name(), W, H)) continue;
                    }
                    screen.setCursorPosition(null);
                    return;
                }
                case Character -> {
                    line.insert(curCol++, k.getCharacter());
                    modified = true;
                }
                default -> {}
            }
        }
    }

    static boolean showDiscardDialog(Screen screen, String filename, int W, int H) throws IOException {
        int dw = Math.min(W - 4, 64);
        int dh = 7;
        int dx = (W - dw) / 2;
        int dy = (H - dh) / 2;

        TextGraphics g = screen.newTextGraphics();
        drawBox(g, dx, dy, dw, dh, " Unsaved Changes ", TextColor.ANSI.WHITE, TextColor.ANSI.BLUE);

        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.setBackgroundColor(TextColor.ANSI.BLUE);
        g.putString(dx + 2, dy + 2, trunc(filename + " has unsaved changes.", dw - 4));
        g.putString(dx + 2, dy + 3, "Discard and close?");

        g.setForegroundColor(TextColor.ANSI.YELLOW);
        String hint = "[ Enter / Y ] Discard    [ Esc / N ] Cancel";
        g.putString(dx + (dw - hint.length()) / 2, dy + dh - 2, hint);

        screen.refresh();
        return awaitConfirm(screen);
    }

    // ─── Mkdir logic ────────────────────────────────────────────────────────────

    static void mkDir(Screen screen, Panel panel, int W, int H) throws IOException {
        String name = showInputDialog(screen, " New Directory ", "Directory name:", panel.path, W, H);
        if (name == null || name.isBlank()) return;
        try {
            Files.createDirectory(panel.path.resolve(name));
            panel.load();
            // position cursor on the new entry
            for (int i = 0; i < panel.entries.size(); i++) {
                if (panel.entries.get(i).name().equals(name)) { panel.cursor = i; break; }
            }
        } catch (IOException ex) {
            showMessageDialog(screen, " Error ", ex.getMessage(), W, H);
        }
    }

    // ─── Dialogs ────────────────────────────────────────────────────────────────

    /** Generic copy/move confirmation dialog — title is " Copy " or " Move ". */
    static boolean showOpDialog(Screen screen, String title, FileEntry e, Path from, Path to, int W, int H) throws IOException {
        int dw = Math.min(W - 4, 64);
        int dh = 9;
        int dx = (W - dw) / 2;
        int dy = (H - dh) / 2;

        TextGraphics g = screen.newTextGraphics();
        drawBox(g, dx, dy, dw, dh, title, TextColor.ANSI.WHITE, TextColor.ANSI.BLUE);

        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.setBackgroundColor(TextColor.ANSI.BLUE);
        String kind = e.isDirectory() ? "Dir: " : "File:";
        g.putString(dx + 2, dy + 2, kind + " " + trunc(e.name(),       dw - 9));
        g.putString(dx + 2, dy + 3, "From:" + " " + trunc(from.toString(), dw - 9));
        g.putString(dx + 2, dy + 4, "  To:" + " " + trunc(to.toString(),   dw - 9));

        if (Files.exists(to.resolve(e.name()))) {
            g.setForegroundColor(TextColor.ANSI.YELLOW);
            g.putString(dx + 2, dy + 6, trunc("Warning: destination already exists — will overwrite!", dw - 4));
        }

        g.setForegroundColor(TextColor.ANSI.YELLOW);
        String hint = "[ Enter / Y ] Confirm    [ Esc / N ] Cancel";
        g.putString(dx + (dw - hint.length()) / 2, dy + dh - 2, hint);

        screen.refresh();
        return awaitConfirm(screen);
    }

    static boolean showDeleteDialog(Screen screen, FileEntry e, Path parent, int W, int H) throws IOException {
        int dw = Math.min(W - 4, 64);
        int dh = 8;
        int dx = (W - dw) / 2;
        int dy = (H - dh) / 2;

        TextGraphics g = screen.newTextGraphics();
        drawBox(g, dx, dy, dw, dh, " Delete ", TextColor.ANSI.WHITE, TextColor.ANSI.RED);

        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.setBackgroundColor(TextColor.ANSI.RED);
        String kind = e.isDirectory() ? "Dir: " : "File:";
        g.putString(dx + 2, dy + 2, kind + " " + trunc(e.name(),           dw - 9));
        g.putString(dx + 2, dy + 3, "  In:" + " " + trunc(parent.toString(), dw - 9));

        g.setForegroundColor(TextColor.ANSI.YELLOW);
        if (e.isDirectory())
            g.putString(dx + 2, dy + 5, trunc("Warning: entire directory tree will be deleted!", dw - 4));

        String hint = "[ Enter / Y ] Delete    [ Esc / N ] Cancel";
        g.putString(dx + (dw - hint.length()) / 2, dy + dh - 2, hint);

        screen.refresh();
        return awaitConfirm(screen);
    }

    static boolean showCopyDialog(Screen screen, FileEntry e, Path from, Path to, int W, int H) throws IOException {
        return showOpDialog(screen, " Copy ", e, from, to, W, H);
    }

    static void showMessageDialog(Screen screen, String title, String message, int W, int H) throws IOException {
        int dw = Math.min(W - 4, Math.max(42, message.length() + 6));
        int dh = 6;
        int dx = (W - dw) / 2;
        int dy = (H - dh) / 2;

        TextGraphics g = screen.newTextGraphics();
        drawBox(g, dx, dy, dw, dh, title, TextColor.ANSI.WHITE, TextColor.ANSI.RED);

        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.setBackgroundColor(TextColor.ANSI.RED);
        g.putString(dx + 2, dy + 2, trunc(message, dw - 4));

        g.setForegroundColor(TextColor.ANSI.YELLOW);
        String ok = "[ OK ]";
        g.putString(dx + (dw - ok.length()) / 2, dy + dh - 2, ok);

        screen.refresh();
        // consume one key to dismiss
        while (true) {
            switch (screen.readInput().getKeyType()) {
                case Enter, Escape, Character -> { return; }
                default -> {}
            }
        }
    }

    /** Shows a single-line text input dialog. Returns the typed string, or null if cancelled. */
    static String showInputDialog(Screen screen, String title, String label, Path context, int W, int H) throws IOException {
        int dw = Math.min(W - 4, 64);
        int dh = 7;
        int dx = (W - dw) / 2;
        int dy = (H - dh) / 2;
        int fieldW = dw - 4;

        StringBuilder input = new StringBuilder();

        while (true) {
            TextGraphics g = screen.newTextGraphics();
            drawBox(g, dx, dy, dw, dh, title, TextColor.ANSI.WHITE, TextColor.ANSI.BLUE);

            g.setForegroundColor(TextColor.ANSI.WHITE);
            g.setBackgroundColor(TextColor.ANSI.BLUE);
            g.putString(dx + 2, dy + 2, pad(label, fieldW));
            g.putString(dx + 2, dy + 3, trunc(context.toString(), fieldW));

            // Input field
            g.setForegroundColor(TextColor.ANSI.BLACK);
            g.setBackgroundColor(TextColor.ANSI.WHITE);
            String fieldText = pad(input.toString(), fieldW);
            g.putString(dx + 2, dy + 4, fieldText);

            g.setForegroundColor(TextColor.ANSI.YELLOW);
            g.setBackgroundColor(TextColor.ANSI.BLUE);
            String hint = "[ Enter ] Confirm    [ Esc ] Cancel";
            g.putString(dx + (dw - hint.length()) / 2, dy + dh - 2, hint);

            screen.refresh();

            KeyStroke k = screen.readInput();
            switch (k.getKeyType()) {
                case Enter  -> { return input.toString(); }
                case Escape -> { return null; }
                case Backspace -> { if (!input.isEmpty()) input.deleteCharAt(input.length() - 1); }
                case Character -> {
                    if (input.length() < fieldW) input.append(k.getCharacter());
                }
                default -> {}
            }
        }
    }

    /** Blocks until Y/Enter (→ true) or N/Esc (→ false). */
    static boolean awaitConfirm(Screen screen) throws IOException {
        while (true) {
            KeyStroke k = screen.readInput();
            switch (k.getKeyType()) {
                case Enter  -> { return true; }
                case Escape -> { return false; }
                case Character -> {
                    char c = k.getCharacter();
                    if (c == 'y' || c == 'Y') return true;
                    if (c == 'n' || c == 'N') return false;
                }
                default -> {}
            }
        }
    }

    // ─── Rendering ──────────────────────────────────────────────────────────────

    static void draw(Screen screen, Panel left, Panel right,
                     int active, int W, int H, int visibleRows,
                     boolean menuActive, int menuIdx, boolean dropdownOpen, int dropdownIdx,
                     boolean subMenuOpen, int subMenuIdx) throws IOException {
        screen.clear();
        TextGraphics g  = screen.newTextGraphics();
        Theme        t  = THEMES[currentTheme];
        int          d  = W / 2;
        int          rw = W - d - 2;

        g.setForegroundColor(t.border());
        g.setBackgroundColor(TextColor.ANSI.DEFAULT);

        // Row 1: top border  ╔═══╤═══╗
        g.putString(0, 1, "╔" + "═".repeat(Math.max(0, d - 1)) + "╤" + "═".repeat(Math.max(0, rw)) + "╗");

        // Rows 2..H-3: outer sides ║, inner divider │
        for (int r = 2; r <= H - 3; r++) {
            g.putString(0,     r, "║");
            g.putString(d,     r, "│");
            g.putString(W - 1, r, "║");
        }

        // Row H-4: middle border  ╠═══╪═══╣
        g.putString(0, H - 4, "╠" + "═".repeat(Math.max(0, d - 1)) + "╪" + "═".repeat(Math.max(0, rw)) + "╣");

        // Row H-2: bottom border  ╚═══╧═══╝
        g.putString(0, H - 2, "╚" + "═".repeat(Math.max(0, d - 1)) + "╧" + "═".repeat(Math.max(0, rw)) + "╝");

        drawMenuBar(g, W, menuActive, menuIdx);
        drawPanel(g, left,  1,     d - 1, visibleRows, active == 0, H, t);
        drawPanel(g, right, d + 1, rw,    visibleRows, active == 1, H, t);

        // Status bar
        g.setForegroundColor(t.accentFg());
        g.setBackgroundColor(t.accent());
        g.putString(0, H - 1, pad("  Tab:Switch  F3:View  F4:Edit  F5:Copy  F6:Move  F7:MkDir  F8:Delete  F9:Menu  Q:Quit", W));
        g.setForegroundColor(TextColor.ANSI.DEFAULT);
        g.setBackgroundColor(TextColor.ANSI.DEFAULT);

        // Dropdown drawn on top of everything
        if (menuActive && dropdownOpen) drawDropdown(g, menuIdx, dropdownIdx, subMenuOpen, subMenuIdx, W, t);

        screen.refresh();
    }

    static void drawPanel(TextGraphics g, Panel panel,
                          int x, int w, int visibleRows, boolean active, int H, Theme t) {
        if (w <= 0) return;

        final int SIZE_W = 5;  // "<DIR>" and fmtSize() are always 5 chars
        final int TIME_W = 11; // "MM/dd HH:mm"
        int nameW = Math.max(1, w - SIZE_W - TIME_W - 2);
        int sepX  = x + nameW;          // │ between name and size
        int sep2X = sepX + 1 + SIZE_W;  // │ between size and time

        // Header row (row 2)
        g.setForegroundColor(TextColor.ANSI.YELLOW);
        g.setBackgroundColor(TextColor.ANSI.DEFAULT);
        g.putString(x,         2, pad(trunc(" Name", nameW), nameW));
        g.putString(sepX,      2, "│");
        g.putString(sepX + 1,  2, pad("Size", SIZE_W));
        g.putString(sep2X,     2, "│");
        g.putString(sep2X + 1, 2, pad("Modify time", TIME_W));

        // File list (rows 3..H-5)
        for (int i = 0; i < visibleRows; i++) {
            int idx = i + panel.scroll;
            int row = 3 + i;

            // Separators run through every row
            g.setForegroundColor(t.border());
            g.setBackgroundColor(TextColor.ANSI.DEFAULT);
            g.putString(sepX,  row, "│");
            g.putString(sep2X, row, "│");

            if (idx >= panel.entries.size()) {
                g.setForegroundColor(TextColor.ANSI.DEFAULT);
                g.setBackgroundColor(TextColor.ANSI.DEFAULT);
                g.putString(x,        row, pad("", nameW));
                g.putString(sepX + 1, row, pad("", SIZE_W));
                g.putString(sep2X + 1,row, pad("", TIME_W));
                continue;
            }

            FileEntry e        = panel.entries.get(idx);
            boolean   selected = (idx == panel.cursor);

            TextColor fg, bg;
            if (selected && active) {
                fg = t.accentFg(); bg = t.accent();
            } else if (selected) {
                fg = TextColor.ANSI.BLACK; bg = TextColor.ANSI.WHITE;
            } else if (e.isDirectory()) {
                fg = t.dirFg(); bg = TextColor.ANSI.DEFAULT;
            } else {
                fg = TextColor.ANSI.WHITE; bg = TextColor.ANSI.DEFAULT;
            }

            g.setForegroundColor(fg);
            g.setBackgroundColor(bg);

            String sizeCol = e.isDirectory() ? "<DIR>" : fmtSize(e.size());
            String display = e.isDirectory() ? "/" + e.name() : e.name();
            g.putString(x,         row, pad(trunc(" " + display, nameW), nameW));
            g.putString(sepX + 1,  row, sizeCol);
            g.putString(sep2X + 1, row, fmtTime(e.modified()));
        }

        // Column separator connectors at panel borders
        g.setForegroundColor(t.border());
        g.setBackgroundColor(TextColor.ANSI.DEFAULT);
        if (w > SIZE_W + TIME_W + 4) {
            g.putString(sepX,  1,     "╤"); // top border: ═ → ╤
            g.putString(sepX,  H - 4, "╧"); // middle border: ═ → ╧
            g.putString(sep2X, 1,     "╤");
            g.putString(sep2X, H - 4, "╧");
        }

        // Path row (H-3, inside the 1-row sub-box)
        g.setForegroundColor(active ? t.accentFg() : TextColor.ANSI.WHITE);
        g.setBackgroundColor(active ? t.accent()    : TextColor.ANSI.DEFAULT);
        g.putString(x, H - 3, pad(trunc(" " + panel.path, w), w));

        g.setForegroundColor(TextColor.ANSI.DEFAULT);
        g.setBackgroundColor(TextColor.ANSI.DEFAULT);
    }

    static void drawMenuBar(TextGraphics g, int W, boolean menuActive, int menuIdx) {
        g.setForegroundColor(TextColor.ANSI.BLACK);
        g.setBackgroundColor(TextColor.ANSI.WHITE);
        g.putString(0, 0, pad("", W));
        int x = 1;
        for (int i = 0; i < MENU_TITLES.length; i++) {
            String label = " " + MENU_TITLES[i] + " ";
            if (menuActive && i == menuIdx) {
                g.setForegroundColor(TextColor.ANSI.WHITE);
                g.setBackgroundColor(TextColor.ANSI.BLACK);
            } else {
                g.setForegroundColor(TextColor.ANSI.BLACK);
                g.setBackgroundColor(TextColor.ANSI.WHITE);
            }
            g.putString(x, 0, label);
            x += label.length();
        }
    }

    static void drawDropdown(TextGraphics g, int menuIdx, int dropdownIdx,
                             boolean subMenuOpen, int subMenuIdx, int W, Theme t) {
        String[] items = MENU_ITEMS[menuIdx];
        int itemW = 0;
        for (String item : items) itemW = Math.max(itemW, item.length());
        int dw = itemW + 4;
        int dh = items.length + 2;
        int dx = 1;
        for (int i = 0; i < menuIdx; i++) dx += MENU_TITLES[i].length() + 2;
        dx = Math.min(dx, W - dw);
        drawBox(g, dx, 1, dw, dh, null, TextColor.ANSI.WHITE, TextColor.ANSI.BLACK);
        for (int i = 0; i < items.length; i++) {
            if (i == dropdownIdx) {
                g.setForegroundColor(t.accentFg());
                g.setBackgroundColor(t.accent());
            } else {
                g.setForegroundColor(TextColor.ANSI.WHITE);
                g.setBackgroundColor(TextColor.ANSI.BLACK);
            }
            g.putString(dx + 2, 2 + i, pad(items[i], dw - 4));
        }

        // Submenu box to the right of the dropdown
        if (subMenuOpen && SUBMENUS[menuIdx][dropdownIdx] != null) {
            String[] sub   = SUBMENUS[menuIdx][dropdownIdx];
            int      subW  = 0;
            for (String s : sub) subW = Math.max(subW, s.length());
            int sdw = subW + 4;
            int sdh = sub.length + 2;
            int sdx = dx + dw; // to the right; clamp if needed
            if (sdx + sdw > W) sdx = dx - sdw;
            int sdy = 1 + dropdownIdx; // aligned with selected item
            drawBox(g, sdx, sdy, sdw, sdh, null, TextColor.ANSI.WHITE, TextColor.ANSI.BLACK);
            for (int i = 0; i < sub.length; i++) {
                if (i == subMenuIdx) {
                    g.setForegroundColor(t.accentFg());
                    g.setBackgroundColor(t.accent());
                } else {
                    g.setForegroundColor(TextColor.ANSI.WHITE);
                    g.setBackgroundColor(TextColor.ANSI.BLACK);
                }
                g.putString(sdx + 2, sdy + 1 + i, pad(sub[i], sdw - 4));
            }
        }
    }

    static void runMenuItem(Screen screen, int menuIdx, int itemIdx, int W, int H) throws IOException {
        if (menuIdx == 0 && itemIdx == 0) { // Configure → Preferences...
            showMessageDialog(screen, " Preferences ", "Not yet implemented.", W, H);
        }
    }

    static void drawBox(TextGraphics g, int x, int y, int w, int h,
                        String title, TextColor fg, TextColor bg) {
        g.setForegroundColor(fg);
        g.setBackgroundColor(bg);
        // Fill interior
        for (int row = y; row < y + h; row++) g.putString(x, row, pad("", w));
        // Top border with centred title
        int inner = w - 2;
        String t  = (title != null && title.length() <= inner - 2) ? title : "";
        int pl    = (inner - t.length()) / 2;
        int pr    = inner - t.length() - pl;
        g.putString(x, y,         "┌" + "─".repeat(pl) + t + "─".repeat(pr) + "┐");
        g.putString(x, y + h - 1, "└" + "─".repeat(inner) + "┘");
        for (int row = y + 1; row < y + h - 1; row++) {
            g.putString(x,         row, "│");
            g.putString(x + w - 1, row, "│");
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    static String fmtTime(long millis) {
        if (millis == 0) return "           "; // 11 spaces for ".."
        java.time.LocalDateTime dt = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(millis), java.time.ZoneId.systemDefault());
        int currentYear = java.time.LocalDate.now().getYear();
        String pattern = dt.getYear() == currentYear ? "MM/dd HH:mm" : "MM/dd  yyyy";
        return dt.format(java.time.format.DateTimeFormatter.ofPattern(pattern));
    }

    static String fmtSize(long b) {
        if (b < 1_024)          return String.format("%4dB", b);
        if (b < 1_048_576)      return String.format("%4dK", b / 1_024);
        if (b < 1_073_741_824L) return String.format("%4dM", b / 1_048_576);
        return                         String.format("%4dG", b / 1_073_741_824L);
    }

    static String pad(String s, int len) {
        if (len <= 0)          return "";
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }

    static String trunc(String s, int max) {
        if (max <= 0)          return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 1) + "~";
    }
}
