package blockchain;

import java.util.HashSet;
import java.util.Set;

public class Block {
	private final String prev;
	private final String owner;
	private final Set<Transaction> S;
	private final String search_proof;
	private final String h;
	private final String sigma;
	
	public Block(String prev, String owner, String search_proof, String h, String sigma) {
		super();
		this.prev = prev;
		this.owner = owner;
		S = new HashSet<Transaction>();
		this.search_proof = search_proof;
		this.h = h;
		this.sigma = sigma;
	}
	
	
}
