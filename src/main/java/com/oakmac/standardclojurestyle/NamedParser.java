package com.oakmac.standardclojurestyle;

/**
 * Parser that wraps another parser and assigns a name to its result.
 */
class NamedParser implements IParser {
    private final Object parser;
    private final String name;

    public NamedParser(Object parser, String name) {
        this.parser = parser;
        this.name = name;
    }

    @Override
    public Node parse(String input, int startIdx) {
        IParser actualParser = ParserDefinitions.getParser(parser);
        Node node = actualParser.parse(input, startIdx);

        if (node == null) {
            return null;
        } 
        
        // If node doesn't have a name, assign our name to it
        if (node.getName() == null || node.getName().isEmpty()) {
            node = new Node(name, node.getStartIdx(), node.getEndIdx(),
                          node.getChildren(), node.getNumChildren());
            return node;
        }
        
        // If node has a name, wrap it in a parent node with our name
        return new Node(name, node.getStartIdx(), node.getEndIdx(),
                       new Node[]{node}, 1);
    }
}