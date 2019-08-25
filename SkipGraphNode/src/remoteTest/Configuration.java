package remoteTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;

import skipGraph.NodeInfo;

//Configuration stores config needed to spin up a SkipGraph node
//current fields:
//   - introducer (address)
//   - nameID
//   - numID
//   - port (RMI)

public final class Configuration implements Serializable{



	private static final long serialVersionUID = 1L;
	private static final String defaultPath = System.getProperty("user.dir") + "/node.conf"; 
	private String configPath;
	Properties prop = new Properties();

	// Fields
	private String introducer  = "none";
	private String nameID      = "nameID";
	private String numID       = "0";
	private String port        = "1099";
	private boolean malicious  = false;

	public Configuration(String introducer, String nameID, String numID, String port, boolean malicious) {
		this.introducer = introducer;
		this.nameID     = nameID;
		this.numID      = numID;
		this.port       = port;
		this.malicious  = malicious;
	}

	public Configuration(String introducer, String port, boolean malicious) {
		this.introducer = introducer;
		this.port       = port;
		this.malicious  = malicious;
	}
	
	public Configuration(String configPath) {
		this.configPath = configPath;
	}

	public Configuration() {
		this.configPath = defaultPath;
	}

	public void parseConfig() {
		try {
			prop.load(new FileInputStream(configPath));
		} catch(Exception e) {
			System.err.println("could not open config file: " + configPath);
			System.exit(1);
		}
		introducer = prop.getProperty("introducer");
		nameID = prop.getProperty("nameID");
		numID  = prop.getProperty("numID");
		port   = prop.getProperty("port");
	}

	public static ArrayList<Configuration> parseConfigurations(){
		ArrayList<Configuration> lst = new ArrayList<Configuration>();
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(defaultPath));
			String input = in.readLine();
			int n = Integer.parseInt(input);
			while((input = in.readLine()) != null) {
				String[] tokens = input.split("=");
				if(tokens.length<2) {
					System.out.println("Error in parsing configurations. Please double check the configuration file. Line: "+ input + " is not valid.");
				}
				Configuration cur = assign(tokens[0],tokens[1]);
				if(cur!=null) lst.add(cur);
			}
			Configuration cur = finalizeAssign();
			if(cur!=null) lst.add(cur);
			if(lst.size() != n) {
				System.out.println("Number of configurations given does not match the number of configurations parsed!");
			}
			in.close();
		} catch (Exception e) {
			System.out.println(defaultPath);
		}
		return lst;
	}

	public void parseIntroducer() {
		try {
			System.out.println(configPath);
			prop.load(new FileInputStream(configPath));
		} catch(Exception e) {
			System.err.println("could not open config file: " + configPath);
			System.exit(1);
		}
		introducer = prop.getProperty("introducer");
		System.out.println("intro " + introducer);
	}

	public String getIntroducer() {
		return introducer;
	}
	
	public void setIntroducer(String introducer) {
		this.introducer= introducer;
	}

	public String getNameID() {
		return nameID;
	}

	public String getNumID() {
		return numID;
	}

	public String getPort() {
		return port;
	}
	
	public boolean isMalicious() {
		return malicious;
	}
	
	//To make parsing configurations more generic
	private static Configuration currentConfig = new Configuration();
	private static boolean[] assigned = new boolean[5];
	private static Configuration assign(String propertyName, String value) {
		Configuration ret = null; //Return null by default, if we encounter a property that has been set before, return the built configuration and start making a new one.
		switch(propertyName) {
			case "introducer" : if(assigned[0]) break; assigned[0]=true; currentConfig.setIntroducer(value); return ret;
			case "numID" : if(assigned[1]) break; assigned[1]=true; currentConfig.setNumID(value); return ret;
			case "nameID" : if(assigned[2]) break; assigned[2]=true; currentConfig.setNameID(value); return ret;
			case "port" : if(assigned[3]) break; assigned[3]=true; currentConfig.setPort(value); return ret;
			case "malicious" : if(assigned[4]) break; assigned[4]=true; currentConfig.setMalicious(Boolean.parseBoolean(value)); return ret;
			default: System.out.println("Property: "+propertyName+" in config file not recognized! Please double check the config file and keep in mind"+
										" that config files are case sensitive. Valid flags: 'introducer', 'numID', 'nameID', 'port', 'malicious'");
		}//If we reach this stage, this means that we have a ready config file for return.
		for(int i=0;i<assigned.length;i++) { //reset all flags
			assigned[i] = false;
		}
		ret = currentConfig;
		currentConfig = new Configuration();
		assign(propertyName,value);
		return ret;
	}
	private static Configuration finalizeAssign() {
		Configuration ret = currentConfig;
		currentConfig = new Configuration();
		return ret;
	}
	
	public static void generateConfigFile(ArrayList<NodeInfo> lst) {
		generateConfigFile(lst, "node_"+System.currentTimeMillis()%100+".conf");
	}
	
	public static void generateConfigFile(ArrayList<NodeInfo> lst, String filePath) {
		try {
			if(filePath.length()<5 || !filePath.substring(filePath.length()-5).equalsIgnoreCase(".conf")) {
				filePath = filePath+".conf";
			}
			PrintWriter writer = new PrintWriter(new File(filePath));
			writer.println(lst.size());
			int prt=45000;
			for(NodeInfo cur : lst) {
				writer.println("introducer=none");//Default values
				writer.println("nameID="+cur.getNameID());
				writer.println("numID="+cur.getNumID());
				writer.println("port="+prt++);//Default values
				writer.println("malicious=false");//Default values
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void setNameID(String nameID) {
		this.nameID = nameID;
	}

	public void setNumID(String numID) {
		this.numID = numID;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setMalicious(boolean malicious) {
		this.malicious = malicious;
	}
}
