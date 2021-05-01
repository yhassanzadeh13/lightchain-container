package underlay.requests;

public class SearchNameRequest extends GenericRequest{
    public int numID;
    public String searchTarget;
    public int level;
    public int direction;

    public SearchNameRequest(int numID, String searchTarget, int level, int direction) {
        super(RequestType.SearchNameRequest);
        this.numID = numID;
        this.searchTarget = searchTarget;
        this.level = level;
        this.direction = direction;
    }
}