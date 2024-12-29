package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegexParserTest {
    @Test
    void testParse() {
        RegexParser regexTest1 = new RegexParser("(c|d)+", "foo");
        
        // matches must be made at the beginning of what we're matching
        assertNull(regexTest1.parse("aaacb", 0));
        
        // match single character
        Node regexResult2 = regexTest1.parse("aaacb", 3);
        assertEquals("foo", regexResult2.getName());
        assertEquals(3, regexResult2.getStartIdx());
        assertEquals(4, regexResult2.getEndIdx());
        assertEquals("c", regexResult2.getText());
        
        // match multiple characters
        Node regexResult3 = regexTest1.parse("aaacddb", 3);
        assertEquals("foo", regexResult3.getName());
        assertEquals(3, regexResult3.getStartIdx());
        assertEquals(6, regexResult3.getEndIdx());
        assertEquals("cdd", regexResult3.getText());
        
        // match from middle position
        Node regexResult4 = regexTest1.parse("aaacddb", 4);
        assertEquals("foo", regexResult4.getName());
        assertEquals(4, regexResult4.getStartIdx());
        assertEquals(6, regexResult4.getEndIdx());
        assertEquals("dd", regexResult4.getText());
    }

    @Test
    void testParseWithGroups() {
        // Test with a specific group index
        RegexParser groupTest = new RegexParser("(foo)(bar)", "group_test", 0);
        Node result = groupTest.parse("foobar", 0);
        assertEquals("foo", result.getText());
        assertEquals(0, result.getStartIdx());
        assertEquals(3, result.getEndIdx());
    }
}