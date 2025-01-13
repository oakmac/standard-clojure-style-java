package com.oakmac.standardclojurestyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
  public static boolean isNewlineNode(Node n) {
    return n.getName().equals("whitespace") && isString(n.getText()) && n.getText().contains("\n");
  }

  public static boolean isNsNode(Node n) {
    return n.getName().equals("token") && n.getText().equals("ns");
  }

  public static boolean isReferClojureNode(Node n) {
    return n != null
        && isString(n.getText())
        && (n.getText().equals(":refer-clojure") || n.getText().equals("refer-clojure"));
  }

  public static boolean isRequireNode(Node n) {
    return n != null
        && isString(n.getText())
        && (n.getText().equals(":require") || n.getText().equals("require"));
  }

  public static boolean isImportNode(Node n) {
    return n != null
        && isString(n.getText())
        && (n.getText().equals(":import") || n.getText().equals("import"));
  }

  public static boolean isRequireMacrosKeyword(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":require-macros");
  }

  public static boolean isGenClassNode(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":gen-class");
  }

  public static boolean isParenOpener(Node n) {
    if (n == null || !n.getName().equals(".open")) return false;
    String text = n.getText();
    return text.equals("(")
        || text.equals("[")
        || text.equals("{")
        || text.equals("#{")
        || text.equals("#(")
        || text.equals("#?(")
        || text.equals("#?@(")
        || isNamespacedMapOpener(n);
  }

  public static boolean isNamespacedMapOpener(Node n) {
    return n.getName().equals(".open") && n.getText().startsWith("#:") && n.getText().endsWith("{");
  }

  public static boolean isParenCloser(Node n) {
    if (n == null || !n.getName().equals(".close")) return false;
    String text = n.getText();
    return text.equals(")") || text.equals("]") || text.equals("}");
  }

  public static boolean isReaderConditionalOpener(Node n) {
    String text = n.getText();
    return text.equals("#?(") || text.equals("#?@(");
  }

  public static void stackPush(List list, Object item) {
    list.add(item);
  }

  public static Object stackPop(List list) {
    if (list.isEmpty()) return null;
    return list.remove(list.size() - 1);
  }

  public static boolean isTokenNode(Node n) {
    return n.getName().equals("token");
  }

  public static boolean nodeContainsText(Node n) {
    return n != null && isString(n.getText()) && !n.getText().isEmpty();
  }

  public static boolean isCommentNode(Node n) {
    return n.getName().equals("comment");
  }

  public static boolean isReaderCommentNode(Node n) {
    return n.getName().equals("discard");
  }

  public static Node getLastChildNodeWithText(Node n) {
    Node lastNode = null;
    if (n.getChildren() != null) {
      for (Node child : n.getChildren()) {
        if (nodeContainsText(child)) {
          lastNode = child;
        }
        Node possibleLast = getLastChildNodeWithText(child);
        if (possibleLast != null) {
          lastNode = possibleLast;
        }
      }
    }
    return lastNode;
  }

  public static Node findNextNonWhitespaceNode(List<Node> nodes, int startIdx) {
    for (int i = startIdx; i < nodes.size(); i++) {
      Node n = nodes.get(i);
      if (!isWhitespaceNode(n)) {
        return n;
      }
    }
    return null;
  }

  public static boolean isWhitespaceNode(Node n) {
    return n != null
        && (n.getName().equals("whitespace") || n.getName().equals("whitespace:newline"));
  }

  public static Node findNextNodeWithText(List<Node> nodes, int startIdx) {
    for (int i = startIdx; i < nodes.size(); i++) {
      Node n = nodes.get(i);
      if (isString(n.getText()) && !n.getText().isEmpty()) {
        return n;
      }
    }
    return null;
  }

  public static boolean isKeywordNode(Node n) {
    return n != null && isString(n.getText()) && n.getText().startsWith(":");
  }

  public static boolean isStringNode(Node n) {
    return n != null
        && n.getName().equals("string")
        && n.getChildren() != null
        && n.getChildren().size() == 3
        && n.getChildren().get(1).getName().equals(".body");
  }

  public static String getTextFromStringNode(Node n) {
    return n.getChildren().get(1).getText();
  }

  public static boolean isExcludeKeyword(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":exclude");
  }

  public static String getTextFromRootNode(Node node) {
    StringBuilder text = new StringBuilder();
    appendNodeText(node, text);
    return text.toString();
  }

  private static void appendNodeText(Node node, StringBuilder text) {
    if (isString(node.getText())) {
      text.append(node.getText());
    }
    if (node.getChildren() != null) {
      for (Node child : node.getChildren()) {
        appendNodeText(child, text);
      }
    }
  }

  public static boolean isOnlyKeyword(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":only");
  }

  public static boolean isRenameKeyword(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":rename");
  }

  public static boolean isAsKeyword(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":as");
  }

  public static boolean isAsAliasKeyword(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":as-alias");
  }

  public static boolean isIncludeMacrosNode(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":include-macros");
  }

  public static boolean isBooleanNode(Node n) {
    return n != null
        && isString(n.getText())
        && (n.getText().equals("true") || n.getText().equals("false"));
  }

  public static boolean isReferKeyword(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":refer");
  }

  public static boolean isDefaultKeyword(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":default");
  }

  public static boolean isReferMacrosKeyword(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":refer-macros");
  }

  public static Node findNextTokenInsideRequireForm(List<Node> nodes, int startIdx) {
    for (int i = startIdx; i < nodes.size(); i++) {
      Node node = nodes.get(i);
      if (isParenCloser(node)) {
        return null;
      } else if (isTokenNode(node) && !node.getText().isEmpty()) {
        return node;
      }
    }
    return null;
  }

  public static boolean isAllNode(Node n) {
    return n != null && isString(n.getText()) && n.getText().equals(":all");
  }

  public static Map<String, Object> parseJavaPackageWithClass(String s) {
    String[] chunks = s.split("\\.");
    String lastItem = chunks[chunks.length - 1];
    Map<String, Object> result = new HashMap<>();

    if (Character.isUpperCase(lastItem.charAt(0))) {
      StringBuilder pkg = new StringBuilder();
      for (int i = 0; i < chunks.length - 1; i++) {
        if (i > 0) pkg.append(".");
        pkg.append(chunks[i]);
      }
      result.put("package", pkg.toString());
      result.put("className", lastItem);
    } else {
      result.put("package", s);
      result.put("className", null);
    }

    return result;
  }

  public static boolean isGenClassKeyword(Node n) {
    if (n == null || !isString(n.getText())) return false;
    return GenClassKeys.contains(n.getText().substring(1)); // remove leading :
  }

  public static boolean isGenClassNameKey(String keyTxt) {
    return keyTxt.equals("name")
        || keyTxt.equals("extends")
        || keyTxt.equals("init")
        || keyTxt.equals("post-init")
        || keyTxt.equals("factory")
        || keyTxt.equals("state")
        || keyTxt.equals("impl-ns");
  }

  public static boolean isGenClassBooleanKey(String keyTxt) {
    return keyTxt.equals("main") || keyTxt.equals("load-impl-ns");
  }

  private static boolean isString(Object s) {
    return s instanceof String;
  }

  public static <T> T arrayLast(List<T> list) {
    if (list == null || list.isEmpty()) return null;
    return list.get(list.size() - 1);
  }

  public static List<Node> flattenTree(Node tree) {
    List<Node> nodes = new ArrayList<>();
    appendToFlatList(tree, nodes);
    return nodes;
  }

  private static void appendToFlatList(Node node, List<Node> flatList) {
    flatList.add(node);
    if (node.getChildren() != null) {
      for (Node child : node.getChildren()) {
        appendToFlatList(child, flatList);
      }
    }
  }

  // TODO: we should use an immutable data structure library for this instead
  @SuppressWarnings("unchecked")
  public static boolean deepEquals(Object obj1, Object obj2) {
    if (obj1 == null && obj2 == null) {
      return true;
    }
    if (obj1 == null || obj2 == null) {
      return false;
    }

    // Compare primitives and strings
    if (obj1 instanceof String || obj1 instanceof Number || obj1 instanceof Boolean) {
      return obj1.equals(obj2);
    }

    // Compare lists/arrays
    if (obj1 instanceof List && obj2 instanceof List) {
      List<Object> list1 = (List<Object>) obj1;
      List<Object> list2 = (List<Object>) obj2;

      if (list1.size() != list2.size()) {
        return false;
      }

      for (int i = 0; i < list1.size(); i++) {
        if (!deepEquals(list1.get(i), list2.get(i))) {
          return false;
        }
      }
      return true;
    }

    // Compare maps
    if (obj1 instanceof Map && obj2 instanceof Map) {
      Map<String, Object> map1 = (Map<String, Object>) obj1;
      Map<String, Object> map2 = (Map<String, Object>) obj2;

      if (map1.size() != map2.size()) {
        return false;
      }

      for (Map.Entry<String, Object> entry : map1.entrySet()) {
        String key = entry.getKey();
        if (!map2.containsKey(key)) {
          return false;
        }
        if (!deepEquals(entry.getValue(), map2.get(key))) {
          return false;
        }
      }
      return true;
    }

    return false;
  }
}
