package zenit.filesystem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FolderHandler
 *
 * Tests folder operations including:
 * - Creating single and nested folders
 * - Renaming folders (including reserved name protection)
 * - Deleting folders (including recursive deletion of nested content)
 *
 * Metrics:
 * - WMC: 12 (Low complexity)
 * - OCavg: 1.0 (Low coupling)
 *
 * Related requirements:
 * - FUI102: Run button should work with libraries
 * - FCC100: Can run selected .java file
 */
public class FolderHandlerTest {

    private File tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("FolderHandlerTest").toFile();
    }

    @After
    public void tearDown() {
        deleteRecursively(tempDir);
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    // -------------------------------------------------------------------------
    // createNewFolder
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should create a single new folder")
    public void testCreateNewFolderSuccess() throws IOException {
        File folder = new File(tempDir, "newFolder");

        FolderHandler.createNewFolder(folder);

        assertTrue(folder.exists(), "Folder should exist after creation");
        assertTrue(folder.isDirectory(), "Created path should be a directory");
    }

    @Test
    @DisplayName("Should create nested folders in one call")
    public void testCreateNewFolderCreatesNestedFolders() throws IOException {
        File nested = new File(tempDir, "parent/child/grandchild");

        FolderHandler.createNewFolder(nested);

        assertTrue(nested.exists(), "All nested folders should be created");
        assertTrue(nested.isDirectory(), "Deepest path should be a directory");
    }

    @Test
    @DisplayName("Should throw IOException when folder cannot be created")
    public void testCreateNewFolderThrowsWhenCannotCreate() throws IOException {
        // Place a file at the path so mkdirs() has nowhere to go
        File blockingFile = new File(tempDir, "blockingFile");
        blockingFile.createNewFile();

        assertThrows(IOException.class,
                () -> FolderHandler.createNewFolder(new File(blockingFile, "subFolder")),
                "Should throw IOException when folder cannot be created");
    }

    // -------------------------------------------------------------------------
    // renameFolder
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should rename a folder to a new name")
    public void testRenameFolderSuccess() throws IOException {
        File original = new File(tempDir, "oldFolder");
        original.mkdir();

        File renamed = FolderHandler.renameFolder(original, "newFolder");

        assertTrue(renamed.exists(), "Renamed folder should exist");
        assertEquals("newFolder", renamed.getName(), "Folder should have the new name");
        assertFalse(original.exists(), "Original folder should no longer exist after rename");
    }

    @Test
    @DisplayName("Should throw IOException when renaming to reserved name 'package'")
    public void testRenameFolderThrowsForReservedName() throws IOException {
        File folder = new File(tempDir, "myPackage");
        folder.mkdir();

        assertThrows(IOException.class,
                () -> FolderHandler.renameFolder(folder, "package"),
                "Should throw IOException when new name is the reserved word 'package'");
    }

    @Test
    @DisplayName("Should throw IOException when target folder name already exists")
    public void testRenameFolderThrowsWhenTargetExists() throws IOException {
        File original = new File(tempDir, "folderA");
        original.mkdir();
        new File(tempDir, "folderB").mkdir();

        assertThrows(IOException.class,
                () -> FolderHandler.renameFolder(original, "folderB"),
                "Should throw IOException when the target folder name already exists");
    }

    @Test
    @DisplayName("Should throw IOException when folder has no parent directory")
    public void testRenameFolderThrowsWhenNoParentDirectory() {
        assertThrows(IOException.class,
                () -> FolderHandler.renameFolder(new File("orphanFolder"), "newName"),
                "Should throw IOException when folder has no parent directory");
    }

    @Test
    @DisplayName("Renamed folder should retain its contents")
    public void testRenameFolderRetainsContents() throws IOException {
        File original = new File(tempDir, "sourceFolder");
        original.mkdir();
        new File(original, "insideFile.txt").createNewFile();

        File renamed = FolderHandler.renameFolder(original, "destFolder");

        assertTrue(renamed.exists(), "Renamed folder should exist");
        assertTrue(new File(renamed, "insideFile.txt").exists(),
                "File inside the original folder should exist inside the renamed folder");
    }

    // -------------------------------------------------------------------------
    // deleteFolder
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should delete an empty folder")
    public void testDeleteFolderEmpty() throws IOException {
        File folder = new File(tempDir, "emptyFolder");
        folder.mkdir();

        FolderHandler.deleteFolder(folder);

        assertFalse(folder.exists(), "Empty folder should be deleted");
    }

    @Test
    @DisplayName("Should recursively delete folder with nested files and subfolders")
    public void testDeleteFolderRecursive() throws IOException {
        File root = new File(tempDir, "rootFolder");
        File sub = new File(root, "subFolder");
        sub.mkdirs();
        new File(root, "file1.java").createNewFile();
        new File(sub, "file2.java").createNewFile();

        FolderHandler.deleteFolder(root);

        assertFalse(root.exists(), "Root folder should be deleted");
        assertFalse(sub.exists(), "Nested subfolder should also be deleted");
    }

    @Test
    @DisplayName("Should throw IOException when trying to delete a non-existent folder")
    public void testDeleteFolderThrowsForMissingFolder() {
        assertThrows(IOException.class,
                () -> FolderHandler.deleteFolder(new File(tempDir, "ghostFolder")),
                "Should throw IOException when the folder does not exist");
    }

    @Test
    @DisplayName("Should delete a single file when called on a file instead of a directory")
    public void testDeleteFolderOnFile() throws IOException {
        File file = new File(tempDir, "justAFile.txt");
        file.createNewFile();

        FolderHandler.deleteFolder(file);

        assertFalse(file.exists(), "File should be deleted when deleteFolder is called on it directly");
    }
}
