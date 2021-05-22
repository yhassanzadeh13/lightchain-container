package underlay.requests.skipgraph;

import skipGraph.NodeInfo;
import underlay.requests.RequestType;

import java.util.List;

public class SearchNumIDRequest extends GenericSkipGraphRequest {
  public final int numID;
  public final int searchTarget;
  public final int level;
  public final List<NodeInfo> lst;

  public SearchNumIDRequest(int numID, int searchTarget, int level, List<NodeInfo> lst) {
    super(RequestType.SearchNumIDRequest);
    this.numID = numID;
    this.searchTarget = searchTarget;
    this.level = level;
    this.lst = lst;
  }
}
