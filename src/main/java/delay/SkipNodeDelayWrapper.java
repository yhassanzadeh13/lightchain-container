package delay;

import remoteTest.PingLog;
import skipGraph.NodeInfo;
import skipGraph.SkipGraphNode;
import skipGraph.SkipNode;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.List;

public class SkipNodeDelayWrapper implements SkipGraphNode {

    private SkipNode innerNode;
    private int delay;

    public SkipNodeDelayWrapper(SkipNode innerNode, String senderAddress, String receiverAddress){
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
    public NodeInfo getLeftNode(int level, int num) throws RemoteException {
        before();
        return innerNode.getLeftNode(level, num);
    }

    @Override
    public NodeInfo getRightNode(int level, int num) throws RemoteException {
        before();
        return innerNode.getRightNode(level, num);
    }

    @Override
    public String getNameID() throws RemoteException {
        before();
        return innerNode.getNameID();
    }

    @Override
    public String getAddress() throws RemoteException {
        before();
        return innerNode.getAddress();
    }

    @Override
    public String getLeftNameID(int level, int num) throws RemoteException {
        before();
        return innerNode.getLeftNameID(level, num);
    }

    @Override
    public String getRightNameID(int level, int num) throws RemoteException {
        before();
        return innerNode.getRightNameID(level,num);
    }

    @Override
    public NodeInfo getNode(int num) throws RemoteException {
        before();
        return innerNode.getNode(num);
    }

    @Override
    public int getNumID() throws RemoteException {
        before();
        return innerNode.getNumID();
    }

    @Override
    public int getLeftNumID(int level, int num) throws RemoteException {
        before();
        return innerNode.getLeftNumID(level, num);
    }

    @Override
    public int getRightNumID(int level, int num) throws RemoteException {
        before();
        return innerNode.getRightNumID(level, num);
    }

    @Override
    public void delete(int num) throws RemoteException{
        before();
        innerNode.delete(num);
    }

    @Override
    public boolean setLeftNode(int num, int level, NodeInfo newNode, NodeInfo oldNode) throws RemoteException {
        before();
        return innerNode.setLeftNode(num, level, newNode, oldNode);
    }

    @Override
    public boolean setRightNode(int num, int level, NodeInfo newNode, NodeInfo oldNode) throws RemoteException {
        before();
        return innerNode.setRightNode(num, level, newNode, oldNode);
    }

    @Override
    public NodeInfo searchByNameID(String targetString) throws RemoteException {
        before();
        return innerNode.searchByNameID(targetString);
    }

    @Override
    public NodeInfo searchByNumID(int targetNum) throws RemoteException {
        before();
        return innerNode.searchByNumID(targetNum);
    }

    @Override
    public List<NodeInfo> searchByNumIDHelper(int targetNum, List<NodeInfo> lst) throws RemoteException {
        before();
        return innerNode.searchByNumIDHelper(targetNum, lst);
    }

    @Override
    public List<NodeInfo> searchNumID(int numID, int searchTarget, int level, List<NodeInfo> lst) throws RemoteException {
        before();
        return innerNode.searchNumID(numID, searchTarget, level, lst);
    }

    @Override
    public NodeInfo searchName(int numID, String searchTarget, int level, int direction) throws RemoteException {
        before();
        return innerNode.searchName(numID, searchTarget, level, direction);
    }

    @Override
    public NodeInfo insertSearch(int level, int direction, int num, String target) throws RemoteException, FileNotFoundException {
        before();
        return innerNode.insertSearch(level, direction, num, target);
    }

    @Override
    public boolean ping() throws RemoteException {
        before();
        return innerNode.ping();
    }

    @Override
    public PingLog pingStart(NodeInfo node, int freq) throws RemoteException {
        before();
        return innerNode.pingStart(node, freq);
    }

    @Override
    public PingLog retroPingStart(NodeInfo node, int freq) throws RemoteException {
        before();
        return innerNode.retroPingStart(node, freq);
    }
}
