package underlay.requests;

public class GetRightNumIDRequest extends GenericRequest {
  public int level;
  public int num;

  public GetRightNumIDRequest(int level, int num) {
    super(RequestType.GetRightNumIDRequest);
    this.level = level;
    this.num = num;
  }
}
