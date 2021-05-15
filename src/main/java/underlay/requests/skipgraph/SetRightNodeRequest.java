package underlay.requests.skipgraph;

import skipGraph.NodeInfo;
import underlay.requests.RequestType;

public class SetRightNodeRequest extends GenericSkipGraphRequest {
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
