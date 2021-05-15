package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class GetLeftNumIDRequest extends GenericSkipGraphRequest {
  public int level;
  public int num;

  public GetLeftNumIDRequest(int level, int num) {
    super(RequestType.GetLeftNumIDRequest);
    this.level = level;
    this.num = num;
  }
}
