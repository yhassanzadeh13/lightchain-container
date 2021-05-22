package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class GetLeftNodeRequest extends GenericSkipGraphRequest {
  public final int level;
  public final int num;

  public GetLeftNodeRequest(int level, int num) {
    super(RequestType.GetLeftNodeRequest);
    this.level = level;
    this.num = num;
  }
}
