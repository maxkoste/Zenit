package zenit.util;

import zenit.util.StringUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StringUtilities class
 *
 * Test Strategy: White-box testing
 * Coverage Target: 100%
 *
 * Based on metrics:
 * - WMC: Low (2 static methods)
 * - OCavg: 0 (no dependencies)
 * - Testability: EXCELLENT
 *
 * @author MT
 */
class StringUtilitiesTest {


    @Test
    @DisplayName("Should return 0 for null string")
    void testCountLeadingSpaces_nullString() {
        int result = StringUtilities.countLeadingSpaces(null);
        assertEquals(0, result, "Null string should return 0 leading spaces");
    }

    @Test
    @DisplayName("Should return 0 for empty string")
    void testCountLeadingSpaces_emptyString() {
        int result = StringUtilities.countLeadingSpaces("");
        assertEquals(0, result, "Empty string should return 0 leading spaces");
    }

    @Test
    @DisplayName("Should return 0 for string without leading spaces")
    void testCountLeadingSpaces_noLeadingSpaces() {
        int result = StringUtilities.countLeadingSpaces("hello");
        assertEquals(0, result, "String without leading spaces should return 0");
    }

    @ParameterizedTest
    @DisplayName("Should count leading spaces correctly")
    @CsvSource({
            "'  hello', 2",      // 2 spaces
            "'    world', 4",    // 4 spaces
            "' test', 1",        // 1 space
            "'      ', 6"        // 6 spaces only
    })
    void testCountLeadingSpaces_withSpaces(String input, int expected) {
        int result = StringUtilities.countLeadingSpaces(input);
        assertEquals(expected, result,
                "Should count " + expected + " leading spaces in '" + input + "'");
    }

    @Test
    @DisplayName("Should handle string with trailing spaces")
    void testCountLeadingSpaces_trailingSpaces() {
        int result = StringUtilities.countLeadingSpaces("hello  ");
        assertEquals(0, result, "Should only count LEADING spaces, not trailing");
    }

    //  count() tests

    @Test
    @DisplayName("Should return 0 for null haystack")
    void testCount_nullHaystack() {
        int result = StringUtilities.count(null, 'a');
        assertEquals(0, result, "Null haystack should return 0");
    }

    @Test
    @DisplayName("Should return 0 for empty haystack")
    void testCount_emptyHaystack() {
        int result = StringUtilities.count("", 'a');
        assertEquals(0, result, "Empty haystack should return 0");
    }

    @Test
    @DisplayName("Should return 0 when character not found")
    void testCount_characterNotFound() {
        int result = StringUtilities.count("hello", 'x');
        assertEquals(0, result, "Should return 0 when character not found");
    }

    @ParameterizedTest
    @DisplayName("Should count character occurrences correctly")
    @CsvSource({
            "hello, l, 2",           // 'l' appears twice
            "programming, g, 2",     // 'g' appears twice
            "Mississippi, i, 4",     // 'i' appears 4 times
            "aaaa, a, 4",            // all same character
            "test, e, 1"             // single occurrence
    })
    void testCount_countOccurrences(String haystack, char needle, int expected) {
        int result = StringUtilities.count(haystack, needle);
        assertEquals(expected, result,
                "Should count " + expected + " occurrences of '" + needle + "' in '" + haystack + "'");
    }

    @Test
    @DisplayName("Should be case-sensitive")
    void testCount_caseSensitive() {
        int result = StringUtilities.count("Hello", 'h');
        assertEquals(0, result, "Should be case-sensitive: 'h' != 'H'");

        int result2 = StringUtilities.count("Hello", 'H');
        assertEquals(1, result2, "Should find uppercase 'H'");
    }

    @Test
    @DisplayName("Should count special characters")
    void testCount_specialCharacters() {
        assertEquals(3, StringUtilities.count("a.b.c.d", '.'),
                "Should count special characters like '.'");
        assertEquals(1, StringUtilities.count("hello world", ' '),
                "Should count spaces");
        assertEquals(1, StringUtilities.count("test!", '!'),
                "Should count exclamation marks");
    }

    //  Edge Cases

    @Test
    @DisplayName("Recursive countLeadingSpaces should handle many spaces")
    void testCountLeadingSpaces_manySpaces() {
        String manySpaces = " ".repeat(100);
        int result = StringUtilities.countLeadingSpaces(manySpaces);
        assertEquals(100, result, "Should handle 100 leading spaces");
    }

    @Test
    @DisplayName("Should handle tabs correctly")
    void testCountLeadingSpaces_tabs() {
        assertEquals(0, StringUtilities.countLeadingSpaces("\thello"),
                "Tabs should not be counted as spaces");
    }

    @Test
    @DisplayName("Should handle long strings")
    void testCount_longString() {
        String longString = "a".repeat(1000);
        assertEquals(1000, StringUtilities.count(longString, 'a'),
                "Should handle long strings");
    }

    @Test
    @DisplayName("Should count newline characters")
     void testCount_newlines() {
        assertEquals(2, StringUtilities.count("hello\nworld\n", '\n'),
                "Should count newline characters");
    }

}