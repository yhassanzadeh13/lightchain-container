package underlay.requests;

public class InsertSearchRequest extends GenericRequest {
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
