package blockchain;

//import jdk.nashorn.internal.ir.annotations.Ignore;
import java.io.*;
import evm.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import underlay.Underlay;
import underlay.rmi.RMIUnderlay;
import util.Const;
import util.Util;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.util.BytecodeCompiler;
import org.ethereum.vm.util.HexUtil;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LightChainNodeTest {

	private static int port = 7000;
	private Parameters params;
	private int RMIPort1;
	private int RMIPort2;
	private int RMIPort3;
	private int RMIPort4;
	private RepositoryMock repository = new RepositoryMock();
	private ContractTransaction tesq = new ContractTransaction();

	private LightChainNode node1;
	private LightChainNode node2;
	private LightChainNode node3;
	private LightChainNode node4;

	private Underlay underlay1;
	private Underlay underlay2;
	private Underlay underlay3;
	private Underlay underlay4;


	@BeforeEach
	void init() {
		params = new Parameters();

		RMIPort1 = port++;
		RMIPort2 = port++;
		RMIPort3 = port++;
		RMIPort4 = port++;

		underlay1 = new RMIUnderlay(RMIPort1);
		underlay2 = new RMIUnderlay(RMIPort2);
		underlay3 = new RMIUnderlay(RMIPort3);
		underlay4 = new RMIUnderlay(RMIPort4);

		node1 = new LightChainNode(params, RMIPort1, Const.DUMMY_INTRODUCER, true, underlay1);
		node2 = new LightChainNode(params, RMIPort2, node1.getAddress(), false, underlay2);
		node3 = new LightChainNode(params, RMIPort3, node1.getAddress(), false, underlay3);
		node4 = new LightChainNode(params, RMIPort4, node1.getAddress(), false, underlay4);

	}

	@AfterEach
	void destroy(){
		underlay1.terminate();
		underlay2.terminate();
		underlay3.terminate();
		underlay4.terminate();
	}


	@Test
	void testUpdateView() {
		try {

			// test view update gotten by 3 transaction pointing at genesis block

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

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// This test fails
	@Test
	void testMineAttempt() {
		try {
			/*
			 * Scenario: with genesis block, 3 nodes each insert a transaction then node1
			 * attempts to mine a block, using very basic parameters TxMin = 1 : 1
			 * transaction is enough to cast a block Alpha = 10 : give space for extra
			 * attempts SignaturesThreshold = 1 : 1 validator is enough. These parameters
			 * can be tweaked to test different values
			 */
			params.setTxMin(1);
			params.setAlpha(10);
			params.setSignaturesThreshold(1);

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

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void testGetLatestBlockExtensively() {
		try {

			List<LightChainNode> nodeList = new ArrayList<>();
			nodeList.add(node1);
			nodeList.add(node2);
			nodeList.add(node3);
			nodeList.add(node4);

			int iterations = 100;

			Block latestBlock = node1.insertGenesis();

			for (int i = 0; i < iterations; ++i) {
				for (int j = 0; j < nodeList.size(); ++j) {
					String randStr = Util.getRandomString(10);
					Transaction t = new Transaction(latestBlock.getHash(), nodeList.get(j).getNumID(), randStr,
							nodeList.get(j).getAddress(), params.getLevels());
					nodeList.get(j).insertTransaction(t);
				}
				
				for(int j = 0 ; j < nodeList.size(); ++j) {
					Block testBlock = nodeList.get(j).getLatestBlock();
					assertEquals(latestBlock,testBlock,"latest block wrong");
					Util.log("Comparing " + latestBlock.getNumID() + " and " + testBlock.getNumID());
				}
				
				if (i % 2 == 0) {
					int index = i % nodeList.size();
					Block blk = new Block(latestBlock.getHash(), nodeList.get(index).getNumID(),
							nodeList.get(index).getAddress(), latestBlock.getIndex(), params.getLevels());
					 nodeList.get(index).insertBlock(blk, latestBlock.getAddress());
					 latestBlock = blk;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void testGetLatestBlock() {

		try {

			// test detecting genesis block

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

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

//	@Ignore
//	void testGetLatestTransaction() {
//		/*
//		 * Test correctness of returned value by different cases: - Make a node insert
//		 * transaction pointers for several other node and test the correctness of the
//		 * returned result by calling from different nodes
//		 */
//	}
//
//	@Ignore
//	void testValidateBlock() {
//		fail("Not yet implemented");
//	}
//
//	@Ignore
//	void testValidateTransaction() {
//		fail("Not yet implemented");
//	}
//
//	@Ignore
//	void testPoVBlock() {
//		fail("Not yet implemented");
//	}
//
//	@Ignore
//	void testIsAuthenticatedBlock() {
//		fail("Not yet implemented");
//	}
//
//	@Ignore
//	void testIsConsistent() {
//		fail("Not yet implemented");
//	}
//
//	@Ignore
//	void testGetValidators() {
//		try {
//			// Test uniqueness of validators
//			LightChainNode node1 = new LightChainNode(params, RMIPort1, Const.DUMMY_INTRODUCER, true);
//
//			/*
//			 * Test behavior given different parameters of ALPHA
//			 */
//
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//	}
//
//	@Ignore
//	void testPoVTransaction() {
//		fail("Not yet implemented");
//	}
//
//	@Ignore
//	void testIsSound() {
//		fail("Not yet implemented");
//	}
//
//	@Ignore
//	void testIsCorrect() {
//		fail("Not yet implemented");
//	}
//
//	@Ignore
//	void testIsAuthenticatedTransaction() {
//		fail("Not yet implemented");
//	}
//
//	@Ignore
//	void testHasBalanceCompliance() {
//		fail("Not yet implemented");
//	}

	/**
	* Scenario: We create two nodes, one generating a block, and the other one generating a transaction.
	* Then we evaluate that the transaction should be correct from the view point of node 2. This test is for checking the isCorrect() method in smart-contract mode.
	* We will be checking if our contract sends back correct value to the wrapper function.
	* 
	* Here we test ContractCV.
	*/
	@Test
    void testIsCorrectContractMode() {
        try {
            Block blk = node1.insertGenesis();
            String randStr1 = Util.getRandomString(10);
            
            Transaction t2 = new Transaction(blk.getHash(), node2.getNumID(), randStr1, node2.getAddress(), params.getLevels());
            node2.insertTransaction(t2);
            
            ContractCV cv = new ContractCV(node2);
            assertEquals(true, cv.isCorrect(t2), "working correctly");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


	/**
	* Scenario: We create two nodes, one generating a block, and the other one generating a transaction.
	* Then we evaluate that the transaction should be correct from the view point of node 2. 
	* This test is for checking the isCorrect() method in original Lightchain mode.
	*
	* Here we test LightChainCV.
	*/
	@Test
    void testIsCorrectOriginalMode() {
        try {
            Block blk = node1.insertGenesis();
            String randStr1 = Util.getRandomString(10);
            
            Transaction t2 = new Transaction(blk.getHash(), node2.getNumID(), randStr1, node2.getAddress(), params.getLevels());
            node2.insertTransaction(t2);
            
            LightChainCV cv = new LightChainCV(node2);
            
            assertEquals(true, cv.isCorrect(t2), "working correctly");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	* Scenario: We have passed an account address to create a new account, 
	* then we have complied a solidity contract code and saved it using saveCode() function,
	* we have passed a value using the putStorageRow() function, 
	* also we have added some balance to the address using the addBalance() function.
	*
	* Finally we have checked if all the values are returned correctly or not.
	*/
	@Test
    void testRepository() {
        DataWord key1 = DataWord.of(999);
        DataWord value1 = DataWord.of(3);
		BigInteger balance = BigInteger.TEN.pow(18);

        // Set contract into Database
        String address = "77045e71a7a2c50903d88e564cd72fab11e82051";
        String code = "PUSH2 0x03e7 SLOAD PUSH1 0x00 MSTORE PUSH1 0x00 PUSH1 0x00 MLOAD GT ISZERO PUSH4 0x0000004c JUMPI PUSH1 0x01 PUSH1 0x00 MLOAD SUB PUSH2 0x03e7 SSTORE PUSH1 0x00 PUSH1 0x00 PUSH1 0x00 PUSH1 0x00 PUSH1 0x00 PUSH20 0x"+ address + " PUSH1 0x08 PUSH1 0x0a GAS DIV MUL CALL PUSH4 0x0000004c STOP JUMP JUMPDEST STOP";

        byte[] addressB = HexUtil.fromHexString(address);
        byte[] codeB = BytecodeCompiler.compile(code);

        repository.createAccount(addressB);
        repository.saveCode(addressB, codeB);
        repository.putStorageRow(addressB, key1, value1); // Setting value
        repository.addBalance(addressB,balance); // Adding balance to the address

		assertEquals(balance,repository.getBalance(addressB), "working correctly");
        assertTrue(repository.exists(addressB), "working correctly");
        assertEquals(codeB,repository.getCode(addressB), "stored code correctly");
        assertEquals(value1,repository.getStorageRow(addressB,key1), "stored data correctly");
    }

	/**
	* Scenario: We will be passing the contract name as well as the function in the contract that we want to test.
	* Here the check() function takes a single parameter which is a integer (token) and checks weather the provided 
	* value is greater than 10. If so then returns TRUE.
	*/
	@Test
	void testContractTransaction1() throws IOException {
		// For testcon.sol
		tesq.setup();
		String contractName1 = "testcon.sol";
		String functname1 = "check(uint256)";
		int token = 25;

		boolean value = tesq.TransctSol(token, contractName1, functname1);
		assertEquals(true, value);
	}

	@Test
	void testGetOwnerPublicKey() {
		try {
			PublicKey pk2 = node1.getOwnerPublicKey(node2.getNumID());
			PublicKey pk3 = node1.getOwnerPublicKey(node3.getNumID());

			assertEquals(node2.getPublicKey(), pk2, "wrong public key found");
			assertEquals(node3.getPublicKey(), pk3, "wrong public key found");

		} catch (Exception e) {

		}
	}

}
