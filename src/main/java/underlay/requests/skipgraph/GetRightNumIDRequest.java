package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class GetRightNumIDRequest extends GenericSkipGraphRequest {
  public int level;
  public int num;

  public GetRightNumIDRequest(int level, int num) {
    super(RequestType.GetRightNumIDRequest);
    this.level = level;
    this.num = num;
  }
}
