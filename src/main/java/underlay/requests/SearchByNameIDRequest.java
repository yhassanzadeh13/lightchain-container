package underlay.requests;

public class SearchByNameIDRequest extends GenericRequest {
  public String targetString;

  public SearchByNameIDRequest(String targetString) {
    super(RequestType.SearchByNameIDRequest);
    this.targetString = targetString;
  }
}
