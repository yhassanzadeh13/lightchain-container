package blockchain;

import java.rmi.RemoteException;
import java.security.PublicKey;

import remoteTest.Configuration;
import remoteTest.TestingLog;
import skipGraph.RMIInterface;

public interface LightChainRMIInterface extends RMIInterface {
	public String PoV(Transaction t) throws RemoteException ;
	public String PoV(Block blk) throws RemoteException ;
	public int getMode() throws RemoteException;
	public PublicKey getPublicKey() throws RemoteException ;
	
	// Testing:
	public void put(Transaction t) throws RemoteException;
	public void put(Block t) throws RemoteException;
	public Configuration getConf() throws RemoteException;
	public TestingLog startSim(int numTransactions, int pace) throws RemoteException;
	public void insertGen() throws RemoteException;
	
	//To shut down all instances
	public void shutDown() throws RemoteException;
	public TestingLog getTestLog() throws RemoteException;
	public void printLog(String name) throws RemoteException;
}
