package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SequenceParserTest {
    @Test
    void testParse() {
        IParser[] parsers = new IParser[]{
            new CharParser('a', "AAA"),
            new CharParser('b', "BBB"),
            new CharParser('c', "CCC")
        };
        
        SequenceParser testSeq1 = new SequenceParser(parsers, "seq_test_1");
        
        // Test successful sequence
        Node seqResult1 = testSeq1.parse("abc", 0);
        assertEquals(0, seqResult1.getStartIdx());
        assertEquals(3, seqResult1.getEndIdx());
        assertEquals("abc", seqResult1.getText());
        
        // Test children names
        Node[] children = seqResult1.getChildren();
        assertEquals("AAA", children[0].getName());
        assertEquals("BBB", children[1].getName());
        assertEquals("CCC", children[2].getName());
        
        // Test invalid sequences
        assertNull(testSeq1.parse("aba", 0));
        assertNull(testSeq1.parse("ab", 0));
        
        // Test sequence with extra characters
        Node seqResult4 = testSeq1.parse("abcd", 0);
        assertEquals(0, seqResult4.getStartIdx());
        assertEquals(3, seqResult4.getEndIdx());
        assertEquals("abc", seqResult4.getText());
    }
}