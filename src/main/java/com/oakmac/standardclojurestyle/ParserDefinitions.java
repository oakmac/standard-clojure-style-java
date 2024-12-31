package com.oakmac.standardclojurestyle;

import java.util.HashMap;
import java.util.Map;

/**
 * Static registry for parser definitions, allowing parsers to reference each other by name.
 * Similar to the JavaScript parsers object in the original implementation.
 */
public class ParserDefinitions {
    private static final Map<String, IParser> parsers = new HashMap<>();
    
    public static void register(String name, IParser parser) {
        parsers.put(name, parser);
    }
    
    public static IParser getParserByName(String name) {
        IParser parser = parsers.get(name);
        if (parser == null) {
            throw new RuntimeException("Could not find parser: " + name);
        }
        return parser;
    }

    public static IParser getParser(ParserRef ref) {
        return ref.getParser();
    }

    public static ParserRef toParserRef(Object ref) {
        if (ref instanceof String) {
            return new ParserRef.StringRef((String) ref);
        } else if (ref instanceof IParser) {
            return new ParserRef.ParserInstanceRef((IParser) ref);
        }
        throw new RuntimeException("Invalid parser reference: " + ref);
    }

    // Clear all registered parsers - useful for testing
    public static void clearParsers() {
        parsers.clear();
    }
}