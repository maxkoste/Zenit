package zenit.jreversions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import zenit.filesystem.jreversions.JDKVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JDKVerifier
 *
 * Tests JDK validation and executable path resolution including:
 * - Null, missing, and non-directory inputs
 * - Valid and invalid JDK directory structures
 * - OS-specific executable path construction
 * - Edge cases for null and empty JDK path inputs
 */

public class JDKVerifierTest {

    private File tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("JDKVerifierTest").toFile();
    }

    @AfterEach
    public void tearDown() {
        deleteRecursively(tempDir);
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursively(child);
            }
        }
        file.delete();
    }


    //valid JDK

    @Test
    @DisplayName("Should return false for null JDK")
    public void testValidJDKReturnsFalseForNull() {
        assertFalse(JDKVerifier.validJDK(null),
                "validJDK should return false when JDK is null");
    }

    @Test
    @DisplayName("Should return false for non-existent JDK directory")
    public void testValidJDKReturnsFalseForMissingDirectory() {
        assertFalse(JDKVerifier.validJDK(new File(tempDir, "nonexistent")),
                "validJDK should return false when JDK directory does not exist");
    }

    @Test
    @DisplayName("Should return false when JDK path points to a file instead of a directory")
    public void testValidJDKReturnsFalseForFile() throws IOException {
        File file = new File(tempDir, "notADirectory.txt");
        file.createNewFile();

        assertFalse(JDKVerifier.validJDK(file),
                "validJDK should return false when JDK path is a file, not a directory");
    }

    @Test
    @DisplayName("Should return false for an empty directory with no bin folder")
    public void testValidJDKReturnsFalseForEmptyDirectory() {
        assertFalse(JDKVerifier.validJDK(tempDir),
                "validJDK should return false when directory has no java/javac executables");
    }

    @Test
    @DisplayName("Should return false when only java executable is present")
    public void testValidJDKReturnsFalseWhenOnlyJavaPresent() throws IOException {
        String os = System.getProperty("os.name");
        File binDir;

        if (os.contains("Mac") || os.contains("Darwin")) {
            binDir = new File(tempDir, "Contents/Home/bin");
        } else {
            binDir = new File(tempDir, "bin");
        }
        binDir.mkdirs();
        new File(binDir, "java").createNewFile();

        assertFalse(JDKVerifier.validJDK(tempDir),
                "validJDK should return false when only java is present but not javac");
    }

    @Test
    @DisplayName("Should return true when both java and javac executables are present")
    public void testValidJDKReturnsTrueForValidStructure() throws IOException {
        String os = System.getProperty("os.name");
        File binDir;

        if (os.contains("Mac") || os.contains("Darwin")) {
            binDir = new File(tempDir, "Contents/Home/bin");
        } else if (os.contains("Windows")) {
            binDir = new File(tempDir, "bin");
        } else {
            binDir = new File(tempDir, "bin");
        }
        binDir.mkdirs();

        String javaSuffix = os.contains("Windows") ? ".exe" : "";
        new File(binDir, "java" + javaSuffix).createNewFile();
        new File(binDir, "javac" + javaSuffix).createNewFile();

        assertTrue(JDKVerifier.validJDK(tempDir),
                "validJDK should return true when both java and javac are present");
    }


    //get executable path

    @Test
    @DisplayName("Should return null for null JDK path")
    public void testGetExecutablePathReturnsNullForNullPath() {
        assertNull(JDKVerifier.getExecutablePath(null, "java"),
                "getExecutablePath should return null when JDK path is null");
    }

    @Test
    @DisplayName("Should return null for empty JDK path")
    public void testGetExecutablePathReturnsNullForEmptyPath() {
        assertNull(JDKVerifier.getExecutablePath("", "java"),
                "getExecutablePath should return null when JDK path is empty");
    }

    @Test
    @DisplayName("Should return a non-null path for a valid JDK path and tool name")
    public void testGetExecutablePathReturnsNonNullForValidInput() {
        String result = JDKVerifier.getExecutablePath("/some/jdk/path", "java");

        assertNotNull(result,
                "getExecutablePath should return a path string for valid input");
    }

    @Test
    @DisplayName("Should include the tool name in the returned path")
    public void testGetExecutablePathIncludesToolName() {
        String resultJava = JDKVerifier.getExecutablePath("/some/jdk", "java");
        String resultJavac = JDKVerifier.getExecutablePath("/some/jdk", "javac");

        assertNotNull(resultJava, "Path for java should not be null");
        assertNotNull(resultJavac, "Path for javac should not be null");
        assertTrue(resultJava.endsWith("java") || resultJava.endsWith("java.exe"),
                "Returned path should end with the java executable name");
        assertTrue(resultJavac.endsWith("javac") || resultJavac.endsWith("javac.exe"),
                "Returned path should end with the javac executable name");
    }

    @Test
    @DisplayName("Should return paths for both java and javac from the same JDK root")
    public void testGetExecutablePathReturnsDifferentPathsForDifferentTools() {
        String javaPath = JDKVerifier.getExecutablePath("/some/jdk", "java");
        String javacPath = JDKVerifier.getExecutablePath("/some/jdk", "javac");

        assertNotEquals(javaPath, javacPath,
                "Paths for java and javac should be different");
    }

    @Test
    @DisplayName("Should include the JDK root path in the returned executable path")
    public void testGetExecutablePathIncludesJDKRoot() {
        String root = "/my/custom/jdk";
        String result = JDKVerifier.getExecutablePath(root, "java");

        assertNotNull(result, "Result should not be null");
        assertTrue(result.startsWith(root),
                "Returned path should start with the provided JDK root");
    }
}
