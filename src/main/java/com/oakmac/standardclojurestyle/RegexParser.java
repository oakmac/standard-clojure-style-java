package com.oakmac.standardclojurestyle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParser implements IParser {
    private final Pattern pattern;
    private final String name;
    private final Integer groupIdx;

    public RegexParser(String regex, String name) {
        this(regex, name, null);
    }

    public RegexParser(String regex, String name, Integer groupIdx) {
        this.pattern = Pattern.compile(regex);
        this.name = name;
        this.groupIdx = groupIdx;
    }

    @Override
    public Node parse(String input, int startIdx) {
        if (input == null || input.isEmpty() || startIdx >= input.length()) {
            return null;
        }

        String textToMatch = input.substring(startIdx);
        Matcher matcher = pattern.matcher(textToMatch);

        if (matcher.lookingAt()) {
            String matchedStr;
            if (groupIdx != null && groupIdx < matcher.groupCount()) {
                matchedStr = matcher.group(groupIdx + 1);
            } else {
                matchedStr = matcher.group(0);
            }
            
            if (matchedStr != null) {
                return new Node(
                    matchedStr,
                    name,
                    startIdx,
                    startIdx + matchedStr.length()
                );
            }
        }
        return null;
    }
}