package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class InsertSearchRequest extends GenericSkipGraphRequest {
  public final int level;
  public final int direction;
  public final int num;
  public final String target;

  public InsertSearchRequest(int level, int direction, int num, String target) {
    super(RequestType.InsertSearchRequest);
    this.level = level;
    this.direction = direction;
    this.num = num;
    this.target = target;
  }
}
