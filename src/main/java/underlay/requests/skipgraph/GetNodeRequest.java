package underlay.requests.skipgraph;

import underlay.requests.RequestType;

public class GetNodeRequest extends GenericSkipGraphRequest {
  public final int num;

  public GetNodeRequest(int num) {
    super(RequestType.GetNodeRequest);
    this.num = num;
  }
}
