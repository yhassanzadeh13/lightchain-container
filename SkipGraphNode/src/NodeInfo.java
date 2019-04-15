
public class NodeInfo {
	
	private String address;
	private String numID ;
	private String nameID ;
	
	public NodeInfo(String adrs, String num, String name) {
		this.address = adrs;
		this.numID = num;
		this.nameID = name;
	}
	
	public String getAddress() {
		return address;
	}
	public String getNumID() {
		return numID;
	}
	public String getNameId() {
		return nameID;
	}
	public void setAddress(String adrs) {
		this.address = adrs;
	}
	public void setNumID(String num) {
		this.numID = num;
	}
	public void setNameID(String name) {
		this.nameID = name;
	}
}
