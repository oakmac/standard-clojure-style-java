package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RepeatParserTest {
    @BeforeEach
    void clearRegistry() {
        ParserDefinitions.clearParsers();
    }

    @Test
    void testParseWithDirectParser() {
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

    @Test
    void testParseWithStringRef() {
        // Register a parser we'll reference by string
        ParserDefinitions.register("a_parser", new CharParser('a', "AAA"));

        // Create repeat parser using string reference
        RepeatParser testRepeat = new RepeatParser("a_parser", "repeat_test_string_ref");

        // Test single match
        Node result1 = testRepeat.parse("a", 0);
        assertEquals(0, result1.getStartIdx());
        assertEquals(1, result1.getEndIdx());
        assertEquals("repeat_test_string_ref", result1.getName());
        assertEquals("a", result1.getChildren()[0].getText());
        assertEquals("AAA", result1.getChildren()[0].getName());

        // Test multiple matches
        Node result2 = testRepeat.parse("aaa", 0);
        assertEquals(0, result2.getStartIdx());
        assertEquals(3, result2.getEndIdx());
        assertEquals("repeat_test_string_ref", result2.getName());
        assertEquals(3, result2.getChildren().length);
        for (Node child : result2.getChildren()) {
            assertEquals("a", child.getText());
            assertEquals("AAA", child.getName());
        }
    }

    @Test
    void testParseWithRecursiveRef() {
        // Create a parser that references itself indirectly
        RepeatParser repeatParser = new RepeatParser(
            "recursive_parser",
            "repeat_with_recursive"
        );

        // Register the parsers (including the recursive one)
        ParserDefinitions.register("repeat_with_recursive", repeatParser);
        ParserDefinitions.register("recursive_parser", new CharParser('x', "XXX"));

        // Test multiple matches
        Node result = repeatParser.parse("xxx", 0);
        assertEquals(0, result.getStartIdx());
        assertEquals(3, result.getEndIdx());
        assertEquals("repeat_with_recursive", result.getName());
        assertEquals(3, result.getChildren().length);
        for (Node child : result.getChildren()) {
            assertEquals("x", child.getText());
            assertEquals("XXX", child.getName());
        }
    }

    @Test
    void testParseWithMinMatches() {
        // Test with minMatches = 2
        RepeatParser parser = new RepeatParser(
            new CharParser('a', "AAA"),
            "repeat_min_2",
            2
        );

        // Test with fewer than minimum matches
        Node result1 = parser.parse("a", 0);
        assertEquals(0, result1.getStartIdx());
        assertEquals(0, result1.getEndIdx());
        assertNull(result1.getName());

        // Test with exactly minimum matches
        Node result2 = parser.parse("aa", 0);
        assertEquals(0, result2.getStartIdx());
        assertEquals(2, result2.getEndIdx());
        assertEquals("repeat_min_2", result2.getName());
        assertEquals(2, result2.getChildren().length);

        // Test with more than minimum matches
        Node result3 = parser.parse("aaa", 0);
        assertEquals(0, result3.getStartIdx());
        assertEquals(3, result3.getEndIdx());
        assertEquals("repeat_min_2", result3.getName());
        assertEquals(3, result3.getChildren().length);
    }

    @Test
    void testParseWithMinMatchesStringRef() {
        // Register a parser we'll reference by string
        ParserDefinitions.register("b_parser", new CharParser('b', "BBB"));

        // Create repeat parser with minMatches using string reference
        RepeatParser parser = new RepeatParser("b_parser", "repeat_min_string", 2);

        // Test with fewer than minimum matches
        Node result1 = parser.parse("b", 0);
        assertEquals(0, result1.getStartIdx());
        assertEquals(0, result1.getEndIdx());
        assertNull(result1.getName());

        // Test with exactly minimum matches
        Node result2 = parser.parse("bb", 0);
        assertEquals(0, result2.getStartIdx());
        assertEquals(2, result2.getEndIdx());
        assertEquals("repeat_min_string", result2.getName());
        assertEquals(2, result2.getChildren().length);
        for (Node child : result2.getChildren()) {
            assertEquals("b", child.getText());
            assertEquals("BBB", child.getName());
        }
    }

    @Test
    void testEdgeCases() {
        RepeatParser parser = new RepeatParser(
            new CharParser('x', "XXX"),
            "repeat_edge_cases"
        );

        // Test with null input
        assertNull(parser.parse(null, 0));

        // Test with empty input
        assertNull(parser.parse("", 0));

        // Test with start index beyond input length
        assertNull(parser.parse("x", 2));

        // Test with start index at input length
        assertNull(parser.parse("x", 1));

        // Test with negative minMatches (should treat as 0)
        RepeatParser negativeParser = new RepeatParser(
            new CharParser('x', "XXX"),
            "repeat_negative",
            -1
        );
        Node result = negativeParser.parse("x", 0);
        assertEquals(1, result.getChildren().length);
    }
}