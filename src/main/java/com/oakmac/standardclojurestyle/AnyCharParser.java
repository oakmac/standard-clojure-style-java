package com.oakmac.standardclojurestyle;

public class AnyCharParser {
    private final String name;

    public AnyCharParser(String name) {
        this.name = name;
    }

    public Node parse(String input, int startIdx) {
        if (input == null || input.isEmpty() || startIdx >= input.length()) {
            return null;
        }
        return new Node(
            String.valueOf(input.charAt(startIdx)), 
            name,
            startIdx,
            startIdx + 1
        );
    }
}