package blockchain;

import java.util.ArrayList;
import java.util.List;

import hashing.Hasher;
import hashing.HashingTools;
import signature.SignedBytes;
import skipGraph.NodeInfo;
import skipGraph.SkipNode;
import util.Const;

public class Block extends NodeInfo {

	private static final long serialVersionUID = 1L;
	private final String prev;
	private final int owner;
	private List<Transaction> transactionSet;
	private final String hash;
	private List<SignedBytes> sigma;
	private Hasher hasher;
	private final int index;

	/**
	 * @param prev the address of the previous block
	 * @param owner the address of the owner of the block
	 */
	public Block(String prev, int owner, String address, int idx) {
		super(address, 0, prev);
		this.index = idx;
		this.prev = prev;
		this.owner = owner;
		this.transactionSet = new ArrayList<>();
		this.sigma = new ArrayList<>();
		hasher = new HashingTools();
		this.hash = hasher.getHash(prev + owner, Const.TRUNC);
		super.setNumID(Integer.parseInt(this.hash, 2));
	}

	public Block(String prev, int owner, String address, List<Transaction> tList, int idx) {
		super(address, 0, prev);
		this.index = idx;
		this.prev = prev;
		this.owner = owner;
		this.transactionSet = tList;
		hasher = new HashingTools();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tList.size(); ++i)
			sb.append(tList.get(i).toString());
		this.hash = hasher.getHash(prev + owner + sb.toString(), Const.TRUNC);
		super.setNumID(Integer.parseInt(this.hash, 2));
	}

	public Block(Block blk) {
		super(blk.getAddress(), blk.getNumID(), blk.getNameID());
		hasher = new HashingTools();
		this.index = blk.getIndex();
		this.prev = blk.getPrev();
		this.owner = blk.getOwner();
		this.transactionSet = blk.getTransactionSet();
		this.hash = blk.getHash();
		this.sigma = blk.getSigma();
	}

	public String getPrev() {
		return prev;
	}

	public int getOwner() {
		return owner;
	}

	public List<Transaction> getTransactionSet() {
		return transactionSet;
	}

	public String getHash() {
		return hash;
	}

	public List<SignedBytes> getSigma() {
		return sigma;
	}

	public void addSignature(SignedBytes signature) {
		sigma.add(signature);
	}

	public void addTransactions(List<Transaction> tList) {
		transactionSet = tList;
	}

	public int getIndex() {
		return index;
	}

}
