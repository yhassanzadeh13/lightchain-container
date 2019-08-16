package blockchain;

import java.util.ArrayList;

import hashing.Hasher;
import hashing.HashingTools;
import skipGraph.NodeInfo;
import skipGraph.SkipNode;


public class Block extends NodeInfo{
	
	private static final long serialVersionUID = 1L;
	private final String prev;
	private final int owner;
	private ArrayList<Transaction> S;
	private final String h;
	private ArrayList<String> sigma;
	private Hasher hasher ;
	private final int index;
	
	/*
	 * @param prev the address of the previous block
	 * @param owner the address of the owner of the block
	 */
	public Block(String prev, int owner, String address,int idx) {
		super(address,0,prev);
		this.index = idx;
		this.prev = prev;
		this.owner = owner;
		S = new ArrayList<Transaction>();
		hasher = new HashingTools();
		this.h = hasher.getHash(prev + owner,SkipNode.TRUNC);
		super.setNumID(Integer.parseInt(this.h,2));
	}
	public Block(String prev, int owner,String address ,ArrayList<Transaction> tList,int idx) {
		super(address,0,prev);
		this.index = idx;
		this.prev = prev;
		this.owner = owner;
		this.S = tList;
		hasher = new HashingTools();
		StringBuilder sb = new StringBuilder();
		for(int i=0 ; i < tList.size(); ++i)
			sb.append(tList.get(i).toString());
		this.h = hasher.getHash(prev + owner + sb.toString(),SkipNode.TRUNC);
		super.setNumID(Integer.parseInt(this.h,2));
	}
	
	public String getPrev() {
		return prev;
	}
	public int getOwner() {
		return owner;
	}
	public ArrayList<Transaction> getS(){
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
	public int getIndex() {
		return index;
	}
	
}
