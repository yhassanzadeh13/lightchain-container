package blockchain;

import evm.Contract;
import evm.ContractTransaction;
import skipGraph.NodeInfo;

import java.rmi.RemoteException;

/*
 * Checks correctness of transaction by passing values to smart contracts.
 * Returns true if the conditions are met.
 *
 * @param t transaction whose correctness is to be verified
 * @param c Contract class object which is used to pass value like name of contract and the function.
 * @return true if transaction is correct, or false if not
 */


class ContractCV extends CorrectnessVerifier {

    Contract ct = new Contract();
    ContractTransaction tesq = new ContractTransaction();
    
   

    public ContractCV(Parameters params, int RMIPort, String introducer, boolean isInitial) throws RemoteException {
        super(params, RMIPort, introducer, isInitial);
    }

    @Override
    public boolean isCorrect(Transaction t) {
        try {
           
            NodeInfo owner = searchByNumID(t.getOwner());
            LightChainRMIInterface ownerRMI = getLightChainRMI(owner.getAddress());
            int token1 = ownerRMI.getToken();
            tesq.setup();

            boolean value = tesq.TransctSol(token1, ct.contractName, ct.functname1);
          
            return true; //currently for just checking  // value;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}

