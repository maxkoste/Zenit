
package zenit.util;

import zenit.util.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Tuple class
 * Tests generic data structure with different types
 */
class TupleTest {

    private Tuple<String, Integer> stringIntTuple;
    private Tuple<Integer, Integer> intIntTuple;

    @BeforeEach
    void setUp() {
        stringIntTuple = new Tuple<>();
        intIntTuple = new Tuple<>();
    }

    @Test
    @DisplayName("Should create empty tuple with default constructor")
    void testEmptyConstructor() {
        Tuple<String, String> tuple = new Tuple<>();
        assertNull(tuple.fst(), "First element should be null");
        assertNull(tuple.snd(), "Second element should be null");
    }

    @Test
    @DisplayName("Should create tuple with initial values")
    void testConstructorWithValues() {
        Tuple<String, Integer> tuple = new Tuple<>("hello", 42);

        assertEquals("hello", tuple.fst(), "First element should be 'hello'");
        assertEquals(42, tuple.snd(), "Second element should be 42");
    }

    @Test
    @DisplayName("Should set values correctly")
    void testSet() {
        stringIntTuple.set("test", 100);

        assertEquals("test", stringIntTuple.fst());
        assertEquals(100, stringIntTuple.snd());
    }

    @Test
    @DisplayName("Should update values when set called multiple times")
    void testSetMultipleTimes() {
        stringIntTuple.set("first", 1);
        assertEquals("first", stringIntTuple.fst());
        assertEquals(1, stringIntTuple.snd());

        stringIntTuple.set("second", 2);
        assertEquals("second", stringIntTuple.fst());
        assertEquals(2, stringIntTuple.snd());
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        Tuple<String, String> tuple = new Tuple<>(null, null);

        assertNull(tuple.fst());
        assertNull(tuple.snd());
    }

    @Test
    @DisplayName("Should work with different types")
    void testDifferentTypes() {
        Tuple<Integer, String> intStringTuple = new Tuple<>(123, "abc");
        assertEquals(123, intStringTuple.fst());
        assertEquals("abc", intStringTuple.snd());

        Tuple<Boolean, Double> boolDoubleTuple = new Tuple<>(true, 3.14);
        assertEquals(true, boolDoubleTuple.fst());
        assertEquals(3.14, boolDoubleTuple.snd());
    }

    @Test
    @DisplayName("toString should format tuple correctly")
    void testToString() {
        Tuple<String, Integer> tuple = new Tuple<>("hello", 42);
        String result = tuple.toString();

        assertTrue(result.contains("hello"), "Should contain first element");
        assertTrue(result.contains("42"), "Should contain second element");
        assertTrue(result.contains("("), "Should start with parenthesis");
        assertTrue(result.contains(","), "Should have comma separator");
    }


    @Test
    @DisplayName("BUG: toString missing closing parenthesis")
    void testToString_bugDocumentation() {
        Tuple<String, Integer> tuple = new Tuple<>("test", 1);
        String result = tuple.toString();

        // This test FAILS -

        assertFalse(result.endsWith(")"),
                "BUG FOUND: toString() is missing closing parenthesis. " +
                        "Expected format: (fst, snd) but got: " + result);
    }
}
