package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SequenceParserTest {
    @BeforeEach
    void clearRegistry() {
        ParserDefinitions.clearParsers();
    }

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

    @Test
    void testParseWithStringRefs() {
        // Register some parsers we'll reference by string
        ParserDefinitions.register("a_parser", new CharParser('a', "AAA"));
        ParserDefinitions.register("b_parser", new CharParser('b', "BBB"));

        // Create sequence using mix of string refs and direct parser
        Object[] parsers = new Object[]{
            "a_parser",                          // string reference
            "b_parser",                          // string reference
            new CharParser('c', "CCC")           // direct parser
        };
        
        SequenceParser testSeq = new SequenceParser(parsers, "seq_test_mixed");
        
        // Test successful sequence
        Node result = testSeq.parse("abc", 0);
        assertEquals(0, result.getStartIdx());
        assertEquals(3, result.getEndIdx());
        assertEquals("abc", result.getText());
        
        // Test children names
        Node[] children = result.getChildren();
        assertEquals("AAA", children[0].getName());
        assertEquals("BBB", children[1].getName());
        assertEquals("CCC", children[2].getName());
    }

    @Test
    void testParseWithRecursiveRefs() {
        // Create a parser that references a parser that doesn't exist yet
        Object[] parsers = new Object[]{"recursive_parser", new CharParser('x', "XXX")};
        SequenceParser seqParser = new SequenceParser(parsers, "seq_with_recursive");
        
        // Register the parsers (including the recursive one)
        ParserDefinitions.register("seq_with_recursive", seqParser);
        ParserDefinitions.register("recursive_parser", new CharParser('y', "YYY"));
        
        // Test the sequence
        Node result = seqParser.parse("yx", 0);
        assertNotNull(result);
        assertEquals("yx", result.getText());
        assertEquals("YYY", result.getChildren()[0].getName());
        assertEquals("XXX", result.getChildren()[1].getName());
    }
}