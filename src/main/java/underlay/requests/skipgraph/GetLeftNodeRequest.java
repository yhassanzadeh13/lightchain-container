package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class GetLeftNodeRequest extends GenericSkipGraphRequest {
  public int level;
  public int num;

  public GetLeftNodeRequest(int level, int num) {
    super(RequestType.GetLeftNodeRequest);
    this.level = level;
    this.num = num;
  }
}
