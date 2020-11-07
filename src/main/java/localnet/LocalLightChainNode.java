package localnet;

import blockchain.Block;
import blockchain.Parameters;
import blockchain.View;
import hashing.Hasher;
import hashing.HashingTools;
import signature.DigitalSignature;
import skipGraph.NodeInfo;
import util.Const;

import java.util.List;

public class LocalLightChainNode {

  LocalSkipGraph skipGraph;
  Parameters params;
  Hasher hasher;
  DigitalSignature[] digitalSignature;
  View[] view;

  public LocalLightChainNode(LocalSkipGraph skipGraph, Parameters params) {
    this.skipGraph = skipGraph;
    this.params = params;

    this.hasher = new HashingTools();
    this.digitalSignature = new DigitalSignature[skipGraph.getNumNodes()];
    this.view = new View[skipGraph.getNumNodes()];
  }

  public Block getLatestBlock() {

    NodeInfo flag = skipGraph.searchByNumID(Const.ZERO_ID);
    int num = Integer.parseInt(flag.getNameID(), 2);
    NodeInfo block = skipGraph.searchByNumID(num);

    return (Block) block;
  }

  public void insertBlock(Block blk, String prevAddress) {

    skipGraph.delete(Const.ZERO_ID);

    skipGraph.insertNode(new NodeInfo(blk.getAddress(), Const.ZERO_ID, blk.getHash()));
    skipGraph.insertNode(blk);
  }


}
