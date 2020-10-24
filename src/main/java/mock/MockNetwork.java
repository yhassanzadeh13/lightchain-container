package mock;

import blockchain.Block;
import blockchain.LightChainNode;
import blockchain.Parameters;
import blockchain.Transaction;
import remoteTest.Configuration;
import remoteTest.PingLog;
import signature.SignedBytes;
import simulation.SimLog;
import skipGraph.NodeInfo;
import util.Const;
import util.PropertyManager;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

public class MockNetwork {

  private static PropertyManager propMng;
  Map<Integer, LightChainNode> nodes;
  boolean mode; // false: sequential, else concurrent

  public MockNetwork() {

    propMng = new PropertyManager("mock.config");

    Parameters params = new Parameters();
    params.setAlpha(getIntProperty("alpha", "12"));
    params.setTxMin(getIntProperty("txmin", "5"));
    params.setSignaturesThreshold(getIntProperty("signaturesThreshold", "5"));
    params.setInitialBalance(getIntProperty("initialBalance", "20"));
    params.setLevels(getIntProperty("levels", "30"));
    params.setValidationFees(getIntProperty("validationFees", "1"));
    params.setMode(getBoolProperty("Mode", "True"));
    int nodeCount = getIntProperty("nodeCount", "20");
    int iterations = getIntProperty("iterations", "50");
    int pace = getIntProperty("pace", "1");

    int port = 3331;
    LightChainNode initialNode = null;
    for (int i = 0; i < nodeCount; ++i) {
      try {
        LightChainNode node;
        if (i == 0) {
          node = new LightChainNode(params, port, Const.DUMMY_INTRODUCER, true);
          initialNode = node;
        } else {
          node = new LightChainNode(params, port, initialNode.getAddress(), false);
        }
        nodes.put(node.getNumID(), node);
      } catch (RemoteException re) {
        i--;
        continue;
      }
    }

    try {
      initialNode.insertGenesis();
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    this.mode = false;
  }

  private static int getIntProperty(String key, String def) {
    return Integer.parseInt(propMng.getProperty(key, def));
  }

  private static boolean getBoolProperty(String key, String def) {
    return Boolean.parseBoolean(propMng.getProperty(key, def));
  }

  public void setMode(boolean mode) {
    this.mode = mode;
  }

  public NodeInfo getNodeByAddress(String address) {

    for (Integer ID : nodes.keySet()) {
      if (address.equals(nodes.get(ID).getAddress())) return nodes.get(ID).getNode(ID);
    }
    return null;
  }


  public SignedBytes PoV(Transaction t, Integer recipient) throws RemoteException {
    return nodes.get(recipient).PoV(t);
  }

  public SignedBytes PoV(Block blk, Integer recipient) throws RemoteException {
    return nodes.get(recipient).PoV(blk);
  }

  public boolean getMode(Integer recipient) throws RemoteException {
    return nodes.get(recipient).getMode();
  }

  public PublicKey getPublicKey(Integer recipient) throws RemoteException {
    return nodes.get(recipient).getPublicKey();
  }

  public void removeFlagNode(Integer recipient) throws RemoteException {
    nodes.get(recipient).removeFlagNode();
  }

  public Configuration getConf(Integer recipient) throws RemoteException {
    return nodes.get(recipient).getConf();
  }

  public SimLog startSim(int numTransactions, int pace, Integer recipient) throws RemoteException {
    return nodes.get(recipient).startSim(numTransactions, pace);
  }

  public Block insertGenesis(Integer recipient) throws RemoteException {
    return nodes.get(recipient).insertGenesis();
  }

  public void shutDown(Integer recipient) throws RemoteException {
    nodes.get(recipient).shutDown();
  }

  public NodeInfo getLeftNode(int level, int num, Integer recipient) throws RemoteException {
    return nodes.get(recipient).getLeftNode(level, num);
  }

  public NodeInfo getRightNode(int level, int num, Integer recipient) throws RemoteException {
    return nodes.get(recipient).getRightNode(level, num);
  }

  public String getNameID(Integer recipient) throws RemoteException {
    return nodes.get(recipient).getNameID();
  }

  public String getAddress(Integer recipient) throws RemoteException {
    return nodes.get(recipient).getAddress();
  }

  public String getLeftNameID(int level, int num, Integer recipient) throws RemoteException {
    return nodes.get(recipient).getLeftNameID(level, num);
  }

  public String getRightNameID(int level, int num, Integer recipient) throws RemoteException {
    return nodes.get(recipient).getRightNameID(level, num);
  }

  public NodeInfo getNode(int num, Integer recipient) throws RemoteException {
    return nodes.get(recipient).getNode(num);
  }

  public int getNumID(Integer recipient) throws RemoteException {
    return nodes.get(recipient).getNumID();
  }

  public int getLeftNumID(int level, int num, Integer recipient) throws RemoteException {
    return nodes.get(recipient).getLeftNumID(level, num);
  }

  public int getRightNumID(int level, int num, Integer recipient) throws RemoteException {
    return nodes.get(recipient).getRightNumID(level, num);
  }

  public void delete(int num, Integer recipient) throws RemoteException {
    nodes.get(recipient).delete(num);
  }

  public boolean setLeftNode(int num, int level, NodeInfo newNode, NodeInfo oldNode, Integer recipient) throws RemoteException {
    return nodes.get(recipient).setLeftNode(num, level, newNode, oldNode);
  }

  public boolean setRightNode(int num, int level, NodeInfo newNode, NodeInfo oldNode, Integer recipient) throws RemoteException {
    return nodes.get(recipient).setRightNode(num, level, newNode, oldNode);
  }

  public NodeInfo searchByNameID(String targetString, Integer recipient) throws RemoteException {
    return nodes.get(recipient).searchByNameID(targetString);
  }

  public NodeInfo searchByNumID(int targetNum, Integer recipient) throws RemoteException {
    return nodes.get(recipient).searchByNumID(targetNum);
  }

  public List<NodeInfo> searchByNumIDHelper(int targetNum, List<NodeInfo> lst, Integer recipient) throws RemoteException {
    return nodes.get(recipient).searchByNumIDHelper(targetNum, lst);
  }

  public List<NodeInfo> searchNumID(int numID, int searchTarget, int level, List<NodeInfo> lst, Integer recipient) throws RemoteException {
    return nodes.get(recipient).searchNumID(numID, searchTarget, level, lst);
  }

  public NodeInfo searchName(int numID, String searchTarget, int level, int direction, Integer recipient) throws RemoteException {
    return nodes.get(recipient).searchName(numID, searchTarget, level, direction);
  }

  public NodeInfo insertSearch(int level, int direction, int num, String target, Integer recipient) throws RemoteException, FileNotFoundException {
    return nodes.get(recipient).insertSearch(level, direction, num, target);
  }

  public boolean ping(Integer recipient) throws RemoteException {
    return nodes.get(recipient).ping();
  }

  public PingLog pingStart(NodeInfo node, int freq, Integer recipient) throws RemoteException {
    return nodes.get(recipient).pingStart(node, freq);
  }

  public PingLog retroPingStart(NodeInfo node, int freq, Integer recipient) throws RemoteException {
    return nodes.get(recipient).retroPingStart(node, freq);
  }


}
