package underlay.requests;

public class GetNodeRequest extends GenericRequest {
    public int num;

    public GetNodeRequest(int num) {
        super(RequestType.GetNodeRequest);
        this.num = num;
    }
}
