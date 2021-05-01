package blockchain;

import evm.Contract;
import evm.ContractTransaction;
import skipGraph.NodeInfo;
import underlay.InterfaceTypes;
import underlay.Underlay;
import underlay.requests.GetTokenRequest;
import underlay.responses.IntegerResponse;

import java.rmi.RemoteException;

/** 
* ContractCV contains the isCorrect() function which is used to 
* interact with the wrapper function TransctSol() 
*/
class ContractCV extends CorrectnessVerifier {
    Underlay underlay = new Underlay();
    Contract ct = new Contract();
    ContractTransaction tesq = new ContractTransaction();

    public ContractCV(LightChainNode owner) throws RemoteException {
        super(owner);
    }
    
    /** 
    * Checks correctness of transaction by passing values to smart contracts.
    * Returns true if the conditions are met.
    * @param t transaction whose correctness is to be verified
    * @return true if transaction is correct, or false if not
    */
    @Override
    public boolean isCorrect(Transaction t) {
        try {
            NodeInfo ndowner = owner.searchByNumID(t.getOwner());
            int token1 = ((IntegerResponse) underlay.sendMessage(new GetTokenRequest(), ndowner.getAddress(), InterfaceTypes.LightChainInterface)).result;
            tesq.setup();
            boolean value = tesq.TransctSol(token1, ct.contractName1, ct.functname1);
            return value; 
        } 
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}

