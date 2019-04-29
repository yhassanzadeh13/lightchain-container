import java.io.Serializable;

public class NodeInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String address;
	private int numID ;
	private String nameID ;
	
	public NodeInfo(String adrs, int num, String name) {
		this.address = adrs;
		this.numID = num;
		this.nameID = name;
	}
	
	public String getAddress() {
		return address;
	}
	public int getNumID() {
		return numID;
	}
	public String getNameID() {
		return nameID;
	}
	public void setAddress(String adrs) {
		this.address = adrs;
	}
	public void setNumID(int num) {
		this.numID = num;
	}
	public void setNameID(String name) {
		this.nameID = name;
	}
	
	public boolean equals(NodeInfo node) {
		return address.equals(node.getAddress());
	}
}
