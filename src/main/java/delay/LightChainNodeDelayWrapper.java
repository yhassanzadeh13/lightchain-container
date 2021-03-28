package delay;

import blockchain.Block;
import blockchain.LightChainRMIInterface;
import blockchain.Transaction;
import remoteTest.Configuration;
import signature.SignedBytes;
import simulation.SimLog;
import skipGraph.RMIInterface;

import java.rmi.RemoteException;
import java.security.PublicKey;

public class LightChainNodeDelayWrapper extends SkipNodeDelayWrapper implements LightChainRMIInterface {
    private LightChainRMIInterface innerNode;
    private int delay;

    public LightChainNodeDelayWrapper(LightChainRMIInterface innerNode, String senderAddress, String receiverAddress) {
        super(innerNode, senderAddress, receiverAddress);
        this.innerNode = innerNode;
        delay = DelayTracker.getInstance().getDelay(senderAddress, receiverAddress);
    }

    // This method is executed before every method call to the innerNode
    private void before(){
        try{
            if(delay>0)
                Thread.sleep(delay);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public SignedBytes PoV(Transaction t) throws RemoteException {
        before();
        return innerNode.PoV(t);
    }

    @Override
    public SignedBytes PoV(Block blk) throws RemoteException {
        before();
        return innerNode.PoV(blk);
    }

    @Override
    public boolean getMode() throws RemoteException {
        before();
        return innerNode.getMode();
    }

    @Override
    public PublicKey getPublicKey() throws RemoteException {
        before();
        return innerNode.getPublicKey();
    }

    @Override
    public void removeFlagNode() throws RemoteException {
        before();
        innerNode.removeFlagNode();
    }

    @Override
    public Configuration getConf() throws RemoteException {
        before();
        return innerNode.getConf();
    }

    @Override
    public SimLog startSim(int numTransactions, int pace) throws RemoteException {
        before();
        return innerNode.startSim(numTransactions, pace);
    }

    @Override
    public Block insertGenesis() throws RemoteException {
        before();
        return innerNode.insertGenesis();
    }

    @Override
    public void shutDown() throws RemoteException {
        before();
        innerNode.shutDown();
    }

    @Override
    public int getToken() throws RemoteException {
        before();
        return innerNode.getToken();
    }
}
