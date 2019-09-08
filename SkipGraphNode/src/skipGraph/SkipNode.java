package skipGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import blockchain.Block;
import blockchain.Transaction;
import hashing.HashingTools;
import remoteTest.Configuration;
import remoteTest.PingLog;
import remoteTest.TestingLog;

// Things to change:
// - Rethink of Data Nodes


public class SkipNode extends UnicastRemoteObject implements RMIInterface{

	protected static SkipNode node;
	protected static String address;
	protected static String nameID;
	protected static int numID;
	protected static HashingTools hasher;
	protected static String IP ;

	// lookup: (Right Left @Level)
	//protected static NodeInfo[][][] lookup ;
	protected static lookupTable lookup2;
	private static String introducer; 
	protected static int RMIPort ;
	protected static Scanner in = new Scanner(System.in);
	//private static HashMap<Integer,Integer> dataID;
	//protected static ArrayList<NodeInfo> data;
	private static int dataNum = 0 ; 
	
	public static final boolean local = true;//If set to true, it would work fine on local networks without having to go through the hassle of 

	
	/*
	 * Constants
	 */
	private static final long serialVersionUID = 1L;
	protected static int maxLevels;
	private static int MAX_DATA = 10000;
	private static final int RIGHT = lookupTable.RIGHT;
	private static final int LEFT = lookupTable.LEFT;
	private static final int ZERO_LEVEL = 0;
	private static final int UNASSIGNED = -1;
	public static int TRUNC = 30;
	
	/*
	 * For simulations
	 */
	protected static TestingLog testLog;
	
	/*
	 * Constructor for SkipNode class
	 */
	
	protected SkipNode() throws RemoteException{
		super(RMIPort);
		maxLevels = TRUNC;
		//lookup = new NodeInfo[maxLevels+2][2][MAX_DATA];
		lookup2 = new lookupTable(maxLevels);
//		if(dataID==null) dataID = new HashMap<>();
//		if(data==null)data = new ArrayList<>();
		// get Port number
//		log("Enter RMI port: ");
//		String numInput = get();
//		while(!numInput.matches("0|[1-9][0-9]*")) {
//			log("Invalid port. Enter a valid port number for RMI:");
//			numInput = get();			
//		}
//		RMIPort = Integer.parseInt(numInput);
//		Registry reg = LocateRegistry.createRegistry(RMIPort);
//		reg.rebind("RMIImpl", this);
//		log("Rebinding Successful");
	}
	/*
	 * This method initializes the information of the current node
	 * and prints them to console
	 */
	public static void setInfo() {
//		if(data == null) data =  new ArrayList<>();
//		if(dataID == null) dataID = new HashMap<>();
		if(lookup2==null) lookup2 = new lookupTable(maxLevels);
		String numInput ;
		log("Enter the address of the introducer:");
		introducer = get();
		while(!(introducer.equalsIgnoreCase("None") || validateIP(introducer))) {
			log("Invalid IP. Please enter a valid IP address ('none' if original node): ");
			introducer = get();
		}
		log("Enter RMI port: ");
		numInput = get();
		while(!numInput.matches("0|[1-9][0-9]*")) {
			log("Invalid port. Enter a valid port number for RMI:");
			numInput = get();
		}
		RMIPort = Integer.parseInt(numInput);
		address = IP + ":" + RMIPort;
		// The nameID and numID are hash values of the address
		//nameID = hasher.getHash(address,TRUNC);
		//numID = Integer.parseInt(nameID,2);

		// In case the introducer to this node is null, then the insert method
		// will not be called on it, so we manually add it to the data list and
		// map index in the data array with its numID.
		if(introducer.equalsIgnoreCase("none")) {
			lookup2.addNode(new NodeInfo(address,numID,nameID));
		}
		log("Your INFO:\nnameID: "+nameID+"\nnumID: "+numID+"\nintroducer: "+introducer);
	}

	public void setInfo(Configuration conf) {
		hasher = new HashingTools();
		introducer = conf.getIntroducer();
		log("Set introducer " + introducer + " for " + numID);
		RMIPort = Integer.parseInt(conf.getPort());
		address = IP + ":" + RMIPort;
		if(!conf.getNameID().equals(Configuration.UNASSIGNED_NAMEID)) nameID = conf.getNameID();
		if(!conf.getNumID().equals(Configuration.UNASSIGNED_NUMID)) numID = Integer.parseInt(conf.getNumID());
		if(introducer.equalsIgnoreCase("none")) {
			lookup2.addNode(new NodeInfo(address,numID,nameID));
			dataNum++;
		}
	}

	/*
	 * Initialize system properties and current node's IP address.
	 */
	

