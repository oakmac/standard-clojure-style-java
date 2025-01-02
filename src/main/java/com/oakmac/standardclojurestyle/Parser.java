package com.oakmac.standardclojurestyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Parser {
    private static int idCounter = 0;
    private static final Map<String, IParserFunction> parsers = new HashMap<>();
    
    public static int createId() {
        return ++idCounter;
    }

    // ---------------------------------------------------------------------------
    // Type Predicates
    
    private static boolean isString(Object s) {
        return s instanceof String;
    }
    
    private static boolean isInteger(Object x) {
        if (!(x instanceof Number)) return false;
        Number n = (Number)x;
        return n.intValue() == n.doubleValue();
    }
    
    private static boolean isPositiveInt(Object i) {
        return isInteger(i) && ((Number)i).intValue() >= 0;
    }

    // ---------------------------------------------------------------------------
    // String Utils
    
    private static String charAt(String s, int n) {
        if (n < 0 || n >= s.length()) {
            return "";
        }
        return String.valueOf(s.charAt(n));
    }

    private static String substr(String s, int startIdx, int endIdx) {
        if (startIdx == endIdx) return "";
        if (endIdx < 0) {
            endIdx = s.length();
        }
        if (startIdx < 0 || startIdx >= s.length()) {
            return "";
        }
        if (endIdx > s.length()) {
            endIdx = s.length();
        }
        return s.substring(startIdx, endIdx);
    }

    public static Map<String, Object> Named(final Map<String, Object> opts) {
        Map<String, Object> parser = new HashMap<>();
        final String name = (String)opts.get("name");
        final Object childParser = opts.get("parser");
        
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            IParserFunction parserFn = getParser(childParser);
            Node node = parserFn.parse(txt, pos);
            
            if (node == null) {
                return null;
            } else if (node != null && node.getName() == null) {
                // Create new node with the name instead of using setAttribute
                Map<String, Object> nodeOpts = new HashMap<>();
                nodeOpts.put("endIdx", node.getEndIdx());
                nodeOpts.put("name", name);
                nodeOpts.put("startIdx", node.getStartIdx());
                nodeOpts.put("text", node.getText());
                return new Node(nodeOpts);
            } else {
                Map<String, Object> nodeOpts = new HashMap<>();
                nodeOpts.put("children", Arrays.asList(node));
                nodeOpts.put("endIdx", node.getEndIdx());
                nodeOpts.put("name", name);
                nodeOpts.put("startIdx", node.getStartIdx());
                return new Node(nodeOpts);
            }
        });
        return parser;
    }

    public static Map<String, Object> Regex(Map<String, Object> opts) {
        Map<String, Object> parser = new HashMap<>();
        final String name = (String)opts.get("name");
        final Pattern regex = Pattern.compile((String)opts.get("regex"));
        final Integer groupIdx = opts.containsKey("groupIdx") ? ((Number)opts.get("groupIdx")).intValue() : null;
        
        parser.put("name", name);
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            // Get the substring from pos to end
            String innerTxt = substr(txt, pos, -1);
            
            // Create matcher
            Matcher matcher = regex.matcher(innerTxt);
            
            // Check for match at start of string
            if (!matcher.lookingAt()) {
                return null;
            }

            // Determine which group to use
            String matchedStr = null;
            if (groupIdx != null && matcher.groupCount() >= groupIdx && matcher.group(groupIdx + 1) != null) {
                matchedStr = matcher.group(groupIdx + 1);
            } else if (matcher.group(0) != null) {
                matchedStr = matcher.group(0);
            }

            if (matchedStr != null) {
                Map<String, Object> nodeOpts = new HashMap<>();
                nodeOpts.put("endIdx", pos + matchedStr.length());
                nodeOpts.put("name", name);
                nodeOpts.put("startIdx", pos);
                nodeOpts.put("text", matchedStr);
                return new Node(nodeOpts);
            }

            return null;
        });
        
        return parser;
    }

    private static Node stringBodyParser(String txt, int pos) {
        int maxLength = txt.length();
        if (maxLength == 0) {
            return null;
        }

        int charIdx = pos;
        int endIdx = -1;
        boolean keepSearching = true;
        StringBuilder parsedTxt = new StringBuilder();

        while (keepSearching) {
            String ch = charAt(txt, charIdx);
            if (ch == null || ch.isEmpty()) {
                keepSearching = false;
            } else if (ch.equals("\\")) {
                String nextChar = charAt(txt, charIdx + 1);
                if (nextChar != null && !nextChar.isEmpty()) {
                    parsedTxt.append(ch).append(nextChar);
                    charIdx++;
                } else {
                    return null;
                }
            } else if (ch.equals("\"")) {
                keepSearching = false;
                endIdx = charIdx;
            } else {
                parsedTxt.append(ch);
            }

            charIdx++;
            if (charIdx > maxLength) {
                keepSearching = false;
            }
        }

        if (endIdx > 0) {
            Map<String, Object> nodeOpts = new HashMap<>();
            nodeOpts.put("endIdx", endIdx);
            nodeOpts.put("name", ".body");
            nodeOpts.put("startIdx", pos);
            nodeOpts.put("text", parsedTxt.toString());
            return new Node(nodeOpts);
        }

        return null;
    }

    // FIXME: we can probably change these to be Sets

    // Character lookup tables for tokenParser
    private static final Map<String, Boolean> whitespaceCharsTbl = new HashMap<String, Boolean>() {{
        // Common chars
        put(" ", true);
        put(",", true);
        put("\n", true);
        put("\r", true);
        put("\t", true);
        put("\f", true);

        // Unicode chars
        put("\u000B", true);
        put("\u001C", true);
        put("\u001D", true);
        put("\u001E", true);
        put("\u001F", true);
        put("\u2028", true);
        put("\u2029", true);
        put("\u1680", true);
        put("\u2000", true);
        put("\u2001", true);
        put("\u2002", true);
        put("\u2003", true);
        put("\u2004", true);
        put("\u2005", true);
        put("\u2006", true);
        put("\u2008", true);
        put("\u2009", true);
        put("\u200a", true);
        put("\u205f", true);
        put("\u3000", true);
    }};

    private static final Map<String, Boolean> invalidTokenHeadCharsTbl = new HashMap<String, Boolean>() {{
        put("(", true);
        put(")", true);
        put("[", true);
        put("]", true);
        put("{", true);
        put("}", true);
        put("\"", true);
        put("@", true);
        put("~", true);
        put("^", true);
        put(";", true);
        put("`", true);
        put("#", true);
        put("'", true);
    }};

    private static final Map<String, Boolean> invalidTokenTailCharsTbl = new HashMap<String, Boolean>() {{
        put("(", true);
        put(")", true);
        put("[", true);
        put("]", true);
        put("{", true);
        put("}", true);
        put("\"", true);
        put("@", true);
        put("^", true);
        put(";", true);
        put("`", true);
    }};

    private static boolean isValidTokenHeadChar(String ch) {
        return !whitespaceCharsTbl.containsKey(ch) && !invalidTokenHeadCharsTbl.containsKey(ch);
    }

    private static boolean isValidTokenTailChar(String ch) {
        return !whitespaceCharsTbl.containsKey(ch) && !invalidTokenTailCharsTbl.containsKey(ch);
    }

    private static Node tokenParser(String txt, int pos) {
        int maxLength = txt.length();
        if (maxLength == 0) {
            return null;
        }

        int charIdx = pos;
        int endIdx = -1;
        boolean keepSearching = true;
        StringBuilder parsedTxt = new StringBuilder();
        boolean firstChar = true;

        // Check for ## prefix
        String firstTwoChars = substr(txt, pos, pos + 2);
        if (firstTwoChars.equals("##")) {
            parsedTxt.append("##");
            charIdx += 2;
        }

        while (keepSearching) {
            String ch = charAt(txt, charIdx);
            if (ch == null || ch.isEmpty()) {
                keepSearching = false;
            } else if (firstChar) {
                if (isValidTokenHeadChar(ch)) {
                    parsedTxt.append(ch);
                    endIdx = charIdx;
                    firstChar = false;
                } else {
                    return null;
                }
            } else if (isValidTokenTailChar(ch)) {
                parsedTxt.append(ch);
                endIdx = charIdx;
            } else {
                keepSearching = false;
            }

            charIdx++;
            if (charIdx > maxLength) {
                keepSearching = false;
            }
        }

        if (endIdx > -1) {
            Map<String, Object> nodeOpts = new HashMap<>();
            nodeOpts.put("endIdx", endIdx + 1);
            nodeOpts.put("name", "token");
            nodeOpts.put("startIdx", pos);
            nodeOpts.put("text", parsedTxt.toString());
            return new Node(nodeOpts);
        }

        return null;
    }

    // Character lookup table for specialCharParser
    private static final Map<String, Boolean> specialCharsTbl = new HashMap<String, Boolean>() {{
        put("(", true);
        put(")", true);
        put("[", true);
        put("]", true);
        put("{", true);
        put("}", true);
        put("\"", true);
        put("@", true);
        put("^", true);
        put(";", true);
        put("`", true);
        put(",", true);
        put(" ", true);
    }};

    private static Node specialCharParser(String txt, int pos) {
        int maxLength = txt.length();
        if (maxLength == 0) {
            return null;
        }

        String firstChar = charAt(txt, pos);
        if (firstChar.equals("\\")) {
            String secondChar = charAt(txt, pos + 1);
            if (specialCharsTbl.containsKey(secondChar)) {
                Map<String, Object> nodeOpts = new HashMap<>();
                nodeOpts.put("endIdx", pos + 2);
                nodeOpts.put("name", "token");
                nodeOpts.put("startIdx", pos);
                nodeOpts.put("text", firstChar + secondChar);
                return new Node(nodeOpts);
            }
        }

        return null;
    }

    private static Node whitespaceParser(String txt, int pos) {
        int maxLength = txt.length();
        if (maxLength == 0) {
            return null;
        }

        int charIdx = pos;
        int endIdx = -1;
        boolean keepSearching = true;
        StringBuilder parsedTxt = new StringBuilder();

        while (keepSearching) {
            String ch = charAt(txt, charIdx);
            if (ch == null || ch.isEmpty()) {
                keepSearching = false;
            } else if (whitespaceCharsTbl.containsKey(ch)) {
                parsedTxt.append(ch);
                endIdx = charIdx;
            } else {
                keepSearching = false;
            }

            charIdx++;
            if (charIdx > maxLength) {
                keepSearching = false;
            }
        }

        if (endIdx > -1) {
            Map<String, Object> nodeOpts = new HashMap<>();
            nodeOpts.put("endIdx", endIdx + 1);
            nodeOpts.put("name", "whitespace");
            nodeOpts.put("startIdx", pos);
            nodeOpts.put("text", parsedTxt.toString());
            return new Node(nodeOpts);
        }

        return null;
    }

    // // NOTE: this is parsers.string implementation, not the same thing as String terminal parser above
    // public static Map<String, Object> StringParser(Map<String, Object> opts) {
    //     Map<String, Object> parser = new HashMap<>();
    //     parser.put("name", opts.get("name"));
    //     parser.put("parse", (IParserFunction) (txt, pos) -> {
    //         // Create sequence parser for string parts
    //         List<Map<String, Object>> parsers = new ArrayList<>();
            
    //         // String open quote
    //         Map<String, Object> openQuoteOpts = new HashMap<>();
    //         openQuoteOpts.put("regex", "^#?\"");
    //         openQuoteOpts.put("name", ".open");
    //         parsers.add(Regex(openQuoteOpts));
            
    //         // Optional string body
    //         parsers.add(Optional(new HashMap<String, Object>() {{
    //             put("parse", (IParserFunction) (t, p) -> stringBodyParser(t, p));
    //         }}));
            
    //         // Optional closing quote
    //         Map<String, Object> closeQuoteOpts = new HashMap<>();
    //         closeQuoteOpts.put("char", "\"");
    //         closeQuoteOpts.put("name", ".close");
    //         parsers.add(Optional(Char(closeQuoteOpts)));
            
    //         // Create and run sequence parser
    //         Map<String, Object> seqOpts = new HashMap<>();
    //         seqOpts.put("name", opts.get("name"));
    //         seqOpts.put("parsers", parsers);
    //         return ((IParserFunction)Seq(seqOpts).get("parse")).parse(txt, pos);
    //     });
        
    //     return parser;
    // }

    // ---------------------------------------------------------------------------
    // Terminal Parsers

    /**
     * Terminal parser that matches one character.
     */
    public static Map<String, Object> Char(final Map<String, Object> opts) {
        Map<String, Object> parser = new HashMap<>();
        final String chr = (String)opts.get("char");
        final String name = (String)opts.get("name");
        
        parser.put("isTerminal", true);
        parser.put("char", chr);
        parser.put("name", name);
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            if (pos < txt.length() && txt.charAt(pos) == chr.charAt(0)) {
                Map<String, Object> nodeOpts = new HashMap<>();
                nodeOpts.put("endIdx", pos + 1);
                nodeOpts.put("name", name);
                nodeOpts.put("startIdx", pos);
                nodeOpts.put("text", chr);
                return new Node(nodeOpts);
            }
            return null;
        });
        return parser;
    }

    /**
     * Terminal parser that matches any single character, except one.
     */
    public static Map<String, Object> NotChar(final Map<String, Object> opts) {
        Map<String, Object> parser = new HashMap<>();
        final String chr = (String)opts.get("char");
        final String name = (String)opts.get("name");
        
        parser.put("isTerminal", true);
        parser.put("char", chr);
        parser.put("name", name);
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            if (pos < txt.length()) {
                char charAtPos = txt.charAt(pos);
                if (charAtPos != chr.charAt(0)) {
                    Map<String, Object> nodeOpts = new HashMap<>();
                    nodeOpts.put("endIdx", pos + 1);
                    nodeOpts.put("name", name);
                    nodeOpts.put("startIdx", pos);
                    nodeOpts.put("text", String.valueOf(charAtPos));
                    return new Node(nodeOpts);
                }
            }
            return null;
        });
        return parser;
    }

    /**
     * Terminal parser that matches any single character.
     */
    public static Map<String, Object> AnyChar(final Map<String, Object> opts) {
        Map<String, Object> parser = new HashMap<>();
        final String name = (String)opts.get("name");
        
        parser.put("name", name);
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            if (pos < txt.length()) {
                Map<String, Object> nodeOpts = new HashMap<>();
                nodeOpts.put("endIdx", pos + 1);
                nodeOpts.put("name", name);
                nodeOpts.put("startIdx", pos);
                nodeOpts.put("text", String.valueOf(txt.charAt(pos)));
                return new Node(nodeOpts);
            }
            return null;
        });
        return parser;
    }

    /**
     * Terminal parser that matches a String
     */
    public static Map<String, Object> StringParser(final Map<String, Object> opts) {
        Map<String, Object> parser = new HashMap<>();
        final String str = (String)opts.get("str");
        final String name = (String)opts.get("name");
        
        parser.put("name", name);
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            int len = str.length();
            if (pos + len <= txt.length()) {
                String strToCompare = substr(txt, pos, pos + len);
                if (str.equals(strToCompare)) {
                    Map<String, Object> nodeOpts = new HashMap<>();
                    nodeOpts.put("endIdx", pos + len);
                    nodeOpts.put("name", name);
                    nodeOpts.put("startIdx", pos);
                    nodeOpts.put("text", str);
                    return new Node(nodeOpts);
                }
            }
            return null;
        });
        return parser;
    }

    // ---------------------------------------------------------------------------
    // Sequence Parsers

    /**
     * Parser that matches a linear sequence of other parsers
     */
    public static Map<String, Object> Seq(final Map<String, Object> opts) {
        Map<String, Object> parser = new HashMap<>();
        final String name = (String)opts.get("name");
        final List<Map<String, Object>> parsers = (List<Map<String, Object>>)opts.get("parsers");
        
        parser.put("isTerminal", false);
        parser.put("name", name);
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            List<Node> children = new ArrayList<>();
            int endIdx = pos;

            for (Map<String, Object> p : parsers) {
                IParserFunction parserFn = (IParserFunction)p.get("parse");
                Node possibleNode = parserFn.parse(txt, endIdx);
                if (possibleNode != null) {
                    appendChildren(children, possibleNode);
                    endIdx = possibleNode.getEndIdx();
                } else {
                    // not a valid sequence: early return
                    return null;
                }
            }

            Map<String, Object> nodeOpts = new HashMap<>();
            nodeOpts.put("children", children);
            nodeOpts.put("endIdx", endIdx);
            nodeOpts.put("name", name);
            nodeOpts.put("startIdx", pos);
            return new Node(nodeOpts);
        });
        return parser;
    }

    /**
     * Parser that matches the first matching of several parsers
     */
    public static Map<String, Object> Choice(final Map<String, Object> opts) {
        Map<String, Object> parser = new HashMap<>();
        final List<Object> parsers = (List<Object>)opts.get("parsers");
        
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            for (Object p : parsers) {
                IParserFunction parserFn = getParser(p);
                if (parserFn != null) {
                    Node possibleNode = parserFn.parse(txt, pos);
                    if (possibleNode != null) {
                        return possibleNode;
                    }
                }
            }
            return null;
        });
        return parser;
    }

    /**
     * Parser that matches child parser zero or more times
     */
    public static Map<String, Object> Repeat(final Map<String, Object> opts) {
        Map<String, Object> parser = new HashMap<>();
        final Object childParser = opts.get("parser");
        final String name = (String)opts.get("name");
        final Integer minMatches = opts.containsKey("minMatches") ? 
            ((Number)opts.get("minMatches")).intValue() : 0;
        
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            IParserFunction parserFn = getParser(childParser);
            List<Node> children = new ArrayList<>();
            int endIdx = pos;

            boolean lookForTheNextNode = true;
            while (lookForTheNextNode) {
                Node node = parserFn.parse(txt, endIdx);
                if (node != null) {
                    appendChildren(children, node);
                    endIdx = node.getEndIdx();
                } else {
                    lookForTheNextNode = false;
                }
            }

            String name2 = null;
            if (isString(name) && endIdx > pos) {
                name2 = name;
            }

            if (children.size() >= minMatches) {
                Map<String, Object> nodeOpts = new HashMap<>();
                nodeOpts.put("children", children);
                nodeOpts.put("endIdx", endIdx);
                nodeOpts.put("name", name2);
                nodeOpts.put("startIdx", pos);
                return new Node(nodeOpts);
            }

            return null;
        });
        return parser;
    }

    /**
     * Parser that either matches a child parser or skips it
     */
    public static Map<String, Object> Optional(final Map<String, Object> childParser) {
        Map<String, Object> parser = new HashMap<>();
        
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            IParserFunction parserFn = (IParserFunction)childParser.get("parse");
            Node node = parserFn.parse(txt, pos);
            if (node != null && isString(node.getText()) && !node.getText().isEmpty()) {
                return node;
            } else {
                Map<String, Object> nodeOpts = new HashMap<>();
                nodeOpts.put("startIdx", pos);
                nodeOpts.put("endIdx", pos);
                return new Node(nodeOpts);
            }
        });
        return parser;
    }

    // Helper function for sequence parsers
    private static void appendChildren(List<Node> childrenArr, Node node) {
        if (isString(node.getName()) && !node.getName().isEmpty()) {
            childrenArr.add(node);
        } else if (node.getChildren() != null) {
            for (Node child : node.getChildren()) {
                if (child != null) {
                    appendChildren(childrenArr, child);
                }
            }
        }
    }

    // One-time initialization of all parsers
    static {
        // string parser
        parsers.put("string", (IParserFunction) (txt, pos) -> {
            List<Map<String, Object>> seqParsers = new ArrayList<>();
            
            // String open quote
            Map<String, Object> openQuoteOpts = new HashMap<>();
            openQuoteOpts.put("regex", "#?\"");
            openQuoteOpts.put("name", ".open");
            seqParsers.add(Regex(openQuoteOpts));
            
            // Optional string body 
            Map<String, Object> bodyParser = new HashMap<>();
            bodyParser.put("parse", (IParserFunction) Parser::stringBodyParser);
            seqParsers.add(Optional(bodyParser));
            
            // Optional closing quote
            Map<String, Object> closeQuoteOpts = new HashMap<>();
            closeQuoteOpts.put("char", "\"");
            closeQuoteOpts.put("name", ".close"); 
            seqParsers.add(Optional(Char(closeQuoteOpts)));
            
            Map<String, Object> seqOpts = new HashMap<>();
            seqOpts.put("name", "string");
            seqOpts.put("parsers", seqParsers);
            return ((IParserFunction)Seq(seqOpts).get("parse")).parse(txt, pos);
        });

        // token parser
        parsers.put("token", (IParserFunction) (txt, pos) -> {
            List<Map<String, Object>> choices = new ArrayList<>();
            Map<String, Object> specialCharParserOpts = new HashMap<>();
            specialCharParserOpts.put("parse", (IParserFunction) Parser::specialCharParser);
            choices.add(specialCharParserOpts);
            
            Map<String, Object> tokenParserOpts = new HashMap<>();
            tokenParserOpts.put("parse", (IParserFunction) Parser::tokenParser);
            choices.add(tokenParserOpts);
            
            Map<String, Object> choiceOpts = new HashMap<>();
            choiceOpts.put("parsers", choices);
            return ((IParserFunction)Choice(choiceOpts).get("parse")).parse(txt, pos);
        });

        // whitespace parser (_ws)
        parsers.put("_ws", (IParserFunction) Parser::whitespaceParser);

        // comment parser
        Map<String, Object> commentOpts = new HashMap<>();
        commentOpts.put("regex", ";[^\\n]*");
        commentOpts.put("name", "comment");
        parsers.put("comment", (IParserFunction)Regex(commentOpts).get("parse"));

        // discard parser
        List<Map<String, Object>> discardParsers = new ArrayList<>();
        Map<String, Object> markerOpts = new HashMap<>();
        markerOpts.put("str", "#_");
        markerOpts.put("name", "marker");
        discardParsers.add(StringParser(markerOpts));
        discardParsers.add(Repeat(new HashMap<String, Object>() {{ 
            put("parser", "_gap");
        }}));
        discardParsers.add(Named(new HashMap<String, Object>() {{
            put("name", ".body");
            put("parser", "_form");
        }}));
        Map<String, Object> discardOpts = new HashMap<>();
        discardOpts.put("name", "discard");
        discardOpts.put("parsers", discardParsers);
        parsers.put("discard", (IParserFunction)Seq(discardOpts).get("parse"));

        // braces parser
        List<Map<String, Object>> bracesParsers = new ArrayList<>();

        // First the opening brace options
        List<Map<String, Object>> openChoices = new ArrayList<>();
        openChoices.add(Char(new HashMap<String, Object>() {{
            put("name", ".open");
            put("char", "{");
        }}));
        openChoices.add(StringParser(new HashMap<String, Object>() {{
            put("name", ".open");
            put("str", "#{");
        }}));
        openChoices.add(StringParser(new HashMap<String, Object>() {{
            put("name", ".open");
            put("str", "#::{");
        }}));
        openChoices.add(Regex(new HashMap<String, Object>() {{
            put("name", ".open");
            put("regex", "#::{1,2}[a-zA-Z][a-zA-Z0-9\\.\\-_]*\\{");
        }}));

        bracesParsers.add(Choice(new HashMap<String, Object>() {{
            put("parsers", openChoices);
        }}));

        // Then the body
        List<Map<String, Object>> bodyChoices = new ArrayList<>();
        // Instead of strings, we need to wrap these in maps with parser references
        Map<String, Object> gapRef = new HashMap<String, Object>() {{
            put("parse", parsers.get("_gap"));
        }};
        Map<String, Object> formRef = new HashMap<String, Object>() {{
            put("parse", parsers.get("_form"));
        }};
        bodyChoices.add(gapRef);
        bodyChoices.add(formRef);
        bodyChoices.add(NotChar(new HashMap<String, Object>() {{
            put("name", "error");
            put("char", "}");
        }}));

        bracesParsers.add(Repeat(new HashMap<String, Object>() {{
            put("name", ".body");
            put("parser", Choice(new HashMap<String, Object>() {{
                put("parsers", bodyChoices);
            }}));
        }}));

        // Finally the closing brace
        bracesParsers.add(Optional(Char(new HashMap<String, Object>() {{
            put("name", ".close"); 
            put("char", "}");
        }})));

        // Create the braces parser
        Map<String, Object> bracesOpts = new HashMap<>();
        bracesOpts.put("name", "braces");
        bracesOpts.put("parsers", bracesParsers);
        parsers.put("braces", (IParserFunction)Seq(bracesOpts).get("parse"));

        // brackets parser
        List<Map<String, Object>> bracketsParsers = new ArrayList<>();

        // Opening bracket
        bracketsParsers.add(Char(new HashMap<String, Object>() {{
            put("name", ".open");
            put("char", "[");
        }}));

        // Body of the brackets
        List<Object> bracketBodyChoices = new ArrayList<>();
        bracketBodyChoices.add("_gap");
        bracketBodyChoices.add("_form");
        bracketBodyChoices.add(NotChar(new HashMap<String, Object>() {{
            put("name", "error");
            put("char", "]");
        }}));

        Map<String, Object> choiceOpts = new HashMap<>();
        choiceOpts.put("parsers", bracketBodyChoices);

        Map<String, Object> repeatOpts = new HashMap<>();
        repeatOpts.put("name", ".body");
        repeatOpts.put("parser", Choice(choiceOpts));
        bracketsParsers.add(Repeat(repeatOpts));

        // Closing bracket (optional)
        bracketsParsers.add(Optional(Char(new HashMap<String, Object>() {{
            put("name", ".close");
            put("char", "]");
        }})));

        // Create the brackets parser
        Map<String, Object> bracketsOpts = new HashMap<>();
        bracketsOpts.put("name", "brackets");
        bracketsOpts.put("parsers", bracketsParsers);
        parsers.put("brackets", (IParserFunction)Seq(bracketsOpts).get("parse"));

        // parens parser
        List<Map<String, Object>> parensParsers = new ArrayList<>();

        // Opening paren choices first
        List<Map<String, Object>> openParenChoices = new ArrayList<>();
        openParenChoices.add(Char(new HashMap<String, Object>() {{
            put("name", ".open");
            put("char", "(");
        }}));
        openParenChoices.add(Regex(new HashMap<String, Object>() {{
            put("name", ".open");
            put("regex", "#\\?@\\(");
        }}));
        openParenChoices.add(Regex(new HashMap<String, Object>() {{
            put("name", ".open");
            put("regex", "#\\?\\(");
        }}));
        openParenChoices.add(Regex(new HashMap<String, Object>() {{
            put("name", ".open");
            put("regex", "#=\\(");
        }}));
        openParenChoices.add(Regex(new HashMap<String, Object>() {{
            put("name", ".open");
            put("regex", "#\\(");
        }}));

        parensParsers.add(Choice(new HashMap<String, Object>() {{
            put("parsers", openParenChoices);
        }}));

        // Body choices
        List<Map<String, Object>> parenBodyChoices = new ArrayList<>();

        // Create wrapper objects for the parser references
        Map<String, Object> gapRef2 = new HashMap<>();
        gapRef2.put("parse", parsers.get("_gap"));
        parenBodyChoices.add(gapRef2);

        Map<String, Object> formRef2 = new HashMap<>();
        formRef2.put("parse", parsers.get("_form"));
        parenBodyChoices.add(formRef2);

        parenBodyChoices.add(NotChar(new HashMap<String, Object>() {{
            put("name", "error");
            put("char", ")");
        }}));

        parensParsers.add(Repeat(new HashMap<String, Object>() {{
            put("name", ".body");
            put("parser", Choice(new HashMap<String, Object>() {{
                put("parsers", parenBodyChoices);
            }}));
        }}));

        // Optional closing paren
        parensParsers.add(Optional(Char(new HashMap<String, Object>() {{
            put("name", ".close");
            put("char", ")");
        }})));

        parsers.put("parens", (IParserFunction)Seq(new HashMap<String, Object>() {{
            put("name", "parens");
            put("parsers", parensParsers);
        }}).get("parse"));

        // _gap parser
        List<Map<String, Object>> gapChoices = new ArrayList<>();

        // Create wrapper objects for each parser reference
        Map<String, Object> wsRef = new HashMap<>();
        wsRef.put("parse", parsers.get("_ws"));
        gapChoices.add(wsRef);

        Map<String, Object> commentRef = new HashMap<>();
        commentRef.put("parse", parsers.get("comment"));
        gapChoices.add(commentRef);

        Map<String, Object> discardRef = new HashMap<>();
        discardRef.put("parse", parsers.get("discard"));
        gapChoices.add(discardRef);

        parsers.put("_gap", (IParserFunction)Choice(new HashMap<String, Object>() {{
            put("parsers", gapChoices);
        }}).get("parse"));

        // meta parser
        List<Map<String, Object>> metaParsers = new ArrayList<>();
        
        List<Map<String, Object>> metaSeqParsers = new ArrayList<>();
        metaSeqParsers.add(Regex(new HashMap<String, Object>() {{
            put("name", ".marker");
            put("regex", "#?\\^");
        }}));
        metaSeqParsers.add(Repeat(new HashMap<String, Object>() {{
            put("parser", "_gap");
        }}));
        metaSeqParsers.add(Named(new HashMap<String, Object>() {{
            put("name", ".meta");
            put("parser", "_form");
        }}));
        metaSeqParsers.add(Repeat(new HashMap<String, Object>() {{
            put("parser", "_gap");
        }}));
        
        metaParsers.add(Repeat(new HashMap<String, Object>() {{
            put("minMatches", 1);
            put("parser", Seq(new HashMap<String, Object>() {{
                put("parsers", metaSeqParsers);
            }}));
        }}));
        
        metaParsers.add(Named(new HashMap<String, Object>() {{
            put("name", ".body");
            put("parser", "_form");
        }}));
        
        parsers.put("meta", (IParserFunction)Seq(new HashMap<String, Object>() {{
            put("name", "meta");
            put("parsers", metaParsers);
        }}).get("parse"));

        // wrap parser
        List<Map<String, Object>> wrapParsers = new ArrayList<>();
        
        List<Map<String, Object>> wrapMarkerChoices = new ArrayList<>();
        wrapMarkerChoices.add(Regex(new HashMap<String, Object>() {{
            put("name", ".marker");
            put("regex", "~@");
        }}));
        wrapMarkerChoices.add(Regex(new HashMap<String, Object>() {{
            put("name", ".marker");
            put("regex", "#'");
        }}));
        wrapMarkerChoices.add(Char(new HashMap<String, Object>() {{
            put("name", ".marker");
            put("char", "@");
        }}));
        wrapMarkerChoices.add(Char(new HashMap<String, Object>() {{
            put("name", ".marker");
            put("char", "'");
        }}));
        wrapMarkerChoices.add(Char(new HashMap<String, Object>() {{
            put("name", ".marker");
            put("char", "`");
        }}));
        wrapMarkerChoices.add(Char(new HashMap<String, Object>() {{
            put("name", ".marker");
            put("char", "~");
        }}));
        
        wrapParsers.add(Choice(new HashMap<String, Object>() {{
            put("parsers", wrapMarkerChoices);
        }}));
        
        wrapParsers.add(Repeat(new HashMap<String, Object>() {{
            put("parser", "_gap");
        }}));
        
        wrapParsers.add(Named(new HashMap<String, Object>() {{
            put("name", ".body");
            put("parser", "_form");
        }}));
        
        parsers.put("wrap", (IParserFunction)Seq(new HashMap<String, Object>() {{
            put("name", "wrap");
            put("parsers", wrapParsers);
        }}).get("parse"));

        // tagged parser
        List<Map<String, Object>> taggedParsers = new ArrayList<>();
        taggedParsers.add(Char(new HashMap<String, Object>() {{
            put("char", "#");
        }}));
        taggedParsers.add(Repeat(new HashMap<String, Object>() {{
            put("parser", "_gap");
        }}));
        taggedParsers.add(Named(new HashMap<String, Object>() {{
            put("name", ".tag");
            put("parser", "token");
        }}));
        taggedParsers.add(Repeat(new HashMap<String, Object>() {{
            put("parser", "_gap");
        }}));
        taggedParsers.add(Named(new HashMap<String, Object>() {{
            put("name", ".body");
            put("parser", "_form");
        }}));
        
        parsers.put("tagged", (IParserFunction)Seq(new HashMap<String, Object>() {{
            put("name", "tagged");
            put("parsers", taggedParsers);
        }}).get("parse"));
    }

    // Helper to get parser by name or return direct parser object
    public static IParserFunction getParser(Object p) {
        if (p instanceof String && parsers.containsKey(p)) {
            return parsers.get(p);
        }
        if (p instanceof Map) {
            Object parse = ((Map)p).get("parse");
            if (parse instanceof IParserFunction) {
                return (IParserFunction)parse;
            }
        }
        return null;
    }

    // Main parse function that kicks everything off
    public static Node parse(String inputTxt) {
        return ((IParserFunction)parsers.get("source")).parse(inputTxt, 0);
    }
}
