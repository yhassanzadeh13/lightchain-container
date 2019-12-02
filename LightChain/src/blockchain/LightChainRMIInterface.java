package blockchain;

import java.rmi.RemoteException;
import java.security.PublicKey;

import remoteTest.Configuration;
import remoteTest.TestingLog;
import signature.SignedBytes;
import skipGraph.RMIInterface;

public interface LightChainRMIInterface extends RMIInterface {
	public SignedBytes PoV(Transaction t) throws RemoteException;

	public SignedBytes PoV(Block blk) throws RemoteException;

	public boolean getMode() throws RemoteException;

	public PublicKey getPublicKey() throws RemoteException;

	public void removeFlagNode() throws RemoteException;

	// Testing:
	public void put(Transaction t) throws RemoteException;

	public void put(Block t) throws RemoteException;

	public Configuration getConf() throws RemoteException;

	public TestingLog startSim(int numTransactions, int pace) throws RemoteException;

	public Block insertGenesis() throws RemoteException;

	// To shut down all instances
	public void shutDown() throws RemoteException;

	public TestingLog getTestLog() throws RemoteException;
}
