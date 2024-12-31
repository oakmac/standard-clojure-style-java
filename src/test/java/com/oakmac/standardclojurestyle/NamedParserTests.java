package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NamedParserTest {
    
    @BeforeEach
    void clearRegistry() {
        ParserDefinitions.clearParsers();
    }

    @Test 
    void testBasicNamedParser() {
        // Test basic functionality - wrapping a CharParser
        NamedParser namedParser = new NamedParser(
            new CharParser('a', "char_a"),
            "test_name"
        );

        // Test successful match
        Node result1 = namedParser.parse("abc", 0);
        assertNotNull(result1);
        assertEquals("test_name", result1.getName());
        assertEquals(0, result1.getStartIdx());
        assertEquals(1, result1.getEndIdx());
        assertEquals(1, result1.getNumChildren());
        assertEquals("a", result1.getChildren()[0].getText());

        // Test failed match
        Node result2 = namedParser.parse("xyz", 0);
        assertNull(result2);
    }

    @Test
    void testNamedParserWithExistingName() {
        // Test when wrapped parser already has a name
        NamedParser namedParser = new NamedParser(
            new StringParser("test", "inner_name"),
            "outer_name"
        );

        Node result = namedParser.parse("test123", 0);
        assertNotNull(result);
        assertEquals("outer_name", result.getName());
        assertEquals(1, result.getNumChildren());
        assertEquals("inner_name", result.getChildren()[0].getName());
        assertEquals("test", result.getChildren()[0].getText());
    }

    @Test
    void testNamedParserWithUnnamedParser() {
        // Create a simple unnamed parser
        IParser unnamedParser = new IParser() {
            @Override
            public Node parse(String input, int startIdx) {
                if (input.charAt(startIdx) == 'x') {
                    return new Node("x", null, startIdx, startIdx + 1);
                }
                return null;
            }
        };

        NamedParser namedParser = new NamedParser(unnamedParser, "test_name");

        Node result = namedParser.parse("xyz", 0);
        assertNotNull(result);
        assertEquals("test_name", result.getName());
        // assertEquals("x", result.getText());
    }

    @Test
    void testNamedParserWithOptionalParser() {
        NamedParser namedParser = new NamedParser(
            new OptionalParser(new CharParser('x', "char_x")),
            "optional_test"
        );

        // Test when optional char is present
        Node result1 = namedParser.parse("xyz", 0);
        assertNotNull(result1);
        assertEquals("optional_test", result1.getName());
        assertEquals("x", result1.getChildren()[0].getText());

        // Test when optional char is missing
        Node result2 = namedParser.parse("abc", 0);
        assertNotNull(result2);
        assertEquals("optional_test", result2.getName());
        assertEquals(0, result2.getStartIdx());
        assertEquals(0, result2.getEndIdx());
    }

    @Test
    void testNamedParserWithChoiceParser() {
        NamedParser namedParser = new NamedParser(
            new ChoiceParser(new IParser[]{
                new CharParser('a', "char_a"),
                new CharParser('b', "char_b")
            }, null),
            "choice_test"
        );

        // Test first choice
        Node result1 = namedParser.parse("abc", 0);
        assertNotNull(result1);
        assertEquals("choice_test", result1.getName());
        assertEquals("a", result1.getChildren()[0].getText());

        // Test second choice
        Node result2 = namedParser.parse("bcd", 0);
        assertNotNull(result2);
        assertEquals("choice_test", result2.getName());
        assertEquals("b", result2.getChildren()[0].getText());

        // Test no match
        Node result3 = namedParser.parse("xyz", 0);
        assertNull(result3);
    }

    // @Test
    // void testNamedParserEdgeCases() {
    //     NamedParser namedParser = new NamedParser(
    //         new CharParser('a', "char_a"),
    //         "test_name"
    //     );

    //     // Test empty string
    //     Node result1 = namedParser.parse("", 0);
    //     assertNull(result1);

    //     Test null input
    //     assertThrows(NullPointerException.class, () -> {
    //         namedParser.parse(null, 0);
    //     });

    //     // Test invalid position
    //     Node result2 = namedParser.parse("abc", -1);
    //     assertNull(result2);

    //     Node result3 = namedParser.parse("abc", 999);
    //     assertNull(result3);
    // }
}