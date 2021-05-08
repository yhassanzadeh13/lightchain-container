package underlay.requests;

import skipGraph.NodeInfo;

import java.util.List;

public class SearchNumIDRequest extends GenericRequest {
  public int numID;
  public int searchTarget;
  public int level;
  public List<NodeInfo> lst;

  public SearchNumIDRequest(int numID, int searchTarget, int level, List<NodeInfo> lst) {
    super(RequestType.SearchNumIDRequest);
    this.numID = numID;
    this.searchTarget = searchTarget;
    this.level = level;
    this.lst = lst;
  }
}