	/*
	 * This method deletes a data node with a given numID.
	 * Currently, the deletion is done by updating the lookup table of the deleted node
	 * and its neighbors, in addition to setting the information of this node in
	 * the data array to be the information of the main node, because otherwise it will
	 * still be usable in search routing. Another approach to fix this problem might be deleting
	 * it completely from data array, but this approach will have us updating the positions of all
	 * the data nodes that are placed after the deleted node in data array.
	 */

	public void delete(int num) throws RemoteException {
		
		try {
			for(int j=maxLevels ; j >=0  ; j--) {
				// if there are no neighbors at level j, just move on
				NodeInfo lNode=lookup2.get(num, j, LEFT), rNode = lookup2.get(num, j, RIGHT), thisNode = lookup2.get(num);
				//if(lookup[j][LEFT][i] == null && lookup[j][RIGHT][i] == null) {
				if(lNode==null && rNode==null ) {
					continue;
				// if left is null, then update right
				}else if (lNode == null) {
					RMIInterface rightRMI = getRMI(rNode.getAddress());
					//Set it to null only if the current node to the left is this node (don't change it if its something else)
					rightRMI.setLeftNode(rNode.getNumID(), j, lNode, thisNode);
				// if right is null, update left
				}else if (rNode == null) {
					RMIInterface leftRMI = getRMI(lNode.getAddress());
					leftRMI.setRightNode(lNode.getNumID(), j, rNode, thisNode);
				// otherwise update both sides and connect them to each other.
				}else {
					RMIInterface rightRMI = getRMI(rNode.getAddress());
					RMIInterface leftRMI = getRMI(lNode.getAddress());
					rightRMI.setLeftNode(rNode.getNumID(), j, lNode, thisNode);
					leftRMI.setRightNode(lNode.getNumID(), j, rNode, thisNode);
				}
			}
			//Delete the node from the lookup.
			lookup2.remove(num);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	/*
	 * This method is a helper method for insert() method
	 * It is used to make the insert() operation recursive per level.
	 * It receives the level of insertion and the direction of search
	 * if direction == 1, then the search is to the right, and the method returns the right neighbor
	 * if direction == 0, then the search is to the left, and the method returns the left neighbor
	 * This method is also directly accessed by other nodes using RMI if a search should pass through it
	 * @see RMIInterface#insertSearch(int, int, int, java.lang.String)
	 */
	public NodeInfo insertSearch(int level, int direction,int num, String target) throws RemoteException {		
		try {
			NodeInfo wantedNode = lookup2.get(num);

			if(wantedNode == null) return null;
			// If the current node and the inserted node have common bits more than the current level,
			// then this node is the neighbor so we return it
			if(commonBits(target,wantedNode.getNameID()) > level) 
				return wantedNode;
			// If search is to the right then delegate the search to right neighbor if it exists
			// If the right neighbor is null then at this level the right neighbor of the inserted node is null
			if(direction == RIGHT) {
				NodeInfo rNode = lookup2.get(num, level, direction);
				if(rNode==null)
					return null;
				RMIInterface rRMI = getRMI(rNode.getAddress());
				return rRMI.insertSearch(level,direction,rNode.getNumID(),target);
			} else {
				// If search is to the left then delegate the search to the left neighbor if it exists
				// If the left neighbor is null, then the left neighbor of the inserted node at this level is null.
				NodeInfo lNode = lookup2.get(num, level, direction);
				if(lNode == null)
					return null;
				RMIInterface lRMI = getRMI(lNode.getAddress());
				return lRMI.insertSearch(level,direction,lNode.getNumID(),target);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static boolean inserted = false;
	/*
	 * This method inserts either the current node to the skip graph of the introducer,
	 * or it is used to insert a data node.
	 */
	//TODO: Change insert to not always insert from the introducer
	public void insert(NodeInfo node){
		try {
			lookup2.initializeNode(node);
			String left = null;
			String right = null;
			// We search through the introducer node to find the node with 
			// the closest num ID
			NodeInfo position;
			if(introducer.equalsIgnoreCase("none")||inserted) {
				position = searchByNumID(node.getNumID());
			}else {
				inserted = true;
				RMIInterface introRMI = getRMI(introducer);
				position = introRMI.searchByNumID(node.getNumID());
			}
			
			if(position == null) {
				log("The address resulting from the search is null");
				log("Please check the introducer's IP address and try again.");
				return;
			}
			
			RMIInterface posRMI = getRMI(position.getAddress());
			if(posRMI == null) {
				log("RMI registry lookup at address: "+position+" failed. Insert operation stopped.");
				return;
			}
			
			// First, we insert the node at level 0
			
			int posNum = position.getNumID(); // numID of the closest node
			int leftNum = UNASSIGNED ; // numID of left node
			int rightNum = UNASSIGNED ; // numID of right node
			
			if(posNum > node.getNumID()) { // if the closest node is to the right
				
				NodeInfo cur = posRMI.getLeftNode(ZERO_LEVEL,posNum);
				if(cur == null)
					left = null;
				else
					left = cur.getAddress(); // the left of my right will be my left
				
				right = position.getAddress();
				rightNum = position.getNumID(); // we need the numID to be able to access it
				
				if(left != null) { // insert the current node in the lookup table of my left node if it exists
					RMIInterface leftRMI = getRMI(left);
					leftNum = cur.getNumID();
					lookup2.put(node.getNumID(), ZERO_LEVEL, LEFT, assignNode(cur), null); //null because the lookup table is empty
					leftRMI.setRightNode(leftNum, ZERO_LEVEL, node, position);
					System.out.println("set right  node");
				}				
				lookup2.put(node.getNumID(), ZERO_LEVEL, RIGHT, assignNode(position), null);
				posRMI.setLeftNode(posNum, ZERO_LEVEL, node, cur);// insert the current node in the lookup table of its right neighbor
				System.out.println("set left node");
			}else{ // if the closest node is to the left
				
				NodeInfo cur = posRMI.getRightNode(ZERO_LEVEL,posNum);
				if(cur == null)
					right = null;
				else
					right = cur.getAddress(); // the right of my left is my right
				
				left = position.getAddress() ; 
				leftNum = position.getNumID(); // we need the numID to be able to access it
				
				lookup2.put(node.getNumID(), ZERO_LEVEL, LEFT, assignNode(position), null); //This is before the if statement so that the order of insertion is the same
				posRMI.setRightNode(posNum, ZERO_LEVEL, node, cur);// insert the current node in the lookup table of its right neighbor
				if(right != null) { // insert current node in the lookup table of its right neighbor if it exists
					RMIInterface rightRMI = getRMI(right);
					rightNum = cur.getNumID();
					lookup2.put(node.getNumID(), ZERO_LEVEL, RIGHT, assignNode(cur), null); //null because the lookup table is empty
					rightRMI.setLeftNode(rightNum, ZERO_LEVEL, node, position);
				}						
			}
			
			// Now, we insert the node in the rest of the levels
			// In level i , we make a recursive search for the nodes that will be
			// the neighbors of the inserted nodes at level i+1
			
			int level = ZERO_LEVEL;
			while(level < maxLevels) {
				
				if(left != null) {
					
					RMIInterface leftRMI = getRMI(left);
					NodeInfo lft = leftRMI.insertSearch(level,LEFT,leftNum,node.getNameID()); // start search left
					
					lookup2.put(node.getNumID(), level+1, LEFT, assignNode(lft), null);
					
					// set left and leftNum to default values (null,-1)
					// so that if the left neighbor is null then we no longer need
					// to search in higher levels to the left
					left = null;
					leftNum = UNASSIGNED;
					
					if(lft != null) {
						RMIInterface lftRMI = getRMI(lft.getAddress());
						//log("Left node at level " + (level+1) + " is: " + lft.getAddress() + " " + lft.getNumID() + " " + lft.getNameID());
						lftRMI.setRightNode(lft.getNumID(), level+1, node, null);
						left = lft.getAddress();
						leftNum = lft.getNumID();
					}	
				}
				if(right != null) {
					
					RMIInterface rightRMI = getRMI(right);
					NodeInfo rit = rightRMI.insertSearch(level, RIGHT, rightNum, node.getNameID()); // start search right
					lookup2.put(node.getNumID(), level+1, RIGHT, assignNode(rit), null);
					
					// set right and rightNum to default values (null,-1)
					// so that if the right neighbor is null then we no longer need
					// to search in higher levels to the right
					right = null;
					rightNum = UNASSIGNED;
					
					if(rit != null) {
						RMIInterface ritRMI = getRMI(rit.getAddress());
						//log("Right node at level " + (level+1) + " is: " + rit.getAddress() + " " + rit.getNumID() + " " + rit.getNameID());
						ritRMI.setLeftNode(rit.getNumID(), level+1, node, null);
						right = rit.getAddress();
						rightNum = rit.getNumID();
					}
				}
				level++;
			}
			// after we conclude inserting the node in all levels,
			// we add the inserted node to the data array
			// and we map its numID with its index in the data array using dataID
			lookup2.finalizeNode();
			log("inserting actually done.");
		}catch(RemoteException e) {
			e.printStackTrace();
			log("Remote Exception thrown in insert function.");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}


	/*
	 * This method receives a number, and returns the data node (or possibly the main node)
	 * which has the closest numID to the given number
	 */
	public int getBestNum(int num) {
		return lookup2.getBestNum(num);
	}

	/*
	 * This method is a hepler method for searchByNumID()
	 * It recieves the target numID and the level it is searching in,
	 * and it routes the search through the skip graph recursively using RMI
	 * @see RMIInterface#searchNum(int, int)
	 */
	public ArrayList<NodeInfo> searchNum(int numID, int targetInt,int level, ArrayList<NodeInfo> lst, int jumpsLeft) throws RemoteException{
		if(jumpsLeft++ == 40) {
			System.err.println(lookup2.keySet());
			System.err.println("Stack overflow in searchNum, target: "+targetInt+" level: "+level + " lst: " + lst.toString());
			logAllAndShutDown();
		}
		int num;
		if(numID != lookup2.bufferNumId()) {
			num = getBestNum(targetInt);// get the data node (or main node) that is closest to the target search
			lst.add(lookup2.get(num));//Add the current node's info to the search list
			if(num == targetInt) {
				return lst;
			}
		}else {
			num = numID;
		}

		// If the target is greater than the current node then we should search right
		if(num < targetInt) {

			// Keep going down levels as long as there is either no right neighbor
			// or the right neighbor has a numID greater than the target
			while(level >= ZERO_LEVEL && (lookup2.get(num, level, RIGHT) == null || lookup2.get(num, level, RIGHT).getNumID() > targetInt))
				level--;
			// If there are no more levels to go down to return the current node
			if(level < ZERO_LEVEL) {
				return lst;
			}
			// delegate the search to the right neighbor
			RMIInterface rightRMI = getRMI(lookup2.get(num, level, RIGHT).getAddress());
			try{
				return rightRMI.searchNum(lookup2.get(num, level, RIGHT).getNumID(),targetInt,level,lst,jumpsLeft);
			}catch(StackOverflowError e) {
				//Fix overflow logging
				//testLog.logOverflow(e, data, "Overflow in searchNum.\ntargetint: "+ targetInt + "\tlevel: "+level,lst);
				return null;
			}catch(Exception e) {
				log("Exception in searchNum. Target: "+targetInt);
				return lst;
			}
		}
		else { // If the target is less than the current node then we should search left 
			// Keep going down levels as long as there is either no right neighbor
			// or the left neighbor has a numID greater than the target
			while(level >= ZERO_LEVEL && (lookup2.get(num, level, LEFT) == null || lookup2.get(num, level, LEFT).getNumID() < targetInt))
				level--;
			// If there are no more levels to go down to return the current node
			if(level < ZERO_LEVEL)
				return lst;
			// delegate the search to the left neighbor
			RMIInterface leftRMI = getRMI(lookup2.get(num, level, LEFT).getAddress());
			try{
				return leftRMI.searchNum(lookup2.get(num, level, LEFT).getNumID(),targetInt, level, lst,jumpsLeft);
			}catch(StackOverflowError e) {
				//testLog.logOverflow(e, data, "Overflow in searchNum.\ntargetint: "+ targetInt + "\tlevel: "+level,lst);
				return null;
			}catch(Exception e) {
				log("Exception in searchNum. Target: "+targetInt);
				return lst;
			}
		}
	}

	/*
	 * Executes a search through the skip graph by numeric id and returns the a NodeInfo object
	 * which contains the address, numID, and nameID of the node with closest numID to the target
	 * It starts the search from last level of the current node
	 * @see RMIInterface#searchByNumID(java.lang.String)
	 */
	public NodeInfo searchByNumID(int searchTarget){
		try {
			ArrayList<NodeInfo> lst = new ArrayList<NodeInfo>();
			lst = searchByNumID(searchTarget,lst);
			if(lst == null) {
				return null;
			}
			else {
				return lst.get(lst.size()-1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ArrayList<NodeInfo> searchByNumID(int searchTarget, ArrayList<NodeInfo> lst){
		try {
			if(lst == null) {
				lst = new ArrayList<NodeInfo>();
			}
			int level = maxLevels;
			int num = getBestNum(searchTarget); // route search to closest data node
			if(lookup2.get(num, ZERO_LEVEL, LEFT) == null && lookup2.get(num, ZERO_LEVEL, RIGHT) == null) {
				lst.add(lookup2.get(num));
				return lst;
			}
			return searchNum(numID,searchTarget,level,lst,0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * This method receives a nameID and returns the index of the data node which has
	 * the most common prefix with the given nameID
	 */
	public int getBestName(String name,int direction) {
		return lookup2.getBestName(name, direction);
	}

	/*
	 * This method is a helper method for searchByNameID()
	 * It receives the target nameID and the level it is searching in, and also the direction
	 * of search, and it routes the search through the skip graph recursively using RMI
	 * It return the most similar node if the node itself is not found
	 * The similarity is defined to be the the maximum number of common bits
	 * If direction == 1, then search is to the right
	 * If direction == 0, then search is to the left
	 * @see RMIInterface#searchName(java.lang.String, int, int)
	 */

	public NodeInfo searchName(String searchTarget,int level,int direction) throws RemoteException {
		
		try {
			int bestNum = getBestName(searchTarget,direction);
			if(lookup2.get(bestNum).getNameID().equals(searchTarget)) // if the current node hold the same nameID, return it.
				return lookup2.get(bestNum);
			// calculate common bits to find to which level the search must be routed
			int newLevel = commonBits(searchTarget,lookup2.get(bestNum).getNameID()); 
			
			// If the number of common bits is not more than the current level
			// then we continue the search in the same level in the same direction
			if(newLevel <= level ) {
				if(lookup2.get(bestNum, level, direction) == null)// If no more nodes in this direction return the current node
					return lookup2.get(bestNum) ;
				RMIInterface rightRMI = getRMI(lookup2.get(bestNum, level, direction).getAddress());
				return rightRMI.searchName(searchTarget, level, direction);
			}
			// If the number of common bits is more than the current level
			// then the search will be continued on the new level
			// so we start a search in both directions in the new level
			
			NodeInfo result = lookup2.get(bestNum); // we initialize the result to current node
			
			// First we start the search on the same given direction and wait for the result it returns
			if(lookup2.get(bestNum, newLevel, direction) != null) {
				RMIInterface rightRMI = getRMI(lookup2.get(bestNum, newLevel, direction).getAddress());
				result = rightRMI.searchName(searchTarget,newLevel,direction);
			}
			// If it returns a result that differs from the current node then we check it
			if(result != null && !result.equals(lookup2.get(bestNum))) {
				RMIInterface resultRMI = getRMI(result.getAddress());
				// If this is the result we want return it, otherwise continue the search in the opposite direction
				if(resultRMI.getNameID().contains(searchTarget))
					return result;
			}
			// Continue the search on the opposite direction
			if(lookup2.get(bestNum, newLevel, 1-direction) != null) {
				RMIInterface leftRMI = getRMI(lookup2.get(bestNum, newLevel, 1-direction).getAddress());
				NodeInfo k = leftRMI.searchName(searchTarget, newLevel, 1-direction);
				if(result == null || commonBits(k.getNameID(),lookup2.get(bestNum).getNameID()) > commonBits(result.getNameID(),lookup2.get(bestNum).getNameID()))
					result = k;
			}
			return result;
		}catch(StackOverflowError e) {
			//testLog.logOverflow(e, data, "Overflow in searchName.\nSearch target: "+ searchTarget + "\tlevel: "+level+" direction: "+ direction);
			return null;
		}catch (Exception e) {
			e.printStackTrace();
			log("Error when inserting " + searchTarget + " at address " + address);
			return null;
		}	
	}

	/*
	 * This methods starts a search by nameID, and returns the node as an instance
	 * of NodeInfo class which contains (address, numID, nameID) of the node,
	 * such that the nameID of the returned node is the most similar with the searchTarget.
	 * Similarity is defined to be the maximum number of common bits between the two strings
	 * @see RMIInterface#searchByNameID(java.lang.String)
	 */
	public NodeInfo searchByNameID(String searchTarget) throws RemoteException{		
		try {
			
			int bestNum = getBestName(searchTarget,1);
			int newLevel = commonBits(searchTarget);
			NodeInfo result = lookup2.get(bestNum);
			
			// First execute the search in the right direction and see the result it returns
			if(lookup2.get(bestNum, newLevel, RIGHT) != null) {
				RMIInterface rightRMI = getRMI(lookup2.get(bestNum, newLevel, RIGHT).getAddress());
				result = rightRMI.searchName(searchTarget,newLevel,RIGHT);
			}
			// If the result is not null and is different from the default value we check it
			if(result != null && !result.equals(lookup2.get(bestNum))) {
				RMIInterface resultRMI = getRMI(result.getAddress());
				// If this is the result we want return it, otherwise continue searching to the left
				if(resultRMI.getNameID().contains(searchTarget))
					return result;
			}
			// If the desired result was not found try to search to the left
			if(lookup2.get(bestNum, newLevel, LEFT) != null) {
				RMIInterface leftRMI = getRMI(lookup2.get(bestNum, newLevel, LEFT).getAddress());
				NodeInfo k = leftRMI.searchName(searchTarget, newLevel, LEFT);
				if(commonBits(k.getNameID(),lookup2.get(bestNum).getNameID()) > commonBits(result.getNameID(),lookup2.get(bestNum).getNameID()))
					result = k;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			log("Error when inserting " + searchTarget + " at address " + address);
			return null;
		}
	}

	/*
	 * getters and setters for lookup table and numID and nameID
	 *
	 */
	public NodeInfo getLeftNode(int level,int num) throws RemoteException {
		return lookup2.get(num, level, LEFT);
	}

	public NodeInfo getRightNode(int level, int num) throws RemoteException {
		return lookup2.get(num, level, RIGHT);
	}

	public boolean setLeftNode(int num, int level,NodeInfo newNode, NodeInfo oldNode) throws RemoteException{
		return lookup2.put(num, level, LEFT, assignNode(newNode), oldNode);
	}

	public boolean setRightNode(int num, int level,NodeInfo newNode, NodeInfo oldNode) throws RemoteException{
		return lookup2.put(num, level, RIGHT, assignNode(newNode), oldNode);
	}

	public int getNumID(){
		return numID;
	}
	public String getNameID() {
		return nameID ;
	}
	
	protected static int getDataNum() {
		return lookup2.size();
	}
	public String getAddress() {
		return address;
	}
	protected static int getRMIPort() {
		return RMIPort;
	}
	protected void setNumID(int num) {
		this.numID = num;
	}
	protected void setNameID(String s) {
		this.nameID = s;
	}

	public int getLeftNumID(int level,int num) {
		return lookup2.get(num, level, LEFT).getNumID();
	}
	public int getRightNumID(int level, int num) {
		return lookup2.get(num, level, RIGHT).getNumID();
	}
	public String getLeftNameID(int level,int num) {
		return lookup2.get(num, level, LEFT).getNameID();
	}
	public String getRightNameID(int level,int num) {
		return lookup2.get(num, level, RIGHT).getNameID();
	}
	public NodeInfo getNode(int num) {
		return lookup2.get(num);
	}
	
	public NodeInfo assignNode(NodeInfo node) {
		if(node == null)
			return null;
		if(node instanceof Transaction)
			return new Transaction((Transaction)node);
		else if (node instanceof Block)
			return new Block((Block)node);
		return new NodeInfo(node);
	}
	

	/*
	 * This method returns an RMI instance of the node with the given address
	 */
	public static RMIInterface getRMI(String adrs) {
		if(validateIP(adrs)) {
			if(adrs.equalsIgnoreCase(address)) return node;
			try {
				return (RMIInterface)Naming.lookup("//"+adrs+"/RMIImpl");
			}catch(Exception e) {
				log("Exception while attempting to lookup RMI located at address: "+adrs);
				e.printStackTrace();
			}
		}else {
			log("Error in looking up RMI. Address: "+ adrs + " is not a valid address.");
		}
		return null;
	}
	/*
	 * This method grabs the public ip from an external server
	 */

	public static String grabIP() {
		boolean localIP = local; //set to true if testing locally.
		if(localIP) {
			try { //To return the local address in case you want to test locally.
				return Inet4Address.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			}
		}

		String result=null;
		URL url;
		String[] services = {"http://checkip.amazonaws.com/",  
							 "https://api.ipify.org/?format=text", 
							 "https://ip.seeip.org/"};
		BufferedReader in;
		for(int i=0;i<services.length;i++) {
			try {
				url = new URL(services[i]);
				in = new BufferedReader(new InputStreamReader(
				        url.openStream()));
				result = in.readLine();
				in.close();
			}catch(Exception e) {
				System.out.println("Error grabbing IP from " + services[i] + ". Trying a different service.");
			}
			if(validateIP(result)) {
				break;
			}
		}
		return result;
	}

	/*
	 * This method initializes all the RMI system properties required for proper functionality
	 */
	
	protected static void init() {
		hasher = new HashingTools();
		IP = grabIP();
		try {
			System.setProperty("java.rmi.server.hostname",IP);
			System.setProperty("java.rmi.server.useLocalHostname", "false");
			System.out.println("RMI Server proptery set. Inet4Address: "+IP);
		}catch (Exception e) {
			System.err.println("Exception in initialization. Please try running the program again.");
			System.exit(0);
		}
	}
	
		
	/*
	 * This method validates the ip and makes sure its of the form xxx.xxx.xxx.xxx
	 */

	public static boolean validateIP(String adrs) {
		if(adrs == null) return false;

		int colonIndex = adrs.indexOf(':');
		String ip = adrs;
		if(colonIndex != -1) ip = adrs.substring(0,colonIndex);
		String[] parts = ip.split("\\.");
		if(parts.length!=4) {
			return false;
		}
		try {
			for(String el : parts) {
				int num = Integer.parseInt(el);
				if(num<0||num>255) return false;
			}
		}catch(NumberFormatException e) {
			return false;
		}
		if(ip.endsWith("."))
			return false;
		return true;
	}

	// This method calculate the length of the common prefix
	// between the nameID of the current node and the given name

	public static int commonBits(String name) {	
		if(name.length() != nameID.length())
			return -1;
		int i = 0 ;
		for(i = 0 ; i < name.length() && name.charAt(i) == nameID.charAt(i);i++);
		return i ;		
	}
	
	/*
	 * This method returns the length of the common prefix between two given strings
	 */
	public static int commonBits(String name1, String name2) {
		if(name1 == null || name2 == null) {
			return -1;
		}
		if(name1.length() != name2.length())
			return -1;
		int i = 0;
		for(i = 0; i < name1.length() && name1.charAt(i) == name2.charAt(i) ; ++i);
			return i;
		}
	/*
	 * A shortcut for printing to console
	 */
	public static void log(String s) {
		System.out.println(s);
	}
	public static void logLine(String s) {
		System.out.print(s);
	}
	/*
	 * Print the contents of the lookup table
	 */
	public static void printLookup(int num) {
        System.out.println("\n");
        for(int i = maxLevels-1 ; i >= 0 ; i--)
        {
           	if(lookup2.get(num, i, LEFT)== null)
        		logLine("null\t");
        	else {
        		NodeInfo lNode = lookup2.get(num, i, LEFT);
        		logLine(lNode.getAddress() + " " + lNode.getNumID() + " " + lNode.getNameID()+"\t");
        	}
           	if(lookup2.get(num, i, RIGHT)== null)
        		logLine("null\t");
        	else {
        		NodeInfo rNode = lookup2.get(num, i, RIGHT);
        		logLine(rNode.getAddress() + " " + rNode.getNumID() + " " + rNode.getNameID()+"\t");
        	}
            log("\n\n");
        }
    }
	
	/*
	 * A shortcut for getting input from user
	 */
	public static String get() {
		String response = in.nextLine();
		return response;
	}
		
	public static void printData() {
		Set<Integer> numIDs = lookup2.keySet();
		for(int numID : numIDs) {
			log("NumID: " + lookup2.get(numID).getNumID());
			log("NameID: " + lookup2.get(numID).getNameID());
			log("");
		}
	}
	
	protected void printLevel(int k) throws RemoteException {
		//TODO: fix this
		try {
			ArrayList<NodeInfo> nodes = new ArrayList<>();
			NodeInfo node = null;
			int num =  UNASSIGNED;
			if(lookup2.get(numID,k,LEFT) != null) {
				node = lookup2.get(numID,k,LEFT);
				num = node.getNumID();
			}
			while(node != null) {
				RMIInterface no = getRMI(node.getAddress());
				nodes.add(no.getNode(num));
				node = no.getLeftNode(k, num);
				if(node != null)
				num = no.getLeftNumID(k, num);
			}
			Collections.reverse(nodes);
			nodes.add(lookup2.get(numID));
			node = null;
			if(lookup2.get(numID,k,RIGHT) != null) {
				node = lookup2.get(numID,k,RIGHT);
				num = node.getNumID();
			}
			while(node != null) {
				RMIInterface no = getRMI(node.getAddress());
				nodes.add(no.getNode(num));
				node = no.getRightNode(k,num);
				if(node != null)
					num = no.getRightNumID(k, num);
			}
			for(int i=0 ; i<nodes.size(); ++i) {
				log(nodes.get(i).getAddress() + " " + nodes.get(i).getNumID() + " " + nodes.get(i).getNameID());
			}
			log("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getNodes(int k) {
		//TODO: fix this
		return "no";
//		try {
//			StringBuilder sb = new StringBuilder();
//			ArrayList<NodeInfo> nodes = new ArrayList<>();
//			NodeInfo node = null;
//			int num =  UNASSIGNED;
//			if(lookup[k][LEFT][0] != null) {
//				node = lookup[k][LEFT][0];
//				num = lookup[k][LEFT][0].getNumID();
//			}
//			while(node != null) {
//				RMIInterface no = getRMI(node.getAddress());
//				nodes.add(no.getNode(num));
//				node = no.getLeftNode(k, num);
//				if(node != null)
//				num = no.getLeftNumID(k, num);
//			}
//			Collections.reverse(nodes);
//			nodes.add(data.get(0));
//			node = null;
//			if(lookup[k][RIGHT][0] != null) {
//				node = lookup[k][RIGHT][0];
//				num = lookup[k][RIGHT][0].getNumID();
//			}
//			while(node != null) {
//				RMIInterface no = getRMI(node.getAddress());
//				nodes.add(no.getNode(num));
//				node = no.getRightNode(k,num);
//				if(node != null)
//					num = no.getRightNumID(k, num);
//			}
//			for(int i=0 ; i<nodes.size(); ++i) {
//				sb.append(nodes.get(i).getAddress() + " " + nodes.get(i).getNumID() + " " + nodes.get(i).getNameID());
//				sb.append("\n");
//			}
//
//			return sb.toString();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
	}
	
	// For Testing purposes
	protected static int configurationsLeft = 0;
	protected static ArrayList<Configuration> cnfs; //0th = master nodeinfo


	// For Testing purposes

	/*
	 * Retro ping function does not test latency using RMI calls but rather through the command line 
	 */
	
	public PingLog retroPingStart(NodeInfo node, int freq) {
		PingLog lg = new PingLog(new NodeInfo(address, numID, nameID), node);
		
		//The commands to put in the commandline
		List<String> commands = new ArrayList<String>();
		commands.add("ping");

		//-n if windows, -c if other OS
		String osname = System.getProperty("os.name");
		if(osname.contains("Win")) {
			commands.add("-n");
		}else {
			commands.add("-c");
		}
		
		//The number of times to ping
		commands.add(freq+"");
		
		//The address of the node
		commands.add(node.getAddress().split(":")[0]);//Just the address without the port
		
	    try {
	    	ProcessBuilder pb = new ProcessBuilder(commands);
		    Process process = pb.start();
		    
		    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream())); //to read the output
		    String cur;
		    
		    int timeIndex = -1; //The index of the token containing time, in order to not repeat many calculations
		    while((cur=br.readLine())!=null) {
		    	System.out.println(cur);
		    	if(cur.length()>5 && (cur.substring(0, 5).equalsIgnoreCase("reply") || cur.substring(0, 2).equalsIgnoreCase("64"))) {
		    		String[] arr = cur.split(" ");
		    		if(timeIndex==-1) {
		    			for(int i=0;i<arr.length;i++) {
		    				String tok = arr[i];
		    				if(tok.length()>4 && tok.substring(0,4).equalsIgnoreCase("time")) {
		    					timeIndex = i;
		    					break;
		    				}
		    			}
		    		}
		    		lg.Log(parseTime(arr[timeIndex]));
		    	}
		    }
	    }catch(IOException e) {
	    	System.out.println("Exception in retro pinging");
	    	e.printStackTrace();
	    	return lg;
	    }
		return lg;
	}
	
	private double parseTime(String st) { //Gets the time from the string
		st = st.replace("time=", "");
		st = st.replace("time<", "");
		st = st.replace("ms", "");
		return Double.parseDouble(st); 
	}
	
	
	/*
	 * Method which pings a specific node a freq number of times using the function ping().
	 */
	public PingLog pingStart(NodeInfo node, int freq) throws RemoteException{
		PingLog lg = new PingLog(new NodeInfo(address, numID, nameID), node);
		RMIInterface nodeToPing = getRMI(node.getAddress());
		long befTime;
		long afrTime;
		while(freq-->0) {
			try {
				befTime = System.currentTimeMillis();
				nodeToPing.ping();
				afrTime = System.currentTimeMillis();
				lg.Log((double) afrTime-befTime);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}
		return lg;
	}
	/*
	 * Ping function. Simply returns true. 	 
	 */
	public boolean ping() throws RemoteException{
		return true;
	}

	/*
	 * Getters (For use in the remote testing)
	 */
	
	public Configuration getConf() throws RemoteException{
		synchronized(this) {
				if(configurationsLeft == 0 || cnfs == null) {
				return null;
			}
			System.out.println("Configuration given out. Configurations left: " +(configurationsLeft-1));
			return cnfs.get(cnfs.size() - (configurationsLeft--));
		}
	}

	public ArrayList<NodeInfo> getData() throws RemoteException{
		//TODO: fix this
		return new ArrayList<NodeInfo>();
	}
	public NodeInfo[][][] getLookupTable() throws RemoteException{
		//TODO: fix this
		return new NodeInfo[1][1][1];
	}

	public void logAllAndShutDown() {
		File logFile = new File(System.getProperty("user.dir")+File.separator+"logs" + File.separator + "CrashLogs"+File.separator+"Log_"+numID+".txt");
		logFile.getParentFile().mkdirs();
		try {
			NodeInfo curNode = null;
			ArrayList<NodeInfo> nodeList = new ArrayList<NodeInfo>();
			try {
				curNode = searchByNumID(0);
				System.out.println();
				while(curNode!=null) {
					nodeList.add(curNode);
					RMIInterface curRMI = getRMI(curNode.getAddress());
					curNode = curRMI.getRightNode(0, curNode.getNumID());
				}
			}catch(RemoteException e) {
				e.printStackTrace();
			}
			System.out.println("Total number of nodes: " + nodeList.size());
			Configuration.generateConfigFile(nodeList);
			PrintWriter writer = new PrintWriter(logFile);
			writer.println(lookup2.keySet());
			for(Integer cur : lookup2.keySet()) {
				writer.println(lookup2.get(cur).superString());
		        System.out.println("\n");
		        for(int i = maxLevels ; i >= 0 ; i--){
		           	if(lookup2.get(cur, i, LEFT)== null)
		        		writer.print("null\t");
		        	else {
		        		NodeInfo lNode = lookup2.get(cur, i, LEFT);
		        		writer.print(lNode.getAddress() + " " + lNode.getNumID() + " " + lNode.getNameID()+"\t");
		        	}
		           	if(lookup2.get(cur, i, RIGHT)== null)
		           		writer.print("null\t");
		        	else {
		        		NodeInfo rNode = lookup2.get(cur, i, RIGHT);
		        		writer.print(rNode.getAddress() + " " + rNode.getNumID() + " " + rNode.getNameID()+"\t");
		        	}
		           	writer.println("\n\n");
		        }
			}
			writer.close();
			System.exit(0);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
