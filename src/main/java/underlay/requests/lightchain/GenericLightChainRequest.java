package underlay.requests.lightchain;

import underlay.requests.GenericRequest;
import underlay.requests.RequestType;

/** The base class of requests. */
public class GenericLightChainRequest extends GenericRequest {
  public GenericLightChainRequest(RequestType type) {
    super(type);
  }
}
