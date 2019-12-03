package blockchain;

import static org.junit.jupiter.api.Assertions.*;

import java.rmi.RemoteException;
import java.security.PublicKey;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import skipGraph.NodeConfig;
import util.Const;
import util.Util;

class LightChainNodeTest {

	private static int port = 7000;
	private Parameters params;
	private int RMIPort1;
	private int RMIPort2;
	private int RMIPort3;
	private int RMIPort4;

	@BeforeEach
	void init() {
		params = new Parameters();

		RMIPort1 = port++;
		RMIPort2 = port++;
		RMIPort3 = port++;
		RMIPort4 = port++;
	}

	@Test
	void testUpdateView() {
		try {

			// test view update gotten by 3 transaction pointing at genesis block

			LightChainNode node1 = new LightChainNode(params, RMIPort1, Const.DUMMY_INTRODUCER, true);
			LightChainNode node2 = new LightChainNode(params, RMIPort2, node1.getAddress(), false);
			LightChainNode node3 = new LightChainNode(params, RMIPort3, node1.getAddress(), false);
			LightChainNode node4 = new LightChainNode(params, RMIPort4, node1.getAddress(), false);

			Block blk = node1.insertGenesis();

			String randStr1 = Util.getRandomString(10);
			String randStr2 = Util.getRandomString(11);
			String randStr3 = Util.getRandomString(12);

			Transaction t2 = new Transaction(blk.getHash(), node2.getNumID(), randStr1, node2.getAddress(),
					params.getLevels());
			Transaction t3 = new Transaction(blk.getHash(), node3.getNumID(), randStr2, node3.getAddress(),
					params.getLevels());
			Transaction t4 = new Transaction(blk.getHash(), node4.getNumID(), randStr3, node4.getAddress(),
					params.getLevels());

			node2.insertTransaction(t2);
			node3.insertTransaction(t3);
			node4.insertTransaction(t4);

			View v = node1.updateView();

			// view should contain entries for each of the nodes above

			assertEquals(blk.getNumID(), v.getLastBlk(node2.getNumID()), "view not updated correctly");
			assertEquals(blk.getNumID(), v.getLastBlk(node3.getNumID()), "view not updated correctly");
			assertEquals(blk.getNumID(), v.getLastBlk(node4.getNumID()), "view not updated correctly");

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testMineAttempt() {
		try {
			/*
			 * Scenario: with genesis block, 3 nodes each insert a transaction then node1
			 * attempts to mine a block, using very basic parameters 
			 * TxMin = 1 : 1 transaction is enough to cast a block 
			 * Alpha = 10 : give space for extra attempts 
			 * SignaturesThreshold = 1 : 1 validator is enough. 
			 * These parameters can be tweaked to test different values
			 */
			params.setTxMin(1);
			params.setAlpha(10);
			params.setSignaturesThreshold(1);

			LightChainNode node1 = new LightChainNode(params, RMIPort1, Const.DUMMY_INTRODUCER, true);
			LightChainNode node2 = new LightChainNode(params, RMIPort2, node1.getAddress(), false);
			LightChainNode node3 = new LightChainNode(params, RMIPort3, node1.getAddress(), false);
			LightChainNode node4 = new LightChainNode(params, RMIPort4, node1.getAddress(), false);

			Block blk = node1.insertGenesis();

			String randStr1 = Util.getRandomString(10);
			String randStr2 = Util.getRandomString(11);
			String randStr3 = Util.getRandomString(12);

			Transaction t2 = new Transaction(blk.getHash(), node2.getNumID(), randStr1, node2.getAddress(),
					params.getLevels());
			Transaction t3 = new Transaction(blk.getHash(), node3.getNumID(), randStr2, node3.getAddress(),
					params.getLevels());
			Transaction t4 = new Transaction(blk.getHash(), node4.getNumID(), randStr3, node4.getAddress(),
					params.getLevels());

			node2.insertTransaction(t2);
			node3.insertTransaction(t3);
			node4.insertTransaction(t4);

			Block newBlk = node1.mineAttempt();
			Block lstBlk = node2.getLatestBlock();

			assertNotNull(newBlk, "block was not mining failed");
			assertEquals(newBlk, lstBlk, "new block was not properly added");

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testGetLatestBlock() {

		try {

			// test detecting genesis block
			LightChainNode node1 = new LightChainNode(params, RMIPort1, Const.DUMMY_INTRODUCER, true);
			LightChainNode node2 = new LightChainNode(params, RMIPort2, node1.getAddress(), false);
			LightChainNode node3 = new LightChainNode(params, RMIPort3, node1.getAddress(), false);

			Block blk = node1.insertGenesis();

			Block b2 = node2.getLatestBlock();
			assertEquals(blk, b2, "latest block not found");

			Block b3 = node3.getLatestBlock();
			assertEquals(blk, b3, "latest block not found");

			// Test detecting second block after genesis

			Block blk2 = new Block(blk.getHash(), node2.getNumID(), node2.getAddress(), 1, params.getLevels());
			node2.insertBlock(blk2, blk.getAddress());

			Block b1 = node1.getLatestBlock();
			assertEquals(blk2, b1, "latest block not found");

			b3 = node3.getLatestBlock();
			assertEquals(blk2, b3, "latest block not found");

			// Test detecting third block

			Block blk3 = new Block(blk2.getHash(), node3.getNumID(), node3.getAddress(), 2, params.getLevels());
			node3.insertBlock(blk3, blk2.getAddress());

			b1 = node1.getLatestBlock();
			assertEquals(blk3, b1, "latest block not found");

			b2 = node2.getLatestBlock();
			assertEquals(blk3, b2, "latest block not found");

			// TODO: Test with transactions of similar nameID

		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	@Ignore
	void testGetLatestTransaction() {
		/*
		 * Test correctness of returned value by different cases:
		 * - Make a node insert transaction pointers for several other node and test the
		 * 	 correctness of the returned result by calling from different nodes
		 */
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
	void testGetValidators() {
		try {
			// Test uniqueness of validators
			LightChainNode node1 = new LightChainNode(params,RMIPort1,Const.DUMMY_INTRODUCER,true);
			
			/*
			 * Test behavior given different parameters of ALPHA
			 */
			
		}catch(RemoteException e) {
			e.printStackTrace();
		}
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


	@Test
	void testGetOwnerPublicKey() {
		try {
			LightChainNode node1 = new LightChainNode(params, RMIPort1, Const.DUMMY_INTRODUCER, true);
			LightChainNode node2 = new LightChainNode(params, RMIPort2, node1.getAddress(), false);
			LightChainNode node3 = new LightChainNode(params, RMIPort3, node1.getAddress(), false);

			PublicKey pk2 = node1.getOwnerPublicKey(node2.getNumID());
			PublicKey pk3 = node1.getOwnerPublicKey(node3.getNumID());

			assertEquals(node2.getPublicKey(), pk2, "wrong public key found");
			assertEquals(node3.getPublicKey(), pk3, "wrong public key found");

		} catch (RemoteException e) {

		}
	}

}
