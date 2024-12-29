package com.oakmac.standardclojurestyle;

public class Node {
    private final String text;
    private final String name;
    private final int startIdx;
    private final int endIdx;

    public Node(String text, String name, int startIdx, int endIdx) {
        this.text = text;
        this.name = name;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
    }

    public String getText() { return text; }
    public String getName() { return name; }
    public int getStartIdx() { return startIdx; }
    public int getEndIdx() { return endIdx; }
}