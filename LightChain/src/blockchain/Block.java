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
	private List<Transaction> S;
	private final String h;
	private List<SignedBytes> sigma;
	private Hasher hasher;
	private final int index;

	/*
	 * @param prev the address of the previous block
	 * 
	 * @param owner the address of the owner of the block
	 */
	public Block(String prev, int owner, String address, int idx) {
		super(address, 0, prev);
		this.index = idx;
		this.prev = prev;
		this.owner = owner;
		this.S = new ArrayList<>();
		this.sigma = new ArrayList<>();
		hasher = new HashingTools();
		this.h = hasher.getHash(prev + owner, Const.TRUNC);
		super.setNumID(Integer.parseInt(this.h, 2));
	}

	public Block(String prev, int owner, String address, List<Transaction> tList, int idx) {
		super(address, 0, prev);
		this.index = idx;
		this.prev = prev;
		this.owner = owner;
		this.S = tList;
		hasher = new HashingTools();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tList.size(); ++i)
			sb.append(tList.get(i).toString());
		this.h = hasher.getHash(prev + owner + sb.toString(), Const.TRUNC);
		super.setNumID(Integer.parseInt(this.h, 2));
	}

	public Block(Block blk) {
		super(blk.getAddress(), blk.getNumID(), blk.getNameID());
		hasher = new HashingTools();
		this.index = blk.getIndex();
		this.prev = blk.getPrev();
		this.owner = blk.getOwner();
		this.S = blk.getS();
		this.h = blk.getH();
		this.sigma = blk.getSigma();
	}

	public String getPrev() {
		return prev;
	}

	public int getOwner() {
		return owner;
	}

	public List<Transaction> getS() {
		return S;
	}

	public String getH() {
		return h;
	}

	public List<SignedBytes> getSigma() {
		return sigma;
	}

	public void addSignature(SignedBytes signature) {
		sigma.add(signature);
	}

	public void addTransactions(List<Transaction> tList) {
		S = tList;
	}

	public int getIndex() {
		return index;
	}

}
