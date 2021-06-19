package underlay.requests.lightchain;

import underlay.requests.RequestType;

public class GetPublicKeyRequest extends GenericLightChainRequest {
  public GetPublicKeyRequest() {
    super(RequestType.GetPublicKeyRequest);
  }
}
