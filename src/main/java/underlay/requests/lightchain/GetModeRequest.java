package underlay.requests.lightchain;

import underlay.requests.RequestType;

public class GetModeRequest extends GenericLightChainRequest {
  public GetModeRequest() {
    super(RequestType.GetModeRequest);
  }
}
