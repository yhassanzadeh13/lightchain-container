package blockchain;
import evm.Contract;
import java.rmi.RemoteException;

public abstract class CorrectnessVerifier  {
   
    protected LightChainNode owner;

    public CorrectnessVerifier(LightChainNode owner) throws RemoteException {
              this.owner = owner;
    }
    
    abstract public boolean isCorrect(Transaction t) throws RemoteException;

}
