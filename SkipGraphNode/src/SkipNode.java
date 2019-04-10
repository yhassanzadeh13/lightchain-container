import java.util.Scanner;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SkipNode extends UnicastRemoteObject implements RMIInterface{
	
	
	private static final long serialVersionUID = 1L;
	public static String address;
	public static String nameID;
	public static String numID;
	public static String IP ;
	public static String[][] lookup ;
	public static int port;
	public static int maxLevels = 5; 
	public static String introducer; 

	public static void main(String args[]) throws IOException {
		
		lookup = new String[maxLevels][2];
		setInfo();
		try {
			
			SkipNode skipNode = new SkipNode();
			Naming.rebind("//"+address+"/"+"RMIImpl",skipNode);
			log("Rebinding Successful");
			
		}catch(IOException e){
			log("Error in Rebinding");
		}
		
		ServerConnection server = new ServerConnection();
		server.start();		
		
		while(true) {
			printMenu();
			query();
		}
		
	}

	
	protected SkipNode() throws RemoteException, UnknownHostException {
		super();
		
		// I don't understand what these lines do but I will inspect them later
		String st = Inet4Address.getLocalHost().getHostAddress();
		System.setProperty("java.rmi.server.hostname",st);
		System.out.println("RMI Server proptery set. Inet4Address: "+st);
		// TODO Auto-generated constructor stub
	}
	
	public static void setInfo() {
		
		log("Enter your Name ID:");
		nameID = get();
		log("Enter your Numeric Id:");
		numID = get();
		log("Enter the address of the introducer:");
		introducer = get();
		
		log("Your INFO:\nnameID: "+nameID+"\nnumID: "+numID+"\nintroducer: "+introducer);
	}
	
	public static void printMenu() throws IOException{
        InetAddress adrs = InetAddress.getLocalHost();
        log("Node at the address: " + address);
        log("Name ID: "+ nameID +" Number ID: " + numID);
        log("Choose a query by entering it's code and then press Enter:\n"
                + "1-Insert\n2-Search By Name ID\n3-Search By Number ID\n4-Print the Lookup Table\n"); 
    }
	
	
	public static void query() {
        
        
    }
	
	public int commonPrefix(String name) {
		
		if(name.length() != nameID.length())
			return -1;
		
		int i = 0 ;
		for(i = 0 ; i < name.length() && name.charAt(i) == nameID.charAt(i);i++);
		
		log("Common Prefix for " + nameID + " and " + name + " is: " + i);
		
		return i ;		
	}
	
	public int commonPrefix(String name1, String name2) {
		if(name1.length() != name2.length())
			return -1;
		int i = 0;
		for(i = 0; i < name1.length() && name1.charAt(i) == name2.charAt(i) ; ++i);
		
		log("Common Prefix for " + name1 + " and " + name2 + " is: " + i);
		
		return i;		
	}

	// Implement after search
//	public static void Insert() {
//		try {
//			
//			String left = null;
//			String right = null;
//			// Look for 
//			
//			
//		}catch(IOException e) {
//			log("Error in Inserting");
//		}
//	}
	
	// returns the address of the node with Numeric ID num
	public static String searchByNumID(String targetNum) throws MalformedURLException, RemoteException, NotBoundException {
		
		int level = maxLevels ;
		int numIDInt = Integer.parseInt(numID); 
		int targetInt = Integer.parseInt(targetNum); 
		
		// If the introducer exists only
		if(lookup[0][0] == null && lookup[0][1] == null) {
			return address ;
		}
		// The Target is on the right of numID
		else if (numIDInt < targetInt) {
			
			String next = null ;
			while(level > 0 && lookup[level][1] == null)
				level--;
		
			if(level >= 0) {
				
				next = lookup[level][1];
				
				while(level >= 0) {
					
					String[] nextAddress = next.split(":");
					
					RMIInterface nextRMI = (RMIInterface)Naming.lookup("//"+nextAddress[0]+":1099/RMIImpl");
					
					if(nextRMI.getRightNode(level) != null) {
						
						String[] nextOfNext = nextRMI.getRightNode(level).split(":");
						
						RMIInterface nextOfNextRMI = (RMIInterface)Naming.lookup("//"+nextOfNext[0]+":1099/RMIImpl");
						
						if(Integer.parseInt(nextOfNextRMI.getNumID()) <= targetInt) {
							
							next = nextRMI.getRightNode(level);
							
						}else
							level--;
						
					}else
						level--;
				}
		
			}
			
			return next ;
		
		}else {
			
			String next = null;
			
			while(level > 0 && lookup[level][0] == null)
				level--;
			
			if(level >= 0) {
				
				next = lookup[level][0];
				
				while(level >= 0) {
					
					String[] nextAddress = next.split(":");
					
					RMIInterface nextRMI = (RMIInterface)Naming.lookup("//"+nextAddress[0]+":1099/RMIImpl");
					
					if(nextRMI.getLeftNode(level) != null) {
						
						String[] nextOfNext = nextRMI.getLeftNode(level).split(":");
						
						RMIInterface nextOfNextRMI = (RMIInterface)Naming.lookup("//"+nextOfNext[0]+":1099/RMIImpl");
						
						if(Integer.parseInt(nextOfNextRMI.getNumID()) >= Integer.parseInt(targetNum)) {
							next = nextRMI.getLeftNode(level);
						}
						
					}
					
				}
				
			}
			
		}
		
		
	}
	
	
	public String getLeftNode(int level) throws RemoteException {
		return lookup[level][0];
	}
	
	public String getRightNode(int level) throws RemoteException {
		return lookup[level][1];
	}
	public void setLeftNode(int level,String newVal) throws RemoteException{
		lookup[level][0] = newVal;
	}
	public void setRightNode(int level,String newVal) throws RemoteException {
		lookup[level][1] = newVal ;
	}
	public String getNumID() {
		return numID;
	}
	public String getNameID() {
		return nameID ;
	}
	
	
	public static void log(String s) {
		System.out.println(s);
	}
	
	
	public static String get() {
		Scanner in = new Scanner(System.in);
		String response = in.nextLine();
		return response;
	}
}
