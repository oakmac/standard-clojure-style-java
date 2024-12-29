package com.oakmac.standardclojurestyle;

import java.util.ArrayList;

public class RepeatParser implements IParser {
    private final IParser parser;
    private final String name;
    private final Integer minMatches;

    public RepeatParser(IParser parser, String name) {
        this(parser, name, null);
    }

    public RepeatParser(IParser parser, String name, Integer minMatches) {
        this.parser = parser;
        this.name = name;
        this.minMatches = minMatches;
    }

    @Override
    public Node parse(String input, int startIdx) {
        if (input == null || input.isEmpty() || startIdx >= input.length()) {
            return null;
        }

        int minMatchCount = (minMatches != null && minMatches > 0) ? minMatches : 0;
        ArrayList<Node> children = new ArrayList<>();
        int currentIdx = startIdx;

        while (true) {
            Node node = parser.parse(input, currentIdx);
            if (node == null) {
                break;
            }
            children.add(node);
            currentIdx = node.getEndIdx();
        }

        String finalName = null;
        if (name != null && currentIdx > startIdx) {
            finalName = name;
        }

        if (children.size() >= minMatchCount) {
            return new Node(finalName, startIdx, currentIdx, 
                          children.toArray(new Node[0]), children.size());
        }

        return new Node(null, startIdx, startIdx, new Node[0], 0);
    }
}