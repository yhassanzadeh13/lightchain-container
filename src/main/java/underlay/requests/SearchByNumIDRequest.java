package underlay.requests;

public class SearchByNumIDRequest extends GenericRequest {
  public int num;

  public SearchByNumIDRequest(int num) {
    super(RequestType.SearchByNumIDRequest);
    this.num = num;
  }
}
