
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {

	public String getLeftNode(int level) throws RemoteException ;
	public String getRightNode(int level) throws RemoteException ;
	public void setLeftNode(int level, NodeInfo newNode) throws RemoteException;
	public void setRightNode(int level,NodeInfo newNode) throws RemoteException;
	public int getNumID() throws RemoteException;
	public String getNameID() throws RemoteException;
	public String searchByNameID(String targetString) throws RemoteException;
	public String searchByNumID(int targetNum) throws RemoteException;
	public String searchName(String searchTarget,int level,int direction) throws RemoteException;
	public String searchNum(int searchTarget,int level) throws RemoteException;
}
