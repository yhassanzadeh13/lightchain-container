package remoteTest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
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
	static String nameID;
	static int numID;
	static boolean skipInit = false;
	static RMIInterface node;

	public static void main(String[] args) {
		while(true) {
			if(!skipInit) {
				log("Enter IP address along with port (x to exit)");
				ip = in.nextLine();
				while(ip.equals("x") || (!validateIP(ip) && ip.split(":").length!=2)) {
					if(ip.equals("x")) System.exit(0);
					log("Please enter a valid IP address with respective port");
					ip = in.nextLine();
				}
			}else skipInit = false;
			if(ip == null) continue;
			String[] ipsp = ip.split(":");
			ip = ipsp[0];
			port = ipsp[1];
			
			node = getRMI(ip+":"+port);	 
			if(node == null) {
				log("Couldn't fetch the node. Please make sure the input is correct.");
				continue;
			}
			try {
				nameID = node.getNameID();
				numID = node.getNumID();
				data = node.getData();
				lookup = node.getLookupTable();
				if(data == null || lookup == null) {
					log("Couldn't fetch data and lookup properly. Please try again.");
					continue;
				}
				while(true) {
					printMenu();
					String input = get();
					if(!input.matches("[1-9]")) {
						log("Invalid query. Please enter the number of one of the possible operations");
						continue;
					}
					int query = Integer.parseInt(input);
					if(query == 1) {
						log("Enter prev of transaction");
						String prev = get();
						log("Enter cont of transaction");
						String cont = get();
						Transaction t = new Transaction(prev,numID,cont);
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
						ArrayList<NodeInfo> lst = new ArrayList<NodeInfo>();
						NodeInfo result = null;
						try{
							lst = node.searchByNameID(name,lst);
							result = lst.get(lst.size()-1);
						}catch(RemoteException e) {
							e.printStackTrace();
							log("Remote Exception in query.");
						}
						log("The search path is: ");
						for(int i=0;i<lst.size();i++) {
							log(i+") " + lst.get(i).getNameID());
						}
						log("The result of search by name ID is: "+result.getAddress());
						if(promptSwitch(result)) break;
					}else if(query == 4) { // search by num ID
						log("Please Enter the numeric ID to be searched");
						String numInput = get();
						while(!numInput.matches("0|[1-9][0-9]*")) {
							log("Invalid number entered. Please enter a valid number");
							numInput = get();
						}
						int num = Integer.parseInt(numInput);
						ArrayList<NodeInfo> lst = new ArrayList<NodeInfo>();
						NodeInfo result = null;
						try{
							lst = node.searchByNumID(num,lst);
							result = lst.get(lst.size()-1);
						}catch(RemoteException e) {
							e.printStackTrace();
							log("Remote Exception in query.");
						}
						log("The search path is: ");
						for(int i=0;i<lst.size();i++) {
							log(i+") " + lst.get(i).getNumID());
						}
						log("The result of search by numeric ID is: "+ result.getAddress());
						if(promptSwitch(result)) break;
					}else if(query == 5) { // print the lookup table of the current node
//						log("In case you want the lookup table of the original node enter 0.");
//						log("Otherwise, enter the index of the data node ");
//						int num = Integer.parseInt(get());
						printLookup(0);
//						if(num < node.getDataNum())
//							printLookup(num);
//						else
//							log("Data node with given index does not exist");
					}else if(query == 6) {
						printData();
					}else if(query == 7) {
						log("This is the current lookup table: ");
						printLookup(0);
						log("Enter the number of the node you want to connect to: (invalid number to abort)");
						String st = get();
						try {
							int inp = Integer.parseInt(st);
							NodeInfo swtch = lookup[inp/2][inp%2][0];
							if(swtch==null) throw new Exception();
							if(promptSwitch(swtch)) break;
						}catch(Exception e){
							log("Invalid number, aborting...");
						}
					}else if(query == 8) {
						pingStats();
					}else if(query == 9) {
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
        log("7-Traverse");
        log("8-Perform latency testing");
        log("9-Exit");
	}

	
	public static void pingStats() {
		try {
			ArrayList<NodeInfo> nodeList = new ArrayList<NodeInfo>();
			NodeInfo curNode = node.searchByNumID(0);
			while(curNode!=null) {
				nodeList.add(curNode);
				RMIInterface curRMI = getRMI(curNode.getAddress());
				curNode = curRMI.getRightNode(0, curNode.getNumID());
			}
			HashMap<NodeInfo, ArrayList<PingLog>> res = new HashMap<NodeInfo, ArrayList<PingLog>>();
			for(int i = 0 ; i < nodeList.size(); i++) {
				for(int j=0; j<nodeList.size(); j++) {
					if(i==j) continue;
					RMIInterface curRMI = getRMI(nodeList.get(i).getAddress());								
					PingLog curLog = curRMI.pingStart(nodeList.get(j), 200);
					if(res.containsKey(nodeList.get(i))) {
						ArrayList<PingLog> tmp = res.get(nodeList.get(i));
						tmp.add(curLog);
						res.put(nodeList.get(i), tmp);	
					}else {
						ArrayList<PingLog> tmp = new ArrayList<PingLog>();
						tmp.add(curLog);
						res.put(nodeList.get(i), tmp);									
					}
				}
			}
			PrintWriter writer = new PrintWriter(new File("test" + System.currentTimeMillis()%20 + ".csv"));
			StringBuilder sb = new StringBuilder();
			
			for(NodeInfo cur : res.keySet()) {
				sb.append("Pinger," + cur.getNameID());
				sb.append('\n');
				sb.append("Pinged,Avg Ping");sb.append('\n');
				for(int i=0;i<res.get(cur).size();i++) {
					sb.append(res.get(cur).get(i).getPinged().getNameID());
					ArrayList<Long> hm = res.get(cur).get(i).getRTTLog();
					for(int j=0;j<hm.size();j++) {
						sb.append("," + hm.get(j));
					}
					sb.append('\n');
				}
				sb.append('\n');
			}
			writer.write(sb.toString());
			writer.close();
		}catch(Exception e) {
			System.out.println("Exception in ping stats testing.");
		}
	}
	
	/*
	 * Taken from SkipNode class. However, it needs to be implemented here so that println would print here rather than in the other node.
	 */
	
	public static void printLookup(int num) {
		try {
			lookup = node.getLookupTable();
		}catch(Exception e) {
			log("Couldn't update the lookup table properly. Aborting...");
			return;
		}
        int cnt = (lookup.length-1)*2;
        for(int i = lookup.length-2 ; i >= 0 ; i--)//double check the initial value of i
        {
        		cnt-=2;
            	log(cnt + " " + ((lookup[i][0][num] == null)?"null\t":(lookup[i][0][num].getNameID()+"\t"))
               +(cnt+1) + " " + ((lookup[i][1][num] == null)?"null\t":(lookup[i][1][num].getNameID()+"\t")));
        }
        
    }
	public static void printData() {
		for(int i=0 ; i<data.size(); ++i)
			log(data.get(i).getNumID() + " " + data.get(i).getNameID());
		log("");
	}
	
	public static boolean promptSwitch(NodeInfo node) {
		if(node == null) {
			System.out.println("Can't switch to null node. Aborting...");
			return false;
		}
		log("Would you like to switch the remote to node at address: " + node.getAddress() + " ?");
		log("The node's name ID is: " + node.getNameID() + " and its num ID is: " + node.getNumID());
		log("Enter 'Y' to confirm, anything else to abort.");
		String inp = get();
		if(inp.equalsIgnoreCase("Y")) {
			ip = node.getAddress();
		}else {
			return false;
		}
		skipInit = true;
		return true;
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
