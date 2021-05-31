package underlay;

import blockchain.LightChainInterface;
import skipGraph.SkipGraphNode;
import underlay.requests.GenericRequest;
import underlay.responses.GenericResponse;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;

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

  public abstract GenericResponse sendMessage(GenericRequest req, String targetAddress)
      throws RemoteException, FileNotFoundException;

  public abstract GenericResponse answer(GenericRequest req)
      throws RemoteException, FileNotFoundException;

  public abstract boolean terminate();
}
