package underlay;

import blockchain.LightChainInterface;
import skipGraph.SkipGraphNode;
import underlay.requests.GenericRequest;
import underlay.responses.GenericResponse;

public abstract class Underlay {
  /** The underlying SkipGraphNode instance. Is used for skipnode calls */
  protected SkipGraphNode skipNode;

  /** The underlying LightChainInterface instance. Is used for lightchainnode class */
  protected LightChainInterface lightChainNode;

  public void setSkipNode(SkipGraphNode skipNode) {
    this.skipNode = skipNode;
  }

  public void setLightChainNode(LightChainInterface lightChainNode) {
    this.lightChainNode = lightChainNode;
  }

  /**
   * This method is used to send a request to a remote server specified by the address
   * The target server then uses the answer method below to call the underlying LightChainNode
   * or the SkipNode using the request and its specific data
   * @param req             The request which will be invoked on the remote server
   * @param targetAddress   The address used to specify the target remote server
   * @return                The response returned by the remote server
   */
  public abstract GenericResponse sendMessage(GenericRequest req, String targetAddress);

  /**
   * Terminates the underlay.
   * @return true iff the termination was successful.
   */
  public abstract boolean terminate();
}
