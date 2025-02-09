package com.oakmac.standardclojurestyle;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.junit.jupiter.api.Test;

public class ParseNsTest {
  // Test case class used for JSON deserialization
  private static class TestCase {
    private String name;
    private String input;
    private String expected;

    // Required for Jackson deserialization
    public TestCase() {}

    // Getters and setters
    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getInput() {
      return input;
    }

    public void setInput(String input) {
      this.input = input;
    }

    public String getExpected() {
      return expected;
    }

    public void setExpected(String expected) {
      this.expected = expected;
    }
  }

  @Test
  public void testBasicNamespaceParsing() {
    try {
      // Create ObjectMapper instance
      ObjectMapper mapper = new ObjectMapper();

      // Load the test cases JSON file
      InputStream inputStream = getClass().getResourceAsStream("/parse_ns_tests.json");
      if (inputStream == null) {
        fail("Could not load parse_ns_tests.json");
        return;
      }

      List<TestCase> testCases =
          mapper.readValue(
              inputStream,
              mapper.getTypeFactory().constructCollectionType(List.class, TestCase.class));

      // Set of test cases to skip for now
      Set<String> skipTests =
          new HashSet<>(
              Arrays.asList(
                  // add test cases to skip here

                  // FIXME: this is the next step: get this test case to pass
                  // "GitHub Issue #152 - :use in the ns form should error"
                
                  ));

      // Process each test case
      for (TestCase testCase : testCases) {
        String testName = testCase.getName();

        // Skip tests if they're in the skip set
        if (skipTests.contains(testName)) {
          continue;
        }

        String input = testCase.getInput();
        String expected = testCase.getExpected();

        // Convert expected JSON string into a Map
        Map<String, Object> expectedObj = mapper.readValue(expected, Map.class);

        // Parse the input into nodes
        Node parsedNodes = Parser.parse(input);
        List<Node> flatNodes = Utils.flattenTree(parsedNodes);

        // Parse the ns form
        Map<String, Object> nsResult = ParseNs.parseNs(flatNodes);

        // Compare with expected output
        boolean resultIsTheSame = Utils.deepEquals(nsResult, expectedObj);

        if (!resultIsTheSame) {
          System.out.println("");
          System.out.println("parseNs structure does not match: " + testName);
          System.out.println("");
          System.out.println("Expected:");
          System.out.println(
              mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedObj));
          System.out.println("");
          System.out.println("Actual:");
          System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nsResult));
          System.out.println("");
        }

        assertTrue(resultIsTheSame, "parse_ns test case " + testName + " failed");
      }

    } catch (IOException e) {
      fail("Failed to read parse_ns_tests.json: " + e.getMessage());
    }
  }

  @Test
  public void testLookForIgnoreFile() {
    // TODO: implement test cases for lookForIgnoreFile
  }
}
