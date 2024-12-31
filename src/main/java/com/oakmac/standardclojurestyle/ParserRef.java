package com.oakmac.standardclojurestyle;

/**
 * Represents a reference to a parser, which can be either a String name 
 * referencing a registered parser, or a direct IParser instance.
 */
public interface ParserRef {
    IParser getParser();

    /**
     * Represents a reference to a parser by its registered name.
     */
    public static class StringRef implements ParserRef {
        private final String name;

        public StringRef(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public IParser getParser() {
            return ParserDefinitions.getParserByName(name);
        }
    }

    /**
     * Represents a direct reference to a parser instance.
     */
    public static class ParserInstanceRef implements ParserRef {
        private final IParser parser;

        public ParserInstanceRef(IParser parser) {
            this.parser = parser;
        }

        public IParser getParser() {
            return parser;
        }
    }
}