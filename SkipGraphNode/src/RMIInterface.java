
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
	public String getLeftNode(int level) throws RemoteException ;
	public String getRightNode(int level) throws RemoteException ;
	public void setLeftNode(int level,String newVal) throws RemoteException;
	public void setRightNode(int level,String newVal) throws RemoteException;
	public String getNumID() throws RemoteException;
	public String getNameID() throws RemoteException;
	public String searchByNameID(String targetString) throws RemoteException, MalformedURLException, NotBoundException;
	public String searchByNumID(String targetNum) throws RemoteException, MalformedURLException, NotBoundException;
}
