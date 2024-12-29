package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OptionalParserTest {
    @Test
    void testParse() {
        // Create an optional parser wrapping a char parser
        IParser charParser = new CharParser('a', "optional_a");
        OptionalParser optParser = new OptionalParser(charParser);

        // Test successful match
        Node result1 = optParser.parse("abc", 0);
        assertEquals(0, result1.getStartIdx());
        assertEquals(1, result1.getEndIdx());
        assertEquals("optional_a", result1.getName());
        assertEquals("a", result1.getText());

        // Test no match - should return empty node at same position
        Node result2 = optParser.parse("xyz", 0);
        assertEquals(0, result2.getStartIdx());
        assertEquals(0, result2.getEndIdx());
        assertNull(result2.getName());
        
        // Test with offset
        Node result3 = optParser.parse("xabc", 1);
        assertEquals(1, result3.getStartIdx());
        assertEquals(2, result3.getEndIdx());
        assertEquals("optional_a", result3.getName());
        assertEquals("a", result3.getText());

        // Test empty string
        Node result4 = optParser.parse("", 0);
        assertEquals(0, result4.getStartIdx());
        assertEquals(0, result4.getEndIdx());
        assertNull(result4.getName());

        // Test beyond string length
        Node result5 = optParser.parse("a", 1);
        assertEquals(1, result5.getStartIdx());
        assertEquals(1, result5.getEndIdx());
        assertNull(result5.getName());
    }

    @Test
    void testWithStringParser() {
        // Test with a string parser to ensure it works with other parser types
        IParser stringParser = new StringParser("foo", "optional_foo");
        OptionalParser optParser = new OptionalParser(stringParser);

        // Test successful match
        Node result1 = optParser.parse("foobar", 0);
        assertEquals(0, result1.getStartIdx());
        assertEquals(3, result1.getEndIdx());
        assertEquals("optional_foo", result1.getName());
        assertEquals("foo", result1.getText());

        // Test no match
        Node result2 = optParser.parse("bar", 0);
        assertEquals(0, result2.getStartIdx());
        assertEquals(0, result2.getEndIdx());
        assertNull(result2.getName());
    }
}