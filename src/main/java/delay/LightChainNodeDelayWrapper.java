package delay;

import blockchain.Block;
import blockchain.LightChainNode;
import blockchain.LightChainInterface;
import blockchain.Transaction;
import remoteTest.Configuration;
import signature.SignedBytes;
import simulation.SimLog;

import java.security.PublicKey;

public class LightChainNodeDelayWrapper extends SkipNodeDelayWrapper implements LightChainInterface {
    private LightChainNode innerNode;
    private int delay;

    public LightChainNodeDelayWrapper(LightChainNode innerNode, String senderAddress, String receiverAddress) {
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
    public SignedBytes PoV(Transaction t) {
        before();
        return innerNode.PoV(t);
    }

    @Override
    public SignedBytes PoV(Block blk) {
        before();
        return innerNode.PoV(blk);
    }

    @Override
    public boolean getMode() {
        before();
        return innerNode.getMode();
    }

    @Override
    public PublicKey getPublicKey() {
        before();
        return innerNode.getPublicKey();
    }

    @Override
    public void removeFlagNode() {
        before();
        innerNode.removeFlagNode();
    }

    @Override
    public Configuration getConf() {
        before();
        return innerNode.getConf();
    }

    @Override
    public SimLog startSim(int numTransactions, int pace) {
        before();
        return innerNode.startSim(numTransactions, pace);
    }

    @Override
    public Block insertGenesis() {
        before();
        return innerNode.insertGenesis();
    }

    @Override
    public void shutDown() {
        before();
        innerNode.shutDown();
    }

    @Override
    public int getToken() {
        before();
        return innerNode.getToken();
    }
}
