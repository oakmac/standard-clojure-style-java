package com.oakmac.standardclojurestyle;

public class ChoiceParser implements IParser {
    private final IParser[] parsers;
    private final String name;

    public ChoiceParser(IParser[] parsers, String name) {
        this.parsers = parsers;
        this.name = name;
    }

    @Override
    public Node parse(String input, int startIdx) {
        if (input == null || input.isEmpty() || startIdx >= input.length()) {
            return null;
        }

        for (IParser parser : parsers) {
            Node possibleNode = parser.parse(input, startIdx);
            if (possibleNode != null) {
                return possibleNode;
            }
        }
        return null;
    }
}