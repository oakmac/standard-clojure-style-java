package com.oakmac.standardclojurestyle;

public class StringParser {
    private final String str;
    private final String name;

    public StringParser(String str, String name) {
        this.str = str;
        this.name = name;
    }

    public Node parse(String input, int startIdx) {
        if (input == null || input.isEmpty() || startIdx >= input.length()) {
            return null;
        }

        int len = str.length();
        if (startIdx + len <= input.length()) {
            String strToCompare = input.substring(startIdx, startIdx + len);
            if (str.equals(strToCompare)) {
                return new Node(str, name, startIdx, startIdx + len);
            }
        }
        return null;
    }
}