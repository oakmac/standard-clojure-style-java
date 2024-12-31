package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

class ParserTests {
    // Data class to hold each test case
    private static class TestCase {
        @JsonProperty("name")
        public String name;
        
        @JsonProperty("input")
        public String input;
        
        @JsonProperty("expected")
        public String expected;

        @Override
        public String toString() {
            return String.format("Test Case: %s\nInput: %s\nExpected: %s", 
                name, input, expected);
        }
    }

    private static final int SPACES_PER_INDENT = 2;

    private String nodeToString(Node node) {
        return nodeToString(node, 0);
    }

    private String nodeToString(Node node, int indentLevel) {
        // Skip whitespace nodes
        if (isWhitespaceNode(node)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (!"source".equals(node.getName())) {
            sb.append("\n");
        }
        // Add indentation
        sb.append(" ".repeat(indentLevel * SPACES_PER_INDENT));
        // Add node info - subtract 1 from indices to match JS 0-based indexing
        sb.append("(").append(node.getName()).append(" ")
          .append(node.getStartIdx() - 1).append("..")
          .append(node.getEndIdx() - 1);

        // Add text content if it exists
        if (node.getText() != null && !node.getText().isEmpty()) {
            String escapedText = node.getText().replace("\n", "\\n");
            sb.append(" '").append(escapedText).append("'");
        }

        // Add children recursively
        Node[] children = node.getChildren();
        if (children != null) {
            for (Node child : children) {
                sb.append(nodeToString(child, indentLevel + 1));
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private boolean isWhitespaceNode(Node node) {
        String name = node.getName();
        return name != null && 
               (name.equals("whitespace") || name.equals("whitespace:newline"));
    }

    private void debugPrintNode(Node node, String prefix) {
        System.out.println(prefix + "Node: " + node.getName());
        System.out.println(prefix + "Text: '" + node.getText() + "'");
        System.out.println(prefix + "Index: " + (node.getStartIdx() - 1) + ".." + (node.getEndIdx() - 1));
        
        Node[] children = node.getChildren();
        if (children != null) {
            System.out.println(prefix + "Children: " + children.length);
            for (Node child : children) {
                debugPrintNode(child, prefix + "  ");
            }
        }
    }

    // @Test
    // void testParser() {
    //     // Load test cases from JSON file
    //     ObjectMapper mapper = new ObjectMapper();
    //     String jsonResourcePath = "/parser_tests.json";
    //     InputStream is = getClass().getResourceAsStream(jsonResourcePath);
        
    //     if (is == null) {
    //         fail("Test configuration error: Cannot find test data file " + jsonResourcePath);
    //         return;
    //     }
        
    //     List<TestCase> testCases;
    //     try {
    //         testCases = mapper.readValue(is, 
    //             mapper.getTypeFactory().constructCollectionType(List.class, TestCase.class));
    //         System.out.println("Successfully loaded " + testCases.size() + " test cases");
    //     } catch (IOException e) {
    //         fail("Test data error: parser_tests.json is not a valid JSON file. " + 
    //              "Details: " + e.getMessage());
    //         return;
    //     }
        
    //     assertFalse(testCases.isEmpty(), "No test cases found in parser_tests.json");
        
    //     int passCount = 0;
    //     int skipCount = 0;
    //     int failCount = 0;

    //     for (TestCase testCase : testCases) {
    //         if ("String with emoji".equals(testCase.name)) {
    //             skipCount++;
    //             continue;
    //         }

    //         try {
    //             System.out.println("\nExecuting test: " + testCase.name);
    //             System.out.println("Input: " + testCase.input);
                
    //             Node parsed = Parser.parse(testCase.input);
    //             assertNotNull(parsed, "Parser returned null for test: " + testCase.name);
                
    //             String result = nodeToString(parsed);
                
    //             if (!testCase.expected.equals(result)) {
    //                 failCount++;
    //                 System.out.println("\nFAILED: " + testCase.name);
    //                 System.out.println("Expected:\n" + testCase.expected);
    //                 System.out.println("Actual:\n" + result);
    //                 System.out.println("\nParse Tree Debug:");
    //                 debugPrintNode(parsed, "");
    //                 fail(String.format("Parser output does not match for test: %s%n" +
    //                                  "Expected:%n%s%n" +
    //                                  "Actual:%n%s%n", 
    //                                  testCase.name, testCase.expected, result));
    //             } else {
    //                 passCount++;
    //                 System.out.println("PASSED: " + testCase.name);
    //             }
    //         } catch (Exception e) {
    //             failCount++;
    //             System.out.println("\nFAILED (Exception): " + testCase.name);
    //             System.out.println("Input: " + testCase.input);
    //             System.out.println("Exception: " + e.getMessage());
    //             e.printStackTrace();
    //             fail(String.format("Test case threw exception: %s%nException: %s", 
    //                 testCase.name, e.getMessage()));
    //         }
    //     }

    //     System.out.println("\nTest Summary:");
    //     System.out.println("Total Tests: " + testCases.size());
    //     System.out.println("Passed: " + passCount);
    //     System.out.println("Failed: " + failCount);
    //     System.out.println("Skipped: " + skipCount);
    // }
}