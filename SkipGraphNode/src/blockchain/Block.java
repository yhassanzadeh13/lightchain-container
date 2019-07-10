package blockchain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import hashing.Hasher;
import hashing.HashingTools;
import skipGraph.NodeInfo;
import skipGraph.SkipNode;


public class Block extends NodeInfo{
	private final String prev;
	private final int owner;
	private ArrayList<Transaction> S;
	private final String h;
	private ArrayList<String> sigma;
	private Hasher hasher ;
	
	
	// need to add address to block
	
	/*
	 * @param prev the address of the previous block
	 * @param owner the address of the owner of the block
	 */
	public Block(String prev, int owner, String address) {
		super(address,0,prev);
		this.prev = prev;
		this.owner = owner;
		S = new ArrayList<Transaction>();
		hasher = new HashingTools();
		this.h = hasher.getHash(prev + owner,SkipNode.TRUNC);
		super.setNumID(Integer.parseInt(this.h,2));
	}
	public Block(String prev, int owner, ArrayList<Transaction> tList) {
		super("",0,prev);
		this.prev = prev;
		this.owner = owner;
		this.S = tList;
		hasher = new HashingTools();
		this.h = hasher.getHash(prev + owner + S.toString(),SkipNode.TRUNC);
		super.setNumID(Integer.parseInt(this.h,2));
	}
	
	public String getPrev() {
		return prev;
	}
	public int getOwner() {
		return owner;
	}
	public ArrayList<Transaction> getTransactions(){
		return S;
	}
	public String getH() {
		return h;
	}
	public ArrayList<String> getSigma(){
		return sigma;
	}
	public void setSigma(ArrayList<String> s) {
		sigma = s;
	}
	public void addTransactions(ArrayList<Transaction> tList) {
		S = tList;
	}
	
	
}
