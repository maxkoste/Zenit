package zenit.filesystem;

import zenit.filesystem.RunnableClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RunnableClass
 *
 * Tests configuration for runnable Java classes including:
 * - Path to .java file
 * - Program arguments (passed to main method)
 * - VM arguments (passed to JVM)
 *
 * Metrics:
 * - WMC: 10 (Low complexity)
 * - OCavg: 1.0 (Low coupling)
 *
 * Related requirements:
 * - FUI102: Run button should work with libraries
 * - FCC100: Can run selected .java file
 * - FUF200: Program uses standard JDK
 */
class RunnableClassTest {

    @Test
    @DisplayName("Should create RunnableClass with all parameters")
    void testConstructorWithAllParameters() {
        // Arrange & Act
        RunnableClass rc = new RunnableClass(
                "src/main/Main.java",
                "-arg1 -arg2",
                "-Xmx512m"
        );

        // Assert
        assertEquals("src/main/Main.java", rc.getPath(),
                "Path should be set correctly");
        assertEquals("-arg1 -arg2", rc.getPaArguments(),
                "Program arguments should be set correctly");
        assertEquals("-Xmx512m", rc.getVmArguments(),
                "VM arguments should be set correctly");
    }

    @Test
    @DisplayName("Should create RunnableClass with only path (default empty args)")
    void testConstructorWithPathOnly() {
        // Arrange & Act
        RunnableClass rc = new RunnableClass("src/Test.java");

        // Assert
        assertEquals("src/Test.java", rc.getPath(),
                "Path should be set correctly");
        assertEquals("", rc.getPaArguments(),
                "Program arguments should default to empty string");
        assertEquals("", rc.getVmArguments(),
                "VM arguments should default to empty string");
    }

    @Test
    @DisplayName("Should create RunnableClass from File object")
    void testConstructorWithFile() {
        // Arrange
        File testFile = new File("src/example/Example.java");

        // Act
        RunnableClass rc = new RunnableClass(testFile);

        // Assert
        assertEquals(testFile.getPath(), rc.getPath(),
                "Path should be extracted from File object");
        assertEquals("", rc.getPaArguments(),
                "Program arguments should default to empty");
        assertEquals("", rc.getVmArguments(),
                "VM arguments should default to empty");
    }

    @Test
    @DisplayName("Should set and get path correctly")
    void testSetAndGetPath() {
        // Arrange
        RunnableClass rc = new RunnableClass("old/path.java");

        // Act
        rc.setPath("new/path.java");

        // Assert
        assertEquals("new/path.java", rc.getPath(),
                "Path should be updated to new value");
    }

    @Test
    @DisplayName("Should set and get program arguments correctly")
    void testSetAndGetPaArguments() {
        // Arrange
        RunnableClass rc = new RunnableClass("path.java");

        // Act
        rc.setPaArguments("--verbose --debug");

        // Assert
        assertEquals("--verbose --debug", rc.getPaArguments(),
                "Program arguments should be updated");
    }

    @Test
    @DisplayName("Should set and get VM arguments correctly")
    void testSetAndGetVmArguments() {
        // Arrange
        RunnableClass rc = new RunnableClass("path.java");

        // Act
        rc.setVmArguments("-Xmx1024m -XX:+UseG1GC");

        // Assert
        assertEquals("-Xmx1024m -XX:+UseG1GC", rc.getVmArguments(),
                "VM arguments should be updated");
    }

    @Test
    @DisplayName("toString should contain all fields separated by newlines")
    void testToString() {
        // Arrange
        RunnableClass rc = new RunnableClass(
                "src/Main.java",
                "-arg",
                "-Xmx256m"
        );

        // Act
        String result = rc.toString();

        // Assert - All fields must be present
        assertTrue(result.contains("src/Main.java"),
                "toString should contain path");
        assertTrue(result.contains("-arg"),
                "toString should contain program arguments");
        assertTrue(result.contains("-Xmx256m"),
                "toString should contain VM arguments");

        // Should have newlines between fields (based on implementation)
        String[] lines = result.split("\n");
        assertEquals(3, lines.length,
                "toString should have 3 lines: path, paArgs, vmArgs");
    }

    @Test
    @DisplayName("Should handle null path")
    void testNullPath() {
        // Arrange & Act
        RunnableClass rc = new RunnableClass(null, "arg", "vm");

        // Assert
        assertNull(rc.getPath(),
                "Null path should be allowed");
        assertEquals("arg", rc.getPaArguments());
        assertEquals("vm", rc.getVmArguments());
    }

    @Test
    @DisplayName("Should handle empty strings for all fields")
    void testEmptyStrings() {
        // Arrange & Act
        RunnableClass rc = new RunnableClass("", "", "");

        // Assert
        assertEquals("", rc.getPath(),
                "Empty path should be allowed");
        assertEquals("", rc.getPaArguments(),
                "Empty program arguments should be allowed");
        assertEquals("", rc.getVmArguments(),
                "Empty VM arguments should be allowed");
    }

