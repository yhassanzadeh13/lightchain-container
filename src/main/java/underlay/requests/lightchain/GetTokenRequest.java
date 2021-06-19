package underlay.requests.lightchain;

import underlay.requests.RequestType;

public class GetTokenRequest extends GenericLightChainRequest {
  public GetTokenRequest() {
    super(RequestType.GetTokenRequest);
  }
}
