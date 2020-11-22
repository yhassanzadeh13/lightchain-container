package blockchain;
import evm.Contract;
import java.rmi.RemoteException;

public abstract class CorrectnessVerifier extends LightChainNode {
   
    public CorrectnessVerifier(Parameters params, int RMIPort, String introducer, boolean isInitial) throws RemoteException {
        super(params, RMIPort, introducer, isInitial);
    }

    abstract public boolean isCorrect(Transaction t) throws RemoteException;

}
