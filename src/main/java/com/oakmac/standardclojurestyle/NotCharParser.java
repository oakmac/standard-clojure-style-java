package com.oakmac.standardclojurestyle;

public class NotCharParser {
    private final char excludedChar;
    private final String name;

    public NotCharParser(char excludedChar, String name) {
        this.excludedChar = excludedChar;
        this.name = name;
    }

    public Node parse(String input, int startIdx) {
        if (input == null || input.isEmpty() || startIdx >= input.length()) {
            return null;
        }

        char charAtThisPos = input.charAt(startIdx);
        if (charAtThisPos != excludedChar) {
            return new Node(
                String.valueOf(charAtThisPos),
                name,
                startIdx,
                startIdx + 1
            );
        }
        return null;
    }
}