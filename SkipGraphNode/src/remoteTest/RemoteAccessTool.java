package remoteTest;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

import skipGraph.NodeInfo;
import skipGraph.RMIInterface;

public class RemoteAccessTool {
	static String ip;
	static String port;
	static Scanner in = new Scanner(System.in);
	static ArrayList<NodeInfo> data;
	static NodeInfo[][][] lookup;

	public static void main(String[] args) {
		while(true) {
			log("Enter IP address along with port (x to exit)");
			ip = in.nextLine();
			while(ip.equals("x") || (!validateIP(ip) && ip.split(":").length!=2)) {
				if(ip.equals("x")) System.exit(0);
				log("Please enter a valid IP address with respective port");
				ip = in.nextLine();
			}
			String[] ipsp = ip.split(":");
			ip = ipsp[0];
			port = ipsp[1];
			String nameID, numID;
			log("Enter name ID:");
			nameID = in.nextLine();
			log("Enter num ID:");
			numID = in.nextLine();

			RMIInterface node = getRMI(ip+":"+port);	 
			if(node == null) {
				log("Couldn't fetch the node. Please make sure the input is correct.");
				continue;
			}
			data = node.getData();
			lookup = node.getLookupTable();
			if(data == null || lookup == null) {
				log("Couldn't fetch data and lookup properly. Please try again.");
				continue;
			}
			try {
			while(true) {
				String input = get();
				if(!input.matches("[1-7]")) {
					log("Invalid query. Please enter the number of one of the possible operations");
					return;
				}
				int query = Integer.parseInt(input);

				if(query == 1) { // search by name ID
					log("Please Enter the name ID to be searched");
					String name = get();
					while(!name.matches("[0-1]+")) {//Makes sure the name is a binary string
						log("Name ID should be a binary string. Please enter a valid Name ID:");
						name = get();
					}
					NodeInfo result = null;
					try{
						result = node.searchByNameID(name);
					}catch(RemoteException e) {
						e.printStackTrace();
						log("Remote Exception in query.");
					}
					log("The result of search by name ID is: "+result.getAddress());
				}else if(query == 3) { // search by num ID
					log("Please Enter the numeric ID to be searched");
					String numInput = get();
					while(!numInput.matches("0|[1-9][0-9]*")) {
						log("Invalid number entered. Please enter a valid number");
						numInput = get();
					}
					int num = Integer.parseInt(numInput);
					NodeInfo result = node.searchByNumID(num);
					log("The result of search by numberic ID is: "+ result.getAddress());
				}else if(query == 4) { // print the lookup table of the current node
					log("In case you want the lookup table of the original node enter 0.");
					log("Otherwise, emter the index of the data node ");
					int num = Integer.parseInt(get());
					if(num < node.getDataNum())
						printLookup(num);
					else
						log("Data node with given index does not exist");
				}else if( query == 5) { // print the left neighbor at a certain level
					log("Please Enter the required level:");
					int lvl = Integer.parseInt(get());
					if(lookup[lvl][0][0] == null)
						log("No left node present at level "+lvl);
					else
						log("Left node at level "+lvl+" is:" + lookup[lvl][0][0].getAddress());
				}
				else if (query == 6) { // print the right neighbor at a certain level
					log("Please Enter the required level:");
					int lvl = Integer.parseInt(get());
					if(lookup[lvl][1][0] == null)
						log("No right node present at level "+lvl);
					else
						log("Right node at level "+lvl+" is:" + lookup[lvl][1][0].getAddress());
				}else if (query == 7) {
					printData();
				}
			}
			}catch(Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception caught. Restarting.");
			}
		}
	}


	/*
	 * Taken from SkipNode class. However, it needs to be implemented here so that println would print here rather than in the other node.
	 */
	
	public static void printLookup(int num) {
        System.out.println("\n");
        for(int i = lookup.length-2 ; i >= 0 ; i--)//double check the initial value of i
        {
            for(int j = 0 ; j<2 ; j++)
            	if(lookup[i][j][num] == null)
            		log("null\t");
            	else
            		log(lookup[i][j][num].getAddress()+"\t");
            log("\n\n");
        }
    }
	public static void printData() {
		for(int i=0 ; i<data.size(); ++i)
			log(data.get(i).getNumID() + " " + data.get(i).getNameID());
		log("");
	}
	
	
	/*
	 * The following methods are taken from the other class. It might be a good idea to have a way to use them in both classes without having to 
	 * copy paste it here again
	 */


	public static void log(String st) {
		System.out.println(st);
	}

	public static String get() {
		return in.nextLine();
	}

	private static boolean validateIP(String adrs) { 
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

	public static RMIInterface getRMI(String adrs) {		
		if(validateIP(adrs))
			try {
				return (RMIInterface)Naming.lookup("//"+adrs+"/RMIImpl");
			}catch(Exception e) {
				log("Exception while attempting to lookup RMI located at address: "+adrs);
			}
		else {
			log("Error in looking up RMI. Address: "+ adrs + " is not a valid address.");
		}
		return null;
	}	
}
