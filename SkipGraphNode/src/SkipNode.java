import java.util.Scanner;

public class SkipNode {
	
	public static String address;
	public static String nameID;
	public static String numID;
	public static String IP ;
	public static int port;
	public static String introducer; 
	
	
	
	
	
	public void setInfo() {
		
		log("Enter your Name ID:");
		nameID = get();
		log("Enter your Numeric Id:");
		numID = get();
		log("Enter the address of the introducer:");
		introducer = get();
		
		log("Your INFO:\nnameID: "+nameID+"\nnumID: "+numID+"\nintroducer: "+introducer);
	}
	
	public int commonPrefix(String name) {
		
		if(name.length() != nameID.length())
			return -1;
		
		int i = 0;
		for(i = 0 ; i < name.length() && name.charAt(i) == nameID.charAt(i);i++);
		
		log("Common Prefix for " + nameID + " and " + name + " is: " + i);
		
		return i ;		
	}
	
	public int commonPrefix(String name1, String name2) {
		if(name1.length() != name2.length())
			return -1;
		int i = 0;
		for(i = 0; i < name1.length() && name1.charAt(i) == name2.charAt(i) ; ++i);
		
		log("Commone Prefix for " + name1 + " and " + name2 + " is: " + i);
		
		return i;		
	}
	
	
	public void log(String s) {
		System.out.println(s);
	}
	
	
	public String get() {
		Scanner in = new Scanner(System.in);
		String response = in.nextLine();
		return response;
	}
}
