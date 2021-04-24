package underlay.requests;

public class GetLeftNodeRequest extends GenericRequest{
    public int level;
    public int num;

    public GetLeftNodeRequest(int level, int num) {
        super(RequestType.GetLeftNodeRequest);
        this.level = level;
        this.num = num;
    }
}
