package localnet;

import blockchain.Block;
import blockchain.Parameters;
import blockchain.View;
import hashing.Hasher;
import hashing.HashingTools;
import signature.DigitalSignature;
import skipGraph.NodeInfo;
import util.Const;
import util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class LocalLightChainNode {

  LocalSkipGraph skipGraph;
  Parameters params;
  List<NodeInfo> nodes;
  Hasher hasher;
  DigitalSignature[] digitalSignature;
  View[] view;
  Map<Integer, Integer> numIDToIndex;
  Map<Integer, NodeInfo> numToNode;

  Logger logger;

  public LocalLightChainNode(List<Integer> numIDs, Parameters params) {

    this.params = params;
    this.hasher = new HashingTools();
    this.digitalSignature = new DigitalSignature[numIDs.size()];
    this.view = new View[numIDs.size()];
    this.numIDToIndex = new HashMap<>();
    this.nodes = new ArrayList<>();
    this.numToNode = new HashMap<>();
    this.logger = Logger.getLogger(this.getClass());

    int i = 0;
    for(Integer numID : numIDs){

      this.digitalSignature[i] = new DigitalSignature();
      numIDToIndex.put(numID, i);
      String nameID = hasher.getHash(this.digitalSignature[i].getPublicKey().getEncoded(), params.getLevels());
		  nameID = hasher.getHash(nameID, params.getLevels());
      NodeInfo node = new NodeInfo(Util.getRandomString(10), numID, nameID);
      this.nodes.add(node);
      this.numToNode.put(numID, node);
      i++;
    }

    this.skipGraph = new LocalSkipGraph(nodes, params.getLevels());
  }

  public Block getLatestBlock() {

    logger.info("Fetching flag node ...");
    NodeInfo flag = skipGraph.searchByNumID(Const.ZERO_ID);
    logger.info("Found flag node with numID: " + flag.getNumID() + " and nameID: " + flag.getNameID());
    int num = Integer.parseInt(flag.getNameID(), 2);
    logger.info("found block numID of: " + num);
    NodeInfo block = skipGraph.searchByNumID(num);

    return (Block) block;
  }

  public Block createBlock(String prev, int owner, String address, int idx, int levels) {
    Block blk = new Block(prev, owner, address, idx, levels);
    logger.info("Block created with numID " + blk.getNumID());
    insertBlock(blk);
    return blk;
  }

  public void insertBlock(Block blk) {

    skipGraph.delete(Const.ZERO_ID);
    skipGraph.insertNode(new NodeInfo(blk.getAddress(), Const.ZERO_ID, blk.getHash()));
    skipGraph.insertNode(blk);
  }

  public NodeInfo getNode(int numID) {
    if(!numToNode.containsKey(numID))
      return null;

    return numToNode.get(numID);
  }


}
