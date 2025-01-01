package com.oakmac.standardclojurestyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private static int idCounter = 0;
    private static final Map<String, ParserFunction> parsers = new HashMap<>();

    // Function interface for parsers
    interface ParserFunction {
        Node parse(String text, int position);
    }
    
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
        parser.put("parse", new ParserFunction() {
            @Override
            public Node parse(String txt, int pos) {
                if (pos < txt.length() && txt.charAt(pos) == chr.charAt(0)) {
                    Map<String, Object> nodeOpts = new HashMap<>();
                    nodeOpts.put("endIdx", pos + 1);
                    nodeOpts.put("name", name);
                    nodeOpts.put("startIdx", pos);
                    nodeOpts.put("text", chr);
                    return new Node(nodeOpts);
                }
                return null;
            }
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
        parser.put("parse", new ParserFunction() {
            @Override
            public Node parse(String txt, int pos) {
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
            }
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
        parser.put("parse", new ParserFunction() {
            @Override
            public Node parse(String txt, int pos) {
                if (pos < txt.length()) {
                    Map<String, Object> nodeOpts = new HashMap<>();
                    nodeOpts.put("endIdx", pos + 1);
                    nodeOpts.put("name", name);
                    nodeOpts.put("startIdx", pos);
                    nodeOpts.put("text", String.valueOf(txt.charAt(pos)));
                    return new Node(nodeOpts);
                }
                return null;
            }
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
        parser.put("parse", new ParserFunction() {
            @Override
            public Node parse(String txt, int pos) {
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
            }
        });
        return parser;
    }
}