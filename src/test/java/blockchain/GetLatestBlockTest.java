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

	//@Test
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

	//@Test
	void testGetLatestBlocksTwoNodesSequential() {

    List<Integer> numIDs = new ArrayList<>();
    int numLevels = 10;
    int numID1 = 5;
    int numID2 = 10;
    int iterations = 100;
    String dummyPrev = "1111111111";
    params.setLevels(numLevels);
    numIDs.add(numID1);
    numIDs.add(numID2);
    localLightChain = new LocalLightChainNode(numIDs, params);

    NodeInfo fullNode1 = localLightChain.getNode(numID1);
    NodeInfo fullNode2 = localLightChain.getNode(numID2);

    Block blk = localLightChain.createBlock(dummyPrev, numID1, fullNode1.getAddress(), 0, params.getLevels());

    for(int i = 0 ; i < iterations ; ++i) {

      blk = localLightChain.createBlock(blk.getHash(), numID1, fullNode1.getAddress(), blk.getIndex() + 1, params.getLevels());

      Block latestBlk = localLightChain.getLatestBlock();

      assertEquals(blk.getHash(), latestBlk.getHash(), "Different Latest Block found at iteration: " + i);

      blk = localLightChain.createBlock(blk.getHash(), numID2, fullNode2.getAddress(), blk.getIndex() + 1, params.getLevels());

      latestBlk = localLightChain.getLatestBlock();

      assertEquals(blk.getHash(), latestBlk.getHash(), "Different Latest Block found at iteration: " + i);
    }
	}

	@Test
	void testGetLatestBlocksTwoNodesConcurrent() {

    List<Integer> numIDs = new ArrayList<>();
    int numLevels = 20;
    int numID1 = 5;
    int numID2 = 10;
    int iterations = 100;
    String dummyPrev = "11111111111111111111";
    params.setLevels(numLevels);
    numIDs.add(numID1);
    numIDs.add(numID2);
    localLightChain = new LocalLightChainNode(numIDs, params);

    NodeInfo fullNode1 = localLightChain.getNode(numID1);
    NodeInfo fullNode2 = localLightChain.getNode(numID2);

    Block blk = localLightChain.createBlock(dummyPrev, numID1, fullNode1.getAddress(), 0, params.getLevels());

    final Block[] blks = new Block[2];

    for(int i = 0 ; i < iterations ; ++i) {

      Block finalBlk = blk;
      (new Thread(new Runnable() {
        @Override
        public void run() {
          blks[0] = localLightChain.createBlock(finalBlk.getHash(), numID1, fullNode1.getAddress(), finalBlk.getIndex() + 1, params.getLevels());
        }
      })).start();

      (new Thread(new Runnable() {
      @Override
      public void run() {
        blks[1] = localLightChain.createBlock(finalBlk.getHash(), numID2, fullNode2.getAddress(), finalBlk.getIndex() + 1, params.getLevels());
      }
      })).start();

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      blk = localLightChain.getLatestBlock();

      assertEquals(localLightChain.getSkipGraph().getLatestInsertion().getNumID(), blk.getNumID(), "latest blocks do not match at iteration " + i);
    }

	}

}
