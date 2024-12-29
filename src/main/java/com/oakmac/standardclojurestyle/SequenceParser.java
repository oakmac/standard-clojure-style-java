package com.oakmac.standardclojurestyle;

public class SequenceParser implements IParser {
    private final IParser[] parsers;
    private final String name;

    public SequenceParser(IParser[] parsers, String name) {
        this.parsers = parsers;
        this.name = name;
    }

    @Override
    public Node parse(String input, int startIdx) {
        if (input == null || input.isEmpty() || startIdx >= input.length()) {
            return null;
        }

        Node[] children = new Node[parsers.length];
        int currentIdx = startIdx;
        int numChildren = 0;

        for (IParser parser : parsers) {
            Node possibleNode = parser.parse(input, currentIdx);
            if (possibleNode == null) {
                return null;
            }
            children[numChildren++] = possibleNode;
            currentIdx = possibleNode.getEndIdx();
        }

        return new Node(name, startIdx, currentIdx, children, numChildren);
    }
}