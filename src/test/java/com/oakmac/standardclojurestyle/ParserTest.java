package com.oakmac.standardclojurestyle;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

public class ParserTest {
    @Test
    public void testCharParser() {
        Map<String, Object> opts = new HashMap<>();
        opts.put("char", "a");
        opts.put("name", "char_test_a");
        Map<String, Object> charTest1 = Parser.Char(opts);
        ParserFunction parser = (ParserFunction)charTest1.get("parse");
        
        Node result1 = parser.parse("a", 0);
        assertNotNull(result1, "Should parse 'a'");
        assertEquals("char_test_a", result1.getName(), "Should have correct name");
        assertEquals("a", result1.getText(), "Should have correct text");
        assertEquals(0, result1.getStartIdx(), "Should have correct startIdx");
        assertEquals(1, result1.getEndIdx(), "Should have correct endIdx");
        
        Node result2 = parser.parse("=", 0);
        assertNull(result2, "Should not parse '='");
    }

    @Test
    public void testNotCharParser() {
        Map<String, Object> opts = new HashMap<>();
        opts.put("char", "a");
        opts.put("name", "notchar_test_a");
        Map<String, Object> notCharTest1 = Parser.NotChar(opts);
        ParserFunction parser = (ParserFunction)notCharTest1.get("parse");
        
        // Test matching any non-'a' character
        Node result1 = parser.parse("b", 0);
        assertNotNull(result1, "Should parse 'b'");
        assertEquals("notchar_test_a", result1.getName(), "Should have correct name");
        assertEquals("b", result1.getText(), "Should have correct text");
        assertEquals(0, result1.getStartIdx(), "Should have correct startIdx");
        assertEquals(1, result1.getEndIdx(), "Should have correct endIdx");
        
        // Test failing to match 'a'
        Node result2 = parser.parse("a", 0);
        assertNull(result2, "Should not parse 'a'");
        
        // Test with different characters
        assertNotNull(parser.parse("x", 0), "Should parse 'x'");
        assertNotNull(parser.parse("1", 0), "Should parse '1'");
        assertNotNull(parser.parse(" ", 0), "Should parse space");
        assertNotNull(parser.parse("!", 0), "Should parse '!'");
        
        // Test beyond string length
        assertNull(parser.parse("xyz", 3), "Should not parse beyond string length");
        
        // Test empty string
        assertNull(parser.parse("", 0), "Should not parse empty string");
    }

    @Test 
    public void testAnyCharParser() {
        Map<String, Object> opts = new HashMap<>();
        opts.put("name", "anychar_test1");
        Map<String, Object> anyCharTest1 = Parser.AnyChar(opts);
        ParserFunction parser = (ParserFunction)anyCharTest1.get("parse");
        
        Node result1 = parser.parse("a", 0);
        assertNotNull(result1, "Should parse 'a'");
        assertEquals("a", result1.getText(), "Should have correct text");
        
        Node result2 = parser.parse("b", 0);
        assertNotNull(result2, "Should parse 'b'");
        assertEquals("b", result2.getText(), "Should have correct text");
        assertEquals("anychar_test1", result2.getName(), "Should have correct name");
        
        assertNotNull(parser.parse(" ", 0), "Should parse space");
        assertNotNull(parser.parse("+", 0), "Should parse '+'");
        
        Node result3 = parser.parse("!~^", 0);
        assertNotNull(result3, "Should parse first char of multiple chars");
        assertEquals("!", result3.getText(), "Should have correct text");
        
        assertNull(parser.parse("", 0), "Should not parse empty string");
    }

    @Test
    public void testStringParser() {
        Map<String, Object> opts = new HashMap<>();
        opts.put("str", "foo");
        opts.put("name", "string_test_foo");
        Map<String, Object> stringTest1 = Parser.String(opts);
        ParserFunction parser = (ParserFunction)stringTest1.get("parse");
        
        // Basic matching
        Node result1 = parser.parse("foo", 0);
        assertNotNull(result1, "Should parse 'foo'");
        assertEquals("string_test_foo", result1.getName(), "Should have correct name");
        assertEquals("foo", result1.getText(), "Should have correct text");
        assertEquals(0, result1.getStartIdx(), "Should have correct startIdx");
        assertEquals(3, result1.getEndIdx(), "Should have correct endIdx");
        
        // Non-matching
        Node result2 = parser.parse("bar", 0);
        assertNull(result2, "Should not parse 'bar'");
        
        // Partial match
        Node result3 = parser.parse("fo", 0);
        assertNull(result3, "Should not parse partial match");
        
        // Middle of string
        Node result4 = parser.parse("foo", 1);
        assertNull(result4, "Should not parse from middle");
        
        // With leading characters
        Node result5 = parser.parse("barfoo", 3);
        assertNotNull(result5, "Should parse with offset");
        assertEquals(3, result5.getStartIdx(), "Should have correct startIdx");
        assertEquals(6, result5.getEndIdx(), "Should have correct endIdx");
        assertEquals("foo", result5.getText(), "Should have correct text");
        
        // With trailing characters
        Node result6 = parser.parse("foobar", 0);
        assertNotNull(result6, "Should parse with trailing chars");
        assertEquals("foo", result6.getText(), "Should have correct text");
        assertEquals(3, result6.getEndIdx(), "Should have correct endIdx");
    }
}