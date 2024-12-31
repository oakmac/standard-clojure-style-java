package com.oakmac.standardclojurestyle;

public class RepeatParser implements IParser {
    private final ParserRef parser;
    private final String name;
    private final Integer minMatches;

    // Original constructor still supported for backward compatibility
    public RepeatParser(IParser directParser, String name) {
        this(directParser, name, null);
    }

    // Original constructor with minMatches
    public RepeatParser(IParser directParser, String name, Integer minMatches) {
        this.parser = new ParserRef.ParserInstanceRef(directParser);
        this.name = name;
        this.minMatches = minMatches;
    }

    // New constructor that takes Object for JS-like usage
    public RepeatParser(Object parserRef, String name) {
        this(parserRef, name, null);
    }

    // New constructor that takes Object and minMatches
    public RepeatParser(Object parserRef, String name, Integer minMatches) {
        this.parser = ParserDefinitions.toParserRef(parserRef);
        this.name = name;
        this.minMatches = minMatches;
    }

    @Override
    public Node parse(String input, int startIdx) {
        if (input == null || input.isEmpty() || startIdx >= input.length()) {
            return null;
        }

        int minMatchCount = (minMatches != null && minMatches > 0) ? minMatches : 0;
        java.util.ArrayList<Node> children = new java.util.ArrayList<>();
        int currentIdx = startIdx;

        while (true) {
            Node node = ParserDefinitions.getParser(parser).parse(input, currentIdx);
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