package com.oakmac.standardclojurestyle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class NodeTest {
  @Test
  public void testNodeConstruction() {
    Map<String, Object> opts = new HashMap<>();
    List<Node> children = new ArrayList<>();
    opts.put("children", children);
    opts.put("endIdx", 5);
    opts.put("name", "test");
    opts.put("startIdx", 0);
    opts.put("text", "hello");

    Node node = new Node(opts);

    assertEquals(children, node.getChildren(), "Should set children");
    assertEquals(5, node.getEndIdx(), "Should set endIdx");
    assertTrue(node.getId() > 0, "Should have id");
    assertEquals("test", node.getName(), "Should set name");
    assertEquals(0, node.getStartIdx(), "Should set startIdx");
    assertEquals("hello", node.getText(), "Should set text");
  }

  @Test
  public void testNodeConstructionWithMissingFields() {
    Map<String, Object> opts = new HashMap<>();
    Node node = new Node(opts);

    assertNull(node.getChildren(), "Should handle null children");
    assertEquals(0, node.getEndIdx(), "Should default endIdx to 0");
    assertTrue(node.getId() > 0, "Should have id");
    assertNull(node.getName(), "Should handle null name");
    assertEquals(0, node.getStartIdx(), "Should default startIdx to 0");
    assertNull(node.getText(), "Should handle null text");
  }

  @Test
  public void testNodeAttributes() {
    Map<String, Object> opts = new HashMap<>();
    Node node = new Node(opts);

    assertNull(node.getAttribute("test"), "Should not have attribute initially");

    node.setAttribute("test", "value");
    assertEquals("value", node.getAttribute("test"), "Should set attribute");

    node.setAttribute("test", "new-value");
    assertEquals("new-value", node.getAttribute("test"), "Should update attribute");

    node.setAttribute("number", 42);
    assertEquals(42, node.getAttribute("number"), "Should handle number attributes");
  }

  @Test
  public void testNodeToString() {
    Map<String, Object> opts = new HashMap<>();
    List<Node> children = new ArrayList<>();
    opts.put("children", children);
    opts.put("endIdx", 5);
    opts.put("name", "test");
    opts.put("startIdx", 0);
    opts.put("text", "hello");

    Node node = new Node(opts);
    String str = node.toString();

    assertTrue(str.contains("name='test'"), "toString should contain name");
    assertTrue(str.contains("startIdx=0"), "toString should contain startIdx");
    assertTrue(str.contains("endIdx=5"), "toString should contain endIdx");
    assertTrue(str.contains("text='hello'"), "toString should contain text");
    assertTrue(str.contains("id="), "toString should contain id");
    assertTrue(str.contains("children=0"), "toString should contain children size");
  }

  @Test
  public void testChildrenHandling() {
    // Create parent node
    Map<String, Object> parentOpts = new HashMap<>();
    List<Node> children = new ArrayList<>();
    parentOpts.put("children", children);
    parentOpts.put("name", "parent");
    Node parent = new Node(parentOpts);

    // Create child node
    Map<String, Object> childOpts = new HashMap<>();
    childOpts.put("name", "child");
    Node child = new Node(childOpts);

    // Add child to parent's children list
    children.add(child);

    assertEquals(1, parent.getChildren().size(), "Parent should have one child");
    assertEquals(child, parent.getChildren().get(0), "Child should be in parent's children");
  }
}
