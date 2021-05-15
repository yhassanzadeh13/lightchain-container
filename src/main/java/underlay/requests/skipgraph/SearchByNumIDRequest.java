package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class SearchByNumIDRequest extends GenericSkipGraphRequest {
  public int num;

  public SearchByNumIDRequest(int num) {
    super(RequestType.SearchByNumIDRequest);
    this.num = num;
  }
}
