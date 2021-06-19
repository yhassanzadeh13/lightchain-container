package underlay.requests;

import java.io.Serializable;

// javarmi requires serializability
public class GenericRequest implements Serializable {
  public RequestType type;

  public GenericRequest(RequestType type) {
    this.type = type;
  }
}
