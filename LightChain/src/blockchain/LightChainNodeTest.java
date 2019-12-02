package blockchain;

import static org.junit.jupiter.api.Assertions.*;

import java.rmi.RemoteException;
import java.security.PublicKey;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import skipGraph.NodeConfig;
import util.Const;

class LightChainNodeTest {

	private static int port = 7000;
	private int maxLevels = 3;
	private int numID1 = 1;
	private int numID2 = 2;
	private int numID3 = 3;
	private int numID4 = 15;
	private int numID5 = 50;
	private int numID6 = 100;
	private String nameID1 = "011";
	private String nameID2 = "001";
	private String nameID3 = "100";
	private String nameID4 = "111";
	private String nameID5 = "101";
	private String nameID6 = "010";
	private NodeConfig initialConfig;
	private NodeConfig config1;
	private NodeConfig config2;
	private NodeConfig sameNameIDConfig1;
	private NodeConfig sameNameIDConfig2;
	private NodeConfig sameNameIDConfig3;

	@BeforeEach
	void init() {
		initialConfig = new NodeConfig(maxLevels, port++, numID2, nameID2);
		config1 = new NodeConfig(maxLevels, port++, numID1, nameID1);
		config2 = new NodeConfig(maxLevels, port++, numID3, nameID3);
	}

	@Ignore
	void testUpdateViewTable() {
		fail("Not yet implemented");
	}

	@Ignore
	void testViewUpdate() {
		fail("Not yet implemented");
	}

	@Test
	void testGetLatestBlock() {

		try {

			// test detecting genesis block
			LightChainNode node1 = new LightChainNode(initialConfig, Const.DUMMY_INTRODUCER, true);
			LightChainNode node2 = new LightChainNode(config1, node1.getAddress(), false);
			LightChainNode node3 = new LightChainNode(config2, node1.getAddress(), false);

			Block blk = node1.insertGenesis();

			Block b2 = node2.getLatestBlock();
			assertEquals(blk, b2, "latest block not found");

			Block b3 = node3.getLatestBlock();
			assertEquals(blk, b3, "latest block not found");

			// Test detecting second block after genesis

			Block blk2 = new Block(blk.getH(), node2.getNumID(), node2.getAddress(), 1);
			node2.insertBlock(blk2, blk.getAddress());

			Block b1 = node1.getLatestBlock();
			assertEquals(blk2, b1, "latest block not found");

			b3 = node3.getLatestBlock();
			assertEquals(blk2, b3, "latest block not found");

			// Test detecting third block

			Block blk3 = new Block(blk2.getH(), node3.getNumID(), node3.getAddress(), 2);
			node3.insertBlock(blk3, blk2.getAddress());

			b1 = node1.getLatestBlock();
			assertEquals(blk3, b1, "latest block not found");

			b2 = node2.getLatestBlock();
			assertEquals(blk3, b2, "latest block not found");
			
			
			// Test with transactions of similar nameID
			

		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	@Ignore
	void testGetLatestTransaction() {
		fail("Not yet implemented");
	}

	@Ignore
	void testGetTransactionsWithNameID() {
		fail("Not yet implemented");
	}

	@Ignore
	void testValidateBlock() {
		fail("Not yet implemented");
	}

	@Ignore
	void testValidateTransaction() {
		fail("Not yet implemented");
	}

	@Ignore
	void testPoVBlock() {
		fail("Not yet implemented");
	}

	@Ignore
	void testIsAuthenticatedBlock() {
		fail("Not yet implemented");
	}

	@Ignore
	void testIsConsistent() {
		fail("Not yet implemented");
	}

	@Ignore
	void testGetValidatorsBlock() {
		fail("Not yet implemented");
	}

	@Ignore
	void testPoVTransaction() {
		fail("Not yet implemented");
	}

	@Ignore
	void testIsSound() {
		fail("Not yet implemented");
	}

	@Ignore
	void testIsCorrect() {
		fail("Not yet implemented");
	}

	@Ignore
	void testIsAuthenticatedTransaction() {
		fail("Not yet implemented");
	}

	@Ignore
	void testHasBalanceCompliance() {
		fail("Not yet implemented");
	}

	@Ignore
	void testGetValidatorsTransaction() {
		fail("Not yet implemented");
	}

	@Test
	void testGetOwnerPublicKey() {
		try {
			LightChainNode node1 = new LightChainNode(initialConfig, Const.DUMMY_INTRODUCER, true);
			LightChainNode node2 = new LightChainNode(config1, node1.getAddress(), false);
			LightChainNode node3 = new LightChainNode(config2, node1.getAddress(), false);

			PublicKey pk2 = node1.getOwnerPublicKey(node2.getNumID());
			PublicKey pk3 = node1.getOwnerPublicKey(node3.getNumID());

			assertEquals(node2.getPublicKey(), pk2, "wrong public key found");
			assertEquals(node3.getPublicKey(), pk3, "wrong public key found");

		} catch (RemoteException e) {

		}
	}

}
