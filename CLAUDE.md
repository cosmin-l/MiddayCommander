# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build the project
./gradlew build

# Run the application (requires a real terminal, not an IDE console)
./gradlew run

# Build a distributable archive
./gradlew distZip   # or distTar

# Compile only
./gradlew compileJava
```

There are no tests in this project.

## Architecture

This is a single-file terminal-based file manager (`src/main/java/com/example/Main.java`) modelled after Midnight Commander. It uses the [Lanterna 3.1.2](https://github.com/mabe02/lanterna) library for terminal UI rendering.

**Key classes (all in `Main.java`):**

- `FileEntry` — immutable record holding a file name, whether it is a directory, and its size.
- `Panel` — holds the current directory path, the sorted list of `FileEntry` objects, and cursor/scroll state. Manages navigation (`up`, `down`, `enter`) and directory loading.
- `Main` — entry point and all application logic: the main event loop, file operations, dialogs, and rendering.

**Layout:**
- Two side-by-side `Panel` instances (left/right), divided by a vertical `│` character at the horizontal midpoint.
- `Tab` switches the active panel; `Enter` descends into a directory.
- Status bar at the bottom row shows keybindings.

**File operations (F-keys):**
| Key | Action |
|-----|--------|
| F3  | View file (text viewer with line numbers and horizontal scroll) |
| F5  | Copy file/directory to the other panel |
| F6  | Move file/directory (atomic move, falls back to copy+delete across devices) |
| F7  | Create directory |
| F8  | Delete file/directory (recursive) |
| Q / Esc | Quit |

**Rendering pipeline:** `draw()` calls `drawPanel()` for each panel and then draws the status bar. Dialogs (`showOpDialog`, `showDeleteDialog`, `showInputDialog`, `showMessageDialog`) are drawn directly onto the screen using `drawBox()` and block until confirmed or cancelled via `awaitConfirm()`.

**Dependency:** Lanterna provides `Terminal`, `Screen`, `TextGraphics`, and `KeyStroke` — the entire TUI abstraction layer. Java source/target compatibility is set to Java 25.
