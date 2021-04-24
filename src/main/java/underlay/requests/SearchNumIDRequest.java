package underlay.requests;

import skipGraph.NodeInfo;

import java.util.List;

public class SearchNumID extends GenericRequest{
    public int numID;
    public int searchTarget;
    public int level;
    public List<NodeInfo> lst;

    public SearchNumID(int numID, int searchTarget, int level, List<NodeInfo> lst) {
        super(RequestType.SearchNumID);
        this.numID = numID;
        this.searchTarget = searchTarget;
        this.level = level;
        this.lst = lst;
    }
}
