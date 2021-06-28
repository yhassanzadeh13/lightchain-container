package blockchain;

import underlay.Underlay;


public abstract class CorrectnessVerifier {
    protected LightChainNode owner;
    Underlay underlay;

    // TODO: owner name misleading. Refactor needed. 
    public CorrectnessVerifier(LightChainNode owner) {
        this.owner = owner;
        this.underlay = owner.getUnderlay();
    }

    /**
     * isCorrect receives traction t and verifiers its correctness. The correctness of transaction implies its
     * valid format.
     *
     * @param t transaction to be validated.
     * @return true if transaction is correct and false otherwise.
     */
    public abstract boolean isCorrect(Transaction t);
}
