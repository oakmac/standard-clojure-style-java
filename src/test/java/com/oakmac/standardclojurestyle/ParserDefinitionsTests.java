package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParserDefinitionsTest {
    @BeforeEach
    void clearRegistry() {
        ParserDefinitions.clearParsers();
    }

    @Test
    void testParserRefs() {
        // Test direct parser reference
        CharParser charParser = new CharParser('a', "char_a");
        ParserRef directRef = new ParserRef.ParserInstanceRef(charParser);
        assertEquals(charParser, ParserDefinitions.getParser(directRef));

        // Test string reference
        ParserDefinitions.register("test_parser", charParser);
        ParserRef stringRef = new ParserRef.StringRef("test_parser");
        assertEquals(charParser, ParserDefinitions.getParser(stringRef));

        // Test conversion from Object
        ParserRef ref1 = ParserDefinitions.toParserRef("test_parser");
        assertTrue(ref1 instanceof ParserRef.StringRef);
        ParserRef ref2 = ParserDefinitions.toParserRef(charParser);
        assertTrue(ref2 instanceof ParserRef.ParserInstanceRef);
    }

    @Test
    void testInvalidParserRef() {
        // Test invalid Object conversion
        try {
            ParserDefinitions.toParserRef(42);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Invalid parser reference"));
        }
        
        // Test nonexistent parser reference
        try {
            ParserDefinitions.getParserByName("nonexistent");
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Could not find parser"));
        }
    }

    @Test
    void testRecursiveParsers() {
        // Create a parser that references another parser that doesn't exist yet
        Object[] seqParsers = new Object[]{"parser_b", new CharParser('a', "char_a")};
        SequenceParser parserA = new SequenceParser(seqParsers, "parser_a");
        
        // Register both parsers
        CharParser parserB = new CharParser('b', "char_b");
        ParserDefinitions.register("parser_a", parserA);
        ParserDefinitions.register("parser_b", parserB);
        
        // Test that we can resolve the recursive reference
        ParserRef ref = new ParserRef.StringRef("parser_b");
        IParser resolvedParser = ParserDefinitions.getParser(ref);
        assertEquals(parserB, resolvedParser);
        
        // Test that we can parse using the recursive definition
        Node result = parserA.parse("ba", 0);
        assertNotNull(result);
        assertEquals("parser_a", result.getName());
        Node[] children = result.getChildren();
        assertEquals(2, result.getNumChildren());
        assertEquals("char_b", children[0].getName());
        assertEquals("char_a", children[1].getName());
    }

    @Test
    void testOverwriteParser() {
        CharParser parser1 = new CharParser('x', "test_x");
        CharParser parser2 = new CharParser('y', "test_y");
        
        ParserDefinitions.register("test", parser1);
        ParserDefinitions.register("test", parser2);
        
        ParserRef ref = new ParserRef.StringRef("test");
        assertEquals(parser2, ParserDefinitions.getParser(ref));
    }
}