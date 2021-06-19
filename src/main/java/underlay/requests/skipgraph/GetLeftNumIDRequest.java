package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class GetLeftNumIDRequest extends GenericSkipGraphRequest {
  public final int level;
  public final int num;

  public GetLeftNumIDRequest(int level, int num) {
    super(RequestType.GetLeftNumIDRequest);
    this.level = level;
    this.num = num;
  }
}
