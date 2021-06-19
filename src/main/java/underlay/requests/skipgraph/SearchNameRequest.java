package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class SearchNameRequest extends GenericSkipGraphRequest {
  public final int numID;
  public final String searchTarget;
  public final int level;
  public final int direction;

  public SearchNameRequest(int numID, String searchTarget, int level, int direction) {
    super(RequestType.SearchNameRequest);
    this.numID = numID;
    this.searchTarget = searchTarget;
    this.level = level;
    this.direction = direction;
  }
}
