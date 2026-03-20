package zenit.filesystem;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import zenit.exceptions.TypeCodeException;

/**
 * Unit tests for JavaFileHandler
 *
 * Tests file operations for .java files including:
 * - Creating new .java files (with and without content, with/without extension)
 * - Reading file content
 * - Saving/overwriting file content
 * - Renaming files
 * - Deleting files
 */
public class JavaFileHandlerTest {

    private File tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("JavaFileHandlerTest").toFile();
    }

    @AfterEach
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


    //Create file

    @Test
    @DisplayName("Should create a .java file with provided content")
    public void testCreateFileWithContent() throws IOException, TypeCodeException {
        File file = new File(tempDir, "MyClass.java");
        File created = JavaFileHandler.createFile(file, "public class MyClass {}", 0);

        assertTrue(created.exists(), "File should exist after creation");
        assertEquals("MyClass.java", created.getName(), "File name should match");
        String read = new String(Files.readAllBytes(created.toPath()));
        assertTrue(read.contains("public class MyClass {}"), "File should contain the provided content");
    }

    @Test
    @DisplayName("Should append .java extension if not already present")
    public void testCreateFileAppendsJavaExtension() throws IOException, TypeCodeException {
        File created = JavaFileHandler.createFile(new File(tempDir, "NoExtension"), "public class NoExtension {}", 0);

        assertTrue(created.getName().endsWith(".java"), "File should have .java extension added automatically");
        assertTrue(created.exists(), "File should exist on disk");
    }

    @Test
    @DisplayName("Should not double-append .java if extension is already present")
    public void testCreateFileDoesNotDoubleAppendExtension() throws IOException, TypeCodeException {
        File created = JavaFileHandler.createFile(new File(tempDir, "AlreadyHasExtension.java"), "public class AlreadyHasExtension {}", 0);

        assertEquals("AlreadyHasExtension.java", created.getName(),
                "File name should not have .java appended twice");
    }

    @Test
    @DisplayName("Should throw IOException when file already exists")
    public void testCreateFileThrowsWhenFileAlreadyExists() throws IOException {
        File file = new File(tempDir, "Duplicate.java");
        file.createNewFile();

        assertThrows(IOException.class,
                () -> JavaFileHandler.createFile(file, "content", 0),
                "Should throw IOException if file already exists");
    }

    @Test
    @DisplayName("Should create file using code snippet when content is null")
    public void testCreateFileWithNullContentUsesSnippet() throws IOException, TypeCodeException {
        File created = JavaFileHandler.createFile(new File(tempDir, "SnippetClass.java"), null, 100);

        assertTrue(created.exists(), "File should be created even when content is null");
        String read = new String(Files.readAllBytes(created.toPath()));
        assertFalse(read.isEmpty(), "File content should not be empty when using a code snippet");
    }


    //Read file

    @Test
    @DisplayName("Should read content from an existing file")
    public void testReadFileReturnsContent() throws IOException {
        File file = new File(tempDir, "ReadMe.java");
        Files.write(file.toPath(), "public class ReadMe {}".getBytes());

        String result = JavaFileHandler.readFile(file);

        assertTrue(result.contains("public class ReadMe {}"), "Read content should match what was written to the file");
    }

    @Test
    @DisplayName("Should throw IOException when reading a non-existent file")
    public void testReadFileThrowsForMissingFile() {
        assertThrows(IOException.class,
                () -> JavaFileHandler.readFile(new File(tempDir, "DoesNotExist.java")),
                "Should throw IOException for a file that doesn't exist");
    }

    @Test
    @DisplayName("Should read multi-line file content correctly")
    public void testReadFileMultipleLines() throws IOException {
        File file = new File(tempDir, "MultiLine.java");
        Files.write(file.toPath(), "line1\nline2\nline3".getBytes());

        String result = JavaFileHandler.readFile(file);

        assertTrue(result.contains("line1"), "Should contain first line");
        assertTrue(result.contains("line2"), "Should contain second line");
        assertTrue(result.contains("line3"), "Should contain third line");
    }

    // -------------------------------------------------------------------------
    // saveFile
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should overwrite existing file content when saving")
    public void testSaveFileOverwritesContent() throws IOException {
        File file = new File(tempDir, "Saveable.java");
        Files.write(file.toPath(), "old content".getBytes());

        JavaFileHandler.saveFile(file, "new content");

        String result = new String(Files.readAllBytes(file.toPath()));
        assertTrue(result.contains("new content"), "File should contain new content after save");
        assertFalse(result.contains("old content"), "Old content should be fully replaced");
    }

    @Test
    @DisplayName("Should save empty string to file")
    public void testSaveFileWithEmptyContent() throws IOException {
        File file = new File(tempDir, "Empty.java");
        Files.write(file.toPath(), "some content".getBytes());

        JavaFileHandler.saveFile(file, "");

        assertEquals("", new String(Files.readAllBytes(file.toPath())),
                "File should be empty after saving an empty string");
    }

    // -------------------------------------------------------------------------
    // renameFile
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should rename a .java file to a new name")
    public void testRenameFileSuccess() throws IOException {
        File original = new File(tempDir, "OldName.java");
        original.createNewFile();

        File renamed = JavaFileHandler.renameFile(original, "NewName.java");

        assertTrue(renamed.exists(), "Renamed file should exist");
        assertEquals("NewName.java", renamed.getName(), "File should have the new name");
        assertFalse(original.exists(), "Original file should no longer exist after rename");
    }

    @Test
    @DisplayName("Should append .java extension to new name if original was a .java file")
    public void testRenameFileAppendsJavaExtension() throws IOException {
        File original = new File(tempDir, "OldFile.java");
        original.createNewFile();

        File renamed = JavaFileHandler.renameFile(original, "NewFile");

        assertEquals("NewFile.java", renamed.getName(),
                "Should append .java to the new name when the original was a .java file");
    }

    @Test
    @DisplayName("Should throw IOException when renaming to an already existing file name")
    public void testRenameFileThrowsWhenTargetExists() throws IOException {
        File original = new File(tempDir, "Original.java");
        original.createNewFile();
        new File(tempDir, "Existing.java").createNewFile();

        assertThrows(IOException.class,
                () -> JavaFileHandler.renameFile(original, "Existing.java"),
                "Should throw IOException when the target file name already exists");
    }

    @Test
    @DisplayName("Should throw IOException when file has no parent directory")
    public void testRenameFileThrowsWhenNoParentDirectory() {
        assertThrows(IOException.class,
                () -> JavaFileHandler.renameFile(new File("OrphanFile.java"), "NewName.java"),
                "Should throw IOException when file has no parent directory");
    }

    // -------------------------------------------------------------------------
    // deleteFile
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should delete an existing file successfully")
    public void testDeleteFileSuccess() throws IOException {
        File file = new File(tempDir, "ToDelete.java");
        file.createNewFile();

        JavaFileHandler.deleteFile(file);

        assertFalse(file.exists(), "File should no longer exist after deletion");
    }

    @Test
    @DisplayName("Should throw IOException when trying to delete a non-existent file")
    public void testDeleteFileThrowsForMissingFile() {
        assertThrows(IOException.class,
                () -> JavaFileHandler.deleteFile(new File(tempDir, "Ghost.java")),
                "Should throw IOException when the file does not exist");
    }
}
