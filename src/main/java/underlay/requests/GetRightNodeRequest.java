package underlay.requests;

public class GetRightNodeRequest extends GenericRequest{
    public int level;
    public int num;

    public GetRightNodeRequest(int level, int num) {
        super(RequestType.GetRightNodeRequest);
        this.level = level;
        this.num = num;
    }
}
