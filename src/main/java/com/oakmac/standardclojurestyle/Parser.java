package com.oakmac.standardclojurestyle;

/**
 * Main parser class providing the public API for Standard Clojure Style parsing.
 * This class initializes and manages all the individual parsers.
 */
public class Parser {
    // Constants defined in JS version
    private static final String whitespaceCommons = " ,\\n\\r\\t\\f";
    private static final String whitespaceUnicodes = "\\u000B\\u001C\\u001D\\u001E\\u001F\\u2028\\u2029\\u1680\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2008\\u2009\\u200a\\u205f\\u3000";
    private static final String whitespaceChars = whitespaceCommons + whitespaceUnicodes;

    // NOTE: we can ignore these Regex placeholders for now; we will fix this later

    // private static final String tokenHeadChars = "()\\[\\]{}\\"@~^;`#\\'";
    // private static final String tokenTailChars = "()\\[\\]{}\\"@^;`";
    // private static final String tokenReStr = "[^" + tokenHeadChars + whitespaceChars + "][^" + tokenTailChars + whitespaceChars + "]*";
    private static final String tokenReStr = "bbb";
    // private static final String charReStr = "\\\\[()\\[\\]{}\\"@^;`, ]";
    private static final String charReStr = "aaa";

    static {
        // Initialize parsers in the correct order to avoid circular dependencies
        initializeBasicParsers();
        initializeCompoundParsers();
        initializeFormParser();
        initializeSourceParser();
    }

    private static void initializeBasicParsers() {
        // Basic parsers that don't depend on others
        ParserDefinitions.register("_ws", 
            new RegexParser("^[" + whitespaceChars + "]+", "whitespace")
        );

        ParserDefinitions.register("comment", 
            // new RegexParser("^;[^\\n]*", "comment")
            new RegexParser("^hhh", "comment")
        );

        ParserDefinitions.register("string", new SequenceParser(
            new Object[]{
                new RegexParser("^#?\"", ".open"),
                new OptionalParser(new RegexParser("^([^\"\\\\]+|\\\\.)+", ".body")),
                new OptionalParser(new CharParser('"', ".close"))
            },
            "string"
        ));

        ParserDefinitions.register("token", 
            new RegexParser("^(##)?(" + charReStr + "|" + tokenReStr + ")", "token")
        );
    }

    private static void initializeCompoundParsers() {
        // Initialize _gap first since it's used by many other parsers
        ParserDefinitions.register("_gap", new ChoiceParser(
            new Object[]{
                "_ws",
                "comment",
                "discard"  // This reference is fine because getParser handles forward refs
            },
            null
        ));

        ParserDefinitions.register("discard", new SequenceParser(
            new Object[]{
                new StringParser("#_", "marker"),
                new RepeatParser("_gap", null),
                new SequenceParser(
                    new Object[]{"_form"},  // Forward reference is okay
                    ".body"
                )
            },
            "discard"
        ));

        ParserDefinitions.register("braces", new SequenceParser(
            new Object[]{
                new ChoiceParser(new Object[]{
                    new CharParser('{', ".open"),
                    new StringParser("#{", ".open"),
                    new StringParser("#::{", ".open"),
                    // NOTE: ignore placeholder regex for now
                    // new RegexParser("#:{1,2}[a-zA-Z][a-zA-Z0-9.-_]*{", ".open")
                    new RegexParser("^zzz", ".open")
                }, null),
                new RepeatParser(
                    new ChoiceParser(new Object[]{
                        "_gap",
                        "_form",
                        new NotCharParser('}', "error")
                    }, null),
                    ".body"
                ),
                new OptionalParser(new CharParser('}', ".close"))
            },
            "braces"
        ));

        ParserDefinitions.register("brackets", new SequenceParser(
            new Object[]{
                new CharParser('[', ".open"),
                new RepeatParser(
                    new ChoiceParser(new Object[]{
                        "_gap",
                        "_form",
                        new NotCharParser(']', "error")
                    }, null),
                    ".body"
                ),
                new OptionalParser(new CharParser(']', ".close"))
            },
            "brackets"
        ));

        ParserDefinitions.register("parens", new SequenceParser(
            new Object[]{
                new RegexParser("^(#\\?@|#\\?|#=|#)?\\(", ".open"),
                new RepeatParser(
                    new ChoiceParser(new Object[]{
                        "_gap",
                        "_form",
                        new NotCharParser(')', "error")
                    }, null),
                    ".body"
                ),
                new OptionalParser(new CharParser(')', ".close"))
            },
            "parens"
        ));

        ParserDefinitions.register("meta", new SequenceParser(
            new Object[]{
                new RepeatParser(
                    new SequenceParser(new Object[]{
                        new RegexParser("^#?\\^", ".marker"),
                        new RepeatParser("_gap", null),
                        new SequenceParser(new Object[]{"_form"}, ".meta"),
                        new RepeatParser("_gap", null)
                    }, null),
                    null,
                    1
                ),
                new SequenceParser(new Object[]{"_form"}, ".body")
            },
            "meta"
        ));

        ParserDefinitions.register("wrap", new SequenceParser(
            new Object[]{
                new RegexParser("^(@|'|`|~@|~|#')", ".marker"),
                new RepeatParser("_gap", null),
                new SequenceParser(new Object[]{"_form"}, ".body")
            },
            "wrap"
        ));

        ParserDefinitions.register("tagged", new SequenceParser(
            new Object[]{
                new CharParser('#', null),
                new RepeatParser("_gap", null),
                new SequenceParser(new Object[]{"token"}, ".tag"),
                new RepeatParser("_gap", null),
                new SequenceParser(new Object[]{"_form"}, ".body")
            },
            "tagged"
        ));
    }

    private static void initializeFormParser() {
        ParserDefinitions.register("_form", new ChoiceParser(
            new Object[]{
                "token",
                "string",
                "parens",
                "brackets",
                "braces",
                "wrap",
                "meta",
                "tagged"
            },
            "_form"
        ));
    }

    private static void initializeSourceParser() {
        ParserDefinitions.register("source", new RepeatParser(
            new ChoiceParser(new Object[]{
                "_gap",
                "_form",
                new AnyCharParser("error")
            }, null),
            "source"
        ));
    }

    /**
     * Parse Clojure code into an AST.
     * @param input The Clojure code to parse
     * @return The root Node of the AST
     */
    public static Node parse(String input) {
        return ParserDefinitions.getParserByName("source").parse(input, 0);
    }
}