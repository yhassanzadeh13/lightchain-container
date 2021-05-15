package underlay.requests.skipgraph;

import skipGraph.NodeInfo;
import underlay.requests.RequestType;

public class SetLeftNodeRequest extends GenericSkipGraphRequest {
  public int num;
  public int level;
  public NodeInfo newNode;
  public skipGraph.NodeInfo oldNode;

  public SetLeftNodeRequest(int num, int level, NodeInfo newNode, NodeInfo oldNode) {
    super(RequestType.SetLeftNodeRequest);
    this.num = num;
    this.level = level;
    this.newNode = newNode;
    this.oldNode = oldNode;
  }
}
