package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class GetRightNumIDRequest extends GenericSkipGraphRequest {
  public final int level;
  public final int num;

  public GetRightNumIDRequest(int level, int num) {
    super(RequestType.GetRightNumIDRequest);
    this.level = level;
    this.num = num;
  }
}
