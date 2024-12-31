package com.oakmac.standardclojurestyle;

public class SequenceParser implements IParser {
    private final ParserRef[] parsers;
    private final String name;

    // Original constructor still supported for backward compatibility
    public SequenceParser(IParser[] directParsers, String name) {
        this.parsers = new ParserRef[directParsers.length];
        for (int i = 0; i < directParsers.length; i++) {
            this.parsers[i] = new ParserRef.ParserInstanceRef(directParsers[i]);
        }
        this.name = name;
    }

    // New constructor that takes Object[] for JS-like usage
    public SequenceParser(Object[] parserRefs, String name) {
        this.parsers = new ParserRef[parserRefs.length];
        for (int i = 0; i < parserRefs.length; i++) {
            this.parsers[i] = ParserDefinitions.toParserRef(parserRefs[i]);
        }
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

        for (ParserRef parserRef : parsers) {
            IParser parser = ParserDefinitions.getParser(parserRef);
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