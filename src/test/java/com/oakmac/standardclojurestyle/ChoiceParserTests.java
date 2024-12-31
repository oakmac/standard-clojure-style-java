package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChoiceParserTest {
    @BeforeEach
    void clearRegistry() {
        ParserDefinitions.clearParsers();
    }

    @Test
    void testBasicChoice() {
        IParser[] parsers = new IParser[]{
            new CharParser('a', ".a"),
            new CharParser('b', ".b"),
            new CharParser('c', ".c")
        };
        
        ChoiceParser choiceTest1 = new ChoiceParser(parsers, null);
        assertEquals("a", choiceTest1.parse("a", 0).getText());
        assertEquals("b", choiceTest1.parse("b", 0).getText());
        assertEquals("c", choiceTest1.parse("c", 0).getText());
        assertNull(choiceTest1.parse("z", 0));
    }

    @Test
    void testDetailedChoice() {
        IParser[] parsers = new IParser[]{
            new CharParser('a', "A"),
            new CharParser('b', "B"),
            new CharParser('c', "C")
        };
        
        ChoiceParser testChoice1 = new ChoiceParser(parsers, "choice_test_1");
        
        // Test no match case
        Node choiceResult1 = testChoice1.parse("x", 0);
        assertNull(choiceResult1);
        
        // Test first parser match
        Node choiceResult2 = testChoice1.parse("a", 0);
        assertEquals(0, choiceResult2.getStartIdx());
        assertEquals(1, choiceResult2.getEndIdx());
        assertEquals("A", choiceResult2.getName());
        assertEquals("a", choiceResult2.getText());
        
        // Test second parser match
        Node choiceResult3 = testChoice1.parse("b", 0);
        assertEquals(0, choiceResult3.getStartIdx());
        assertEquals(1, choiceResult3.getEndIdx());
        assertEquals("B", choiceResult3.getName());
        assertEquals("b", choiceResult3.getText());
        
        // Test third parser match
        Node choiceResult4 = testChoice1.parse("c", 0);
        assertEquals(0, choiceResult4.getStartIdx());
        assertEquals(1, choiceResult4.getEndIdx());
        assertEquals("C", choiceResult4.getName());
        assertEquals("c", choiceResult4.getText());
        
        // Test match with offset position
        Node choiceResult5 = testChoice1.parse("xab", 1);
        assertEquals(1, choiceResult5.getStartIdx());
        assertEquals(2, choiceResult5.getEndIdx());
        assertEquals("A", choiceResult5.getName());
        assertEquals("a", choiceResult5.getText());
        
        // Test that it stops at first match
        Node choiceResult6 = testChoice1.parse("abc", 0);
        assertEquals(0, choiceResult6.getStartIdx());
        assertEquals(1, choiceResult6.getEndIdx());
        assertEquals("A", choiceResult6.getName());
        assertEquals("a", choiceResult6.getText());
    }

    @Test
    void testChoiceWithStringRefs() {
        // Register some parsers we'll reference by string
        ParserDefinitions.register("a_parser", new CharParser('a', "A"));
        ParserDefinitions.register("b_parser", new CharParser('b', "B"));

        // Create choice using mix of string refs and direct parser
        Object[] parsers = new Object[]{
            "a_parser",                          // string reference
            "b_parser",                          // string reference
            new CharParser('c', "C")             // direct parser
        };
        
        ChoiceParser testChoice = new ChoiceParser(parsers, "choice_test_mixed");
        
        // Test first parser (string ref)
        Node result1 = testChoice.parse("a", 0);
        assertEquals("A", result1.getName());
        assertEquals("a", result1.getText());
        
        // Test second parser (string ref)
        Node result2 = testChoice.parse("b", 0);
        assertEquals("B", result2.getName());
        assertEquals("b", result2.getText());
        
        // Test third parser (direct)
        Node result3 = testChoice.parse("c", 0);
        assertEquals("C", result3.getName());
        assertEquals("c", result3.getText());
    }

    @Test
    void testChoiceWithRecursiveRefs() {
        // Create a parser that references itself indirectly
        Object[] parsers = new Object[]{
            "recursive_parser",                  // forward reference
            new CharParser('x', "X")             // direct parser
        };
        
        ChoiceParser choiceParser = new ChoiceParser(parsers, "choice_with_recursive");
        
        // Register the parsers (including the recursive one)
        ParserDefinitions.register("choice_with_recursive", choiceParser);
        ParserDefinitions.register("recursive_parser", new CharParser('y', "Y"));
        
        // Test both choices
        Node result1 = choiceParser.parse("y", 0);
        assertEquals("Y", result1.getName());
        assertEquals("y", result1.getText());
        
        Node result2 = choiceParser.parse("x", 0);
        assertEquals("X", result2.getName());
        assertEquals("x", result2.getText());
    }
}