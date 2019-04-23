import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class SkipNode extends UnicastRemoteObject implements RMIInterface{
	
	
	private static final long serialVersionUID = 1L;
	public static String address;
	public static String nameID;
	public static int numID;
	public static String IP ;
	public static NodeInfo[][] lookup ;
	public static int port;
	public static int maxLevels = 5; 
	public static String introducer; 
	public static int RMIPort = 1099;
	private static Scanner in = new Scanner(System.in);
	
	// General Notes :
	// Try defining a function that gets an IP and return RMIInterface instance
	// Currently we are assuming that RMI port for each node is 1099
	
	public static void main(String args[]) {
		
		
		lookup = new NodeInfo[maxLevels+1][2]; // Initialize size of lookup table
		
		setInfo();
		
		ServerConnection server = new ServerConnection();
		server.start();	
		
		try {
			
			SkipNode skipNode = new SkipNode();
			
			Naming.rebind("//localhost/"+"RMIImpl",skipNode); // make methods of skipNode instance available on RMI Registry
			
			log("Rebinding Successful");
			
			while(true) {
				printMenu();
				skipNode.ask();
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
	 * Constructor for SkipNode class needed for RMI setup
	 */
	protected SkipNode() throws RemoteException{
		super();
		try {
			String st = Inet4Address.getLocalHost().getHostAddress();
			System.setProperty("java.rmi.server.hostname",st);
			System.out.println("RMI Server proptery set. Inet4Address: "+st);
		}catch (UnknownHostException e) {
			System.err.println("Unknown Host Exception in constructor. Please terminate the program and try again.");
		}
	}
	/*
	 * This method initializes the information of the current node
	 * and prints them to console
	 */
	public static void setInfo() {
		
		log("Enter your Name ID:");
		nameID = get();
		while(!nameID.matches("[0-1]+")) {//Makes sure the name ID is a binary string
			log("Name ID should be a binary string. Please enter a valid Name ID:");
			nameID = get();
		}
		log("Enter your Numeric Id:");
		String numInput = get();
		while(!numInput.matches("0|[1-9][0-9]*")) {//Makes sure the number ID is an actual number
			log("Number ID should be a number. Please enter a valid Number ID:");
			numInput = get();
		}
		numID = Integer.parseInt(numInput);
		log("Enter the address of the introducer:");
		introducer = get();
		while(!(introducer.equalsIgnoreCase("None") || validateIP(introducer))) {
			log("Invalid IP. Please enter a valid IP address ('none' if original node): ");
			introducer = get();
		}
		log("Your INFO:\nnameID: "+nameID+"\nnumID: "+numID+"\nintroducer: "+introducer);
	}
	
	/*
	 * This method prints the options for user controlling the node to choose.
	 * More options can be appended but require the modification of ask() method
	 */
	public static void printMenu() throws IOException{
        log("Node at the address: " + address);
        log("Name ID: "+ nameID +" Number ID: " + numID);
        log("Choose a query by entering it's code and then press Enter");
        log("1-Insert");
        log("2-Search By Name ID");
        log("3-Search By Number ID");
        log("4-Print the Lookup Table");
        log("5-Print left node at a chosen level.");
        log("6-Print right node at a chose level.");
	}
	
	/*
	 * Gets the type of operation to be executed from the user
	 * and executes the corresponding operation.
	 */
	public void ask(){// throws MalformedURLException, RemoteException, NotBoundException {
        String input = get();
        if(!input.matches("[1-6]")) {
        	log("Invalid query. Please enter the number of one of the possible operations");
        	return;
        }
		int query = Integer.parseInt(input);
		
		if(query == 1)
			if(introducer.equalsIgnoreCase("none"))System.out.println("Can't insert. Current node is the initial node.");
			else insert();
		else if (query == 2) {
			log("Please Enter the name ID to be searched");
			String name = get();
			while(!name.matches("[0-1]")) {//Makes sure the name is a binary string
				log("Name ID should be a binary string. Please enter a valid Name ID:");
				name = get();
			}
			String result = null;
			try{
				result = searchByNameID(name);
			}catch(RemoteException e) {
				e.printStackTrace();
				log("Remote Exception in query.");
			}
			log("The result of search by name ID is: "+result);
		}else if(query == 3) {
			log("Please Enter the numeric ID to be searched");
			String numInput = get();
			while(!numInput.matches("0|[1-9][0-9]*")) {
				log("Invalid number entered. Please enter a valid number");
				numInput = get();
			}
			int num = Integer.parseInt(numInput);
			String result = searchByNumID(num);
			log("The result of search by numberic ID is: "+ result);
		}else if(query == 4)
			printLookup();
		else if( query == 5) {
			log("Please Enter the required level:");
			int lvl = Integer.parseInt(get());
			if(lookup[lvl][0] == null)
				log("No left node present at level "+lvl);
			else
				log("Left node at level "+lvl+" is:" + lookup[lvl][0].getAddress());
		}
		else if (query == 6) {
			log("Please Enter the required level:");
			int lvl = Integer.parseInt(get());
			if(lookup[lvl][1] == null)
				log("No right node present at level "+lvl);
			else
				log("Right node at level "+lvl+" is:" + lookup[lvl][1].getAddress());
		}
        
    }
	
	/* 
	 * This method places the node in its correct position in the skip graph 
	 * with the help of the introducer node.
	 * It executes a search on its numID by numeric ID from the introducer using RMI
	 * and then positions itself in the appropriate positions at each level of the skip graph
	 * 
	 */
	
	public static void insert(){
		try {
			String left = null;
			String right = null;

			RMIInterface introRMI = getRMI(introducer);		
			String position = introRMI.searchByNumID(numID);
			
			if(position == null) {
				log("The address resulting from the search is null");
				log("Please check the introducer's IP address and try again.");
				return;
			}
			else
				log("The address resulting from the search is: " + position);
			
			RMIInterface posRMI = getRMI(position);
			if(posRMI == null) {
				log("RMI registry lookup at address: "+position+" failed. Insert operation stopped.");
				return;
			}
			if(posRMI.getNumID() > numID) {

				right = position;
				left = posRMI.getLeftNode(0);

				lookup[0][1] = new NodeInfo(right,posRMI.getNumID(),posRMI.getNameID());
				posRMI.setLeftNode(0, new NodeInfo(address,numID,nameID));
				
				if(left != null) {
					RMIInterface leftRMI = getRMI(left);
					lookup[0][0] = new NodeInfo(left,leftRMI.getNumID(),leftRMI.getNameID());
					leftRMI.setRightNode(0, new NodeInfo(address,numID,nameID));
				}

			}else {
				
				right  = posRMI.getRightNode(0);
				left = position ; 
				
				lookup[0][0] = new NodeInfo(left,posRMI.getNumID(),posRMI.getNameID());
				posRMI.setRightNode(0, new NodeInfo(address,numID,nameID));
				
				if(right != null) {
					RMIInterface rightRMI = getRMI(right);
					lookup[0][1] = new NodeInfo(right,rightRMI.getNumID(),rightRMI.getNameID()) ;
					rightRMI.setLeftNode(0,new NodeInfo(address,numID,nameID));
				}
				
			}
			
			int level = 0;
			while(level <= maxLevels) {
				
				while(true) {
					if(left != null) {
						RMIInterface leftRMI = getRMI(left);
						if(commonBits(leftRMI.getNameID()) <= level)
							left = leftRMI.getLeftNode(level);
						else break;
					}	
					if(right != null) {
						RMIInterface rightRMI = getRMI(right);
						if(commonBits(rightRMI.getNameID()) <= level)
							right = rightRMI.getRightNode(level);
						else break;	
					}
					if(right == null && left == null)
							break;
				}
				if(left != null) {	
					RMIInterface leftRMI = getRMI(left);
					if(commonBits(leftRMI.getNameID()) > level) {
						String rightNeighbor = leftRMI.getRightNode(level+1);
						leftRMI.setRightNode(level+1, new NodeInfo(address,numID,nameID));
						if(rightNeighbor != null) {
							RMIInterface rightNeighborRMI = getRMI(rightNeighbor);
							rightNeighborRMI.setLeftNode(level+1, new NodeInfo(address,numID,nameID));
							lookup[level+1][1] = new NodeInfo(rightNeighbor,rightNeighborRMI.getNumID(),rightNeighborRMI.getNameID()) ;
						}
						lookup[level+1][0] = new NodeInfo(left,leftRMI.getNumID(),leftRMI.getNameID());
						right = rightNeighbor ;
					}
				}
				if(right != null){
					RMIInterface rightRMI = getRMI(right);
					if(commonBits(rightRMI.getNameID()) > level) {
						String leftNeighbor = rightRMI.getLeftNode(level+1);
						rightRMI.setLeftNode(level+1, new NodeInfo(address,numID,nameID));
						if(leftNeighbor != null) {
							RMIInterface leftNeighborRMI = getRMI(leftNeighbor);
							leftNeighborRMI.setRightNode(level+1, new NodeInfo(address,numID,nameID));
							lookup[level+1][0] = new NodeInfo(leftNeighbor,leftNeighborRMI.getNumID(),leftNeighborRMI.getNameID()) ;
						}
						lookup[level+1][1] = new NodeInfo(right,rightRMI.getNumID(),rightRMI.getNameID());				
						left = leftNeighbor ;
					}
				}
				level++ ;
				if(left == null && right == null)
					break;
			}
		}catch(RemoteException e) {
			e.printStackTrace();
			log("Remote Exception thrown in insert function.");
		}
	}
	
	public String searchNum(int targetInt,int level){
		if(numID == targetInt)
			return address;
		if(numID < targetInt) {
			while(level >= 0 && (lookup[level][1] == null || lookup[level][1].getNumID() > targetInt))
				level--;
			if(level < 0) 
				return address;
			RMIInterface rightRMI = getRMI(lookup[level][1].getAddress());
			try{
				return rightRMI.searchNum(targetInt,level);
			}catch(Exception e) {
				System.out.println("Exception in searchNum. Target: "+targetInt);
			}
		}
		else {
			while(level >= 0 && (lookup[level][0] == null || lookup[level][1].getNumID() > targetInt))
				level--;
			if(level < 0)
				return address;
			RMIInterface leftRMI = getRMI(lookup[level][0].getAddress());
			try{
				return leftRMI.searchNum(targetInt, level);
			}catch(Exception e) {
				System.out.println("Exception in searchNum. Target: "+targetInt);
			}
		}
		return null;
	}
	
	/* 
	 * Executes a search through the skip graph by numeric id and returns the address of the 
	 * the target node or if not found the node with closest numeric ID 
	 * @see RMIInterface#searchByNumID(java.lang.String)
	 */
	public String searchByNumID(int searchTarget){
	
		int level = maxLevels;
		
		if(lookup[0][0] == null && lookup[0][1] == null)
			return address;
		return searchNum(searchTarget,level);
		
//		int level = maxLevels ; // start search at the highest level
//		
//		// cast target ID and this node's ID to integers not to use parsing several times again
//		int numIDInt = Integer.parseInt(numID);  
//		int targetInt = Integer.parseInt(targetNum); 
//		// If the introducer exists only
//		if(lookup[0][0] == null && lookup[0][1] == null) {
//			return address ;
//		}
//		// The Target is on the right of numID
//		else if (numIDInt < targetInt) {
//			String next = null ;
//			// as long as there is no right node keep going down
//			while(level >= 0 && lookup[level][1] == null)
//				level--;
//			if(level < 0)
//				return next;
//			next = lookup[level][1].getAddress();
//			while(level >= 0) {
//				RMIInterface nextRMI = getRMI(next);
//				if(nextRMI.getRightNode(level) != null) {
//					RMIInterface nextOfNextRMI = getRMI(nextRMI.getRightNode(level));
//					if(Integer.parseInt(nextOfNextRMI.getNumID()) < targetInt)
//						next = nextRMI.getRightNode(level);
//					else if (Integer.parseInt(nextOfNextRMI.getNumID()) == targetInt) // if found return it
//						return nextRMI.getRightNode(level);
//					else  level--; // otherwise go down a level
//				}else level--;
//			}
//			return next ;
//		}
//		else{ // the target is to the left of the current node.
//			String next = null;
//			while(level >= 0 && lookup[level][0] == null)
//				level--;
//			if(level < 0)
//				return next;
//			next = lookup[level][0].getAddress();
//			while(level >= 0) {		
//				RMIInterface nextRMI = getRMI(next);
//				if(nextRMI.getLeftNode(level) != null) {
//					RMIInterface nextOfNextRMI = getRMI(nextRMI.getLeftNode(level));
//					if(Integer.parseInt(nextOfNextRMI.getNumID()) > targetInt)
//							next = nextRMI.getLeftNode(level);
//					else if (Integer.parseInt(nextOfNextRMI.getNumID()) == targetInt)
//						return nextRMI.getLeftNode(level);
//					else level--;
//				}else level--;
//			}
//			return next ;
//		}
	}
	// 1: right
	// 0: left
	public String searchName(String searchTarget,int level,int direction) throws RemoteException{
		
		if(nameID.equals(searchTarget))
			return address;
		int newLevel = commonBits(searchTarget);
		if(direction == 1) {
			
			if(newLevel <= level ) {
				if(lookup[level][1] == null)
					return address ;
				RMIInterface rightRMI = getRMI(lookup[level][1].getAddress());
				return rightRMI.searchName(searchTarget, level, direction);
			}
			String result = null;
			if(lookup[newLevel][1] != null) {
				RMIInterface rightRMI = getRMI(lookup[newLevel][1].getAddress());
				result = rightRMI.searchName(searchTarget,newLevel,1);
			}
			if(result != null) {
				RMIInterface resultRMI = getRMI(result);
				if(resultRMI.getNameID().contains(searchTarget))
					return result;
			}
			if(lookup[newLevel][0] != null) {
				RMIInterface leftRMI = getRMI(lookup[newLevel][0].getAddress());
				result = leftRMI.searchName(searchTarget, newLevel, -1);
			}
			return result;	
				
		}
		if(newLevel <= level) {
			if(lookup[level][0] == null)
				return address ;
			RMIInterface leftRMI = getRMI(lookup[level][0].getAddress());
			return leftRMI.searchName(searchTarget, level, direction);
		}
		String result = null;
		if(lookup[newLevel][1] != null) {
			RMIInterface rightRMI = getRMI(lookup[newLevel][1].getAddress());
			result = rightRMI.searchName(searchTarget,newLevel,1);
		}
		if(result != null) {
			RMIInterface resultRMI = getRMI(result);
			if(resultRMI.getNameID().contains(searchTarget))
				return result;
		}
		if(lookup[newLevel][0] != null) {
			RMIInterface leftRMI = getRMI(lookup[newLevel][0].getAddress());
			result = leftRMI.searchName(searchTarget, newLevel, -1);
		}
		return result;
	}
	/*
	 * Execute search by nameID
	 * and return the closest result
	 * @see RMIInterface#searchByNameID(java.lang.String)
	 */
	public String searchByNameID(String searchTarget) throws RemoteException{
		
		int newLevel = commonBits(searchTarget);
		String result = null;
		
		if(lookup[newLevel][1] != null) {
			RMIInterface rightRMI = getRMI(lookup[newLevel][1].getAddress());
			result = rightRMI.searchName(searchTarget,newLevel,1);
		}
		if(result != null) {
			RMIInterface resultRMI = getRMI(result);
			if(resultRMI.getNameID().contains(searchTarget))
				return result;
		}
		if(lookup[newLevel][0] != null) {
			RMIInterface leftRMI = getRMI(lookup[newLevel][0].getAddress());
			result = leftRMI.searchName(searchTarget, newLevel, -1);
		}
		return result;
//		String left = null
//		String right = null;
//		int prefix = commonBits(nameID,targetName) ;
//		if(prefix > level) {
//			level = prefix ;
//			left = lookup[level][0].getAddress();
//			right = lookup[level][1].getAddress();
//		}
//		while(true) {
//			
//			if(left != null) {		
//				RMIInterface leftRMI = getRMI(left);
//				prefix =  commonBits(leftRMI.getNameID(),targetName) ;
//				if(leftRMI.getNameID().contains(targetName))
//					return left ;
//				else if (prefix <= level)
//					left = leftRMI.getLeftNode(level);
//				else { // commonBits > level
//					level = prefix ;
//					right = leftRMI.getRightNode(level);
//					left = leftRMI.getLeftNode(level);
//					continue;
//				}
//			} else if(right != null) {
//				RMIInterface rightRMI = getrRMI(right);
//				prefix = commonBits(rightRMI.getNameID(),targetName);
//				
//				if(rightRMI.getNameID().contains(targetName))
//					return right;
//				else if (prefix <= level)
//					right = rightRMI.getRightNode(level);
//				else {
//					level = prefix ;
//					right = rightRMI.getRightNode(level);
//					left = rightRMI.getLeftNode(level);
//					continue;
//				}
//			}
//			if(right == null && left == null)
//				break;
//			
//		}
//		return nameID ;
	}
	/*
	 * getters and setters for lookup table and numID and nameID
	 * 
	 */
	public String getLeftNode(int level) throws RemoteException {
		if(lookup[level][0] == null)
			return null;
		return lookup[level][0].getAddress();
	}
	
	public String getRightNode(int level) throws RemoteException {
		if(lookup[level][1] == null)
			return null;
		return lookup[level][1].getAddress();
	}
	public void setLeftNode(int level,NodeInfo newNode) throws RemoteException{
		log("LeftNode at level "+level+" set to: "+newNode.getAddress());
		lookup[level][0] = newNode;
	}
	public void setRightNode(int level,NodeInfo newNode) throws RemoteException {
		log("RightNode at level "+level+" set to: "+newNode.getAddress());
		lookup[level][1] = newNode ;
	}
	public int getNumID() {
		return numID;
	}
	public String getNameID() {
		return nameID ;
	}
	/*
	 * Returns the length of the common prefix between a string and nameID
	 */
	
	public static RMIInterface getRMI(String adrs) {		
		if(validateIP(adrs))
			try {
				return (RMIInterface)Naming.lookup("//"+adrs.split(":")[0]+":1099/RMIImpl");
			}catch(Exception e) {
				System.err.println("Exception while attempting to lookup RMI located at address: "+adrs);
			}
		else {
			System.err.println("Error in looking up RMI. Address: "+ adrs + " is not a valid address.");
		}
		return null;
	}
	
	private static boolean validateIP(String adrs) { //Makes sure its of the form xxx.xxx.xxx.xxx
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
	
	public static int commonBits(String name) {	
			if(name.length() != nameID.length())
				return -1;
			int i = 0 ;
			for(i = 0 ; i < name.length() && name.charAt(i) == nameID.charAt(i);i++);
			log("Common Prefix for " + nameID + " and " + name + " is: " + i);
			return i ;		
		}
	/*
	 * Return the length of the common prefix between two strings
	 */
	public static int commonBits(String name1, String name2) {
		if(name1 == null || name2 == null) {
			return -1;
		}
		if(name1.length() != name2.length())
			return -1;
		int i = 0;
		for(i = 0; i < name1.length() && name1.charAt(i) == name2.charAt(i) ; ++i);
		log("Common Prefix for " + name1 + " and " + name2 + " is: " + i);
			return i;		
		}
	/*
	 * A shortcut for printing to console
	 */
	public static void log(String s) {
		System.out.println(s);
	}
	/* 
	 * Print the contents of the lookup table
	 */
	public static void printLookup() {
        System.out.println("\n");
        for(int i = maxLevels-1 ; i >= 0 ; i--)
        {
            for(int j = 0 ; j<2 ; j++)
            	if(lookup[i][j] == null)
            		log("null");
            	else
            		log(lookup[i][j].getAddress()+"\t");
            log("\n");
        }
    }
	/*
	 * A shortcut for getting input from user
	 */
	public static String get() {
		String response = in.nextLine();
		return response;
	}
}
