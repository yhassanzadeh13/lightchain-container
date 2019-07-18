package blockchain;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

import hashing.Hasher;
import hashing.HashingTools;
import signature.DigitalSignature;
import skipGraph.NodeInfo;
import skipGraph.RMIInterface;
import skipGraph.SkipNode;

public class LightChainNode extends SkipNode implements RMIInterface{
	
	private static final long serialVersionUID = 1L;
	private static ArrayList<Transaction> transactions = new ArrayList<>();
	private static ArrayList<Block> blocks = new ArrayList<>();
	private static DigitalSignature digitalSignature;
	private static Hasher hasher;
	private static HashMap<String,Integer> blkIdx = new HashMap<>();
	private static HashMap<Integer,String> view = new HashMap<>();
	private static HashMap<Integer,Integer> viewBalance = new HashMap<>();
	private static HashMap<Integer,Integer> viewMode = new HashMap<>();
	private static int mode ; // 1: Honest, 0: Malicious
	private static int balance = 20;
	private static final int VALIDATION_FEES = 10;
	private static int VAL_THRESHOLD = 2;
	public static final int TRUNC = 6;
	public static final int TX_MIN = 2;
	public static final int ZERO_ID = 0;
	
	/*
	 * Main method is for Running the tests on the node
	 */
	public static void main(String args[]) {				
		try {
			init();
			LightChainNode node = new LightChainNode();
			while(true) {
				printMenu();
				node.ask();
			}
		}catch(RemoteException e) {
			System.out.println("Remote Exception in main method. Terminating.");
			e.printStackTrace();
			System.exit(1);
		}catch(IOException e){
			log("Error in Rebinding");
			e.printStackTrace();
		}
		in.close();
	}
	/*
	 * A constructor for LightChainNode, in which the following happens:
	 * 	1) initialize Hasher and digital signature
	 * 	2) Get the mode of the node (Honest or Malicious)
	 * 	3) set the numID and nameID as the hash value of the public key
	 */
	public LightChainNode() throws RemoteException {
		super();
		hasher = new HashingTools();
		digitalSignature = new DigitalSignature();
		log("Specify mode of node, enter 1 for HONEST, 0 for MALICIOUS");
		mode = Integer.parseInt(get());
		while(mode != 0 && mode != 1) {
			log("Incorrect input. Specify mode of node, enter 1 for HONEST, 0 for MALICIOUS");
			mode = Integer.parseInt(get());
		}
		setNameID(hasher.getHash(digitalSignature.getPublicKey().getEncoded(),TRUNC));
		setNumID(Integer.parseInt(getNameID(),2));
	}
	
	/*
	 * This method prints the options for user controlling the node to choose.
	 * More options can be appended but require the modification of ask() method.
	 */
	public static void printMenu() throws IOException{
        log("Node at the address: " + address);
        log("Name ID: "+ nameID +" Number ID: " + numID);
        log("Choose a query by entering it's code and then press Enter");
        log("1-Insert Node");
        log("2-Insert Transaction");
        log("3-Insert Block");
        log("4-Search By Name ID");
        log("5-Search By Numeric ID");
        log("6-Print the Lookup Table");
        log("7-Delete node");
	}
	
