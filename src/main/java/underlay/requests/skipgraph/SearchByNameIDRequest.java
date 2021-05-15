package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class SearchByNameIDRequest extends GenericSkipGraphRequest {
  public String targetString;

  public SearchByNameIDRequest(String targetString) {
    super(RequestType.SearchByNameIDRequest);
    this.targetString = targetString;
  }
}
