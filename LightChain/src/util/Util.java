package util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

import blockchain.Block;
import blockchain.Transaction;
import skipGraph.NodeInfo;

public class Util {

	
	protected static Scanner in = new Scanner(System.in);
	public static final boolean local = true;
	//If set to true, it would work fine on local networks without having to go through the hassle of 

	
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
	
	public static NodeInfo assignNode(NodeInfo node) {
		if(node == null)
			return null;
		if(node instanceof Transaction)
			return new Transaction((Transaction)node);
		else if (node instanceof Block)
			return new Block((Block)node);
		return new NodeInfo(node);
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
	 * A shortcut for getting input from user
	 */
	public static String getInput() {
		String response = in.nextLine();
		return response;
	}
	
}
