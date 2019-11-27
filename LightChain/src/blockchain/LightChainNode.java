package blockchain;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import hashing.Hasher;
import hashing.HashingTools;
import remoteTest.TestingLog;
import signature.DigitalSignature;
import signature.SignedBytes;
import skipGraph.NodeConfig;
import skipGraph.NodeInfo;
import skipGraph.SkipNode;
import util.Const;
import util.Util;

public class LightChainNode extends SkipNode implements LightChainRMIInterface {

	private static final long serialVersionUID = 1L;
	private List<Transaction> transactions;
	private List<Block> blocks;
	private DigitalSignature digitalSignature;
	private Hasher hasher;
	private View view;
	private boolean mode;
	private int balance = 20;

	/**
	 * A constructor for LightChainNode.
	 * 
	 * @param config     contains necessary information for the node to function
	 * @param introducer the address of the introducer node
	 * @param isInitial  a flag signaling whether this node is the first node in the
	 *                   network
	 */
	public LightChainNode(NodeConfig config, String introducer, boolean isInitial) throws RemoteException {
		super(config, introducer, isInitial);

		Registry reg = LocateRegistry.createRegistry(config.getRMIPort());
		reg.rebind("RMIImpl", this);
		Util.log("Rebinding Successful");

		digitalSignature = new DigitalSignature();
		hasher = new HashingTools();
		transactions = new ArrayList<>();
		blocks = new ArrayList<>();
		view = new View();

		String name = hasher.getHash(digitalSignature.getPublicKey().getEncoded(), Const.TRUNC);
		super.setNumID(Integer.parseInt(name, 2));
		name = hasher.getHash(name, Const.TRUNC);
		super.setNameID(name);
	}

