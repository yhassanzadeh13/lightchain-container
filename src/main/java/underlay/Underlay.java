package underlay;

import blockchain.LightChainInterface;
import skipGraph.SkipNodeInterface;
import underlay.requests.GenericRequest;
import underlay.requests.lightchain.GenericLightChainRequest;
import underlay.requests.skipgraph.GenericSkipGraphRequest;
import underlay.responses.GenericResponse;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;

public abstract class Underlay {
  /** The underlying SkipNodeInterface instance. Is used for skipnode calls */
  protected SkipNodeInterface skipNode;

  /** The underlying LightChainInterface instance. Is used for lightchainnode class */
  protected LightChainInterface lightChainNode;

  public void setSkipNode(SkipNodeInterface skipNode) {
    this.skipNode = skipNode;
  }

  public void setLightChainNode(LightChainInterface lightChainNode) {
    this.lightChainNode = lightChainNode;
  }

  public abstract GenericResponse sendMessage(GenericSkipGraphRequest req, String targetAddress)
      throws RemoteException, FileNotFoundException;

  public abstract GenericResponse sendMessage(GenericLightChainRequest req, String targetAddress)
      throws RemoteException, FileNotFoundException;

  public abstract GenericResponse answer(GenericRequest req)
      throws RemoteException, FileNotFoundException;

  public abstract boolean terminate();
}
