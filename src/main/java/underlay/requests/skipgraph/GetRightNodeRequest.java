package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class GetRightNodeRequest extends GenericSkipGraphRequest {
  public int level;
  public int num;

  public GetRightNodeRequest(int level, int num) {
    super(RequestType.GetRightNodeRequest);
    this.level = level;
    this.num = num;
  }
}
