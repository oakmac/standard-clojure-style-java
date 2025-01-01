package com.oakmac.standardclojurestyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static Map<String, Object> String(final Map<String, Object> opts) {
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
        final List<Map<String, Object>> parsers = (List<Map<String, Object>>)opts.get("parsers");
        
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            for (Map<String, Object> p : parsers) {
                IParserFunction parserFn = (IParserFunction)p.get("parse");
                Node possibleNode = parserFn.parse(txt, pos);
                if (possibleNode != null) {
                    return possibleNode;
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
        final Map<String, Object> childParser = (Map<String, Object>)opts.get("parser");
        final String name = (String)opts.get("name");
        final Integer minMatches = opts.containsKey("minMatches") ? 
            ((Number)opts.get("minMatches")).intValue() : 0;
        
        parser.put("parse", (IParserFunction) (txt, pos) -> {
            IParserFunction parserFn = (IParserFunction)childParser.get("parse");
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
}