	/**
	 * This method goes to the tail of the blockchain and iterates over the
	 * transactions pointing at it, and then updating the entries corresponding to
	 * the owners of the transactions in the view table.
	 */
	void updateViewTable() {
		try {
			// get the tail of blockchain
			Block blk = getLatestBlock();
			// change numID to nameID format to search for transactions
			String name = numToName(blk.getNumID());
			// get transactions pointing at the tail
			List<Transaction> tList = getTransactionsWithNameID(name);
			if (tList == null)
				return;
			// iterate over found transactions pointing at the blockchain
			for (int i = 0; i < tList.size(); ++i) {
				int owner = tList.get(i).getOwner();
				view.updateLastBlk(owner, blk.getNumID());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method finds the latest block on the blockchain and takes its numID and
	 * uses it to find all transactions that have this numID as nameID and uses
	 * getTransactionsWithNameID() method to get such transactions, and if the
	 * number of found transactions is at least TX_MIN, the transactions are casted
	 * into a block and the block is sent for validation.
	 * 
	 */
	public void viewUpdate() throws RemoteException {

		try {

			Block blk = getLatestBlock();
			// Change numID to a nameID string
			if (blk == null) {
				Util.log("Error in retreiving the Latest block: not a block was returned");
				Util.log("viewUpdate terminated");
				return;
			}

			String name = numToName(blk.getNumID());

			// Get all transaction with this nameID
			List<Transaction> tList = getTransactionsWithNameID(name);
			// If number of transactions obtained is less than TX_MIN then we terminate the
			// process
			if (tList == null || tList.size() < Const.TX_MIN) {
				Util.log("Cannot find TX_MIN number of transactions.");
				Util.log("Found " + tList.size() + "Transactions");
				return;
			}

			// If there are TX_MIN transaction then add them into a new block
			Block newBlk = new Block(blk.getH(), getNumID(), getAddress(), tList, blk.getIndex() + 1);
			// send the new block for PoV validation
			boolean isValidated = validateBlock(newBlk);

			if (!isValidated) {
				Util.log("Block validation failed");
				return;
			}

			// insert new block after it was validated
			insertBlock(newBlk);
			// contact the owner of the previous block and let him withdraw his flag data
			// node.
			if (blk.getAddress().equals(getAddress())) {
				delete(Const.ZERO_ID);
			} else {
				LightChainRMIInterface prevOwnerRMI = getLightChainRMI(blk.getAddress());
				prevOwnerRMI.removeFlagNode();
			}

			// insert flag node for this block
			insertFlagNode(newBlk);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * inserts a flag node that points to the latest block
	 * 
	 * @param blk the block for which a flag node will be inserted
	 */
	private void insertFlagNode(Block blk) {
		super.insertDataNode(Const.ZERO_ID, blk.getH());
	}

	/**
	 * removes the flag node pointing to the latest block that was inserted by this
	 * node
	 */
	public void removeFlagNode() throws RemoteException {
		super.delete(Const.ZERO_ID);
	}
	
	/**
	 * inserts a new transaction into the overlay
	 * @param t transaction to be inserted
	 */
	private void insertTransaction(Transaction t) {
		super.insertNode(t);
	}
	
	/**
	 * inserts a new block into the overlay
	 * @param blk block to be inserted into the overlay
	 */
	private void insertBlock(Block blk) {
		super.insertNode(blk);
	}
	
	/**
	 * inserts the first block to the blockchain
	 */
	public void insertGenesis() throws RemoteException {
		StringBuilder st = new StringBuilder();
		for (int i = 0; i < Const.TRUNC; i++) {
			st.append("0");
		}
		String prev = st.toString();
		int index = 0;
		Block b = new Block(prev, getNumID(), getAddress(), index);
		insertFlagNode(b);
		insertBlock(b);
	}

	/**
	 * This method finds the latest block on the blockchain.
	 * 
	 * @return the latest block on the ledger
	 * 
	 *         TODO: this should be refactored
	 */
	public Block getLatestBlock() throws RemoteException {
		try {
			NodeInfo flag = searchByNumID(Const.ZERO_ID);
			NodeInfo blk = searchByNumID(Integer.parseInt(flag.getNameID(), 2));
			if (blk instanceof Block)
				return (Block) blk;
			else {
				Util.log(blk.getNumID() + " we returned as latest block");
				Util.log("Error in getLatestBlock(): instance returned is not a block");
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This method gets the numID of a peer and returns its latest transactions.
	 * 
	 * @return the latest transaction for a particular owner
	 */
	// TODO: this should be refactored, also check where it is being called
	public Transaction getLatestTransaction(int num) throws RemoteException {

		try {
			Transaction t = (Transaction) searchByNameID(numToName(num));
			return t;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * This function retreives transactions with a certain nameID
	 * 
	 * @param name the nameID for which we want to collect transactions that have it
	 *             as nameID
	 * @return a list of transactions that have name as nameID
	 */
	public List<Transaction> getTransactionsWithNameID(String name) {

		List<NodeInfo> list = getNodesWithNameID(name);
		List<Transaction> tList = new ArrayList<>();

		for (NodeInfo t : list) {
			if (t instanceof Transaction)
				tList.add((Transaction) t);
		}
		return tList;
	}

	/**
	 * This method is called by a node to validate a block that it has created.
	 * First it gets the validators of the block, and then contacts each validator
	 * and asks them to validate the block using PoV and then gets their signatures
	 * of the hash value of the block if the validation was successful and gets null
	 * otherwise.
	 * 
	 * @param blk is the block to be validated
	 * @return true of block is valid, and false if block is not valid
	 */
	public boolean validateBlock(Block blk) {
		try {

			List<NodeInfo> validators = getValidators(blk);

			// add the owner's signature to the block
			SignedBytes mySignature = digitalSignature.signString(blk.getH());

			blk.addSignature(mySignature);
			// iterate over validators and ask them to validate the block
			for (int i = 0; i < validators.size(); ++i) {
				LightChainRMIInterface node = getLightChainRMI(validators.get(i).getAddress());
				// TODO: add a dummy signedBytes value
				SignedBytes signature = node.PoV(blk);
				// if one validator returns null, then validation has failed
				if (signature == null) {
					Util.log("Validating Block failed.");
					return false;
				}
				blk.addSignature(signature);
			}
			// update the sigma array of the block with all the signatures
			Util.log("Validation of block is successful");

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * The method is called by a node to validate a transaction that it has created
	 * First it gets the validators of the transaction, and then contacts each of
	 * them to get their signatures and stores their signatures in the sigma list.
	 * 
	 * @param t transaction to be validated
	 * 
	 * @return true if transaction is valid, and false if it is not valid
	 */
	public boolean validateTransaction(Transaction t) throws RemoteException {

		int isAuthenticated = 0;
		int isSound = 0;
		int isCorrect = 0;
		int hasBalance = 0;

		try {

			// obtain validators
			ArrayList<NodeInfo> validators = getValidators(t);

			// add the owner's signature of the transaction's hash value to the sigma
			SignedBytes mySignature = digitalSignature.signString(t.getH());

			t.addSignature(mySignature);

			boolean validated = true;

			// iterate over validators and use RMI to ask them to validate the transaction
			for (int i = 0; i < validators.size(); ++i) {
				LightChainRMIInterface node = getLightChainRMI(validators.get(i).getAddress());
				SignedBytes signature = node.PoV(t);

				if (signature.isAuth())
					isAuthenticated++;
				if (signature.isCorrect())
					isCorrect++;
				if (signature.isSound())
					isSound++;
				if (signature.hasBalance())
					hasBalance++;

				// if a validators returns null that means the validation has failed
				if (signature.getBytes() == null) {
					validated = false;
				}
				t.addSignature(signature);
			}

			if (validated) {
				Util.log("Validation Successful");
			} else
				Util.log("Validated failed.");

			return validated;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This method is used to validate a block. It checks : 1) Authenticity 2)
	 * Consistency 3) Authenticity and soundness of the transactions it contains.
	 * 
	 * @param blk is block to be validated using proof of validation
	 * 
	 * @return signature of validator in case block is valid, or null if block is
	 *         invalid
	 */
	public SignedBytes PoV(Block blk) throws RemoteException {
		try {
			updateViewTable();
			Util.log("Validating block for " + blk.getOwner());
			boolean isAuth = isAuthenticated(blk);
			boolean isCons = isConsistent(blk);
			boolean val = isAuth && isCons;
			if (val == false)
				return new SignedBytes(null, isAuth, true, true, true);
			// iterate over transactions and check them one by one
			List<Transaction> ts = blk.getS();
			for (int i = 0; i < ts.size(); ++i) {
				if (!isAuthenticated(ts.get(i)) /* || !isSound(ts.get(i)) */) {
					Util.log("Transaction inside block is not authentic");
					return new SignedBytes(null, isAuth, true, true, true);
				}
			}
			Util.log("Block Validation Successful");
			SignedBytes signedHash = new SignedBytes(digitalSignature.signString(blk.getH()).getBytes(), isAuth, true,
					true, true);
			return signedHash;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This method recieves a block and checks if: 1) its hash value is generated
	 * properly 2) checks if it contains the signature of its owner
	 * 
	 * @param blk is block whose authenticity is to be checked
	 * 
	 * @return true if block is authentic, or false if block is unauthentic
	 */
	public boolean isAuthenticated(Block blk) throws RemoteException {
		try {

			// concatenate the string values of transactions
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < blk.getS().size(); ++i) {
				sb.append(blk.getS().get(i).toString());
			}
			// generate the hash value and then compare it with the block's
			String hash = hasher.getHash(blk.getPrev() + blk.getOwner() + sb.toString(), Const.TRUNC);
			if (!hash.equals(blk.getH())) {
				Util.log("Hash value of block not generated properly");
				return false;
			}
			// get the sigma array of the block
			List<SignedBytes> blkSigma = blk.getSigma();
			// retrieve the public key of the owner of the block
			PublicKey ownerPublicKey = getOwnerPublicKey(blk.getOwner());
			boolean verified = false;
			if (ownerPublicKey == null)
				return false;
			// iterate over the sigma array looking for the signature of the owner
			for (int i = 0; i < blkSigma.size(); ++i) {
				boolean is = digitalSignature.verifyString(hash, blkSigma.get(i), ownerPublicKey);
				if (is)
					verified = true;
			}
			if (verified == false) {
				Util.log("Block does not contain signature of Owner");
			}

			return verified;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * A block is said to be consistent if its prev points to the current tail of
	 * the block, so at the time of validation, validators should make sure that
	 * prev is still pointing to the tail of the blockchain, or otherwise if the
	 * tail was updated is should terminate the validation process.
	 * 
	 * @param blk whose consistency is to be checked
	 * 
	 * @return true of block is consistent, or false if it is not consistent
	 */
	public boolean isConsistent(Block blk) throws RemoteException {
		try {

			Block lstBlk = getLatestBlock();
			boolean res = blk.getPrev().equals(lstBlk.getH());
			if (res == false) {
				Util.log("Block not consistent");
			}

			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This method returns the validators of a given block
	 * 
	 * @param blk block whose validators are to be found
	 * @return list of validators of the given block
	 */
	public List<NodeInfo> getValidators(Block blk) {
		try {
			// stores the final list of validators
			List<NodeInfo> validators = new ArrayList<>();
			// stores the address of the taken nodes so that we make sure we do not take a
			// node twice
			Map<String, Integer> taken = new HashMap<>();
			// To not take the node itself or any data node belonging to it.
			taken.put(getAddress(), 1);
			int count = 0, i = 0;
			// TODO: determine what to do in case we have reached ALPHA number of iterations
			// but not collected enough validators
			// keep iterating until we get SIGNATURES_THRESHOLD number of validators
			while (count < Const.SIGNATURES_THRESHOLD && i <= Const.ALPHA) {
				String hash = blk.getPrev() + blk.getOwner() + blk.getS().toString() + i;
				int num = Integer.parseInt(hasher.getHash(hash, Const.TRUNC), 2);
				NodeInfo node = searchByNumID(num);
				i++;
				// if already taken or equals the owner's node, then keep iterating.
				if (taken.containsKey(node.getAddress()))
					continue;
				count++;
				taken.put(node.getAddress(), 1);
				validators.add(node);
			}
			return validators;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Checks the soundness, correctness and authenticity of a transaction using
	 * other methods. Checks if the owner of the transaction has enough balance to
	 * cover validation fees. returns null in case any of the tests fails, otherwise
	 * it signs the hashvalue of the transaction and sends the signed value to the
	 * owner.
	 * 
	 * @param t transaction to be validated by proof of validation
	 * @return signature of validators in case transaction is valid, or null if not
	 */
	public SignedBytes PoV(Transaction t) throws RemoteException {
		boolean isAuth = false;
		boolean isCorrect = false;
		boolean isSound = false;
		boolean hasBalance = false;
		try {
			updateViewTable();
			Util.log("Validating Transaction for " + t.getOwner());
			isAuth = isAuthenticated(t);
			isCorrect = isCorrect(t);
			isSound = isSound(t);
			hasBalance = hasBalanceCompliance(t);

			boolean val = isAuth && isCorrect && isSound && hasBalance;
			if (val == false)
				return new SignedBytes(null, isAuth, isSound, isCorrect, hasBalance);
			Util.log("Transaction Validation Successful");
			SignedBytes signedHash = new SignedBytes(digitalSignature.signString(t.getH()).getBytes(), isAuth, isSound,
					isCorrect, hasBalance);
			return signedHash;
		} catch (Exception e) {
			e.printStackTrace();
			return new SignedBytes(null, isAuth, isSound, isCorrect, hasBalance);
		}
	}

	/**
	 * This method validated the soundness of a given transaction It checks if the
	 * given transaction is associated with a block which does not precede the block
	 * that contains the transaction's owner's last transaction.
	 * 
	 * @param t transaction whose soundness is to be checked
	 * @return true if transaction is sound, or false if it is not
	 */
	public boolean isSound(Transaction t) {
		try {

			int prev = Integer.parseInt(t.getPrev(), 2);
			// TODO: rethink this assumption
			// assuming that if a node is not in the view yet then this is the first
			// transaction;
			if (!view.hasLastBlkEntry(t.getOwner())) {
				view.updateLastBlk(t.getOwner(), prev);
				return true;
			}

			// get the hash value of the block that contains the owner's latest transaction
			int blkNumID = view.getLastBlk(t.getOwner());

			// TODO: remove these checks after implementing type search
			NodeInfo b1 = searchByNumID(prev);
			if (!(b1 instanceof Block)) {
				Util.log("isSound(): search for prev did not return a block: " + prev);
				Util.log("It returned numID: " + b1.getNameID());
				return false;
			}
			NodeInfo b2 = searchByNumID(blkNumID);
			if (!(b2 instanceof Block)) {
				Util.log("isSound(): search for latest block of owner did not return a block: " + prev);
				Util.log("It returned numID: " + b2.getNameID());
				return false;
			}

			Block prevBlk = (Block) b1;
			Block thisBlk = (Block) b2;

			int tIdx = prevBlk.getIndex();
			int bIdx = thisBlk.getIndex();

			Util.log("Index of prev: " + tIdx);
			Util.log("Index of latest: " + bIdx);
			if (tIdx <= bIdx) {
				Util.log("Transaction not sound");
				Util.log("Index of prev: " + tIdx);
				Util.log("Index of latest: " + bIdx);
			} else
				Util.log("Transaction is sound");

			return tIdx > bIdx;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Checks correctness of transactionReturns true if both nodes are of same type
	 * (HONEST,HONEST) or (MALICIOUS,MALICIOUS) and returns false if both are of
	 * different types.
	 * 
	 * @param t transaction whose correctness is to be verified
	 * @return true if transaction is correct, or false if not
	 */
	public boolean isCorrect(Transaction t) {
		try {

			if (!view.hasModeEntry(t.getOwner())) {
				LightChainRMIInterface rmi = getLightChainRMI(t.getAddress());
				boolean ownerMode = rmi.getMode();
				view.updateMode(t.getOwner(), ownerMode);
				return ownerMode == mode;
			}
			boolean ownerMode = view.getMode(t.getOwner());
			if (ownerMode != mode) {
				Util.log("Transaction not correct");
			}

			return ownerMode == mode;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This method receives a transaction and verifies two things: 1- The hashvalue
	 * of the transaction was generated according to the correct equation 2- The
	 * sigma of the transaction contains the transaction's owner signed value of the
	 * hashvalue
	 * 
	 * @param t transaction whose authenticity is to be verified
	 * @return true if transaction is authentic, or false if not.
	 */
	public boolean isAuthenticated(Transaction t) throws RemoteException {
		try {

			// generate the hash using the equation to check if it was generated correctly
			String hash = hasher.getHash(t.getPrev() + t.getOwner() + t.getCont(), Const.TRUNC);
			// return false if it was not generated properly
			if (!hash.equals(t.getH())) {
				Util.log("Transaction hash value not generated properly");
				return false;
			}
			// now get the sigma array from transaction and iterate over signatures it
			// contains
			List<SignedBytes> tSigma = t.getSigma();
			PublicKey ownerPublicKey = getOwnerPublicKey(t.getOwner());
			boolean verified = false;
			if (ownerPublicKey == null)
				return false;
			for (int i = 0; i < tSigma.size(); ++i) {
				// if we find one signature which belongs to the owner then we set verified to
				// true
				boolean is = digitalSignature.verifyString(hash, tSigma.get(i), ownerPublicKey);
				if (is)
					verified = true;
			}
			if (verified == false) {
				Util.log("Transaction does not contain signature of owner");
			}

			return verified;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Checks if the owner of the transaction has enough balance to cover the fees
	 * of validation
	 * 
	 * @param t transaction whose owner will be checked for enough balance
	 *          compliance
	 * @return true if owner of transaction has enough balance to cover validation
	 *         fees
	 */
	public boolean hasBalanceCompliance(Transaction t) {
		try {
			long start = System.currentTimeMillis();

			if (!view.hasBalanceEntry(t.getOwner())) {
				view.updateBalance(t.getOwner(), Const.INITIAL_BALANCE);
				return true;
			}
			int ownerBalance = view.getBalance(t.getOwner());

			return ownerBalance >= Const.VALIDATION_FEES;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This method returns the validators of a given transaction
	 * 
	 * @param t transaction whose validators are to be found
	 * @return a list of validators for the given transactions
	 */
	public ArrayList<NodeInfo> getValidators(Transaction t) {
		try {
			// stores the validators to be returned
			ArrayList<NodeInfo> validators = new ArrayList<>();
			// used as a lookup to check if a node has been already added to the validators
			// array
			// because the search might return the same node more than once
			// so in order to avoid this case, when we find an already added node,
			// we repeat the search
			HashMap<String, Integer> taken = new HashMap<>();
			taken.put(address, 1);// To not take the node itself or any data node belonging to it.
			int count = 0, i = 0;
			while (count < Const.SIGNATURES_THRESHOLD && i <= Const.ALPHA) { // terminates when we get the required
																				// number of
				// validators
				String hash = t.getPrev() + t.getOwner() + t.getCont() + i;
				int num = Integer.parseInt(hasher.getHash(hash, Const.TRUNC), 2);
				NodeInfo node = searchByNumID(num);
				i++;
				if (taken.containsKey(node.getAddress()))
					continue;
				count++;
				taken.put(node.getAddress(), 1);
				validators.add(node);
			}
			return validators;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This method recieves the numID of an owner of a transaction or a block and
	 * first verifies of the given public key truly belongs to the owner by hashing
	 * the provided public key and comparing it with the given numID if the test
	 * fails it prints it to console and return null. otherwise it returns the
	 * public key of the owner.
	 * 
	 * @param num numerical ID of node whose public key is to be retrieved
	 * @return the public key of the node whose numerical ID was supplied
	 */
	public PublicKey getOwnerPublicKey(int num) throws RemoteException {
		try {
			// find owner from the network
			NodeInfo owner = searchByNumID(num);

			if (owner.getNumID() != num) {
				Util.log("GetOwnerPublicKey: no node was found with given numID");
				return null;
			}

			// Contact the owner through RMI
			LightChainRMIInterface ownerRMI = getLightChainRMI(owner.getAddress());
			// get the owner'r Public key through RMI
			PublicKey pk = ownerRMI.getPublicKey();
			// Hash the public key and store the hash value as int
			int hashedKey = Integer.parseInt(hasher.getHash(pk.getEncoded(), Const.TRUNC), 2);
			// if hashedKey is not equal to the provided numID, then there is a problem
			// and it is printed to the console
			if (hashedKey != num) {
				Util.log("GetOwnerPublicKey: given numID does not match with hash value of public key.");
				return null;
			}
			return pk;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

	public LightChainRMIInterface getLightChainRMI(String adrs) {
		if (Util.validateIP(adrs)) {
			if (adrs.equalsIgnoreCase(getAddress()))
				return this;
			try {
				return (LightChainRMIInterface) Naming.lookup("//" + adrs + "/RMIImpl");
			} catch (Exception e) {
				Util.log("Exception while attempting to lookup RMI located at address: " + adrs);
				e.printStackTrace();
			}
		} else {
			Util.log("Error in looking up RMI. Address: " + adrs + " is not a valid address.");
		}
		return null;
	}

	public PublicKey getPublicKey() throws RemoteException {
		return digitalSignature.getPublicKey();
	}

	public List<Transaction> getTransactions() throws RemoteException {
		return transactions;
	}

	public int getBalance() {
		return balance;
	}

	public String numToName(int num) {
		String name = Integer.toBinaryString(num);
		while (name.length() < Const.TRUNC) {
			name = "0" + name;
		}
		return name;
	}

	public boolean getMode() throws RemoteException {
		return mode;
	}

	// for Testing:

	// TODO: decide on keeping or removing those after refactoring simulation
	// approach
	public void put(Transaction t) throws RemoteException {
		transactions.add(t);
		insertTransaction(t);
	}

	public void put(Block b) throws RemoteException {
		blocks.add(b);
		insertBlock(b);
	}

	private void createNewTransaction(String cont) {
		try {
			Block lstBlk = getLatestBlock();
			Util.log("The prev found is : " + lstBlk.getNumID());
			Transaction t = new Transaction(lstBlk.getH(), getNumID(), cont, getAddress());
			boolean verified = validateTransaction(t);
			if (verified == false) {
				Util.log("Transaction validation Failed");
				return;
			}
			Util.log("Added transaction with nameID " + lstBlk.getH());
			t.setAddress(getAddress());
			transactions.add(t);
			insertTransaction(t);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TestingLog startSim(int numTransactions, int pace) throws RemoteException {
		testLog = new TestingLog(mode == Const.MALICIOUS);
		Random rnd = new Random();
		try {
			for (int i = 0; i < numTransactions; i++) {
				Thread.sleep(rnd.nextInt(1000 * pace) / 2);// wait for (pace +- 10 seconds)/2
				createNewTransaction(System.currentTimeMillis() + i + "" + rnd.nextDouble());
				Thread.sleep(rnd.nextInt(1000 * pace) / 2);// wait for (pace +- 10 seconds)/2
				updateViewTable();
				if (i % 1 == 0) {
					Thread.sleep(rnd.nextInt(rnd.nextInt(4000)));
					viewUpdate();
				}
				if (i % 5 == 0)
					System.out.println(100.0 * i / numTransactions + "% done.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return testLog;
	}

	public TestingLog getTestLog() throws RemoteException {
		return testLog;
	}

	public void shutDown() throws RemoteException {
		System.exit(0);
	}

}
