package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotCharParserTest {
    @Test
    void testParse() {
        NotCharParser notCharTest1 = new NotCharParser('a', "notchar_test_a");
        
        // Test matching any non-'a' character
        Node result = notCharTest1.parse("b", 0);
        assertEquals("notchar_test_a", result.getName());
        assertEquals("b", result.getText());
        assertEquals(0, result.getStartIdx());
        assertEquals(1, result.getEndIdx());
        
        // Test failing to match 'a'
        assertNull(notCharTest1.parse("a", 0));
        
        // Test with different characters
        assertEquals("x", notCharTest1.parse("x", 0).getText());
        assertEquals("1", notCharTest1.parse("1", 0).getText());
        assertEquals(" ", notCharTest1.parse(" ", 0).getText());
        assertEquals("!", notCharTest1.parse("!", 0).getText());
        
        // Test with position beyond string length
        assertNull(notCharTest1.parse("xyz", 3));
        
        // Test with empty string
        assertNull(notCharTest1.parse("", 0));
        
        // Test with special character
        NotCharParser notCharTest2 = new NotCharParser('$', "notchar_test_special");
        assertEquals("a", notCharTest2.parse("a", 0).getText());
        assertNull(notCharTest2.parse("$", 0));
    }
}