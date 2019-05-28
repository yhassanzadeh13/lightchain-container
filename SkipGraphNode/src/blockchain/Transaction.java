package blockchain;

public class Transaction {
	private final String prev;
	private final String owner;
	private final String cont;
	private final String search_proof;//Type?
	private final String h;
	private final String sigma;
	
	
	public Transaction(String prev, String owner, String cont, String search_proof, String h, String sigma) {
		super();
		this.prev = prev;
		this.owner = owner;
		this.cont = cont;
		this.search_proof = search_proof;
		this.h = h;
		this.sigma = sigma;
	}

		
}
