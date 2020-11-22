package blockchain;


import java.rmi.RemoteException;

class LightChainCV extends CorrectnessVerifier {
    private View view;
    private boolean mode;
    
    public LightChainCV(Parameters params, int RMIPort, String introducer, boolean isInitial,View view, boolean mode) throws RemoteException {
        super(params, RMIPort, introducer, isInitial);
        this.view = view;
        this.mode = mode;
    }

    /*
     * Checks correctness of transactionReturns true if both nodes are of same type
     * (HONEST,HONEST) or (MALICIOUS,MALICIOUS) and returns false if both are of
     * different types.
     *
     * @param t transaction whose correctness is to be verified
     * @return true if transaction is correct, or false if not
      */

    @Override
    public boolean isCorrect(Transaction t) {


        try {
                if (!view.hasModeEntry(t.getOwner())) {
                    LightChainRMIInterface rmi = getLightChainRMI(t.getAddress());
                    boolean ownerMode = rmi.getMode();
                    view.updateMode(t.getOwner(), ownerMode);
                    return ownerMode == mode;
                }
                boolean ownerMode = view.getMode(t.getOwner());
                if (ownerMode != mode) {
                    System.out.println("Transaction not correct");
                }

                return ownerMode == mode;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }
}
