package skipGraph;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import remoteTest.PingLog;


public interface RMIInterface extends Remote {
	
	/*
	 * This are the signatures of the methods we are calling using RMI
	 * for the SkipNode method.
	 */
	
	public NodeInfo getLeftNode(int level,int num) throws RemoteException ;
	public NodeInfo getRightNode(int level,int num) throws RemoteException ;
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
	
	public boolean setLeftNode(int num, int level,NodeInfo newNode, NodeInfo oldNode)  throws RemoteException;
	public boolean setRightNode(int num, int level,NodeInfo newNode, NodeInfo oldNode) throws RemoteException;
	
	/*
	 * Searches
	 */
	
	public NodeInfo searchByNameID(String targetString) throws RemoteException;
	public NodeInfo searchByNumID(int targetNum) throws RemoteException;
	public ArrayList<NodeInfo> searchByNumID(int targetNum,ArrayList<NodeInfo> lst) throws RemoteException;
	public ArrayList<NodeInfo> searchNum(int searchTarget,int level,ArrayList<NodeInfo> lst, int jumpsLeft) throws RemoteException;
	
	/*
	 * Skip Node with data nodes functions
	 */
	
	public NodeInfo searchName(String searchTarget,int level,int direction) throws RemoteException;
	public NodeInfo insertSearch(int level, int direction,int num, String target) throws RemoteException;
	
	public ArrayList<NodeInfo> getData() throws RemoteException;
	public NodeInfo[][][] getLookupTable() throws RemoteException;
	
	//Pinging
	
	public boolean ping() throws RemoteException;
	public PingLog pingStart(NodeInfo node, int freq) throws RemoteException;
	public PingLog retroPingStart(NodeInfo node, int freq) throws RemoteException;

}
