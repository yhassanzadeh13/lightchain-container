package blockchain;

import java.rmi.RemoteException;
import java.security.PublicKey;

import remoteTest.Configuration;
import remoteTest.TestingLog;
import signature.SignedBytes;
import simulation.SimLog;
import skipGraph.RMIInterface;

public interface LightChainRMIInterface extends RMIInterface {
	public SignedBytes PoV(Transaction t) throws RemoteException;

	public SignedBytes PoV(Block blk) throws RemoteException;

	public boolean getMode() throws RemoteException;

	public PublicKey getPublicKey() throws RemoteException;

	public void removeFlagNode() throws RemoteException;

	public Configuration getConf() throws RemoteException;

	public SimLog startSim(int numTransactions, int pace) throws RemoteException;

	public Block insertGenesis() throws RemoteException;

	// To shut down all instances
	public void shutDown() throws RemoteException;

	//For token
	public int getToken() throws RemoteException;
}
