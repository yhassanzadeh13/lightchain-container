
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {

	public String getLeftNode(int level,int num) throws RemoteException ;
	public String getRightNode(int level,int num) throws RemoteException ;
	public void setLeftNode(int level, NodeInfo newNode,int num) throws RemoteException;
	public void setRightNode(int level,NodeInfo newNode,int num) throws RemoteException;
	public int getNumID() throws RemoteException;
	public String getNameID() throws RemoteException;
	public NodeInfo searchByNameID(String targetString) throws RemoteException;
	public NodeInfo searchByNumID(int targetNum) throws RemoteException;
	public NodeInfo searchName(String searchTarget,int level,int direction) throws RemoteException;
	public NodeInfo searchNum(int searchTarget,int level) throws RemoteException;
	public int getLeftNumID(int level,int num) throws RemoteException;
	public int getRightNumID(int level,int num) throws RemoteException;
	public String getLeftNameID(int level,int num) throws RemoteException;
	public String getRightNameID(int level,int num) throws RemoteException ;
	public NodeInfo insertSearch(int level, int direction,int num, String target) throws RemoteException;
}
