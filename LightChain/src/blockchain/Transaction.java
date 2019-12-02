package blockchain;

import java.util.ArrayList;
import java.util.List;

import hashing.Hasher;
import hashing.HashingTools;
import signature.SignedBytes;
import skipGraph.NodeInfo;
import skipGraph.SkipNode;
import util.Const;

public class Transaction extends NodeInfo {

	private static final long serialVersionUID = 1L;
	private final String prev;
	private final int owner;
	private final String cont;// Use random string for this
	private final String hash;// Hash
	private List<SignedBytes> sigma;
	private Hasher hasher;
	private int levels;

	// need to add address to transaction

	public Transaction(String prev, int owner, String cont, String address, int levels) {
		super(address, 0, prev);
		this.prev = prev;
		this.owner = owner;
		this.cont = cont;
		this.levels = levels;
		hasher = new HashingTools();
		this.hash = hasher.getHash(prev + owner + cont, levels);
		super.setNumID(Integer.parseInt(this.hash, 2));
	}

	public Transaction(Transaction t) {
		super(t.getAddress(), t.getNumID(), t.getNameID());
		hasher = new HashingTools();
		this.prev = t.getPrev();
		this.owner = t.getOwner();
		this.cont = t.getCont();
		this.hash = t.getHash();
		this.sigma = t.getSigma();
		this.levels = t.getLevels();
	}

	public List<SignedBytes> getSigma() {
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

	public String getHash() {
		return hash;
	}

	public void addSignature(SignedBytes signature) {
		sigma.add(signature);
	}

	public String toString() {
		return prev + owner + cont + getAddress();
	}
	
	public int getLevels() {
		return levels;
	}

}
