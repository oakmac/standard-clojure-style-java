package com.oakmac.standardclojurestyle;

public class Node {
    private final String text;
    private final String name;
    private final int startIdx;
    private final int endIdx;
    private final Node[] children;
    private final int numChildren;

    public Node(String text, String name, int startIdx, int endIdx) {
        this(text, name, startIdx, endIdx, new Node[0], 0);
    }

    public Node(String name, int startIdx, int endIdx, Node[] children, int numChildren) {
        this(null, name, startIdx, endIdx, children, numChildren);
    }

    private Node(String text, String name, int startIdx, int endIdx, Node[] children, int numChildren) {
        this.text = text;
        this.name = name;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
        this.children = children;
        this.numChildren = numChildren;
    }

    public String getText() { 
        if (text == null && numChildren > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < numChildren; i++) {
                sb.append(children[i].getText());
            }
            return sb.toString();
        }
        return text; 
    }

    public String getName() { return name; }
    public int getStartIdx() { return startIdx; }
    public int getEndIdx() { return endIdx; }
    public Node[] getChildren() { return children; }
    public int getNumChildren() { return numChildren; }
}