package blockchain;

import underlay.InterfaceTypes;
import underlay.Underlay;
import underlay.requests.GetModeRequest;
import underlay.responses.BooleanResponse;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;

class LightChainCV extends CorrectnessVerifier {
    Underlay underlay = new Underlay();

      public LightChainCV(LightChainNode owner) throws RemoteException {
      super(owner);
    }
   
    /** 
     * Checks correctness of transaction, returns true if both nodes are of same type
     * (HONEST,HONEST) or (MALICIOUS,MALICIOUS) and returns false if both are of
     * different types.
     * @param t transaction whose correctness is to be verified
     * @return true if transaction is correct, or false if not
     */
    @Override
    public boolean isCorrect(Transaction t) {
        try {
                if (!owner.view.hasModeEntry(t.getOwner())) {
                    boolean ownerMode = ((BooleanResponse) underlay.sendMessage(new GetModeRequest(), t.getAddress(), InterfaceTypes.LightChainInterface)).getResult();
                    owner.view.updateMode(t.getOwner(), ownerMode);

                    return ownerMode == owner.mode;
                }
                boolean ownerMode = owner.view.getMode(t.getOwner());
                if (ownerMode != owner.mode) {
                    owner.logger.debug("Transaction not correct");
                }
                return ownerMode == owner.mode;
        } catch (RemoteException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
