package com.oakmac.standardclojurestyle;

public class OptionalParser implements IParser {
    private final IParser parser;

    public OptionalParser(IParser parser) {
        this.parser = parser;
    }

    @Override
    public Node parse(String input, int startIdx) {
        if (input == null || input.isEmpty() || startIdx >= input.length()) {
            return new Node(null, startIdx, startIdx, new Node[0], 0);
        }

        Node node = parser.parse(input, startIdx);
        if (node != null && node.getText() != null && !node.getText().isEmpty()) {
            return node;
        }

        return new Node(null, startIdx, startIdx, new Node[0], 0);
    }
}