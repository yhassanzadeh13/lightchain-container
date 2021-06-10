package blockchain;

import underlay.requests.lightchain.GetModeRequest;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;

import static underlay.responses.BooleanResponse.BooleanResponseOf;

/**
 * LightChainCV implements the correctness verifier for traditional LightChain transactions, which of
 * are the types Alice paying some balance to Bob.
 *
 * Correctness verifier is executed by a validator as part of proof-of-validation consensus.
 *
 * Note that at this proof-of-concept level, this correctness verifier approves all transactions that have the same
 * type of owner and validator (i.e., both honest or malicious).
 */
class LightChainCV extends CorrectnessVerifier {
    // TODO: owner is a confusing name, should be refactored.
    public LightChainCV(LightChainNode owner) throws RemoteException {
        super(owner);
    }

    /**
     * Checks correctness of transaction, returns true if both nodes are of same type (HONEST,HONEST)
     * or (MALICIOUS,MALICIOUS) and returns false if both are of different types.
     *
     * @param t transaction whose correctness is to be verified
     * @return true if transaction is correct, or false if not
     */
    @Override
    public boolean isCorrect(Transaction t) {
        try {
            // updates view with the mode of transaction owner (i.e., malicious or honest)
            if (!owner.view.hasModeEntry(t.getOwner())) {
                boolean ownerMode = BooleanResponseOf(underlay.sendMessage(new GetModeRequest(), t.getAddress())).result;
                owner.view.updateMode(t.getOwner(), ownerMode);
                return ownerMode == owner.mode;
            }
            boolean ownerMode = owner.view.getMode(t.getOwner());
            if (ownerMode != owner.mode) {
                // TODO: apply logging best practice.
                owner.logger.debug("Transaction not correct");
            }
            // by convention of prototyping, a transaction is correct if both owner and validator (i.e., this node)
            // are of the same mode, otherwise it is incorrect.
            return ownerMode == owner.mode;
        }
        catch (RemoteException | FileNotFoundException e) {
            // TODO: check for exception best practice.
            e.printStackTrace();
            return false;
        }
    }
}
