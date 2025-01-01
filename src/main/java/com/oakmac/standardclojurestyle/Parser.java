package com.oakmac.standardclojurestyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private static int idCounter = 0;
    // private static final Map<String, ParserFunction> parsers = new HashMap<>();

    // Function interface for parsers
    interface ParserFunction {
        Node parse(String text, int position);
    }
    
    public static int createId() {
        return ++idCounter;
    }

}