package com.oakmac.standardclojurestyle;

import java.util.List;

public class NodeToString {
  private static final int NUM_SPACES_PER_INDENT_LEVEL = 2;

  // returns a String representation of an AST Node Object
  // Note: this matches the Lua implementation
  public static String nodeToString(Node node, int indentLevel) {
    // skip printing whitespace nodes for the parser test suite
    if (isWhitespaceNode(node)) {
      return "";
    }

    // default indentLevel to 0 if not provided
    if (indentLevel < 0) {
      indentLevel = 0;
    }

    String indentationSpaces = repeatString(" ", indentLevel * NUM_SPACES_PER_INDENT_LEVEL);

    StringBuilder outTxt = new StringBuilder();
    if (!"source".equals(node.getName())) {
      outTxt.append("\n");
    }

    outTxt
        .append(indentationSpaces)
        .append("(")
        .append(node.getName())
        .append(" ")
        .append(node.getStartIdx())
        .append("..")
        .append(node.getEndIdx());

    if (node.getText() != null && !node.getText().isEmpty()) {
      String textWithNewlinesEscaped = node.getText().replace("\n", "\\n");
      outTxt.append(" '").append(textWithNewlinesEscaped).append("'");
    }

    List<Node> children = node.getChildren();
    if (children != null) {
      for (Node childNode : children) {
        outTxt.append(nodeToString(childNode, indentLevel + 1));
      }
    }

    outTxt.append(")");
    return outTxt.toString();
  }

  private static boolean isWhitespaceNode(Node node) {
    String name = node.getName();
    return name != null && (name.equals("whitespace") || name.equals("whitespace:newline"));
  }

  private static String repeatString(String str, int times) {
    return new String(new char[times]).replace("\0", str);
  }
}
