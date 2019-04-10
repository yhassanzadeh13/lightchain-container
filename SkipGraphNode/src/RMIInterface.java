
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
	public String getLeftNode(int level) throws RemoteException ;
	public String getRightNode(int level) throws RemoteException ;
	public void setLeftNode(int level,String newVal) throws RemoteException;
	public void setRightNode(int level,String newVal) throws RemoteException;
	public String getNumID() throws RemoteException;
	public String getNameID() throws RemoteException;
	
}
