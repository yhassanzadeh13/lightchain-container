package blockchain;

import java.util.List;

import hashing.Hasher;
import hashing.HashingTools;
import skipGraph.NodeInfo;

public class Transaction extends NodeInfo{
	private final String prev;
	private final String owner;
	private final String cont;//Use random string for this
	private final String h;//Hash
	private final List<String> sigma;
	private Hasher hasher;
	
	public Transaction(String prev, String owner, String cont, String h, List<String> sigma){
		super("temp",19,"temp");
		//super(h,prev);
		this.prev = prev;
		this.owner = owner;
		this.cont = cont;
		this.h = h;
		this.sigma = sigma;
		hasher = new HashingTools();
	}
	
	public String nameID() {
		return prev;
	}
	
	public String numID() {
		return hasher.getHash(cont,20);
	}
	
	public String getPrev() {
		return prev;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public String getCont() {
		return cont;
	}
		
}
