package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class SearchByNumIDRequest extends GenericSkipGraphRequest {
  public final int num;

  public SearchByNumIDRequest(int num) {
    super(RequestType.SearchByNumIDRequest);
    this.num = num;
  }
}
