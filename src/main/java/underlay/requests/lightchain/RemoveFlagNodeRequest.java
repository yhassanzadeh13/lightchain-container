package underlay.requests.lightchain;

import underlay.requests.RequestType;

public class RemoveFlagNodeRequest extends GenericLightChainRequest {
  public RemoveFlagNodeRequest() {
    super(RequestType.RemoveFlagNodeRequest);
  }
}
