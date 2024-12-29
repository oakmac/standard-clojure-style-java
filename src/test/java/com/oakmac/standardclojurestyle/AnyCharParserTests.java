package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnyCharParserTest {
    @Test
    void testParse() {
        AnyCharParser parser = new AnyCharParser("anychar_test1");
        
        // Test various single characters
        assertEquals("a", parser.parse("a", 0).getText());
        assertEquals("b", parser.parse("b", 0).getText());
        assertEquals("anychar_test1", parser.parse("b", 0).getName());
        assertEquals(" ", parser.parse(" ", 0).getText());
        assertEquals("+", parser.parse("+", 0).getText());
        assertEquals("!", parser.parse("!~^", 0).getText());
        
        // Test empty string
        assertNull(parser.parse("", 0));
    }
}