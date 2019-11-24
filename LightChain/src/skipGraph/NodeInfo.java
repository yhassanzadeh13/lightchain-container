package skipGraph;

import java.io.Serializable;

public class NodeInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String address;
	private int numID;
	private String nameID;

	/*
	 * Constructor for NodeInfo
	 */
	public NodeInfo(String adrs, int num, String name) {
		this.address = adrs;
		this.numID = num;
		this.nameID = name;
	}

	public NodeInfo(NodeInfo node) {
		this.address = node.getAddress();
		this.numID = node.getNumID();
		this.nameID = node.getNameID();
	}

	/*
	 * Getters and Setters for NodeInfo entries.
	 */
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((nameID == null) ? 0 : nameID.hashCode());
		result = prime * result + numID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeInfo other = (NodeInfo) obj;

		return address.equals(other.getAddress()) && numID == other.numID && nameID.equals(other.getNameID());
	}

	@Override
	public String toString() {
		return "Node: address: " + this.address + " numID: " + this.numID + " nameID: " + this.nameID;

	}

	public final String debugString() {
		return "Node: address: " + address + "\tnumID: " + numID + "\tnameID: " + nameID;
	}
}
