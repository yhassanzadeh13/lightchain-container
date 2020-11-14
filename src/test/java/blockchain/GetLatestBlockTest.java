package blockchain;

import localnet.LocalLightChainNode;
import localnet.LocalSkipGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import skipGraph.NodeInfo;
import util.Util;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GetLatestBlockTest {

  LocalLightChainNode localLightChain;
  Parameters params;
  @BeforeEach
	void init() {
    params = new Parameters();
	}

	@Test
	void testGetLatestBlockSingleNodeSequential() {

    List<Integer> numIDs = new ArrayList<>();
    int numLevels = 10;
    int numID = 5;
    int iterations = 100;
    String dummyPrev = "1111111111";
    params.setLevels(numLevels);
    numIDs.add(numID);
    localLightChain = new LocalLightChainNode(numIDs, params);
    NodeInfo fullNode = localLightChain.getNode(numID);

    Block blk = localLightChain.createBlock(dummyPrev, numID, fullNode.getAddress(), 0, params.getLevels());

    for(int i = 0 ; i < iterations ; ++i) {

      blk = localLightChain.createBlock(blk.getHash(), numID, fullNode.getAddress(), blk.getIndex() + 1, params.getLevels());

      Block latestBlk = localLightChain.getLatestBlock();

      assertEquals(blk.getHash(), latestBlk.getHash(), "Different Latest Block found at iteration: " + i);
    }
	}

	void testGetLatestBlocksTwoNodesSequential() {

    List<Integer> numIDs = new ArrayList<>();
    int numLevels = 10;
    int numID = 5;
    int iterations = 100;
    String dummyPrev = "1111111111";
    params.setLevels(numLevels);
    numIDs.add(numID);
    localLightChain = new LocalLightChainNode(numIDs, params);
    NodeInfo fullNode = localLightChain.getNode(numID);

    Block blk = localLightChain.createBlock(dummyPrev, numID, fullNode.getAddress(), 0, params.getLevels());

    for(int i = 0 ; i < iterations ; ++i) {

      blk = localLightChain.createBlock(blk.getHash(), numID, fullNode.getAddress(), blk.getIndex() + 1, params.getLevels());

      Block latestBlk = localLightChain.getLatestBlock();

      assertEquals(blk.getHash(), latestBlk.getHash(), "Different Latest Block found at iteration: " + i);
    }
	}
}
