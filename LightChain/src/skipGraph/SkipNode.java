package skipGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import blockchain.LightChainRMIInterface;
import blockchain.Transaction;
import util.Const;
import util.Util;
import remoteTest.Configuration;
import remoteTest.PingLog;
import remoteTest.TestingLog;



public class SkipNode extends UnicastRemoteObject implements RMIInterface{
	
	
	private static final long serialVersionUID = 1L;
		
	protected NodeInfo peerNode;
	protected String address;
	protected String nameID;
	protected String IP ;
	private String introducer; 
	private int maxLevels;
	protected int numID;
	protected int RMIPort ;
	private boolean isInserted = false;
	
	private LookupTable lookup;

	
	/*
	 * Constructor for SkipNode class
	 * The node requires the following info to be able to function:
	 * @param maxLevels		number of levels of the skip graph
	 * @param RMIPort		Port on which Skip Node is operating
	 * @param IP			IP of Skip Node
	 * @param numID			numerical ID of Skip Node
	 * @param nameID 		name ID of Skip Node
	 * @param introducer	the node that helps in inserting the current node
	 * @param isInitial		Indicator that this node is an initial node in the skipGraph
	 */
	public SkipNode(int maxLevels, int RMIPort, String IP, int numID, String nameID, String introducer, boolean isInitial) throws RemoteException{
		super(RMIPort);
		this.RMIPort = RMIPort ;
		this.IP = IP;
		this.maxLevels = maxLevels;
		this.address = IP + ":" + RMIPort;
		this.numID = numID;
		this.nameID = nameID;
		this.introducer = introducer;
		
		//TODO: this should be removed when launching LightChainNode
		Registry reg = LocateRegistry.createRegistry(RMIPort);
		reg.rebind("RMIImpl", this);
		Util.log("Rebinding Successful");
			
		// check if introducer has valid address
		if(!introducer.equals(Const.DUMMY_INTRODUCER) && !Util.validateIP(introducer)) {
			Util.log("Invalid introducer address");
			
		}
		
		// check if node address it valid
		if(!Util.validateIP(address)) {
			Util.log("Invalid node adress");
		}
		
		lookup = new LookupTable(maxLevels);
		peerNode = new NodeInfo(address,numID,nameID);
		lookup.addNode(peerNode);
		initRMI();
		if(!isInitial) {
			insertNode(peerNode);
		}
	}

	/*
	 * Deletes a data node with a given numerical ID
	 * @param	num numerical ID of node to be deleted
	 */

