package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class GetRightNodeRequest extends GenericSkipGraphRequest {
  public final int level;
  public final int num;

  public GetRightNodeRequest(int level, int num) {
    super(RequestType.GetRightNodeRequest);
    this.level = level;
    this.num = num;
  }
}
