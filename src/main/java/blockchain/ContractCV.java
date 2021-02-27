package blockchain;

import evm.Contract;
import evm.ContractTransaction;
import skipGraph.NodeInfo;

import java.rmi.RemoteException;


class ContractCV extends CorrectnessVerifier {

    Contract ct = new Contract();
    ContractTransaction tesq = new ContractTransaction();
    
   
    public ContractCV(LightChainNode owner) throws RemoteException {
        super(owner);
    }
    
    /** 
    * Checks correctness of transaction by passing values to smart contracts.
    * Returns true if the conditions are met.
    *
    * @param t transaction whose correctness is to be verified
    * @return true if transaction is correct, or false if not
    */



    @Override
    public boolean isCorrect(Transaction t) {
        try {
           
            NodeInfo ndowner = owner.searchByNumID(t.getOwner());
            LightChainRMIInterface ownerRMI = owner.getLightChainRMI(ndowner.getAddress());
            int token1 = ownerRMI.getToken();
            tesq.setup();
            System.out.println("value in the function: " + token1);
            boolean value = tesq.TransctSol(token1, ct.contractName, ct.functname1);

            return value; 
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}

