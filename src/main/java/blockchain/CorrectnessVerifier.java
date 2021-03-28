package blockchain;
import evm.Contract;
import java.rmi.RemoteException;

public abstract class CorrectnessVerifier  {
    protected LightChainNode owner;

    /** 
    * Implements a abstract class isCorrect( ) which can be used ContractCV and LightchainCV 
    */
    public CorrectnessVerifier(LightChainNode owner) throws RemoteException {
              this.owner = owner;
    }
    
    abstract public boolean isCorrect(Transaction t) throws RemoteException;

}
