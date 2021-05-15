package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class InsertSearchRequest extends GenericSkipGraphRequest {
  public int level;
  public int direction;
  public int num;
  public String target;

  public InsertSearchRequest(int level, int direction, int num, String target) {
    super(RequestType.InsertSearchRequest);
    this.level = level;
    this.direction = direction;
    this.num = num;
    this.target = target;
  }
}
