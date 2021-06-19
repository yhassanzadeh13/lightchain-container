package skipGraph;

public class NodeConfig {
	
	private int maxLevels;
	private int port;
	private int numID;
	private String nameID;
	
	public NodeConfig(int maxLevels, int port, int numID, String nameID) {
		this.maxLevels = maxLevels;
		this.port = port;
		this.numID = numID;
		this.nameID = nameID;
	}

	public int getMaxLevels() {
		return maxLevels;
	}

	public void setMaxLevels(int maxLevels) {
		this.maxLevels = maxLevels;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
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
