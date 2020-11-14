package localnet;

import skipGraph.NodeInfo;

import java.util.ArrayList;
import java.util.List;

public class LocalLookupTable {

  int levels;

  NodeInfo[] left;
  NodeInfo[] right;
  public LocalLookupTable(int levels) {
    this.levels = levels;
    this.left = new NodeInfo[levels + 1];
    this.right = new NodeInfo[levels + 1];
  }

  public void updateLeft(NodeInfo node, int level) {
    left[level] = node;
  }

  public void updateRight(NodeInfo node, int level) {
    right[level] = node;
  }

  public NodeInfo getLeft(int level) {
    return left[level];
  }

  public NodeInfo getRight(int level) {
    return right[level];
  }

}