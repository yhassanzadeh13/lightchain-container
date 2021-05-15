package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class PingRequest extends GenericSkipGraphRequest {
  public PingRequest() {
    super(RequestType.PingRequest);
  }
}