    @Test
    @DisplayName("Should update fields independently without affecting others")
    void testIndependentFieldUpdates() {
        // Arrange
        RunnableClass rc = new RunnableClass("path1", "arg1", "vm1");

        // Act & Assert - Update path only
        rc.setPath("path2");
        assertEquals("path2", rc.getPath(),
                "Path should be updated");
        assertEquals("arg1", rc.getPaArguments(),
                "Program arguments should NOT be affected by path update");
        assertEquals("vm1", rc.getVmArguments(),
                "VM arguments should NOT be affected by path update");

        // Act & Assert - Update program arguments only
        rc.setPaArguments("arg2");
        assertEquals("path2", rc.getPath(),
                "Path should NOT be affected by program arguments update");
        assertEquals("arg2", rc.getPaArguments(),
                "Program arguments should be updated");
        assertEquals("vm1", rc.getVmArguments(),
                "VM arguments should NOT be affected by program arguments update");

        // Act & Assert - Update VM arguments only
        rc.setVmArguments("vm2");
        assertEquals("path2", rc.getPath(),
                "Path should NOT be affected by VM arguments update");
        assertEquals("arg2", rc.getPaArguments(),
                "Program arguments should NOT be affected by VM arguments update");
        assertEquals("vm2", rc.getVmArguments(),
                "VM arguments should be updated");
    }

    @Test
    @DisplayName("Should handle realistic Java file paths")
    void testRealisticPaths() {
        // Arrange & Act
        RunnableClass rc1 = new RunnableClass("src/main/java/com/example/Main.java");
        RunnableClass rc2 = new RunnableClass("C:/Projects/Zenit/src/Test.java");
        RunnableClass rc3 = new RunnableClass("./relative/path/App.java");

        // Assert
        assertEquals("src/main/java/com/example/Main.java", rc1.getPath());
        assertEquals("C:/Projects/Zenit/src/Test.java", rc2.getPath());
        assertEquals("./relative/path/App.java", rc3.getPath());
    }

    @Test
    @DisplayName("Should handle realistic program arguments")
    void testRealisticProgramArguments() {
        // Arrange & Act
        RunnableClass rc = new RunnableClass("Main.java");

        // Various realistic program arguments
        rc.setPaArguments("--port 8080 --host localhost");
        assertEquals("--port 8080 --host localhost", rc.getPaArguments());

        rc.setPaArguments("input.txt output.txt");
        assertEquals("input.txt output.txt", rc.getPaArguments());

        rc.setPaArguments("--verbose");
        assertEquals("--verbose", rc.getPaArguments());
    }

    @Test
    @DisplayName("Should handle realistic VM arguments")
    void testRealisticVmArguments() {
        // Arrange & Act
        RunnableClass rc = new RunnableClass("Main.java");

        // Various realistic VM arguments
        rc.setVmArguments("-Xmx1024m");
        assertEquals("-Xmx1024m", rc.getVmArguments(),
                "Should handle max heap size argument");

        rc.setVmArguments("-Xms512m -Xmx2048m");
        assertEquals("-Xms512m -Xmx2048m", rc.getVmArguments(),
                "Should handle multiple memory arguments");

        rc.setVmArguments("-XX:+UseG1GC -Xlog:gc");
        assertEquals("-XX:+UseG1GC -Xlog:gc", rc.getVmArguments(),
                "Should handle GC and logging arguments");
    }

    @Test
    @DisplayName("Should handle File with different path separators")
    void testFileWithDifferentSeparators() {
        // Arrange - File will normalize path separators based on OS
        File windowsStyle = new File("C:\\Users\\test\\Main.java");
        File unixStyle = new File("/home/user/test/Main.java");

        // Act
        RunnableClass rc1 = new RunnableClass(windowsStyle);
        RunnableClass rc2 = new RunnableClass(unixStyle);

        // Assert - Should preserve File's path representation
        assertEquals(windowsStyle.getPath(), rc1.getPath());
        assertEquals(unixStyle.getPath(), rc2.getPath());
    }

    @Test
    @DisplayName("toString should not throw exception with null values")
    void testToStringWithNulls() {
        // Arrange
        RunnableClass rc = new RunnableClass(null, null, null);

        // Act & Assert
        assertDoesNotThrow(() -> rc.toString(),
                "toString should not throw exception with null values");
    }

    @Test
    @DisplayName("Should create multiple independent instances")
    void testMultipleIndependentInstances() {
        // Arrange & Act
        RunnableClass rc1 = new RunnableClass("File1.java", "args1", "vm1");
        RunnableClass rc2 = new RunnableClass("File2.java", "args2", "vm2");

        // Assert - Instances should be independent
        assertNotEquals(rc1.getPath(), rc2.getPath());
        assertNotEquals(rc1.getPaArguments(), rc2.getPaArguments());
        assertNotEquals(rc1.getVmArguments(), rc2.getVmArguments());

        // Modifying one should not affect the other
        rc1.setPath("Modified.java");
        assertEquals("Modified.java", rc1.getPath());
        assertEquals("File2.java", rc2.getPath(),
                "Modifying rc1 should not affect rc2");
    }
}