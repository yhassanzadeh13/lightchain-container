package skipGraph;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RMIInterface extends Remote {
	
	/*
	 * This are the signatures of the methods we are calling using RMI
	 * for the SkipNode method.
	 */
	
	/*
	 * Getters
	 */
	public String getLeftNode(int level,int num) throws RemoteException ;
	public String getRightNode(int level,int num) throws RemoteException ;
	public String getNameID() throws RemoteException;
	public String getAddress() throws RemoteException ;
	public String getLeftNameID(int level,int num) throws RemoteException;
	public String getRightNameID(int level,int num) throws RemoteException;
	public NodeInfo getNode(int num) throws RemoteException ;
	public int getNumID() throws RemoteException;
	public int getLeftNumID(int level,int num) throws RemoteException;
	public int getRightNumID(int level,int num) throws RemoteException;
	public void delete(int num) throws RemoteException;
	
	/*
	 * Setters
	 */
	public void setLeftNode(int level, NodeInfo newNode,int num) throws RemoteException;
	public void setRightNode(int level,NodeInfo newNode,int num) throws RemoteException;
	
	/*
	 * Searches
	 */
	
	public NodeInfo searchByNameID(String targetString) throws RemoteException;
	public NodeInfo searchByNumID(int targetNum) throws RemoteException;
	public NodeInfo searchName(String searchTarget,int level,int direction) throws RemoteException;
	public NodeInfo searchNum(int searchTarget,int level) throws RemoteException;
	public NodeInfo insertSearch(int level, int direction,int num, String target) throws RemoteException;
	
	public ArrayList<NodeInfo> getData() throws RemoteException;
	public NodeInfo[][][] getLookupTable() throws RemoteException;
	
}
