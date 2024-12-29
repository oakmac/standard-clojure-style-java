package com.oakmac.standardclojurestyle;

public interface IParser {
    Node parse(String input, int startIdx);
}