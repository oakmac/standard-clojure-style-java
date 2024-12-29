package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StringParserTest {
    @Test
    void testParse() {
        StringParser stringTest1 = new StringParser("foo", "string_test_foo");
        
        // Test exact match
        Node result = stringTest1.parse("foo", 0);
        assertEquals("string_test_foo", result.getName());
        assertEquals("foo", result.getText());
        assertEquals(0, result.getStartIdx());
        assertEquals(3, result.getEndIdx());
        
        // Test non-matching string
        assertNull(stringTest1.parse("bar", 0));
        
        // Test partial match
        assertNull(stringTest1.parse("fo", 0));
        
        // Test inside middle of the string
        assertNull(stringTest1.parse("foo", 1));
        
        // Test with leading characters
        Node result1 = stringTest1.parse("barfoo", 3);
        assertEquals("string_test_foo", result1.getName());
        assertEquals("foo", result1.getText());
        assertEquals(3, result1.getStartIdx());
        assertEquals(6, result1.getEndIdx());
        
        // Test with trailing characters
        Node result2 = stringTest1.parse("foobar", 0);
        assertEquals("foo", result2.getText());
        assertEquals(3, result2.getEndIdx());
    }
}