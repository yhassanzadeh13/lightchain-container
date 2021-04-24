package underlay.requests;

public class GetLeftNumIDRequest extends GenericRequest{
    public int level;
    public int num;

    public GetLeftNumIDRequest(int level, int num) {
        super(RequestType.GetLeftNumIDRequest);
        this.level = level;
        this.num = num;
    }
}
