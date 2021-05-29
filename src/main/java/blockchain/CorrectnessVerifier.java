package blockchain;

import underlay.Underlay;
import underlay.rmi.RMIUnderlay;

import java.rmi.RemoteException;

public abstract class CorrectnessVerifier {
  protected LightChainNode owner;
  Underlay underlay;

  /** Implements a abstract class isCorrect( ) which can be used ContractCV and LightchainCV */
  public CorrectnessVerifier(LightChainNode owner) throws RemoteException {
    this.owner = owner;
    this.underlay = owner.getUnderlay();
  }

  public abstract boolean isCorrect(Transaction t) throws RemoteException;
}
