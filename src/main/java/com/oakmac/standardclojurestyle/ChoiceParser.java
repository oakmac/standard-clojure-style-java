package com.oakmac.standardclojurestyle;

import java.util.Arrays;

public class ChoiceParser implements IParser {
    private final ParserRef[] parsers;
    private final String name;

    // Original constructor that takes ParserRef[]
    public ChoiceParser(ParserRef[] parsers, String name) {
        this.parsers = parsers;
        this.name = name;
    }

    // Original constructor that takes IParser[] for backward compatibility
    public ChoiceParser(IParser[] directParsers, String name) {
        this.parsers = new ParserRef[directParsers.length];
        for (int i = 0; i < directParsers.length; i++) {
            this.parsers[i] = new ParserRef.ParserInstanceRef(directParsers[i]);
        }
        this.name = name;
    }

    // Convenience constructor that accepts Object[] like the JS version
    public ChoiceParser(Object[] parserRefs, String name) {
        this.parsers = Arrays.stream(parserRefs)
            .map(ParserDefinitions::toParserRef)
            .toArray(ParserRef[]::new);
        this.name = name;
    }

    @Override
    public Node parse(String input, int startIdx) {
        if (input == null || input.isEmpty() || startIdx >= input.length()) {
            return null;
        }
        for (ParserRef ref : parsers) {
            IParser parser = ParserDefinitions.getParser(ref);
            Node possibleNode = parser.parse(input, startIdx);
            if (possibleNode != null) {
                return possibleNode;
            }
        }
        return null;
    }
}