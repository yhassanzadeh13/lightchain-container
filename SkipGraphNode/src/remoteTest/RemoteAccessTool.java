package remoteTest;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

import blockchain.Block;
import blockchain.Transaction;
import skipGraph.NodeInfo;
import skipGraph.RMIInterface;

public class RemoteAccessTool {
	static String ip;
	static String port;
	static Scanner in = new Scanner(System.in);
	static ArrayList<NodeInfo> data;
	private static ArrayList<Transaction> transactions;
	static NodeInfo[][][] lookup;
	static String nameID, numID;
	

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
			log("Enter name ID:");
			nameID = in.nextLine();
			log("Enter num ID:");
			numID = in.nextLine();

			RMIInterface node = getRMI(ip+":"+port);	 
			if(node == null) {
				log("Couldn't fetch the node. Please make sure the input is correct.");
				continue;
			}
			try {
				data = node.getData();
				lookup = node.getLookupTable();
				if(data == null || lookup == null) {
					log("Couldn't fetch data and lookup properly. Please try again.");
					continue;
				}
				while(true) {
					String input = get();
					if(!input.matches("[1-7]")) {
						log("Invalid query. Please enter the number of one of the possible operations");
						continue;
					}
					int query = Integer.parseInt(input);
					if(query == 1) {
						log("Enter prev of transaction");
						String prev = get();
						log("Enter cont of transaction");
						String cont = get();
						Transaction t = new Transaction(prev,ip+":"+port,cont);
						node.put(t);
					}else if (query == 2){ // insert block
						log("Enter prev of block");
						String prev = get();
						Block b = new Block(ip+":"+port,prev);
						node.put(b);
					}else if(query == 3) { // search by name ID
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
					}else if(query == 4) { // search by num ID
						log("Please Enter the numeric ID to be searched");
						String numInput = get();
						while(!numInput.matches("0|[1-9][0-9]*")) {
							log("Invalid number entered. Please enter a valid number");
							numInput = get();
						}
						int num = Integer.parseInt(numInput);
						NodeInfo result = node.searchByNumID(num);
						log("The result of search by numeric ID is: "+ result.getAddress());
					}else if(query == 5) { // print the lookup table of the current node
						log("In case you want the lookup table of the original node enter 0.");
						log("Otherwise, emter the index of the data node ");
						int num = Integer.parseInt(get());
						if(num < node.getDataNum())
							printLookup(num);
						else
							log("Data node with given index does not exist");
					}else if(query == 6) {
						printData();
					}else if(query == 7) {
						break;
					}
				}
			}catch(Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception caught. Restarting.");
			}
		}
	}

	
	public static void printMenu() throws IOException{
        log("Address of node being controlled: " + ip + ":" + port);
        log("Name ID: "+ nameID +" Number ID: " + numID);
        log("Choose a query by entering it's code and then press Enter");
        log("1-Insert Transaction");
        log("2-Insert Block");
        log("3-Search By Name ID");
        log("4-Search By Number ID");
        log("5-Print the Lookup Table");
        log("6-Print data");
        log("7-Exit");
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