	/*
	 * The ask method is used to make it possible to test implemented functionalities.
	 * It can be modified according to what method one would like to test.
	 * Of course do not forget to modify printMenu() when modifying this method
	 */
	public void ask() {
        String input = get();
        if(!input.matches("[1-7]")) {
        	log("Invalid query. Please enter the number of one of the possible operations");
        	return;
        }
		int query = Integer.parseInt(input);
		
		if(query == 1) { // insert node
			if(getDataNum() == 0) // if there are no inserted nodes yet then, then we insert the current node
				insert(new NodeInfo(getAddress(),getNumID(),getNameID()));
			else
				log("Already Inserted");
		}else if (query == 2){ // insert transaction
			log("Enter prev of transaction");
			String prev = get();
			log("Enter cont of transaction");
			String cont = get();
			Transaction t = new Transaction(prev, getNumID() ,cont, getAddress());
			t.setAddress(getAddress());
			transactions.add(t);
			insert(t);
		}else if (query == 3){ // insert block
			log("Enter prev of block");
			String prev = get();
			Block b = new Block(prev,getNumID(),getAddress());
			log("Enter Testing numID");
			int num = Integer.parseInt(get());
			b.setNumID(num);
			insert(new NodeInfo(getAddress(),ZERO_ID,Integer.toBinaryString(num)));
			blocks.add(b);
			insert(b);
		}else if (query == 4) {// search by name ID
			log("Please Enter the name ID to be searched");
			String name = get();
			while(!name.matches("[0-1]+")) {//Makes sure the name is a binary string
				log("Name ID should be a binary string. Please enter a valid Name ID:");
				name = get();
			}
			NodeInfo result = null;
			try{
				result = searchByNameID(name);
			}catch(RemoteException e) {
				e.printStackTrace();
				log("Remote Exception in query.");
			}
			log("The result of search by name ID is: " + result.getAddress());
		}else if(query == 5) { // search by num ID
			log("Please Enter the numeric ID to be searched");
			String numInput = get();
			while(!numInput.matches("0|[1-9][0-9]*")) {
				log("Invalid number entered. Please enter a valid number");
				numInput = get();
			}
			int num = Integer.parseInt(numInput);
			NodeInfo result = searchByNumID(num);
			log("The result of search by numberic ID is: "+ result.getAddress());
		}else if(query == 6) { // print the lookup table of the current node
			log("In case you want the lookup table of the original node enter 0.");
			log("Otherwise, enter the index of the data node ");
			int num = Integer.parseInt(get());
			if(num < getDataNum())
				printLookup(num);
			else
				log("Data node with given index does not exist");
		}else if(query == 7) {
			log("Enter the numID of the data node to be deleted.");
			int num = Integer.parseInt(get());
			try {
				delete(num);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    }
	
	/*
	 * This method finds the latest block on the blockchain and takes its numID
	 * and uses it to find all transactions that have this numID as nameID and uses
	 * getTransactionsWithNameID() method to get such transactions, and if the number
	 * of found transactions if at least TX_MIN, the transactions are casted into a block
	 * and the block is sent for validation.
	 * 
	 * NOTE for improvement:
	 * 	make it first search for the latest block and then search for transactions
	 */
	public void viewUpdate() throws RemoteException {
		
		Block blk = getLatestBlock();
		// Change numID to a nameID string
		String name = Integer.toString(Integer.parseInt(Integer.toString(blk.getNumID()),2));
		// Get all transaction with this nameID
		ArrayList<Transaction> tList = getTransactionsWithNameID(name);
		// If number of transactions obtained is less than TX_MIN then we terminate the process
		if(tList.size() < TX_MIN) {
			log("Cannot find TX_MIN number of transactions.");
			return;
		}
		// If there are TX_MIN transaction then add them into a new block
		Block newBlk = new Block(blk.getH(),getNumID(),tList);
		// send the new block for PoV validation
		validate(newBlk);
	}
	
	/*
	 * This method finds the latest block on the blockchain.
	 */
	public Block getLatestBlock() throws RemoteException {
		NodeInfo flag = searchByNumID(ZERO_ID);
		Block blk = (Block)searchByNameID(flag.getNameID());
		return blk;
	}
	
	/*
	 * This method receives the numID (Hash Value) of a block and conducts a nameID 
	 * search on it in order to find all transactions with this nameID. First it finds
	 * one node, and then goes to the linked list at last level of the skip graph which
	 * contains the found node and collects all nodes that are transactions from that level
	 * because they all will have the same nameID according to the structure of the skip graph
	 */
	public ArrayList<Transaction> getTransactionsWithNameID(String name) throws RemoteException{
		// find a transaction that has the given nameID 
		Transaction t = (Transaction)searchByNameID(name);
		// an empty list to add transaction to it and return it
		ArrayList<Transaction> tList = new ArrayList<>();
		// left and right will store the address of the nodes we are visiting in left and right respectively
		String left , right ;
		// leftNum and rightNum will store numIDs of left and right nodes, used to correctly access nodes (data nodes functionality)
		int leftNum = -1, rightNum = -1;
		// thisRMI is just used to extract information of neighbors
		RMIInterface thisRMI = getRMI(t.getAddress());
		
		// get addresses of left and right nodes, as well as their numIDs
		left = thisRMI.getLeftNode(maxLevels,t.getNumID());
		if(left != null)
			leftNum = thisRMI.getLeftNumID(maxLevels, t.getNumID());
		right = thisRMI.getRightNode(maxLevels,t.getNumID());
		if(right != null)
			rightNum = thisRMI.getRightNumID(maxLevels, t.getNumID());
		
		// now in the last level of the skip graph, we go left and right
		while(left != null) {
			RMIInterface leftRMI = getRMI(left);
			NodeInfo node = leftRMI.getNode(leftNum);
			// if this node is a transaction add it to the list
			if(node instanceof Transaction)
				tList.add((Transaction)node);
			// if we got enough transactions return them
			if(tList.size() == TX_MIN)
				return tList;
			// now go to the left node again
			left = leftRMI.getLeftNode(maxLevels, leftNum);
			leftNum = leftRMI.getLeftNumID(maxLevels,leftNum);
		}
		while(right != null) {
			RMIInterface rightRMI = getRMI(right);
			NodeInfo node = rightRMI.getNode(rightNum);
			// if this node is a transaction add it to the list
			if(node instanceof Transaction)
				tList.add((Transaction)node);
			// if we got enought transactions return them
			if(tList.size() == TX_MIN)
				return tList;
			// now go to the right node again
			right = rightRMI.getRightNode(maxLevels, rightNum);
			rightNum = rightRMI.getRightNumID(maxLevels, rightNum);
		}
		return tList;
	}
	
	/*
	 * This method is called by a node to validate a block that it has created.
	 * First it gets the validators of the block, and then contacts each
	 * validator and asks them to validate the block using PoV and then gets
	 * their signatures of the hash value of the block if the validation was successful
	 * and gets null otherwise.
	 */
	public boolean validate(Block blk) throws RemoteException {
		ArrayList<NodeInfo> validators = getValidators(blk);
		ArrayList<String> sigma = new ArrayList<>();
		// add the owner's signature to the block
		String mySign = digitalSignature.signString(blk.getH());
		sigma.add(mySign);
		blk.setSigma(sigma);
		// iterate over validators and ask them to validate the block
		for(int i=0 ; i<validators.size() ; ++i) {
			RMIInterface node = getRMI(validators.get(i).getAddress());
			String signature = node.PoV(blk);
			// if one validator returns null, then validation has failed
			if(signature == null) {
				log("Validating Block failed.");
				return false;
			}
			sigma.add(signature);
		}
		// update the sigma array of the block with all the signatures
		blk.setSigma(sigma);
		log("Validation of block is successful");
		return true;
	}
	
	/*
	 * The method is called by a node to validate a transaction that it has created
	 * First it gets the validators of the transaction, and then contacts each of them 
	 * to get their signatures and stores their signatures in the sigma array.
	 */
	public boolean validate(Transaction t) throws RemoteException {
		// obtain validators
		ArrayList<NodeInfo> validators = getValidators(t);
		// define an empty list
		ArrayList<String> sigma = new ArrayList<>();
		// add the owner's signature of the transaction's hash value to the sigma
		String mySign = digitalSignature.signString(t.getH());
		sigma.add(mySign);
		t.setSigma(sigma);
		
		// iterate over validators and use RMI to ask them to validate the transaction
		for(int i=0 ; i<validators.size(); ++i) {
			RMIInterface node = getRMI(validators.get(i).getAddress());
			String signature = node.PoV(t);
			// if a validators returns null that means the validation has failed
			if(signature == null) {
				log("Validating Transaction failed.");
				return false;
			}
			sigma.add(signature);
		}
		t.setSigma(sigma);
		log("Validation Successful");
		return true;
	}
	/*
	 * This method is used to validate a block. It checks :
	 * 	1) Authenticity
	 * 	2) Consistency
	 * 	3) Authenticity and soundness of the transactions it contains.
	 */
	public String PoV(Block blk) throws RemoteException {
		boolean val = isAuthenticated(blk) && isConsistent(blk);
		if(val == false)
			return null;
		// iterate over transactions and check them one by one
		ArrayList<Transaction> ts = blk.getTransactions();
		for(int i=0 ; i<ts.size() ; ++i) {
			if(!isAuthenticated(ts.get(i)) || !isSound(ts.get(i)))
				return null;
		}
		String signedHash = digitalSignature.signString(blk.getH());
		return signedHash;
	}
	/*
	 * A block is said to be consistent if its prev points to the current tail of the block, 
	 * so at the time of validation, validators should make sure that prev is still pointing
	 * to the tail of the blockchain, or otherwise if the tail was updated is should terminate
	 * the validation process.
	 */
	public boolean isConsistent(Block blk) {
		return true;
	}
	
	
	/*
	 * Checks the soundness, correctness and authenticity of a transaction using other methods.
	 * Checks if the owner of the transaction has enough balance to cover validation fees.
	 * returns null in case any of the tests fails, otherwise it signs the hashvalue of the transaction
	 * and sends the signed value to the owner.
	 */
	public String PoV(Transaction t) throws RemoteException {
		boolean val = isAuthenticated(t) && isCorrect(t) && isSound(t) && hasBalanceCompliance(t);
		if(val == false)
			return null;
		String signedHash = digitalSignature.signString(t.getH());
		return signedHash;
	}
	
	/*
	 * This method validated the soundness of a given transaction
	 * It checks if the given transaction is associated with a block
	 * which does not precede the block that contains the transaction's owner's 
	 * last transaction.
	 */
	public boolean isSound(Transaction t) {
		long start = System.currentTimeMillis();
		// get the hash value of the block that contains the owner's latest transaction
		String lastblk = view.get(t.getOwner());
		// get the index of the block that the given transaction is linked to
		int tIdx = blkIdx.get(t.getPrev());
		// get the index of the block the contains the owner's latest transaction
		int bIdx = blkIdx.get(lastblk);
		long end = System.currentTimeMillis();
		log("Time of Checking Soundness: " + (end-start));
		return tIdx > bIdx ;
	}
	
	/*
	 * Returns true if both nodes are of same type (HONEST,HONEST) or (MALICIOUS,MALICIOUS)
	 * and returns false if both are of different types.
	 */
	public boolean isCorrect(Transaction t) {
		int ownerMode = viewMode.get(t.getOwner());
		return ownerMode == mode;
	}
	
	/*
	 * This method recieves a block 
	 */
	public boolean isAuthenticated(Block blk) throws RemoteException {
		String hash = hasher.getHash(blk.getPrev()+blk.getOwner()+blk.getTransactions().toString(),TRUNC);
		if(!hash.equals(blk.getH()))
			return false;
		ArrayList<String> blkSigma = blk.getSigma();
		PublicKey ownerPublicKey = getOwnerPublicKey(blk.getOwner());
		boolean verified = false;
		if(ownerPublicKey == null)
			return false;
		for(int i=0 ; i<blkSigma.size(); ++i) {
			boolean is = digitalSignature.verifyString(hash, blkSigma.get(i), ownerPublicKey);
			if(is) verified = true;
		}
		return verified;
	}
	
	/*
	 * This method receives a transaction and verifies two things:
	 * 1- The hashvalue of the transaction was generated according to the correct equation
	 * 2- The sigma of the transaction contains the transaction's owner signed value of the hashvalue
	 */
	public boolean isAuthenticated(Transaction t) throws RemoteException {
		// generate the hash using the equation to check if it was generated correctly
		String hash = hasher.getHash(t.getPrev()+t.getOwner()+t.getCont(),TRUNC);
		// return false if it was not generated properly
		if(!hash.equals(t.getH()))
			return false;
		// now get the sigma array from transaction and iterate over signatures it contains
		ArrayList<String> tSigma = t.getSigma();
		PublicKey ownerPublicKey = getOwnerPublicKey(t.getOwner());
		boolean verified = false;
		if(ownerPublicKey == null)
			return false;
		for(int i=0 ; i<tSigma.size(); ++i) {
			// if we find one signature which belongs to the owner then we set verified to true
			boolean is = digitalSignature.verifyString(hash, tSigma.get(i), ownerPublicKey);
			if(is) verified = true;
		}
		return verified;
	}
	/*
	 * Checks if the owner of the transaction has enough balance to cover the fees
	 * of validation
	 */
	public boolean hasBalanceCompliance(Transaction t) {
		int ownerBalance = viewBalance.get(t.getOwner());
		return ownerBalance >= VALIDATION_FEES;
	}
	/*
	 * This method returns the validators of a given block
	 */
	public ArrayList<NodeInfo> getValidators(Block blk){
		ArrayList<NodeInfo> validators = new ArrayList<>();
		HashMap<String,Integer> taken = new HashMap<>();   
		int count = 0, i = 0;
		while(count < VAL_THRESHOLD) {
			String hash = blk.getPrev() + blk.getOwner() + blk.getTransactions().toString() + i;
			int num = Integer.parseInt(hasher.getHash(hash,TRUNC),2);
			NodeInfo node = searchByNumID(num);
			i++;
			if(taken.containsKey(node.getAddress()) || node.equals(data.get(0)))continue;
			count++;
			taken.put(node.getAddress(), 1);
			validators.add(node);
		}
		return validators;
	}
	
	/*
	 * This method returns the validators of a given transaction
	 */
	public ArrayList<NodeInfo> getValidators(Transaction t){
		// stores the validators to be returned
		ArrayList<NodeInfo> validators = new ArrayList<>(); 
		// used as a lookup to check if a node has been already added to the validators array
		// because the search might return the same node more than once
		// so in order to avoid this case, when we find an already added node, 
		// we repeat the search
		HashMap<String,Integer> taken = new HashMap<>(); 
		
		int count = 0 , i = 0;
		while(count < VAL_THRESHOLD) { // terminates when we get the required number of validators
			String hash = t.getPrev() + t.getOwner() + t.getCont() + i ;
			int num = Integer.parseInt(hasher.getHash(hash,TRUNC),2);
			NodeInfo node = searchByNumID(num);
			i++;
			if(taken.containsKey(node.getAddress()) || node.equals(data.get(0)))continue;
			count++;
			taken.put(node.getAddress(), 1);
			validators.add(node);
		}
		return validators;
	}
	
	/*
	 * This method recieves the numID of an owner of a transaction or a block
	 * and first verifies of the given public key truly belongs to the owner
	 * by hashing the provided public key and comparing it with the given numID
	 * if the test fails it prints it to console and return null.
	 * otherwise it returns the public key of the woner.
	 */
	public PublicKey getOwnerPublicKey(int num) throws RemoteException {
		// find owner from the network
		NodeInfo owner = searchByNumID(num);
		// Contact the owner through RMI
		RMIInterface ownerRMI = getRMI(owner.getAddress());
		// get the owner'r Public key through RMI
		PublicKey pk = ownerRMI.getPublicKey();
		// Hash the public key and store the hash value as int
		int hashedKey = Integer.parseInt(hasher.getHash(pk.getEncoded()),2);
		// if hashedKey is not equal to the provided numID, then there is a problem
		// and it is printed to the console
		if(hashedKey != num) {
			log("GetOwnerPublicKey: given numID does not match with hash value of public key.");
			return null;
		}
		return pk;
	}
	
	
	public PublicKey getPublicKey() throws RemoteException {
		return digitalSignature.getPublicKey();
	}
	
	public ArrayList<Transaction> getTransactions() throws RemoteException{
		return transactions;
	}
	
	public int getBalance() {
		return balance;
	}
	
	// for Testing:
	public void put(Transaction t) throws RemoteException{
		transactions.add(t);
		insert(t);
	}
	public void put(Block t) throws RemoteException{
		blocks.add(t);
		insert(t);
	}
}
