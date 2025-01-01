package com.oakmac.standardclojurestyle;

public interface IParserFunction {
    Node parse(String text, int position);
}