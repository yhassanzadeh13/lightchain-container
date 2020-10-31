package mock;

import blockchain.Block;
import blockchain.LightChainRMIInterface;
import blockchain.Transaction;
import remoteTest.Configuration;
import remoteTest.PingLog;
import signature.SignedBytes;
import simulation.SimLog;
import skipGraph.NodeInfo;
import skipGraph.RMIInterface;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.List;

public class NetworkIntermediary implements LightChainRMIInterface, RMIInterface {

  NodeInfo node;
  MockNetwork network;

  public NetworkIntermediary(MockNetwork network, String address) {
    this.node = network.getNodeByAddress(address);
    this.network = network;
  }

  @Override
  public SignedBytes PoV(Transaction t) throws RemoteException {
    return network.PoV(t, node.getNumID());
  }

  @Override
  public SignedBytes PoV(Block blk) throws RemoteException {
    return network.PoV(blk, node.getNumID());
  }

  @Override
  public boolean getMode() throws RemoteException {
    return network.getMode(node.getNumID());
  }

  @Override
  public PublicKey getPublicKey() throws RemoteException {
    return network.getPublicKey(node.getNumID());
  }

  @Override
  public void removeFlagNode() throws RemoteException {
    network.removeFlagNode(node.getNumID());
  }

  @Override
  public Configuration getConf() throws RemoteException {
    return network.getConf(node.getNumID());
  }

  @Override
  public SimLog startSim(int numTransactions, int pace) throws RemoteException {
    return network.startSim(numTransactions, pace, node.getNumID());
  }

  @Override
  public Block insertGenesis() throws RemoteException {
    return network.insertGenesis(node.getNumID());
  }

  @Override
  public void shutDown() throws RemoteException {
    network.shutDown(node.getNumID());
  }

  @Override
  public NodeInfo getLeftNode(int level, int num) throws RemoteException {
    return network.getLeftNode(level, num, node.getNumID());
  }

  @Override
  public NodeInfo getRightNode(int level, int num) throws RemoteException {
    return network.getRightNode(level, num, node.getNumID());
  }

  @Override
  public String getNameID() throws RemoteException {
    return network.getNameID(node.getNumID());
  }

  @Override
  public String getAddress() throws RemoteException {
    return network.getAddress(node.getNumID());
  }

  @Override
  public String getLeftNameID(int level, int num) throws RemoteException {
    return network.getLeftNameID(level, num, node.getNumID());
  }

  @Override
  public String getRightNameID(int level, int num) throws RemoteException {
    return network.getRightNameID(level, num, node.getNumID());
  }

  @Override
  public NodeInfo getNode(int num) throws RemoteException {
    return network.getNode(num, node.getNumID());
  }

  @Override
  public int getNumID() throws RemoteException {
    return network.getNumID(node.getNumID());
  }

  @Override
  public int getLeftNumID(int level, int num) throws RemoteException {
    return network.getLeftNumID(level, num, node.getNumID());
  }

  @Override
  public int getRightNumID(int level, int num) throws RemoteException {
    return network.getRightNumID(level, num, node.getNumID());
  }

  @Override
  public void delete(int num) throws RemoteException {
    network.delete(num, node.getNumID());
  }

  @Override
  public boolean setLeftNode(int num, int level, NodeInfo newNode, NodeInfo oldNode) throws RemoteException {
    return network.setLeftNode(num, level, newNode, oldNode, node.getNumID());
  }

  @Override
  public boolean setRightNode(int num, int level, NodeInfo newNode, NodeInfo oldNode) throws RemoteException {
    return network.setRightNode(num, level, newNode, oldNode, node.getNumID());
  }

  @Override
  public NodeInfo searchByNameID(String targetString) throws RemoteException {
    return network.searchByNameID(targetString, node.getNumID());
  }

  @Override
  public NodeInfo searchByNumID(int targetNum) throws RemoteException {
    return network.searchByNumID(targetNum, node.getNumID());
  }

  @Override
  public List<NodeInfo> searchByNumIDHelper(int targetNum, List<NodeInfo> lst) throws RemoteException {
    return network.searchByNumIDHelper(targetNum, lst, node.getNumID());
  }

  @Override
  public List<NodeInfo> searchNumID(int numID, int searchTarget, int level, List<NodeInfo> lst) throws RemoteException {
    return network.searchNumID(numID, searchTarget, level, lst, node.getNumID());
  }

  @Override
  public NodeInfo searchName(int numID, String searchTarget, int level, int direction) throws RemoteException {
    return network.searchName(numID, searchTarget, level, direction, node.getNumID());
  }

  @Override
  public NodeInfo insertSearch(int level, int direction, int num, String target) throws RemoteException, FileNotFoundException {
    return network.insertSearch(level, direction, num, target, node.getNumID());
  }

  @Override
  public boolean ping() throws RemoteException {
    return network.ping(node.getNumID());
  }

  @Override
  public PingLog pingStart(NodeInfo node, int freq) throws RemoteException {
    return network.pingStart(node, freq, node.getNumID());
  }

  @Override
  public PingLog retroPingStart(NodeInfo node, int freq) throws RemoteException {
    return network.retroPingStart(node, freq, node.getNumID());
  }
}
