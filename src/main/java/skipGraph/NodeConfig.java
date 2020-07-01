package skipGraph;

import java.rmi.RemoteException;

public class NodeConfig {
	
	private int maxLevels;
	private int RMIPort;
	private int numID;
	private String nameID;
	
	public NodeConfig(int maxLevels, int RMIPort, int numID, String nameID) {
		this.maxLevels = maxLevels;
		this.RMIPort = RMIPort;
		this.numID = numID;
		this.nameID = nameID;
	}

	public int getMaxLevels() {
		return maxLevels;
	}

	public void setMaxLevels(int maxLevels) {
		this.maxLevels = maxLevels;
	}

	public int getRMIPort() {
		return RMIPort;
	}

	public void setRMIPort(int rMIPort) {
		RMIPort = rMIPort;
	}

	public int getNumID() {
		return numID;
	}

	public void setNumID(int numID) {
		this.numID = numID;
	}

	public String getNameID() {
		return nameID;
	}

	public void setNameID(String nameID) {
		this.nameID = nameID;
	}

	
}