	public void delete(int num) throws RemoteException {
		
		try {
			for(int j = lookup.getMaxLevels() ; j >=0  ; j--) {
				// if there are no neighbors at level j, just move on
				NodeInfo lNode = lookup.get(num, j, Const.LEFT);
				NodeInfo rNode = lookup.get(num, j, Const.RIGHT);
				NodeInfo thisNode = lookup.get(num);
				
				if(lNode == null && rNode == null ) {
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
			lookup.remove(num);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	
	/*
	 * This method inserts either the current node to the skip graph of the introducer,
	 * or it is used to insert a data node.
	 */
	public void insertNode(NodeInfo insertedNode){
		try {
			
			lookup.initializeNode(insertedNode);
	
			// We search through the introducer node to find the node with 
			// the closest num ID
			NodeInfo closestNode;
			if(isInserted) {	
				
				closestNode = searchByNumID(insertedNode.getNumID());
			
			}else {
				isInserted = true;
				
				RMIInterface introRMI = getRMI(introducer);
				closestNode = introRMI.searchByNumID(insertedNode.getNumID());
			
			}
			
			if(closestNode == null) {
				Util.log("The address resulting from the search is null");
				return;
			}
			
			
			// First, we insert the node at level 0
			RMIInterface closestNodeRMI = getRMI(closestNode.getAddress());
			
			int closestNodeNumID = closestNodeRMI.getNumID(); // numID of the closest node
			
			NodeInfo leftNode = null;
			NodeInfo rightNode = null;
		
			int leftNodeNumID = Const.UNASSIGNED_INT; // numID of left node
			int rightNodeNumID = Const.UNASSIGNED_INT ; // numID of right node
			
			if(insertedNode.getNumID() < closestNodeNumID) { // if the closest node is to the right
				
				leftNode = closestNodeRMI.getLeftNode(Const.ZERO_LEVEL,closestNodeNumID);
				
				rightNode = closestNode;
				rightNodeNumID = closestNode.getNumID(); // we need the numID to be able to access it
				
				if(leftNode != null) { // insert the current node in the lookup table of my left node if it exists
					RMIInterface leftNodeRMI = getRMI(leftNode.getAddress());
					leftNodeNumID = leftNode.getNumID();
					lookup.put(insertedNode.getNumID(), Const.ZERO_LEVEL, Const.LEFT, Util.assignNode(leftNode), null); //null because the lookup table is empty
					leftNodeRMI.setRightNode(leftNodeNumID, Const.ZERO_LEVEL, insertedNode, rightNode);
					
				}				
				lookup.put(insertedNode.getNumID(), Const.ZERO_LEVEL, Const.RIGHT, Util.assignNode(rightNode), null);
				closestNodeRMI.setLeftNode(closestNodeNumID, Const.ZERO_LEVEL, insertedNode, leftNode);// insert the current node in the lookup table of its right neighbor
				
			}else{ // if the closest node is to the left
				
				rightNode = closestNodeRMI.getRightNode(Const.ZERO_LEVEL,closestNodeNumID);
				
				leftNode = closestNode ; 
				leftNodeNumID = closestNode.getNumID(); // we need the numID to be able to access it
				
				lookup.put(insertedNode.getNumID(), Const.ZERO_LEVEL, Const.LEFT, Util.assignNode(leftNode), null); //This is before the if statement so that the order of insertion is the same
				closestNodeRMI.setRightNode(closestNodeNumID, Const.ZERO_LEVEL, insertedNode, leftNode);// insert the current node in the lookup table of its right neighbor
				if(rightNode != null) { // insert current node in the lookup table of its right neighbor if it exists
					RMIInterface rightRMI = getRMI(rightNode.getAddress());
					rightNodeNumID = rightNode.getNumID();
					lookup.put(insertedNode.getNumID(), Const.ZERO_LEVEL, Const.RIGHT, Util.assignNode(rightNode), null); //null because the lookup table is empty
					rightRMI.setLeftNode(rightNodeNumID, Const.ZERO_LEVEL, insertedNode, leftNode);
				}						
			}
			
			// Now, we insert the node in the rest of the levels
			// In level i , we make a recursive search for the nodes that will be
			// the neighbors of the inserted nodes at level i+1
			
			int level = Const.ZERO_LEVEL;
			while(level < lookup.getMaxLevels()) {
				
				if(leftNode != null) {
					
					RMIInterface leftRMI = getRMI(leftNode.getAddress());
					NodeInfo lft = leftRMI.insertSearch(level,Const.LEFT,leftNodeNumID,insertedNode.getNameID()); // start search left
					
					lookup.put(insertedNode.getNumID(), level+1, Const.LEFT, Util.assignNode(lft), null);
					
					// set left and leftNum to default values (null,-1)
					// so that if the left neighbor is null then we no longer need
					// to search in higher levels to the left
					leftNode = null;
					leftNodeNumID = Const.UNASSIGNED_INT;
					
					if(lft != null) {
						RMIInterface lftRMI = getRMI(lft.getAddress());
						lftRMI.setRightNode(lft.getNumID(), level+1, insertedNode, null);
						leftNode = lft;
						leftNodeNumID = lft.getNumID();
					}	
				}
				if(rightNode != null) {
					
					RMIInterface rightRMI = getRMI(rightNode.getAddress());
					NodeInfo rit = rightRMI.insertSearch(level, Const.RIGHT, rightNodeNumID, insertedNode.getNameID()); // start search right
					lookup.put(insertedNode.getNumID(), level+1, Const.RIGHT, Util.assignNode(rit), null);
					
					// set right and rightNum to default values (null,-1)
					// so that if the right neighbor is null then we no longer need
					// to search in higher levels to the right
					rightNode = null;
					rightNodeNumID = Const.UNASSIGNED_INT;
					
					if(rit != null) {
						RMIInterface ritRMI = getRMI(rit.getAddress());
						ritRMI.setLeftNode(rit.getNumID(), level+1, insertedNode, null);
						rightNode = rit;
						rightNodeNumID = rit.getNumID();
					}
				}
				level++;
			}
			// after we conclude inserting the node in all levels,
			// we add the inserted node to the data array
			// and we map its numID with its index in the data array using dataID
			lookup.finalizeNode();
	
		}catch(RemoteException e) {
			e.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * A helper method for Insert(), inserts a node recursively per level.
	 * @param level		The level in which a node is being inserted
	 * @param direction At a certain level, the method go LEFT and RIGHT finding the neighbors of the inserted node
	 * @param num 		The numerical ID of the node at which the search has arrived
	 * @param target	the name ID of the inserted node. 
	 * @return Right neighbor if direction is RIGHT, and left neighbor if direction is LEFT
	 * @see RMIInterface#insertSearch(int, int, int, java.lang.String)
	 */
	public NodeInfo insertSearch(int level, int direction,int nodeNumID, String target) throws RemoteException {		
		try {
			
			NodeInfo currentNode = lookup.get(nodeNumID);

			if(currentNode == null) return null;
			// If the current node and the inserted node have common bits more than the current level,
			// then this node is the neighbor so we return it
			if(Util.commonBits(target, currentNode.getNameID()) > level) 
				return currentNode;
			// If search is to the right then delegate the search to right neighbor if it exists
			// If the right neighbor is null then at this level the right neighbor of the inserted node is null
			if(direction == Const.RIGHT) {
				NodeInfo rNode = lookup.get(nodeNumID, level, direction);
				if(rNode==null)
					return null;
				RMIInterface rRMI = getRMI(rNode.getAddress());
				return rRMI.insertSearch(level,direction,rNode.getNumID(),target);
			} else {
				// If search is to the left then delegate the search to the left neighbor if it exists
				// If the left neighbor is null, then the left neighbor of the inserted node at this level is null.
				NodeInfo lNode = lookup.get(nodeNumID, level, direction);
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


	/*
	 * This method receives a number, and returns the data node (or possibly the main node)
	 * which has the closest numID to the given number
	 */
	public int getBestNum(int num) {
		return lookup.getBestNum(num);
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
			lst = searchByNumIDHelper(searchTarget,lst);
			return lst == null ? null : lst.get(lst.size() - 1);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<NodeInfo> searchByNumIDHelper(int searchTarget, ArrayList<NodeInfo> lst){
		try {
			
			int level = lookup.getMaxLevels();
			int num = getBestNum(searchTarget); // route search to closest data node
			if(lookup.get(num, Const.ZERO_LEVEL, Const.LEFT) == null && lookup.get(num, Const.ZERO_LEVEL, Const.RIGHT) == null) {
				lst.add(lookup.get(num));
				return lst;
			}
			return searchNumID(numID,searchTarget,level,lst);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/*
	 * This method is a hepler method for searchByNumID()
	 * It recieves the target numID and the level it is searching in,
	 * and it routes the search through the skip graph recursively using RMI
	 * @see RMIInterface#searchNum(int, int)
	 */
	public ArrayList<NodeInfo> searchNumID(int numID, int targetInt,int level, ArrayList<NodeInfo> lst) throws RemoteException{
		
		int num;
		if(numID != lookup.bufferNumID()) {
			num = getBestNum(targetInt);// get the data node (or main node) that is closest to the target search
			lst.add(lookup.get(num));//Add the current node's info to the search list
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
			while(level >= Const.ZERO_LEVEL && (lookup.get(num, level, Const.RIGHT) == null || lookup.get(num, level, Const.RIGHT).getNumID() > targetInt))
				level--;
			// If there are no more levels to go down to return the current node
			if(level < Const.ZERO_LEVEL) {
				return lst;
			}
			// delegate the search to the right neighbor
			RMIInterface rightRMI = getRMI(lookup.get(num, level, Const.RIGHT).getAddress());
			try{
				return rightRMI.searchNumID(lookup.get(num, level, Const.RIGHT).getNumID(),targetInt,level,lst);
			}catch(StackOverflowError e) {
				
				return null;
			}catch(Exception e) {
				return lst;
			}
		}
		else { // If the target is less than the current node then we should search left 
			// Keep going down levels as long as there is either no right neighbor
			// or the left neighbor has a numID greater than the target
			while(level >= Const.ZERO_LEVEL && (lookup.get(num, level, Const.LEFT) == null || lookup.get(num, level, Const.LEFT).getNumID() < targetInt))
				level--;
			// If there are no more levels to go down to return the current node
			if(level < Const.ZERO_LEVEL)
				return lst;
			// delegate the search to the left neighbor
			RMIInterface leftRMI = getRMI(lookup.get(num, level, Const.LEFT).getAddress());
			try{
				return leftRMI.searchNumID(lookup.get(num, level, Const.LEFT).getNumID(),targetInt, level, lst);
			}catch(StackOverflowError e) {
				
				return null;
			}catch(Exception e) {
				return lst;
			}
		}
	}
	/*
	 * This method receives a nameID and returns the index of the data node which has
	 * the most common prefix with the given nameID
	 */
	public int getBestName(String name,int direction) {
		return lookup.getBestName(name, direction);
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
			int newLevel = Util.commonBits(searchTarget,nameID);
			NodeInfo result = lookup.get(bestNum);
			
			// First execute the search in the right direction and see the result it returns
			if(lookup.get(bestNum, newLevel, Const.RIGHT) != null) {
				RMIInterface rightRMI = getRMI(lookup.get(bestNum, newLevel, Const.RIGHT).getAddress());
				result = rightRMI.searchName(lookup.get(bestNum, newLevel, Const.RIGHT).getNumID(),searchTarget,newLevel,Const.RIGHT);
			}
			// If the result is not null and is different from the default value we check it
			if(result != null && !result.equals(lookup.get(bestNum))) {
				RMIInterface resultRMI = getRMI(result.getAddress());
				// If this is the result we want return it, otherwise continue searching to the left
				if(resultRMI.getNameID().contains(searchTarget))
					return result;
			}
			// If the desired result was not found try to search to the left
			if(lookup.get(bestNum, newLevel, Const.LEFT) != null) {
				RMIInterface leftRMI = getRMI(lookup.get(bestNum, newLevel, Const.LEFT).getAddress());
				NodeInfo k = leftRMI.searchName(lookup.get(bestNum, newLevel, Const.LEFT).getNumID(),searchTarget, newLevel, Const.LEFT);
				if(Util.commonBits(k.getNameID(),lookup.get(bestNum).getNameID()) > Util.commonBits(result.getNameID(),lookup.get(bestNum).getNameID()))
					result = k;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * This method is a Util method for searchByNameID()
	 * It receives the target nameID and the level it is searching in, and also the direction
	 * of search, and it routes the search through the skip graph recursively using RMI
	 * It return the most similar node if the node itself is not found
	 * The similarity is defined to be the the maximum number of common bits
	 * If direction == 1, then search is to the right
	 * If direction == 0, then search is to the left
	 * @see RMIInterface#searchName(java.lang.String, int, int)
	 */

	public NodeInfo searchName(int numID, String searchTarget,int level,int direction) throws RemoteException {
		
		try {
			
			if(numID == lookup.bufferNumID()) {
				lookup.get(numID, 0, Const.LEFT); //only executes when the buffer node finishes inserting
			}
			int bestNum = getBestName(searchTarget,direction);
			if(lookup.get(bestNum).getNameID().equals(searchTarget)) // if the current node hold the same nameID, return it.
				return lookup.get(bestNum);
			// calculate common bits to find to which level the search must be routed
			int newLevel = Util.commonBits(searchTarget,lookup.get(bestNum).getNameID()); 
			
			// If the number of common bits is not more than the current level
			// then we continue the search in the same level in the same direction
			if(newLevel <= level ) {
				if(lookup.get(bestNum, level, direction) == null)// If no more nodes in this direction return the current node
					return lookup.get(bestNum) ;
				RMIInterface rightRMI = getRMI(lookup.get(bestNum, level, direction).getAddress());
				return rightRMI.searchName(lookup.get(bestNum, level, direction).getNumID(),searchTarget, level, direction);
			}
			// If the number of common bits is more than the current level
			// then the search will be continued on the new level
			// so we start a search in both directions in the new level
			
			NodeInfo result = lookup.get(bestNum); // we initialize the result to current node
			
			// First we start the search on the same given direction and wait for the result it returns
			if(lookup.get(bestNum, newLevel, direction) != null) {
				RMIInterface rightRMI = getRMI(lookup.get(bestNum, newLevel, direction).getAddress());
				result = rightRMI.searchName(lookup.get(bestNum, newLevel, direction).getNumID(),searchTarget,newLevel,direction);
			}
			// If it returns a result that differs from the current node then we check it
			if(result != null && !result.equals(lookup.get(bestNum))) {
				RMIInterface resultRMI = getRMI(result.getAddress());
				// If this is the result we want return it, otherwise continue the search in the opposite direction
				if(resultRMI.getNameID().contains(searchTarget))
					return result;
			}
			// Continue the search on the opposite direction
			if(lookup.get(bestNum, newLevel, 1-direction) != null) {
				RMIInterface leftRMI = getRMI(lookup.get(bestNum, newLevel, 1-direction).getAddress());
				NodeInfo k = leftRMI.searchName(lookup.get(bestNum, newLevel, 1-direction).getNumID(),searchTarget, newLevel, 1-direction);
				if(result == null || Util.commonBits(k.getNameID(),lookup.get(bestNum).getNameID()) > Util.commonBits(result.getNameID(),lookup.get(bestNum).getNameID()))
					result = k;
			}
			return result;
		}catch(StackOverflowError e) {
			//testLog.logOverflow(e, data, "Overflow in searchName.\nSearch target: "+ searchTarget + "\tlevel: "+level+" direction: "+ direction);
			return null;
		}catch (Exception e) {
			e.printStackTrace();
			Util.log("Error when inserting " + searchTarget + " at address " + address);
			return null;
		}	
	}
	
	/*
	 * This method receives the numID (Hash Value) of a block and conducts a nameID 
	 * search on it in order to find all transactions with this nameID. First it finds
	 * one node, and then goes to the linked list at last level of the skip graph which
	 * contains the found node and collects all nodes that are transactions from that level
	 * because they all will have the same nameID according to the structure of the skip graph
	 */
	public ArrayList<NodeInfo> getNodesWithNameID(String name) {
		try {
			// find a transaction that has the given nameID 
			NodeInfo targetNode = searchByNameID(name);
			// an empty list to add transaction to it and return it
			ArrayList<NodeInfo> list = new ArrayList<>();
			
			
			if(targetNode == null || !targetNode.getNameID().equals(name)) {
				Util.log("getNodesWithNameID: No Node was found with the given nameID");
				return list;
			}
			// left and right will store the address of the nodes we are visiting in left and right respectively
			NodeInfo leftNode , rightNode ;
			// leftNum and rightNum will store numIDs of left and right nodes, used to correctly access nodes (data nodes functionality)
			int leftNumID = Const.UNASSIGNED_INT, rightNumID = Const.UNASSIGNED_INT;
			// thisRMI is just used to extract information of neighbors
			RMIInterface thisRMI = getRMI(targetNode.getAddress());
			
			// get addresses of left and right nodes, as well as their numIDs
			leftNode = thisRMI.getLeftNode(maxLevels,targetNode.getNumID());
			if(leftNode != null)
				leftNumID = thisRMI.getLeftNumID(maxLevels, targetNode.getNumID());
			
			rightNode = thisRMI.getRightNode(maxLevels,targetNode.getNumID());
			if(rightNode != null)
				rightNumID = thisRMI.getRightNumID(maxLevels, targetNode.getNumID());
			
			// now in the last level of the skip graph, we go left and right
			while(leftNode != null) {
				RMIInterface leftRMI = getRMI(leftNode.getAddress());
				NodeInfo thisNode = leftRMI.getNode(leftNumID);
				
				leftNode = leftRMI.getLeftNode(lookup.getMaxLevels(), leftNumID);
				if(leftNode != null)
				leftNumID = leftRMI.getLeftNumID(lookup.getMaxLevels(),leftNumID);
			}
			while(rightNode != null) {
				RMIInterface rightRMI = getRMI(rightNode.getAddress());
				NodeInfo node = rightRMI.getNode(rightNumID);
				
				// now go to the right node again
				rightNode = rightRMI.getRightNode(lookup.getMaxLevels(), rightNumID);
				if(rightNode != null)
				rightNumID = rightRMI.getRightNumID(lookup.getMaxLevels(), rightNumID);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/*
	 * getters and setters for lookup table and numID and nameID
	 *
	 */
	public NodeInfo getLeftNode(int level,int num) throws RemoteException {
		return lookup.get(num, level, Const.LEFT);
	}

	public NodeInfo getRightNode(int level, int num) throws RemoteException {
		return lookup.get(num, level, Const.RIGHT);
	}

	public boolean setLeftNode(int num, int level,NodeInfo newNode, NodeInfo oldNode) throws RemoteException{
		return lookup.put(num, level, Const.LEFT, Util.assignNode(newNode), oldNode);
	}

	public boolean setRightNode(int num, int level,NodeInfo newNode, NodeInfo oldNode) throws RemoteException{
		return lookup.put(num, level, Const.RIGHT, Util.assignNode(newNode), oldNode);
	}

	public int getNumID(){
		return numID;
	}
	public String getNameID() {
		return nameID ;
	}
	
	protected int getDataNum() {
		return lookup.size();
	}
	public String getAddress() {
		return address;
	}
	protected int getRMIPort() {
		return RMIPort;
	}
	protected void setNumID(int num) {
		numID = num;
	}
	protected void setNameID(String s) {
		nameID = s;
	}

	public int getLeftNumID(int level,int num) {
		return lookup.get(num, level, Const.LEFT).getNumID();
	}
	public int getRightNumID(int level, int num) {
		return lookup.get(num, level, Const.RIGHT).getNumID();
	}
	public String getLeftNameID(int level,int num) {
		return lookup.get(num, level, Const.LEFT).getNameID();
	}
	public String getRightNameID(int level,int num) {
		return lookup.get(num, level, Const.RIGHT).getNameID();
	}
	public NodeInfo getNode(int num) {
		return lookup.get(num);
	}
	

	/*
	 * This method returns an RMI instance of the node with the given address
	 */
	public RMIInterface getRMI(String adrs) {
		
		if(!Util.validateIP(adrs)) {
			Util.log("Error in lookup up RMI. Address " + adrs + " is not a valid address");
			return null;
		}
		
		if(adrs.equalsIgnoreCase(address)) return this;
		
		try {
			return (RMIInterface)Naming.lookup("//"+adrs+"/RMIImpl");
		
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	

	/*
	 * This method initializes all the RMI system properties required for proper functionality
	 */
	
	protected void initRMI() {
		IP = Util.grabIP();
		try {
			System.setProperty("java.rmi.server.hostname",IP);
			System.setProperty("java.rmi.server.useLocalHostname", "false");
			System.out.println("RMI Server proptery set. Inet4Address: "+IP);
		}catch (Exception e) {
			System.err.println("Exception in initialization. Please try running the program again.");
			System.exit(0);
		}
	}
	

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// For Testing purposes
	protected static int configurationsLeft = 0;
	protected static ArrayList<Configuration> cnfs; //0th = master nodeinfo

	protected static TestingLog testLog;
	
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
			writer.println(lookup.keySet());
			for(Integer cur : lookup.keySet()) {
				writer.println(lookup.get(cur).superString());
		        System.out.println("\n");
		        for(int i = lookup.getMaxLevels() ; i >= 0 ; i--){
		           	if(lookup.get(cur, i, Const.LEFT)== null)
		        		writer.print("null\t");
		        	else {
		        		NodeInfo lNode = lookup.get(cur, i, Const.LEFT);
		        		writer.print(lNode.getAddress() + " " + lNode.getNumID() + " " + lNode.getNameID()+"\t");
		        	}
		           	if(lookup.get(cur, i, Const.RIGHT)== null)
		           		writer.print("null\t");
		        	else {
		        		NodeInfo rNode = lookup.get(cur, i, Const.RIGHT);
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
