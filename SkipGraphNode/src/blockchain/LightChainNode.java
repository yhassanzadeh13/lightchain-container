package blockchain;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
	
	
	public static void main(String args[]) {				
		
		try {
			LightChainNode node = new LightChainNode();
			Registry reg = LocateRegistry.createRegistry(getRMIPort());
			reg.rebind("RMIImpl", node);
			log("Rebinding Successful");
			
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
			Transaction t = new Transaction(prev,getNumID(),cont);
			t.setAddress(getAddress());
			transactions.add(t);
			insert(t);
		}else if (query == 3){ // insert block
			log("Enter prev of block");
			String prev = get();
			Block b = new Block(getAddress(),prev);
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
		}else if(query == 6) {
			log("Which transaction to validate, you have "+transactions.size()+ " option");
			int num = Integer.parseInt(get());
			try {
				validate(transactions.get(num));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else if(query == 7) { // print the lookup table of the current node
			log("In case you want the lookup table of the original node enter 0.");
			log("Otherwise, enter the index of the data node ");
			int num = Integer.parseInt(get());
			if(num < getDataNum())
				printLookup(num);
			else
				log("Data node with given index does not exist");
		}
    }
	
	public void viewUpdate(Block blk) throws RemoteException {
		String name = Integer.toString(blk.getNumID());
		NodeInfo t = searchByNameID(name);
		
	}
	
	/*
	 * The method is called by a node to validate a transaction that it has created
	 * First it gets the validators of the node, and then contacts each of them 
	 * to get their signatures and stores their signatures in the sigma array.
	 */
	public void validate(Transaction t) throws RemoteException {
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
				return;
			}
			sigma.add(signature);
		}
		t.setSigma(sigma);
		log("Validation Successful");
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
	 * This method receives a transaction and the public key of the owner and verifies two things:
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
			if(is)
				verified = true;
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
