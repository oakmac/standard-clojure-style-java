package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

class ParserTest {
    private static final int SPACES_PER_INDENT = 2;
    
    // Data class to hold test cases
    private static class TestCase {
        public String name;
        public String input;
        public String expected;
        
        @Override
        public String toString() {
            return String.format("Test Case: %s%nInput: %s%nExpected: %s", 
                name, input, expected);
        }
    }

    // Test data loaded in @BeforeAll
    private static List<TestCase> testCases;

    @BeforeAll 
    static void loadTestData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = ParserTest.class.getResourceAsStream("/parser_tests.json");
            testCases = mapper.readValue(is, new TypeReference<List<TestCase>>() {});
        } catch (IOException e) {
            fail("Failed to load test data: " + e.getMessage());
        }
    }
    
    private boolean isWhitespaceNode(Node node) {
        String name = node.getName();
        return name != null && 
               (name.equals("whitespace") || name.equals("whitespace:newline"));
    }

    private String nodeToString(Node node) {
        return nodeToString(node, 0);
    }

    private String nodeToString(Node node, int indentLevel) {
        if (isWhitespaceNode(node)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        if (!"source".equals(node.getName())) {
            sb.append("\n");
        }

        sb.append(" ".repeat(indentLevel * SPACES_PER_INDENT));
        sb.append("(").append(node.getName()).append(" ")
          .append(node.getStartIdx()).append("..")
          .append(node.getEndIdx());

        String text = node.getText();
        if (text != null && !text.isEmpty()) {
            String escapedText = text.replace("\n", "\\n");
            sb.append(" '").append(escapedText).append("'");
        }

        Node[] children = node.getChildren();
        if (children != null) {
            for (Node child : children) {
                sb.append(nodeToString(child, indentLevel + 1));
            }
        }

        sb.append(")");
        return sb.toString();
    }

    // @Test
    // void testAllParserCases() {
    //     // Skip these test cases
    //     Set<String> skipTests = new HashSet<>();
    //     skipTests.add("String with emoji");

    //     assertFalse(testCases.isEmpty(), "No test cases loaded");
        
    //     int passCount = 0;
    //     int skipCount = 0;
    //     int failCount = 0;

    //     for (TestCase testCase : testCases) {
    //         if (skipTests.contains(testCase.name)) {
    //             skipCount++;
    //             continue;
    //         }

    //         try {
    //             Node parsed = Parser.parse(testCase.input);
    //             assertNotNull(parsed, 
    //                 "Parser returned null for test: " + testCase.name);
                
    //             String result = nodeToString(parsed);
                
    //             if (!testCase.expected.equals(result)) {
    //                 failCount++;
    //                 fail(String.format(
    //                     "Parser output does not match for test: %s%n" +
    //                     "Expected:%n%s%n" +
    //                     "Actual:%n%s%n", 
    //                     testCase.name, testCase.expected, result));
    //             } else {
    //                 passCount++;
    //             }
    //         } catch (Exception e) {
    //             failCount++;
    //             fail(String.format("Test case threw exception: %s%nException: %s", 
    //                 testCase.name, e.getMessage()));
    //         }
    //     }

    //     System.out.printf(
    //         "Test Summary:%nTotal Tests: %d%nPassed: %d%nFailed: %d%nSkipped: %d%n",
    //         testCases.size(), passCount, failCount, skipCount
    //     );
    // }
}