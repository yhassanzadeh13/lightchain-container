package underlay.requests;

/** The base class of requests. */
public abstract class GenericRequest {
  public RequestType type;

  public GenericRequest(RequestType type) {
    this.type = type;
  }
}
