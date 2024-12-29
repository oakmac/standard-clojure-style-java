package com.oakmac.standardclojurestyle;

public class CharParser {
    private final char expectedChar;
    private final String name;

    public CharParser(char expectedChar, String name) {
        this.expectedChar = expectedChar;
        this.name = name;
    }

    public Node parse(String input, int startIdx) {
        if (input == null || input.isEmpty() || startIdx >= input.length() || 
            input.charAt(startIdx) != expectedChar) {
            return null;
        }
        return new Node(
            String.valueOf(expectedChar),
            name,
            startIdx,
            startIdx + 1
        );
    }
}