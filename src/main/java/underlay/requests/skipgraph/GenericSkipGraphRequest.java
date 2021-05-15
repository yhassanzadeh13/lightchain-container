package underlay.requests.skipgraph;

import underlay.requests.GenericRequest;
import underlay.requests.RequestType;

/** The base class of requests. */
public class GenericSkipGraphRequest extends GenericRequest {
  public GenericSkipGraphRequest(RequestType type) {
    super(type);
  }
}
