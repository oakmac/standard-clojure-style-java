package com.oakmac.standardclojurestyle;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserTest {

    @Test
    public void testNamedParser() {
        // Create a char parser to wrap
        Map<String, Object> charOpts = new HashMap<>();
        charOpts.put("char", "a");
        charOpts.put("name", "char_a");
        Map<String, Object> charParser = Parser.Char(charOpts);
        
        // Create named parser wrapping the char parser
        Map<String, Object> namedOpts = new HashMap<>();
        namedOpts.put("name", "test_name");
        namedOpts.put("parser", charParser);
        Map<String, Object> namedParser = Parser.Named(namedOpts);
        
        IParserFunction parser = (IParserFunction)namedParser.get("parse");
        
        // Test successful match
        Node result1 = parser.parse("abc", 0);
        assertNotNull(result1, "Should parse 'a'");
        assertEquals("test_name", result1.getName(), "Should have wrapper name");
        assertEquals(0, result1.getStartIdx(), "Should have correct startIdx");
        assertEquals(1, result1.getEndIdx(), "Should have correct endIdx");
        assertEquals(1, result1.getChildren().size(), "Should have one child");
        assertEquals("char_a", result1.getChildren().get(0).getName(), "Child should keep original name");
        assertEquals("a", result1.getChildren().get(0).getText(), "Child should have correct text");
        
        // Test with unnamed parser
        Map<String, Object> unnamedParserOpts = new HashMap<>();
        unnamedParserOpts.put("parse", (IParserFunction) (txt, pos) -> {
            if (txt.charAt(pos) == 'x') {
                Map<String, Object> nodeOpts = new HashMap<>();
                nodeOpts.put("startIdx", pos);
                nodeOpts.put("endIdx", pos + 1);
                nodeOpts.put("text", "x");
                return new Node(nodeOpts);
            }
            return null;
        });
        
        Map<String, Object> namedOpts2 = new HashMap<>();
        namedOpts2.put("name", "test_name_2");
        namedOpts2.put("parser", unnamedParserOpts);
        Map<String, Object> namedParser2 = Parser.Named(namedOpts2);
        
        IParserFunction parser2 = (IParserFunction)namedParser2.get("parse");
        
        Node result2 = parser2.parse("xyz", 0);
        assertNotNull(result2, "Should parse 'x'");
        assertEquals("test_name_2", result2.getName(), "Should have wrapper name");
        assertEquals("x", result2.getText(), "Should have correct text");
        
        // Test failed match
        Node result3 = parser.parse("xyz", 0);
        assertNull(result3, "Should not parse non-matching input");
    }
    
    @Test
    public void testCharParser() {
        Map<String, Object> opts = new HashMap<>();
        opts.put("char", "a");
        opts.put("name", "char_test_a");
        Map<String, Object> charTest1 = Parser.Char(opts);
        IParserFunction parser = (IParserFunction)charTest1.get("parse");
        
        Node result1 = parser.parse("a", 0);
        assertNotNull(result1, "Should parse 'a'");
        assertEquals("char_test_a", result1.getName(), "Should have correct name");
        assertEquals("a", result1.getText(), "Should have correct text");
        assertEquals(0, result1.getStartIdx(), "Should have correct startIdx");
        assertEquals(1, result1.getEndIdx(), "Should have correct endIdx");
        
        Node result2 = parser.parse("=", 0);
        assertNull(result2, "Should not parse '='");
    }

    @Test
    public void testNotCharParser() {
        Map<String, Object> opts = new HashMap<>();
        opts.put("char", "a");
        opts.put("name", "notchar_test_a");
        Map<String, Object> notCharTest1 = Parser.NotChar(opts);
        IParserFunction parser = (IParserFunction)notCharTest1.get("parse");
        
        // Test matching any non-'a' character
        Node result1 = parser.parse("b", 0);
        assertNotNull(result1, "Should parse 'b'");
        assertEquals("notchar_test_a", result1.getName(), "Should have correct name");
        assertEquals("b", result1.getText(), "Should have correct text");
        assertEquals(0, result1.getStartIdx(), "Should have correct startIdx");
        assertEquals(1, result1.getEndIdx(), "Should have correct endIdx");
        
        // Test failing to match 'a'
        Node result2 = parser.parse("a", 0);
        assertNull(result2, "Should not parse 'a'");
        
        // Test with different characters
        assertNotNull(parser.parse("x", 0), "Should parse 'x'");
        assertNotNull(parser.parse("1", 0), "Should parse '1'");
        assertNotNull(parser.parse(" ", 0), "Should parse space");
        assertNotNull(parser.parse("!", 0), "Should parse '!'");
        
        // Test beyond string length
        assertNull(parser.parse("xyz", 3), "Should not parse beyond string length");
        
        // Test empty string
        assertNull(parser.parse("", 0), "Should not parse empty string");
    }

    @Test 
    public void testAnyCharParser() {
        Map<String, Object> opts = new HashMap<>();
        opts.put("name", "anychar_test1");
        Map<String, Object> anyCharTest1 = Parser.AnyChar(opts);
        IParserFunction parser = (IParserFunction)anyCharTest1.get("parse");
        
        Node result1 = parser.parse("a", 0);
        assertNotNull(result1, "Should parse 'a'");
        assertEquals("a", result1.getText(), "Should have correct text");
        
        Node result2 = parser.parse("b", 0);
        assertNotNull(result2, "Should parse 'b'");
        assertEquals("b", result2.getText(), "Should have correct text");
        assertEquals("anychar_test1", result2.getName(), "Should have correct name");
        
        assertNotNull(parser.parse(" ", 0), "Should parse space");
        assertNotNull(parser.parse("+", 0), "Should parse '+'");
        
        Node result3 = parser.parse("!~^", 0);
        assertNotNull(result3, "Should parse first char of multiple chars");
        assertEquals("!", result3.getText(), "Should have correct text");
        
        assertNull(parser.parse("", 0), "Should not parse empty string");
    }

    @Test
    public void testStringParser() {
        Map<String, Object> opts = new HashMap<>();
        opts.put("str", "foo");
        opts.put("name", "string_test_foo");
        Map<String, Object> stringTest1 = Parser.String(opts);
        IParserFunction parser = (IParserFunction)stringTest1.get("parse");
        
        // Basic matching
        Node result1 = parser.parse("foo", 0);
        assertNotNull(result1, "Should parse 'foo'");
        assertEquals("string_test_foo", result1.getName(), "Should have correct name");
        assertEquals("foo", result1.getText(), "Should have correct text");
        assertEquals(0, result1.getStartIdx(), "Should have correct startIdx");
        assertEquals(3, result1.getEndIdx(), "Should have correct endIdx");
        
        // Non-matching
        Node result2 = parser.parse("bar", 0);
        assertNull(result2, "Should not parse 'bar'");
        
        // Partial match
        Node result3 = parser.parse("fo", 0);
        assertNull(result3, "Should not parse partial match");
        
        // Middle of string
        Node result4 = parser.parse("foo", 1);
        assertNull(result4, "Should not parse from middle");
        
        // With leading characters
        Node result5 = parser.parse("barfoo", 3);
        assertNotNull(result5, "Should parse with offset");
        assertEquals(3, result5.getStartIdx(), "Should have correct startIdx");
        assertEquals(6, result5.getEndIdx(), "Should have correct endIdx");
        assertEquals("foo", result5.getText(), "Should have correct text");
        
        // With trailing characters
        Node result6 = parser.parse("foobar", 0);
        assertNotNull(result6, "Should parse with trailing chars");
        assertEquals("foo", result6.getText(), "Should have correct text");
        assertEquals(3, result6.getEndIdx(), "Should have correct endIdx");
    }

    @Test
    public void testSeqParser() {
        // Create a sequence parser that matches "abc"
        List<Map<String, Object>> parsers = new ArrayList<>();
        
        Map<String, Object> charAOpts = new HashMap<>();
        charAOpts.put("char", "a");
        charAOpts.put("name", "AAA");
        parsers.add(Parser.Char(charAOpts));
        
        Map<String, Object> charBOpts = new HashMap<>();
        charBOpts.put("char", "b");
        charBOpts.put("name", "BBB");
        parsers.add(Parser.Char(charBOpts));
        
        Map<String, Object> charCOpts = new HashMap<>();
        charCOpts.put("char", "c");
        charCOpts.put("name", "CCC");
        parsers.add(Parser.Char(charCOpts));
        
        Map<String, Object> seqOpts = new HashMap<>();
        seqOpts.put("name", "seq_test_1");
        seqOpts.put("parsers", parsers);
        Map<String, Object> seqTest1 = Parser.Seq(seqOpts);
        
        IParserFunction parser = (IParserFunction)seqTest1.get("parse");
        
        // Test successful sequence
        Node result1 = parser.parse("abc", 0);
        assertNotNull(result1, "Should parse sequence");
        assertEquals(0, result1.getStartIdx(), "Should have correct startIdx");
        assertEquals(3, result1.getEndIdx(), "Should have correct endIdx");
        assertEquals(3, result1.getChildren().size(), "Should have three children");
        assertEquals("AAA", result1.getChildren().get(0).getName(), "First child should be 'a'");
        assertEquals("BBB", result1.getChildren().get(1).getName(), "Second child should be 'b'");
        assertEquals("CCC", result1.getChildren().get(2).getName(), "Third child should be 'c'");
        
        // Test failed sequence
        Node result2 = parser.parse("aba", 0);
        assertNull(result2, "Should not parse incorrect sequence");
        
        // Test partial sequence
        Node result3 = parser.parse("ab", 0);
        assertNull(result3, "Should not parse partial sequence");
        
        // Test with trailing characters
        Node result4 = parser.parse("abcd", 0);
        assertNotNull(result4, "Should parse sequence with trailing chars");
        assertEquals(3, result4.getEndIdx(), "Should only consume three chars");
    }

    @Test
    public void testChoiceParser() {
        List<Map<String, Object>> parsers = new ArrayList<>();
        
        Map<String, Object> charAOpts = new HashMap<>();
        charAOpts.put("char", "a");
        charAOpts.put("name", "A");
        parsers.add(Parser.Char(charAOpts));
        
        Map<String, Object> charBOpts = new HashMap<>();
        charBOpts.put("char", "b");
        charBOpts.put("name", "B");
        parsers.add(Parser.Char(charBOpts));
        
        Map<String, Object> charCOpts = new HashMap<>();
        charCOpts.put("char", "c");
        charCOpts.put("name", "C");
        parsers.add(Parser.Char(charCOpts));
        
        Map<String, Object> opts = new HashMap<>();
        opts.put("parsers", parsers);
        Map<String, Object> choiceTest1 = Parser.Choice(opts);
        
        IParserFunction parser = (IParserFunction)choiceTest1.get("parse");
        
        // Test no match
        Node result1 = parser.parse("x", 0);
        assertNull(result1, "Should not parse 'x'");
        
        // Test first parser match
        Node result2 = parser.parse("a", 0);
        assertNotNull(result2, "Should parse 'a'");
        assertEquals("A", result2.getName());
        assertEquals("a", result2.getText());
        
        // Test second parser match
        Node result3 = parser.parse("b", 0);
        assertNotNull(result3, "Should parse 'b'");
        assertEquals("B", result3.getName());
        assertEquals("b", result3.getText());
        
        // Test third parser match
        Node result4 = parser.parse("c", 0);
        assertNotNull(result4, "Should parse 'c'");
        assertEquals("C", result4.getName());
        assertEquals("c", result4.getText());
        
        // Test with offset
        Node result5 = parser.parse("xab", 1);
        assertNotNull(result5, "Should parse 'a' at offset");
        assertEquals("A", result5.getName());
        assertEquals(1, result5.getStartIdx());
        
        // Test that it stops at first match
        Node result6 = parser.parse("abc", 0);
        assertNotNull(result6, "Should parse first match");
        assertEquals("A", result6.getName());
        assertEquals(0, result6.getStartIdx());
        assertEquals(1, result6.getEndIdx());
    }

    @Test
    public void testRepeatParser() {
        Map<String, Object> charAOpts = new HashMap<>();
        charAOpts.put("char", "a");
        charAOpts.put("name", "AAA");
        Map<String, Object> charParser = Parser.Char(charAOpts);
        
        Map<String, Object> opts = new HashMap<>();
        opts.put("name", "repeat_test_1");
        opts.put("parser", charParser);
        Map<String, Object> repeatTest1 = Parser.Repeat(opts);
        
        IParserFunction parser = (IParserFunction)repeatTest1.get("parse");
        
        // Test no match case (returns empty node)
        Node result1 = parser.parse("b", 0);
        assertNotNull(result1, "Should return empty node for no match");
        assertEquals(0, result1.getStartIdx());
        assertEquals(0, result1.getEndIdx());
        assertNull(result1.getName());
        
        // Test single match
        Node result2 = parser.parse("a", 0);
        assertNotNull(result2, "Should parse single 'a'");
        assertEquals("repeat_test_1", result2.getName());
        assertEquals(1, result2.getChildren().size());
        assertEquals("a", result2.getChildren().get(0).getText());
        
        // Test multiple matches
        Node result3 = parser.parse("aaa", 0);
        assertNotNull(result3, "Should parse multiple 'a's");
        assertEquals("repeat_test_1", result3.getName());
        assertEquals(3, result3.getChildren().size());
        assertEquals("a", result3.getChildren().get(0).getText());
        assertEquals("a", result3.getChildren().get(1).getText());
        assertEquals("a", result3.getChildren().get(2).getText());
        
        // Test with offset
        Node result4 = parser.parse("baac", 1);
        assertNotNull(result4, "Should parse with offset");
        assertEquals(2, result4.getChildren().size());
        assertEquals(1, result4.getStartIdx());
        assertEquals(3, result4.getEndIdx());
        
        // Test with minMatches
        Map<String, Object> optsWithMin = new HashMap<>();
        optsWithMin.put("name", "repeat_test_min");
        optsWithMin.put("parser", charParser);
        optsWithMin.put("minMatches", 2);
        Map<String, Object> repeatTestMin = Parser.Repeat(optsWithMin);
        IParserFunction parserWithMin = (IParserFunction)repeatTestMin.get("parse");
        
        Node result5 = parserWithMin.parse("a", 0);
        assertNull(result5, "Should not match with too few matches");
        
        Node result6 = parserWithMin.parse("aa", 0);
        assertNotNull(result6, "Should match with minimum matches");
        assertEquals(2, result6.getChildren().size());
    }

    @Test
    public void testOptionalParser() {
        Map<String, Object> charAOpts = new HashMap<>();
        charAOpts.put("char", "a");
        charAOpts.put("name", "optional_a");
        Map<String, Object> optTest1 = Parser.Optional(Parser.Char(charAOpts));
        
        IParserFunction parser = (IParserFunction)optTest1.get("parse");
        
        // Test successful match
        Node result1 = parser.parse("abc", 0);
        assertNotNull(result1, "Should parse 'a'");
        assertEquals(0, result1.getStartIdx(), "Should have correct startIdx");
        assertEquals(1, result1.getEndIdx(), "Should have correct endIdx");
        assertEquals("optional_a", result1.getName(), "Should have correct name");
        assertEquals("a", result1.getText(), "Should have correct text");
        
        // Test no match - should return empty node at same position
        Node result2 = parser.parse("xyz", 0);
        assertNotNull(result2, "Should return empty node");
        assertEquals(0, result2.getStartIdx(), "Should have same start position");
        assertEquals(0, result2.getEndIdx(), "Should have same end position");
        
        // Test with offset
        Node result3 = parser.parse("xabc", 1);
        assertNotNull(result3, "Should parse with offset");
        assertEquals(1, result3.getStartIdx(), "Should have correct offset startIdx");
        assertEquals(2, result3.getEndIdx(), "Should have correct offset endIdx");
        assertEquals("a", result3.getText(), "Should have correct text");
    }
}
