package blockchain;

import java.security.PublicKey;

import remoteTest.Configuration;
import signature.SignedBytes;
import simulation.SimLog;
import skipGraph.SkipGraphNode;

public interface LightChainInterface extends SkipGraphNode {
	public SignedBytes PoV(Transaction t);

	public SignedBytes PoV(Block blk);

	public boolean getMode();

	public PublicKey getPublicKey();

	public void removeFlagNode();

	public Configuration getConf();

	public SimLog startSim(int numTransactions, int pace);

	public Block insertGenesis();

	// To shut down all instances
	public void shutDown();

	// For token
	public int getToken();
}
