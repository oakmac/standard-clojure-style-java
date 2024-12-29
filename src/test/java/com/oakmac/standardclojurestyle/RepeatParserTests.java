package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RepeatParserTest {
    @Test
    void testParse() {
        RepeatParser testRepeat1 = new RepeatParser(
            new CharParser('a', "AAA"),
            "repeat_test_1"
        );

        // Test no matches
        Node repeatResult1 = testRepeat1.parse("b", 0);
        assertEquals(0, repeatResult1.getStartIdx());
        assertEquals(0, repeatResult1.getEndIdx());
        assertNull(repeatResult1.getName());

        // Test single match
        Node repeatResult2 = testRepeat1.parse("a", 0);
        assertEquals(0, repeatResult2.getStartIdx());
        assertEquals(1, repeatResult2.getEndIdx());
        assertEquals("repeat_test_1", repeatResult2.getName());
        Node[] repeatResult2Children = repeatResult2.getChildren();
        assertNotNull(repeatResult2Children);
        assertEquals(1, repeatResult2Children.length);
        assertEquals(0, repeatResult2Children[0].getStartIdx());
        assertEquals(1, repeatResult2Children[0].getEndIdx());
        assertEquals("a", repeatResult2Children[0].getText());
        assertEquals("AAA", repeatResult2Children[0].getName());

        // Test multiple matches
        Node repeatResult3 = testRepeat1.parse("aa", 0);
        assertEquals(0, repeatResult3.getStartIdx());
        assertEquals(2, repeatResult3.getEndIdx());
        assertEquals("repeat_test_1", repeatResult3.getName());
        Node[] repeatResult3Children = repeatResult3.getChildren();
        assertNotNull(repeatResult3Children);
        assertEquals(2, repeatResult3Children.length);
        assertEquals(0, repeatResult3Children[0].getStartIdx());
        assertEquals(1, repeatResult3Children[0].getEndIdx());
        assertEquals("a", repeatResult3Children[0].getText());
        assertEquals("AAA", repeatResult3Children[0].getName());
        assertEquals(1, repeatResult3Children[1].getStartIdx());
        assertEquals(2, repeatResult3Children[1].getEndIdx());
        assertEquals("a", repeatResult3Children[1].getText());
        assertEquals("AAA", repeatResult3Children[1].getName());

        // Test with offset
        Node repeatResult4 = testRepeat1.parse("baac", 1);
        assertEquals(1, repeatResult4.getStartIdx());
        assertEquals(3, repeatResult4.getEndIdx());
        assertEquals("repeat_test_1", repeatResult4.getName());
        Node[] repeatResult4Children = repeatResult4.getChildren();
        assertNotNull(repeatResult4Children);
        assertEquals(2, repeatResult4Children.length);

        assertEquals(1, repeatResult4Children[0].getStartIdx());
        assertEquals(2, repeatResult4Children[0].getEndIdx());
        assertEquals("a", repeatResult4Children[0].getText());
        assertEquals("AAA", repeatResult4Children[0].getName());

        assertEquals(2, repeatResult4Children[1].getStartIdx());
        assertEquals(3, repeatResult4Children[1].getEndIdx());
        assertEquals("a", repeatResult4Children[1].getText());
        assertEquals("AAA", repeatResult4Children[1].getName());
    }
}