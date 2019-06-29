package blockchain;

import java.util.ArrayList;
import java.util.List;

import hashing.Hasher;
import hashing.HashingTools;
import skipGraph.NodeInfo;
import skipGraph.SkipNode;

public class Transaction extends NodeInfo{
	private final String prev;
	private final int owner;
	private final String cont;//Use random string for this
	private final String h;//Hash
	private ArrayList<String> sigma;
	private Hasher hasher;
	
	public Transaction(String prev, int owner, String cont){
		super("",0,prev);
		this.prev = prev;
		this.owner = owner;
		this.cont = cont;
		hasher = new HashingTools();
		this.h = hasher.getHash(prev + owner + cont,SkipNode.TRUNC);
		super.setNumID(Integer.parseInt(this.h,2));
	}
	
	public ArrayList<String> getSigma(){
		return sigma;
	}
	
	public String getPrev() {
		return prev;
	}
	
	public int getOwner() {
		return owner;
	}
	
	public String getCont() {
		return cont;
	}
	
	public String getH() {
		return h;
	}
	
	public void setSigma(ArrayList<String> s) {
		sigma = s;
	}
		
}
