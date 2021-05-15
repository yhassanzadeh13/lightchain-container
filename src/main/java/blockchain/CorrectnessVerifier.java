package blockchain;

import underlay.RMIUnderlay;

import java.rmi.RemoteException;

public abstract class CorrectnessVerifier {
  protected LightChainNode owner;
  RMIUnderlay RMIUnderlay;

  /** Implements a abstract class isCorrect( ) which can be used ContractCV and LightchainCV */
  public CorrectnessVerifier(LightChainNode owner) throws RemoteException {
    this.owner = owner;
    this.RMIUnderlay = owner.getUnderlay();
  }

  public abstract boolean isCorrect(Transaction t) throws RemoteException;
}
