package skipGraph;

import static org.junit.jupiter.api.Assertions.*;

import java.rmi.RemoteException;
import java.util.List;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import util.Const;

class SkipNodeTest {
	
	// TODO: add tests to cover duplicate nameID scenarios
	// TODO: add tests to cover more data nodes scenarios

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
	private NodeConfig initialConfig;
	private NodeConfig config1;
	private NodeConfig config2;
	private NodeConfig sameNameIDConfig1;
	private NodeConfig sameNameIDConfig2;
	private NodeConfig sameNameIDConfig3;

	@BeforeEach
	public void init() {
		initialConfig = new NodeConfig(maxLevels, port++, numID2, nameID2);
		config1 = new NodeConfig(maxLevels, port++, numID1, nameID1);
		config2 = new NodeConfig(maxLevels, port++, numID3, nameID3);

		sameNameIDConfig1 = new NodeConfig(maxLevels, port++, numID4, nameID4);
		sameNameIDConfig2 = new NodeConfig(maxLevels, port++, numID5, nameID4);
		sameNameIDConfig3 = new NodeConfig(maxLevels, port++, numID6, nameID4);
	}

	@Test
	void testDelete() {

		// test if deletion of data nodes is successful
		try {

			SkipNode node = new SkipNode(initialConfig, Const.DUMMY_INTRODUCER, true);
			node.insertDataNode(numID1, nameID1);
			node.insertDataNode(numID3, nameID3);
			node.insertDataNode(numID4, nameID4);

			node.delete(numID1);

			// delete first dataNode
			assertNull(node.getNode(numID1));
			assertNotNull(node.getNode(numID3));
			assertNotNull(node.getNode(numID4));

			node.delete(numID3);
			assertNull(node.getNode(numID3));
			assertNotNull(node.getNode(numID4));

		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	@Test
	void testInsertNode() {

		// test if position of insertion is correct;
		try {
			// test if node1 in the middle
			SkipNode node1 = new SkipNode(initialConfig, Const.DUMMY_INTRODUCER, true);
			SkipNode node2 = new SkipNode(config1, node1.getAddress(), false);
			SkipNode node3 = new SkipNode(config2, node1.getAddress(), false);

			assertEquals(node2.getPeer(), node1.getPeerLeftNode(Const.ZERO_LEVEL), "Node 2 is not left of 1");
			assertEquals(node3.getPeer(), node1.getPeerRightNode(Const.ZERO_LEVEL), "Node 3 is not right of 1 ");

		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	// Test if num ID search find exact results
	@Test
	void testSearchByNumIDExact() {
		try {

			// everything search for everything
			SkipNode node1 = new SkipNode(initialConfig, Const.DUMMY_INTRODUCER, true);
			SkipNode node2 = new SkipNode(config1, node1.getAddress(), false);
			SkipNode node3 = new SkipNode(config2, node1.getAddress(), false);

			NodeInfo res1 = node1.searchByNumID(node2.getNumID());
			assertEquals(node2.getPeer(), res1, "node 2 not found");

			NodeInfo res2 = node1.searchByNumID(node3.getNumID());
			assertEquals(node3.getPeer(), res2, "node 3 not found");

			NodeInfo res3 = node2.searchByNumID(node1.getNumID());
			assertEquals(node1.getPeer(), res3, "node 1 not found");

			NodeInfo res4 = node2.searchByNumID(node3.getNumID());
			assertEquals(node3.getPeer(), res4, "node 3 not found");

			NodeInfo res5 = node3.searchByNumID(node1.getNumID());
			assertEquals(node1.getPeer(), res5, "node 1 not found");

			NodeInfo res6 = node3.searchByNumID(node2.getNumID());
			assertEquals(node2.getPeer(), res6, "node 2 not found");

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	// Test if name ID search finds exact results
	@Test
	void testSearchByNameIDExact() {
		try {

			// everything search for everything
			SkipNode node1 = new SkipNode(initialConfig, Const.DUMMY_INTRODUCER, true);
			SkipNode node2 = new SkipNode(config1, node1.getAddress(), false);
			SkipNode node3 = new SkipNode(config2, node1.getAddress(), false);

			NodeInfo res1 = node1.searchByNameID(node2.getNameID());
			assertEquals(node2.getPeer(), res1, "node 2 not found");

			NodeInfo res2 = node1.searchByNameID(node3.getNameID());
			assertEquals(node3.getPeer(), res2, "node 3 not found");

			NodeInfo res3 = node2.searchByNameID(node1.getNameID());
			assertEquals(node1.getPeer(), res3, "node 1 not found");

			NodeInfo res4 = node2.searchByNameID(node3.getNameID());
			assertEquals(node3.getPeer(), res4, "node 3 not found");

			NodeInfo res5 = node3.searchByNameID(node1.getNameID());
			assertEquals(node1.getPeer(), res5, "node 1 not found");

			NodeInfo res6 = node3.searchByNameID(node2.getNameID());
			assertEquals(node2.getPeer(), res6, "node 2 not found");

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	// Test if name ID search detects similarity properly
	@Test
	void testSearchByNameIDSimilarity() {

		try {
			SkipNode node1 = new SkipNode(initialConfig, Const.DUMMY_INTRODUCER, true);
			SkipNode node2 = new SkipNode(config1, node1.getAddress(), false);
			SkipNode node3 = new SkipNode(config2, node1.getAddress(), false);

			NodeInfo res1 = node1.searchByNameID("111");
			assertEquals(node3.getPeer(), res1, "incorrect node found");
			// check if node finds itself
			NodeInfo res2 = node3.searchByNameID("111");
			assertEquals(node3.getPeer(), res2, "node could not find itself");

			NodeInfo res3 = node2.searchByNameID("000");
			assertEquals(node1.getPeer(), res3, "incorrect node found");
			// check if node finds itself
			NodeInfo res4 = node1.searchByNameID("000");
			assertEquals(node1.getPeer(), res4, "node could not find itself");

			NodeInfo res5 = node3.searchByNameID("010");
			assertEquals(node2.getPeer(), res5, "incorrect node found");
			// check if node finds itself
			NodeInfo res6 = node2.searchByNameID("010");
			assertEquals(node2.getPeer(), res6, "node could not find itself");

		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	// Test if testGetNodesWithNameID returns correct results
	@Test
	void testGetNodesWithNameID() {
		try {
			SkipNode node1 = new SkipNode(initialConfig, Const.DUMMY_INTRODUCER, true);
			SkipNode node2 = new SkipNode(sameNameIDConfig1, node1.getAddress(), false);
			SkipNode node3 = new SkipNode(sameNameIDConfig2, node1.getAddress(), false);
			SkipNode node4 = new SkipNode(sameNameIDConfig3, node1.getAddress(), false);

			List<NodeInfo> list = node1.getNodesWithNameID(node2.getNameID());

			assertEquals(list.size(), 3);

			boolean exists2 = false;
			boolean exists3 = false;
			boolean exists4 = false;

			for (NodeInfo el : list) {
				if (el.equals(node2.getPeer()))
					exists2 = true;
				if (el.equals(node3.getPeer()))
					exists3 = true;
				if (el.equals(node4.getPeer()))
					exists4 = true;
			}

			assertTrue(exists2, "node 2 was not found");
			assertTrue(exists3, "node 3 was not found");
			assertTrue(exists4, "node 4 was not found");

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
