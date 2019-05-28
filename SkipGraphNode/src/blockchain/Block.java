package blockchain;

import java.util.HashSet;
import java.util.Set;
import skipGraph.NodeInfo;


public class Block extends NodeInfo{
	private final String prev;
	private final String owner;
	private final Set<Transaction> S;
//	private final String search_proof;
	private final String h;
	private final String sigma;
	
	/*
	 * @param prev the address of the previous block
	 * @param owner the address of the owner of the block
	 */
	public Block(String prev, String owner, String h, String sigma) {
		super("temp",19,"temp");
		//super();
		this.prev = prev;
		this.owner = owner;
		S = new HashSet<Transaction>();
		this.h = h;
		this.sigma = sigma;
	}
	
	
}
