package zenit.javacodecompiler;

import zenit.javacodecompiler.DebugError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DebugError class
 *
 * Tests error representation for compilation/runtime errors.
 * Ensures all error information (place, type, message, position)
 * is stored and retrieved correctly.
 *
 * Metrics:
 * - WMC: 7 (Low complexity - easy to test)
 * - OCavg: 1.0 (Low coupling - pure data model)
 * - Testability: EXCELLENT
 *
 * Related requirements:
 * - FUI400: Display errors in text editor during coding
 */
class DebugErrorTest {

    @Test
    @DisplayName("Should create DebugError with all fields correctly")
    void testConstructorWithAllFields() {
        // Arrange & Act
        DebugError error = new DebugError(
                "Main.java",
                "Syntax Error",
                "Missing semicolon",
                10,
                25
        );

        // Assert
        assertEquals("Main.java", error.getPlace(),
                "Place should be 'Main.java'");
        assertEquals("Syntax Error", error.getProblemType(),
                "Problem type should be 'Syntax Error'");
        assertEquals("Missing semicolon", error.getProblem(),
                "Problem should be 'Missing semicolon'");
        assertEquals(10, error.getRow(),
                "Row should be 10");
        assertEquals(25, error.getColumn(),
                "Column should be 25");
    }

    @Test
    @DisplayName("Should handle empty strings in all fields")
    void testEmptyStrings() {
        // Arrange & Act
        DebugError error = new DebugError("", "", "", 0, 0);

        // Assert
        assertEquals("", error.getPlace(),
                "Empty place should be stored as empty string");
        assertEquals("", error.getProblemType(),
                "Empty problem type should be stored as empty string");
        assertEquals("", error.getProblem(),
                "Empty problem should be stored as empty string");
        assertEquals(0, error.getRow(),
                "Row 0 should be valid");
        assertEquals(0, error.getColumn(),
                "Column 0 should be valid");
    }

    @Test
    @DisplayName("Should handle null strings gracefully")
    void testNullStrings() {
        // Arrange & Act
        DebugError error = new DebugError(null, null, null, 1, 1);

        // Assert
        assertNull(error.getPlace(),
                "Null place should remain null");
        assertNull(error.getProblemType(),
                "Null problem type should remain null");
        assertNull(error.getProblem(),
                "Null problem should remain null");
        assertEquals(1, error.getRow());
        assertEquals(1, error.getColumn());
    }

    @Test
    @DisplayName("Should handle negative row and column values")
    void testNegativeValues() {
        // Arrange & Act
        DebugError error = new DebugError(
                "Test.java",
                "Error",
                "Problem",
                -1,
                -5
        );

        // Assert
        assertEquals(-1, error.getRow(),
                "Negative row should be allowed (might indicate unknown position)");
        assertEquals(-5, error.getColumn(),
                "Negative column should be allowed");
    }

    @Test
    @DisplayName("toString should contain all error information")
    void testToString() {
        // Arrange
        DebugError error = new DebugError(
                "Calculator.java",
                "Runtime Error",
                "Division by zero",
                42,
                15
        );

        // Act
        String result = error.toString();

        // Assert - All information must be present
        assertTrue(result.contains("Calculator.java"),
                "toString should contain the place (file name)");
        assertTrue(result.contains("Runtime Error"),
                "toString should contain the problem type");
        assertTrue(result.contains("Division by zero"),
                "toString should contain the error message");
        assertTrue(result.contains("42:15"),
                "toString should contain row:column position");
    }

    @Test
    @DisplayName("toString should handle null values without crashing")
    void testToStringWithNulls() {
        // Arrange
        DebugError error = new DebugError(null, null, null, 0, 0);

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> error.toString(),
                "toString should handle null values gracefully");

        String result = error.toString();
        assertTrue(result.contains("null"),
                "toString should display 'null' for null values");
    }

    @Test
    @DisplayName("Should handle very long strings without issues")
    void testLongStrings() {
        // Arrange
        String longString = "A".repeat(1000);

        // Act
        DebugError error = new DebugError(
                longString,
                longString,
                longString,
                999,
                999
        );

        // Assert
        assertEquals(longString, error.getPlace(),
                "Should handle long place string");
        assertEquals(longString, error.getProblemType(),
                "Should handle long problem type string");
        assertEquals(longString, error.getProblem(),
                "Should handle long problem string");
        assertEquals(999, error.getRow());
        assertEquals(999, error.getColumn());
    }

    @Test
    @DisplayName("Should handle realistic compiler error scenario")
    void testRealisticCompilerError() {
        // Arrange - Realistic scenario from Java compiler
        DebugError error = new DebugError(
                "src/main/Main.java",
                "error",
                "';' expected",
                15,
                34
        );

        // Act
        String display = error.toString();

        // Assert - Must have all info user needs to fix the error
        assertTrue(display.contains("src/main/Main.java"),
                "User needs to know which file has the error");
        assertTrue(display.contains("error"),
                "User needs to know error severity");
        assertTrue(display.contains("';' expected"),
                "User needs to know what's wrong");
        assertTrue(display.contains("15"),
                "User needs to know which line");
        assertTrue(display.contains("34"),
                "User needs to know which column");
    }

    @Test
    @DisplayName("Should handle special characters in error messages")
    void testSpecialCharacters() {
        // Arrange
        DebugError error = new DebugError(
                "Test.java",
                "Parse Error",
                "Unexpected token: '\\n' at position 5",
                8,
                12
        );

        // Act & Assert
        assertEquals("Unexpected token: '\\n' at position 5", error.getProblem(),
                "Should handle special characters like \\n in error messages");

        String result = error.toString();
        assertTrue(result.contains("\\n"),
                "Special characters should be preserved in toString");
    }

    @Test
    @DisplayName("Should create multiple distinct DebugError instances")
    void testMultipleInstances() {
        // Arrange & Act
        DebugError error1 = new DebugError("File1.java", "Error1", "Problem1", 1, 1);
        DebugError error2 = new DebugError("File2.java", "Error2", "Problem2", 2, 2);

        // Assert - Instances should be independent
        assertNotEquals(error1.getPlace(), error2.getPlace(),
                "Different instances should have different values");
        assertNotEquals(error1.getRow(), error2.getRow(),
                "Different instances should be independent");
    }
}