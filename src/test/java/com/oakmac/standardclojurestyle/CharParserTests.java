package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharParserTest {
    @Test
    void testParse() {
        CharParser charTest1 = new CharParser('a', "char_test_a");
        
        // Test matching character 'a'
        Node result1 = charTest1.parse("a", 0);
        assertEquals("char_test_a", result1.getName());
        assertEquals("a", result1.getText());
        assertEquals(0, result1.getStartIdx());
        assertEquals(1, result1.getEndIdx());
        
        // Test non-matching character
        assertNull(charTest1.parse("=", 0));

        // Test with equals sign
        CharParser charTest2 = new CharParser('=', "char_test_equals");
        Node result2 = charTest2.parse("=", 0);
        assertEquals("char_test_equals", result2.getName());
        assertEquals("=", result2.getText());
        
        // Test non-matching character
        assertNull(charTest2.parse("a", 0));
    }
}