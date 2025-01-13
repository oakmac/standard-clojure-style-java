package com.oakmac.standardclojurestyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ParseNs parses the Clojure/ClojureScript ns form and returns a structured data format
 * that can be used for analysis and reformatting.
 */
public class ParseNs {

  private static int prefixListIdCounter = 0;

  private static int createPrefixListId() {
    return ++prefixListIdCounter;
  }

  /**
   * Parses namespace information from a flat array of Nodes.
   * Returns a data structure of the ns form that can be used to "print from scratch"
   */

  public static Map<String, Object> parseNs(List<Node> nodesArr) {
    int idx = 0;
    int numNodes = nodesArr.size();
    Map<String, Object> result = new HashMap<>();
    result.put("nsSymbol", null);

    boolean continueParsingNsForm = true;
    int nsFormEndsLineIdx = -1;
    int parenNestingDepth = 0;
    int lineNo = 0;
    List<Node> parenStack = new ArrayList<>();
    boolean insideNsForm = false;
    boolean insideReferClojureForm = false;
    int referClojureParenNestingDepth = -1;
    boolean insideRequireForm = false;
    int requireFormParenNestingDepth = -1;
    int requireFormLineNo = -1;
    boolean insideImportForm = false;
    int importFormLineNo = -1;
    boolean nextTextNodeIsNsSymbol = false;
    boolean insideImportPackageList = false;
    boolean collectReferClojureExcludeSymbols = false;
    boolean collectReferClojureOnlySymbols = false;
    boolean collectReferClojureRenameSymbols = false;
    boolean collectRequireExcludeSymbols = false;
    int requireExcludeSymbolParenDepth = -1;
    List<String> renamesTmp = new ArrayList<>();
    Node importPackageListFirstToken = null;
    int nsNodeIdx = -1;
    int nsSymbolIdx = -1;
    boolean beyondNsMetadata = false;
    boolean insideNsMetadataHashMap = false;
    boolean insideNsMetadataShorthand = false;
    boolean nextTokenNodeIsMetadataTrueKey = false;
    boolean nextTextNodeIsMetadataKey = false;
    int metadataValueNodeId = -1;
    String tmpMetadataKey = "";
    int referClojureNodeIdx = -1;
    int requireNodeIdx = -1;
    int referIdx = -1;
    int referParenNestingDepth = -1;
    int importNodeIdx = -1;
    int importNodeParenNestingDepth = -1;
    int activeRequireIdx = -1;
    int requireSymbolIdx = -1;
    boolean nextTokenIsAsSymbol = false;
    List<String> singleLineComments = new ArrayList<>();
    String activeImportPackageName = null;
    boolean prevNodeIsNewline = false;
    int lineOfLastCommentRecording = -1;
    boolean insidePrefixList = false;
    String prefixListPrefix = null;
    int prefixListLineNo = -1;
    Map<String, Object> prefixListComments = new HashMap<>();
    String currentPrefixListId = null;
    boolean insideReaderConditional = false;
    String currentReaderConditionalPlatform = null;
    int readerConditionalParenNestingDepth = -1;
    boolean insideRequireList = false;
    int requireListParenNestingDepth = -1;
    int referMacrosIdx = -1;
    int referMacrosParenNestingDepth = -1;
    boolean insideIncludeMacros = false;
    int activeRequireMacrosIdx = -1;
    boolean insideRequireMacrosForm = false;
    int requireMacrosNodeIdx = -1;
    int requireMacrosLineNo = -1;
    int requireMacrosParenNestingDepth = -1;
    int requireMacrosReferNodeIdx = -1;
    int requireMacrosAsNodeIdx = -1;
    int requireMacrosRenameIdx = -1;
    int genClassNodeIdx = -1;
    boolean insideGenClass = false;
    int genClassLineNo = -1;
    int genClassToggle = 0;
    String genClassKeyStr = null;
    int genClassValueLineNo = -1;
    boolean insideReaderComment = false;
    int idOfLastNodeInsideReaderComment = -1;
    int requireRenameIdx = -1;
    int requireRenameParenNestingDepth = -1;
    int skipNodesUntilWeReachThisId = -1;
    String sectionToAttachEolCommentsTo = null;
    boolean nextTokenIsRequireDefaultSymbol = false;

    while (continueParsingNsForm) {
      Node node = nodesArr.get(idx);
      boolean currentNodeIsNewline = Utils.isNewlineNode(node);

      if (parenNestingDepth == 1 && Utils.isNsNode(node)) {
        insideNsForm = true;
        nextTextNodeIsNsSymbol = true;
        nsNodeIdx = idx;
      } else if (insideNsForm && Utils.isReferClojureNode(node)) {
        insideReferClojureForm = true;
        referClojureParenNestingDepth = parenNestingDepth;
        sectionToAttachEolCommentsTo = "refer-clojure";
        referClojureNodeIdx = idx;
        beyondNsMetadata = true;
      } else if (insideNsForm && Utils.isRequireNode(node)) {
        insideRequireForm = true;
        requireFormParenNestingDepth = parenNestingDepth;
        requireFormLineNo = lineNo;
        requireNodeIdx = idx;
        beyondNsMetadata = true;
        sectionToAttachEolCommentsTo = "require";
      } else if (insideNsForm && Utils.isImportNode(node)) {
        insideImportForm = true;
        importFormLineNo = lineNo;
        importNodeIdx = idx;
        importNodeParenNestingDepth = parenNestingDepth;
        beyondNsMetadata = true;
        sectionToAttachEolCommentsTo = "import";
      } else if (insideNsForm && Utils.isRequireMacrosKeyword(node)) {
        insideRequireMacrosForm = true;
        requireMacrosNodeIdx = idx;
        requireMacrosLineNo = lineNo;
        requireMacrosParenNestingDepth = parenNestingDepth;
        beyondNsMetadata = true;
        sectionToAttachEolCommentsTo = "require-macros";
      } else if (insideNsForm && Utils.isGenClassNode(node)) {
        insideGenClass = true;
        genClassNodeIdx = idx;
        beyondNsMetadata = true;
        sectionToAttachEolCommentsTo = "gen-class";
      }

      if (Utils.isParenOpener(node)) {
        parenNestingDepth++;
        Utils.stackPush(parenStack, node);
        if (insideNsForm && Utils.isReaderConditionalOpener(node)) {
          insideReaderConditional = true;
          currentReaderConditionalPlatform = null;
          readerConditionalParenNestingDepth = parenNestingDepth;
        } else if (insideRequireForm) {
          insideRequireList = true;
          requireListParenNestingDepth = parenNestingDepth;
        } else if (insideImportForm && parenNestingDepth > importNodeParenNestingDepth) {
          insideImportPackageList = true;
        }
      } else if (Utils.isParenCloser(node)) {
        parenNestingDepth--;
        Utils.stackPop(parenStack);

        // TODO: should these be "else if"s or just "if"s ?
        // I think maybe they should be "if"s
        if (insideImportPackageList) {
          insideImportPackageList = false;
          importPackageListFirstToken = null;
        } else if (insideRequireForm && parenNestingDepth < requireFormParenNestingDepth) {
          insideRequireForm = false;
        } else if (insideRequireList && parenNestingDepth < requireListParenNestingDepth) {
          insideRequireList = false;
          requireListParenNestingDepth = -1;
          requireRenameIdx = -1;
        } else if (insideReferClojureForm && parenNestingDepth < referClojureParenNestingDepth) {
          insideReferClojureForm = false;
          referClojureNodeIdx = -1;
        } else if (insideNsForm && parenNestingDepth == 0) {
          // We can assume there is only one ns form per file and exit the main
          // loop once we have finished parsing it.
          insideNsForm = false;
          nsFormEndsLineIdx = lineNo;
        }

        if (insideReferClojureForm && parenNestingDepth <= referClojureParenNestingDepth) {
          collectReferClojureExcludeSymbols = false;
          collectReferClojureOnlySymbols = false;
          collectReferClojureRenameSymbols = false;
        }

        if (referIdx > 0 && parenNestingDepth < referParenNestingDepth) {
          referIdx = -1;
          referParenNestingDepth = -1;
          nextTokenIsRequireDefaultSymbol = false;
        }
        if (insideRequireForm && requireSymbolIdx > 0) {
          requireSymbolIdx = -1;
        }
        if (insideRequireForm && insidePrefixList) {
          insidePrefixList = false;
          prefixListPrefix = null;
        }
        if (insideReaderConditional && parenNestingDepth == readerConditionalParenNestingDepth - 1) {
          insideReaderConditional = false;
          currentReaderConditionalPlatform = null;
          readerConditionalParenNestingDepth = -1;
        }
        if (idx > referMacrosIdx && parenNestingDepth <= referMacrosParenNestingDepth) {
          referMacrosIdx = -1;
          referMacrosParenNestingDepth = -1;
        }
        if (insideImportForm && parenNestingDepth < importNodeParenNestingDepth) {
          insideImportForm = false;
          importNodeIdx = -1;
          importNodeParenNestingDepth = -1;
        }
        if (insideRequireMacrosForm && parenNestingDepth < requireMacrosParenNestingDepth) {
          insideRequireMacrosForm = false;
          requireMacrosParenNestingDepth = -1;
          requireMacrosNodeIdx = -1;
          requireMacrosAsNodeIdx = -1;
        }
        if (collectRequireExcludeSymbols && parenNestingDepth < requireExcludeSymbolParenDepth) {
          collectRequireExcludeSymbols = false;
          requireExcludeSymbolParenDepth = -1;
        }
        if (insideRequireForm && parenNestingDepth < requireRenameParenNestingDepth) {
          requireRenameParenNestingDepth = -1;
        }

        requireMacrosReferNodeIdx = -1;
        requireMacrosRenameIdx = -1;
      }

      boolean isTokenNode2 = Utils.isTokenNode(node);
      boolean isTextNode = Utils.nodeContainsText(node);
      boolean isCommentNode2 = Utils.isCommentNode(node);
      boolean isReaderCommentNode2 = Utils.isReaderCommentNode(node);

      if (isReaderCommentNode2) {
        insideReaderComment = true;
        Node lastNodeOfReaderComment = Utils.getLastChildNodeWithText(node);
        idOfLastNodeInsideReaderComment = lastNodeOfReaderComment.getId();
      }

      if (skipNodesUntilWeReachThisId > 0) {
        if (node.getId() == skipNodesUntilWeReachThisId) {
          skipNodesUntilWeReachThisId = -1;
        }
        // collect ns metadata shorthand
      } else if (insideNsMetadataShorthand) {
        if (node.getName().equals(".marker") && node.getText().equals("^")) {
          nextTokenNodeIsMetadataTrueKey = true;
        } else if (nextTokenNodeIsMetadataTrueKey && isTokenNode2) {
          if (!result.containsKey("nsMetadata")) {
            result.put("nsMetadata", new ArrayList<Map<String, Object>>());
          }

          Map<String, Object> metadataObj = new HashMap<>();
          metadataObj.put("key", node.getText());
          metadataObj.put("value", "true");

          ((List<Map<String, Object>>) result.get("nsMetadata")).add(metadataObj);

          nextTokenNodeIsMetadataTrueKey = false;
          insideNsMetadataShorthand = false;
        }
        // collect ns metadata inside a hash map literal
      } else if (insideNsMetadataHashMap) {
        if (nextTextNodeIsMetadataKey && node.getName().equals(".close") && node.getText().equals("}")) {
          insideNsMetadataHashMap = false;
        } else if (!nextTextNodeIsMetadataKey && node.getName().equals(".open") && node.getText().equals("{")) {
          nextTextNodeIsMetadataKey = true;
        } else if (nextTextNodeIsMetadataKey && isTokenNode2) {
          if (!result.containsKey("nsMetadata")) {
            result.put("nsMetadata", new ArrayList<Map<String, Object>>());
          }

          tmpMetadataKey = node.getText();
          nextTextNodeIsMetadataKey = false;

          // the next node should be a whitespace node, then collect the value for this key
          Node nextNonWhitespaceNode = Utils.findNextNonWhitespaceNode(nodesArr, idx + 1);
          metadataValueNodeId = nextNonWhitespaceNode.getId();
        } else if (node.getId() == metadataValueNodeId) {
          Map<String, Object> metadataObj = new HashMap<>();
          metadataObj.put("key", tmpMetadataKey);
          metadataObj.put("value", Utils.getTextFromRootNode(node));

          ((List<Map<String, Object>>) result.get("nsMetadata")).add(metadataObj);

          tmpMetadataKey = "";
          nextTextNodeIsMetadataKey = true;
          metadataValueNodeId = -1;

          // skip any forward nodes that we have just collected as text
          if (node.getChildren() != null) {
            Node lastChildNode = Utils.arrayLast(node.getChildren());
            skipNodesUntilWeReachThisId = lastChildNode.getId();
          }
        }
        // collect ns metadata before we hit the nsSymbol
      } else if (!insideNsMetadataHashMap && !insideNsMetadataShorthand && insideNsForm && nsSymbolIdx < 0 && node.getName().equals("meta")) {
        Node markerNode = Utils.findNextNodeWithText(nodesArr, idx + 1);

        // NOTE: this should always be true
        if (markerNode.getText().equals("^")) {
          Node nodeAfterMarker = Utils.findNextNodeWithText(nodesArr, idx + 2);

          if (nodeAfterMarker != null && nodeAfterMarker.getText().equals("{")) {
            insideNsMetadataHashMap = true;
          } else if (nodeAfterMarker != null && Utils.isTokenNode(nodeAfterMarker)) {
            insideNsMetadataShorthand = true;
          }
        }
        // collect metadata hash map after the ns symbol
      } else if (insideNsForm && idx > nsNodeIdx && parenNestingDepth >= 1 && !beyondNsMetadata && !insideReaderComment && !insideNsMetadataShorthand && !insideNsMetadataHashMap && node.getName().equals(".open") && node.getText().equals("{")) {
        insideNsMetadataHashMap = true;
        nextTextNodeIsMetadataKey = true;
        // collect the ns symbol
      } else if (idx > nsNodeIdx && nextTextNodeIsNsSymbol && isTokenNode2 && isTextNode) {
        result.put("nsSymbol", node.getText());
        nsSymbolIdx = idx;
        nextTextNodeIsNsSymbol = false;
        // collect reader conditional platform keyword
      } else if (insideReaderConditional && parenNestingDepth == readerConditionalParenNestingDepth && Utils.isKeywordNode(node)) {
        currentReaderConditionalPlatform = node.getText();
        // collect single-line comments
      } else if (insideNsForm && idx > nsNodeIdx && prevNodeIsNewline && isCommentNode2) {
        Utils.stackPush(singleLineComments, node.getText());
        // collect reader macro comment line(s)
      } else if (insideNsForm && idx > nsNodeIdx && prevNodeIsNewline && isReaderCommentNode2) {
        Utils.stackPush(singleLineComments, Utils.getTextFromRootNode(node));
        // collect comments at the end of a line
      } else if (idx > nsNodeIdx && !prevNodeIsNewline && (isCommentNode2 || isReaderCommentNode2)) {
        String commentAtEndOfLine = null;
        if (isCommentNode2) {
          commentAtEndOfLine = node.getText();
        } else {
          commentAtEndOfLine = Utils.getTextFromRootNode(node);
        }

        if (prefixListLineNo == lineNo) {
          if (!prefixListComments.containsKey(currentPrefixListId)) {
            prefixListComments.put(currentPrefixListId, new HashMap<String, Object>());
          }
          ((Map<String, Object>) prefixListComments.get(currentPrefixListId)).put("commentAfter", commentAtEndOfLine);
          lineOfLastCommentRecording = lineNo;
        } else if (requireFormLineNo == lineNo && activeRequireIdx < 0) {
          result.put("requireCommentAfter", commentAtEndOfLine);
          lineOfLastCommentRecording = lineNo;
        } else if (requireFormLineNo == lineNo && activeRequireIdx >= 0) {
          ((Map<String, Object>) ((List<Map<String, Object>>) result.get("requires")).get(activeRequireIdx)).put("commentAfter", commentAtEndOfLine);
          lineOfLastCommentRecording = lineNo;
        } else if (sectionToAttachEolCommentsTo == "refer-clojure" && result.containsKey("referClojure")) {
          result.put("referClojureCommentAfter", commentAtEndOfLine);
          lineOfLastCommentRecording = lineNo;
        } else if (importFormLineNo == lineNo && !result.containsKey("importsObj")) {
          result.put("importCommentAfter", commentAtEndOfLine);
          lineOfLastCommentRecording = lineNo;
        } else if (importFormLineNo == lineNo) {
          ((Map<String, Object>) result.get("importsObj")).get(activeImportPackageName).put("commentAfter", commentAtEndOfLine);
          lineOfLastCommentRecording = lineNo;
        } else if (requireMacrosLineNo == lineNo) {
          ((Map<String, Object>) ((List<Map<String, Object>>) result.get("requireMacros")).get(activeRequireMacrosIdx)).put("commentAfter", commentAtEndOfLine);
          lineOfLastCommentRecording = lineNo;
        } else if (genClassLineNo == lineNo) {
          ((Map<String, Object>) result.get("genClass")).put("commentAfter", commentAtEndOfLine);
          lineOfLastCommentRecording = lineNo;
        } else if (genClassValueLineNo == lineNo) {
          ((Map<String, Object>) ((Map<String, Object>) result.get("genClass")).get(genClassKeyStr)).put("commentAfter", commentAtEndOfLine);
          lineOfLastCommentRecording = lineNo;
        }

        if (!insideNsForm && lineNo == lineOfLastCommentRecording) {
          result.put("commentOutsideNsForm", commentAtEndOfLine);
        }
        // discard nodes that are inside a reader comment
      } else if (insideReaderComment) {
        if (node.getId() == idOfLastNodeInsideReaderComment) {
          insideReaderComment = false;
          idOfLastNodeInsideReaderComment = -1;
        }
        // attach comments to the :require form
      } else if (insideRequireForm && idx == requireNodeIdx && singleLineComments.size() > 0) {
        result.put("requireCommentsAbove", singleLineComments);
        singleLineComments = new ArrayList<>();
        // attach comments to the :import form
      } else if (insideImportForm && idx == importNodeIdx && singleLineComments.size() > 0) {
        result.put("importCommentsAbove", singleLineComments);
        singleLineComments = new ArrayList<>();
        // attach comments to the :refer-clojure form
      } else if (insideReferClojureForm && idx == referClojureNodeIdx && singleLineComments.size() > 0) {
        result.put("referClojureCommentsAbove", singleLineComments);
        singleLineComments = new ArrayList<>();
        // collect the docstring
      } else if (insideNsForm && idx > nsNodeIdx && parenNestingDepth == 1 && !beyondNsMetadata && !insideNsMetadataShorthand && !insideNsMetadataHashMap && Utils.isStringNode(node)) {
        result.put("docstring", Utils.getTextFromStringNode(node));
        // collect :refer-clojure :exclude
      } else if (insideReferClojureForm && idx > referClojureNodeIdx && Utils.isExcludeKeyword(node)) {
        if (!result.containsKey("referClojure")) {
          result.put("referClojure", new HashMap<String, Object>());
        }
        if (!((Map<String, Object>) result.get("referClojure")).containsKey("exclude")) {
          ((Map<String, Object>) result.get("referClojure")).put("exclude", new ArrayList<>());
        }
        collectReferClojureExcludeSymbols = true;
        // collect :refer-clojure :exclude symbols
      } else if (idx > referClojureNodeIdx + 1 && collectReferClojureExcludeSymbols && parenNestingDepth >= 3 && isTokenNode2 && isTextNode && result.containsKey("referClojure") && ((Map<String, Object>) result.get("referClojure")).containsKey("exclude")) {
        Map<String, Object> symbolObj = new HashMap<>();
        symbolObj.put("symbol", node.getText());

        if (insideReaderConditional && currentReaderConditionalPlatform != null) {
          symbolObj.put("platform", currentReaderConditionalPlatform);
        }

        ((List<Map<String, Object>>) ((Map<String, Object>) result.get("referClojure")).get("exclude")).add(symbolObj);
      }

      // FIXME: continue porting this function here

      // increment for next iteration
      idx++;

      // exit condition checks
      if (idx > numNodes) {
        continueParsingNsForm = false;
      } else if (nsNodeIdx > 0 && !insideNsForm && lineNo >= nsFormEndsLineIdx + 2) {
        continueParsingNsForm = false;
      }
    }

    return sortNsResult(result, prefixListComments);
  }

  /**
   * Search for a #_ :standard-clj/ignore-file or similar forms
   * stopping when we reach the first (ns) form
   */
  public static boolean lookForIgnoreFile(List<Node> nodesArr) {
    // TODO: implement lookForIgnoreFile

    return false;
  }

  /**
   * Takes the parse result structure and returns a sorted version of it
   */
  private static Map<String, Object> sortNsResult(Map<String, Object> result, Map<String, Object> prefixListComments) {
    // TODO: implement sortNsResult

    return result;
  }
}
