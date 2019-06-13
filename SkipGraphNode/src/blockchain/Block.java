package blockchain;

import java.util.HashSet;
import java.util.Set;

import hashing.Hasher;
import hashing.HashingTools;
import skipGraph.NodeInfo;
import skipGraph.SkipNode;


public class Block extends NodeInfo{
	private final String prev;
	private final String owner;
	private Set<Transaction> S;
	private final String h;
	private String sigma;
	private Hasher hasher ;
	/*
	 * @param prev the addrexss of the previous block
	 * @param owner the address of the owner of the block
	 */
	public Block(String prev, String owner) {
		super(owner,0,prev);
		this.prev = prev;
		this.owner = owner;
		S = new HashSet<Transaction>();
		hasher = new HashingTools();
		this.h = hasher.getHash(prev + owner,SkipNode.TRUNC);
		super.setNumID(Integer.parseInt(this.h,2));
	}
	
	public String getPrev() {
		return prev;
	}
	public String getOwner() {
		return owner;
	}
	public String getH() {
		return h;
	}
	
	
}
