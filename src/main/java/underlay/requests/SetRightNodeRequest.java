package underlay.requests;

import skipGraph.NodeInfo;

public class SetRightNodeRequest extends GenericRequest {
  public int num;
  public int level;
  public NodeInfo newNode;
  public skipGraph.NodeInfo oldNode;

  public SetRightNodeRequest(int num, int level, NodeInfo newNode, NodeInfo oldNode) {
    super(RequestType.SetRightNodeRequest);
    this.num = num;
    this.level = level;
    this.newNode = newNode;
    this.oldNode = oldNode;
  }
}
