package underlay.requests;

public abstract class GenericRequest {
    public RequestType type;
    public String senderAddress;

    public GenericRequest(RequestType type) {
        this.type = type;
    }
}

