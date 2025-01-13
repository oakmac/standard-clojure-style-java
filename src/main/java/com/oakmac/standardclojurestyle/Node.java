package com.oakmac.standardclojurestyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
  private List<Node> children;
  private int endIdx;
  private int id;
  private String name;
  private int startIdx;
  private String text;
  private Map<String, Object> attributes;

  public Node(Map<String, Object> opts) {
    this.children = (List<Node>) opts.get("children");
    this.endIdx = opts.containsKey("endIdx") ? ((Number) opts.get("endIdx")).intValue() : 0;
    this.id = Parser.createId();
    this.name = (String) opts.get("name");
    this.startIdx = opts.containsKey("startIdx") ? ((Number) opts.get("startIdx")).intValue() : 0;
    this.text = (String) opts.get("text");
    this.attributes = new HashMap<>();
  }

  // Getters
  public List<Node> getChildren() {
    return children;
  }

  public int getEndIdx() {
    return endIdx;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getStartIdx() {
    return startIdx;
  }

  public String getText() {
    return text;
  }

  // Attribute methods
  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }

  public Object getAttribute(String key) {
    return attributes.get(key);
  }

  // For debugging and testing
  @Override
  public String toString() {
    return "Node{"
        + "name='"
        + name
        + '\''
        + ", startIdx="
        + startIdx
        + ", endIdx="
        + endIdx
        + ", text='"
        + text
        + '\''
        + ", id="
        + id
        + ", children="
        + (children != null ? children.size() : "null")
        + '}';
  }
}
