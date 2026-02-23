package zenit.ui.tree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

import javafx.scene.control.TreeView;

/**
 * Manages clipboard (copy/cut/paste) operations for the file tree.
 * Usage in MainController:
 *   private FileTreeClipboard clipboard = new FileTreeClipboard();
 *
 * @author Rasmus Axelsson
 */
public class FileTreeClipboard {

    private File clipboardFile = null;
    private boolean isCut = false;

    public void copy(FileTreeItem<String> item) {
        if (item == null) return;
        clipboardFile = item.getFile();
        isCut = false;
    }

    //isCut is set true
    public void cut(FileTreeItem<String> item) {
        if (item == null) return;
        clipboardFile = item.getFile();
        isCut = true;
    }

    /**
     * Pastes the clipboard file into the directory represented by the selected item.
     * @param selected The currently selected tree item (paste target or its parent)
     * @param treeView The tree view, used to locate the root for tree changes
     */
    public void paste(FileTreeItem<String> selected, TreeView<String> treeView) {
        if (clipboardFile == null || selected == null) return;

        File targetDir = resolveTargetDirectory(selected);
        if (targetDir == null) return;

        FileTreeItem<String> targetItem = FileTree.getTreeItemFromFile(
                (FileTreeItem<String>) treeView.getRoot(), targetDir);
        if (targetItem == null) return;

        //Prevent pasting a directory into itself or a child of itself
        if (clipboardFile.isDirectory() && isAncestorOrSelf(clipboardFile, targetDir)) {
            return;
        }

        File dest = resolveDestination(targetDir, clipboardFile.getName());

        try {
            if (clipboardFile.isDirectory()) {
                copyDirectoryRecursively(clipboardFile.toPath(), dest.toPath());
            } else {
                Files.copy(clipboardFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            if (isCut) {
                removeFromDisk(clipboardFile);
                FileTreeItem<String> sourceItem = FileTree.getTreeItemFromFile(
                        (FileTreeItem<String>) treeView.getRoot(), clipboardFile);
                if (sourceItem != null && sourceItem.getParent() != null) {
                    sourceItem.getParent().getChildren().remove(sourceItem);
                }
                clipboardFile = null; //cut is consumed after paste
                isCut = false;
            }

            FileTree.createParentNode(targetItem, dest);

        } catch (IOException e) {
            System.err.println("FileTreeClipboard.paste: " + e.getMessage());
        }
    }


    public boolean hasContent() {
        return clipboardFile != null;
    }

    public void clear() {
        clipboardFile = null;
        isCut = false;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    //If the selected item is a directory, returns it directly.
    private File resolveTargetDirectory(FileTreeItem<String> selected) {
        File f = selected.getFile();
        if (f == null) return null;
        return f.isDirectory() ? f : f.getParentFile();
    }

    //Builds a destination path name, adding " copy" (or " copy 2", etc.)
    private File resolveDestination(File targetDir, String originalName) {
        File candidate = new File(targetDir, originalName);
        if (!candidate.exists()) return candidate;

        // Split name and extension to insert " copy" before the dot
        int dot = originalName.lastIndexOf('.');
        String base = dot >= 0 ? originalName.substring(0, dot) : originalName;
        String ext  = dot >= 0 ? originalName.substring(dot)    : "";

        candidate = new File(targetDir, base + " copy" + ext);
        int counter = 2;
        while (candidate.exists()) {
            candidate = new File(targetDir, base + " copy " + counter + ext);
            counter++;
        }
        return candidate;
    }

    //Recursively copies a directory and all its contents to the given destination
    private void copyDirectoryRecursively(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(sourcePath -> {
            try {
                Path targetPath = dest.resolve(src.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy: " + sourcePath, e);
            }
        });
    }

    //Deletes a file or directory recursively from disk
    private void removeFromDisk(File file) throws IOException {
        if (file.isDirectory()) {
            // Walk in reverse order so files are deleted before their parent directories
            Files.walk(file.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } else {
            Files.deleteIfExists(file.toPath());
        }
    }


    //Returns true if ancestor is the same as or a parent of child.
    //Used to prevent pasting a directory into itself
    private boolean isAncestorOrSelf(File ancestor, File child) {
        Path a = ancestor.toPath().toAbsolutePath().normalize();
        Path c = child.toPath().toAbsolutePath().normalize();
        return c.startsWith(a);
    }
}