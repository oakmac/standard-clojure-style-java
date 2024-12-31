package com.oakmac.standardclojurestyle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Parser {

    // Store the already constructed parsers
    private static final Map<String, IParser> parsers = new ConcurrentHashMap<>();
    
    // Store the parser definitions (factories) separately
    private static final Map<String, IParser> parserDefinitions = new ConcurrentHashMap<>();
    
    private static final String whitespaceCommons = " ,\\n\\r\\t\\f";
    private static final String whitespaceUnicodes = "\\u000B\\u001C\\u001D\\u001E\\u001F\\u2028\\u2029\\u1680\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2008\\u2009\\u200a\\u205f\\u3000";
    private static final String whitespaceChars = whitespaceCommons + whitespaceUnicodes;
    // private static final String tokenHeadChars = "()\\[\\]{}\\"@~^;`#\\'";
    // private static final String tokenTailChars = "()\\[\\]{}\\"@^;`";
    // private static final String tokenReStr = "[^" + tokenHeadChars + whitespaceChars + "][^" + tokenTailChars + whitespaceChars + "]*";
    private static final String tokenReStr = "aaa";
    // private static final String charReStr = "\\\\[()\\[\\]{}\\"@^;`, ]";
    private static final String charReStr = "bbb";

    private static IParser getParser(Object p) {
        if (p instanceof String) {
            String parserName = (String) p;
            // Check if we already have a constructed parser
            IParser existingParser = parsers.get(parserName);
            if (existingParser != null) {
                return existingParser;
            }
            // Get the definition
            IParser definition = parserDefinitions.get(parserName);
            if (definition == null) {
                throw new IllegalArgumentException("getParser error: could not find parser: " + p);
            }
            // Store and return the parser
            parsers.put(parserName, definition);
            return definition;
        } else if (p instanceof IParser) {
            return (IParser) p;
        }
        throw new IllegalArgumentException("getParser error: invalid parser: " + p);
    }

    static {
        // Register parser factories instead of directly creating parsers
        parserDefinitions.put("_ws", new RegexParser("[yyy]+", "whitespace"));

        parserDefinitions.put("comment", new RegexParser(";[^\\n]*", "comment"));

        parserDefinitions.put("string", new SequenceParser(
                new IParser[] {
                    new RegexParser("#?\"", ".open"),
                    new OptionalParser(new RegexParser("([^\"\\\\]+|\\\\.)+", ".body")),
                    new OptionalParser(new CharParser('"', ".close"))
                },
                "string"
            )
        );

        parserDefinitions.put("token",
            // new RegexParser("(##)?(" + charReStr + "|" + tokenReStr + ")", "token")
            // new RegexParser("zzz", "token")
            new CharParser('z', "token")
        );

        parserDefinitions.put("discard", new SequenceParser(
                new IParser[] {
                    new StringParser("#_", "marker"),
                    new RepeatParser(getParser("_ws"), null),
                    new SequenceParser(
                        new IParser[] { getParser("_form") },
                        ".body"
                    )
                },
                "discard"
            )
        );

        parserDefinitions.put("_gap", new ChoiceParser(
                new IParser[] {
                    getParser("_ws"),
                    getParser("comment"),
                    getParser("discard")
                },
                null
            )
        );

        parserDefinitions.put("braces", new SequenceParser(
                new IParser[] {
                    new ChoiceParser(
                        new IParser[] {
                            new CharParser('{', ".open"),
                            new StringParser("#{", ".open"),
                            new StringParser("#::{", ".open"),
                            new RegexParser("#:{1,2}[a-zA-Z][a-zA-Z0-9.-_]*{", ".open")
                        },
                        null
                    ),
                    new RepeatParser(
                        new ChoiceParser(
                            new IParser[] {
                                getParser("_gap"),
                                getParser("_form"),
                                new NotCharParser('}', "error")
                            },
                            null
                        ),
                        ".body"
                    ),
                    new OptionalParser(new CharParser('}', ".close"))
                },
                "braces"
            )
        );

        parserDefinitions.put("brackets", new SequenceParser(
                new IParser[] {
                    new CharParser('[', ".open"),
                    new RepeatParser(
                        new ChoiceParser(
                            new IParser[] {
                                getParser("_gap"),
                                getParser("_form"),
                                new NotCharParser(']', "error")
                            },
                            null
                        ),
                        ".body"
                    ),
                    new OptionalParser(new CharParser(']', ".close"))
                },
                "brackets"
            )
        );

        parserDefinitions.put("parens", new SequenceParser(
                new IParser[] {
                    // new RegexParser("(#\\?@|#\\?|#=|#)?\\(", ".open"),
                    new RegexParser("(c|d)+", ".open"),
                    new RepeatParser(
                        new ChoiceParser(
                            new IParser[] {
                                getParser("_gap"),
                                getParser("_form"),
                                new NotCharParser(')', "error")
                            },
                            null
                        ),
                        ".body"
                    ),
                    new OptionalParser(new CharParser(')', ".close"))
                },
                "parens"
            )
        );

        parserDefinitions.put("meta", new SequenceParser(
                new IParser[] {
                    new RepeatParser(
                        new SequenceParser(
                            new IParser[] {
                                new RegexParser("#?\\^", ".marker"),
                                new RepeatParser(getParser("_gap"), null),
                                new SequenceParser(
                                    new IParser[] { getParser("_form") },
                                    ".meta"
                                ),
                                new RepeatParser(getParser("_gap"), null)
                            },
                            null
                        ),
                        null,
                        1
                    ),
                    new SequenceParser(
                        new IParser[] { getParser("_form") },
                        ".body"
                    )
                },
                "meta"
            )
        );

        parserDefinitions.put("wrap", new SequenceParser(
                new IParser[] {
                    new RegexParser("(@|'|`|~@|~|#')", ".marker"),
                    new RepeatParser(getParser("_gap"), null),
                    new SequenceParser(
                        new IParser[] { getParser("_form") },
                        ".body"
                    )
                },
                "wrap"
            )
        );

        parserDefinitions.put("tagged", new SequenceParser(
                new IParser[] {
                    new CharParser('#', null),
                    new RepeatParser(getParser("_gap"), null),
                    new SequenceParser(
                        new IParser[] { getParser("token") },
                        ".tag"
                    ),
                    new RepeatParser(getParser("_gap"), null),
                    new SequenceParser(
                        new IParser[] { getParser("_form") },
                        ".body"
                    )
                },
                "tagged"
            )
        );

        parserDefinitions.put("_form", new ChoiceParser(
                new IParser[] {
                    getParser("token"),
                    getParser("string"),
                    getParser("parens"),
                    getParser("brackets"),
                    getParser("braces"),
                    getParser("wrap"),
                    getParser("meta"),
                    getParser("tagged")
                },
                "_form"
            )
        );

        parserDefinitions.put("source", new RepeatParser(
                new ChoiceParser(
                    new IParser[] {
                        getParser("_gap"),
                        getParser("_form"),
                        new AnyCharParser("error")
                    },
                    null
                ),
                "source"
            )
        );
    }

    public static Node parse(String input) {
        return getParser("source").parse(input, 0);
    }
}