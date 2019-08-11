package blockchain;

import java.rmi.RemoteException;
import java.security.PublicKey;

import skipGraph.RMIInterface;

public interface LightChainRMIInterface extends RMIInterface {
	public String PoV(Transaction t) throws RemoteException ;
	public String PoV(Block blk) throws RemoteException ;
	public int getMode() throws RemoteException;
	public PublicKey getPublicKey() throws RemoteException ;
	// Testing:
	public void put(Transaction t) throws RemoteException;
	public void put(Block t) throws RemoteException;
}
