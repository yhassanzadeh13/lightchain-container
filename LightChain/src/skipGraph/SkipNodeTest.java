package skipGraph;

import static org.junit.jupiter.api.Assertions.*;

import java.rmi.RemoteException;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import util.Const;

class SkipNodeTest {

	private static int port = 7000;
	private int maxLevels = 3;
	private int numID1 = 1;
	private int numID2 = 2;
	private int numID3 = 3;
	private int numID4 = 4;
	private int numID5 = 5;
	private int NumID6 = 6;
	private String nameID1 = "011";
	private String nameID2 = "001";
	private String nameID3 = "100";
	private String nameID4 = "111";
	private String nameID5 = "101";
	private String nameID6 = "010";
	private NodeConfig initialNodeConfig ;
	private NodeConfig config1 ;
	private NodeConfig config2 ;
	
	@BeforeEach
	public void init() {
		initialNodeConfig = new NodeConfig(maxLevels,port++,numID2,nameID2);
	    config1 = new NodeConfig(maxLevels,port++,numID1,nameID1);
	    config2 = new NodeConfig(maxLevels,port++,numID3,nameID3);
	}

	@Test
	void testDelete() {
		
		// test if deletion of data nodes is successful
		try {

			SkipNode node = new SkipNode(initialNodeConfig,Const.DUMMY_INTRODUCER,true);
			node.insertDataNode(numID1, nameID1);
			node.insertDataNode(numID3, nameID3);
			node.insertDataNode(numID4,nameID4);
			
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
			SkipNode node1 = new SkipNode(initialNodeConfig,Const.DUMMY_INTRODUCER,true);
			SkipNode node2 = new SkipNode(config1,node1.getAddress(),false);
			SkipNode node3 = new SkipNode(config2,node1.getAddress(),false);

			assertEquals(node2.getPeer(),node1.getPeerLeftNode(Const.ZERO_LEVEL),"Node 2 is not left of 1");
			assertEquals(node3.getPeer(),node1.getPeerRightNode(Const.ZERO_LEVEL),"Node 3 is not right of 1 ");

			
		}catch(RemoteException e) {
			e.printStackTrace();
		}
		
	}

	@Test
	void testSearchByNumID() {
		try {
			
			// everything search for everything
			SkipNode node1 = new SkipNode(initialNodeConfig,Const.DUMMY_INTRODUCER,true);
			SkipNode node2 = new SkipNode(config1,node1.getAddress(),false);
			SkipNode node3 = new SkipNode(config2,node1.getAddress(),false);
			
			NodeInfo res1 = node1.searchByNumID(node2.getNumID());
			assertEquals(node2.getPeer(),res1,"node 2 not found");
			
			NodeInfo res2 = node1.searchByNumID(node3.getNumID());
			assertEquals(node3.getPeer(),res2,"node 3 not found");
			
			NodeInfo res3 = node2.searchByNumID(node1.getNumID());
			assertEquals(node1.getPeer(),res3,"node 1 not found");
			
			NodeInfo res4 = node2.searchByNumID(node3.getNumID());
			assertEquals(node3.getPeer(),res4,"node 3 not found");
			
			NodeInfo res5 = node3.searchByNumID(node1.getNumID());
			assertEquals(node1.getPeer(),res5,"node 1 not found");
			
			NodeInfo res6 = node3.searchByNumID(node2.getNumID());
			assertEquals(node2.getPeer(),res6,"node 2 not found");
			
		}catch(RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testSearchByNameID() {
		try {
			
			// everything search for everything
			SkipNode node1 = new SkipNode(initialNodeConfig,Const.DUMMY_INTRODUCER,true);
			SkipNode node2 = new SkipNode(config1,node1.getAddress(),false);
			SkipNode node3 = new SkipNode(config2,node1.getAddress(),false);
			
			
			NodeInfo res1 = node1.searchByNameID(node2.getNameID());
			assertEquals(node2.getPeer(),res1,"node 2 not found");
			
			NodeInfo res2 = node1.searchByNameID(node3.getNameID());
			assertEquals(node3.getPeer(),res2,"node 3 not found");
			
			NodeInfo res3 = node2.searchByNameID(node1.getNameID());
			assertEquals(node1.getPeer(),res3,"node 1 not found");
			
			NodeInfo res4 = node2.searchByNameID(node3.getNameID());
			assertEquals(node3.getPeer(),res4,"node 3 not found");
			
			NodeInfo res5 = node3.searchByNameID(node1.getNameID());
			assertEquals(node1.getPeer(),res5,"node 1 not found");
			
			NodeInfo res6 = node3.searchByNameID(node2.getNameID());
			assertEquals(node2.getPeer(),res6,"node 2 not found");
			
		}catch(RemoteException e) {
			e.printStackTrace();
		}
	}

	@Ignore
	void testGetNodesWithNameID() {
		fail("Not yet implemented");
	}

}
