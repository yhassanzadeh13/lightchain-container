package underlay.requests;

public class Request implements {

    public final RequestType type;
    public String senderAddress;

    public Request(RequestType type) {
        this.type = type;
    }
}

