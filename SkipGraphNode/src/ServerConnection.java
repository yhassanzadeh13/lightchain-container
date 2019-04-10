import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerConnection extends Thread {
	
	Socket socket = null ;
	ServerSocket serverSocket = null;
	
	/*
	 * Notes: I changed the port of the SkipNode to the default 1099
	 * 
	 * 
	 * 
	 */
	
	public ServerConnection() {
		
		//log("What is my address ? Use format IP:port"); Not needed
		
		try {
			SkipNode.address = Inet4Address.getLocalHost().getHostAddress(); //Used to get the current node address.
			SkipNode.IP = SkipNode.address;
			SkipNode.port = 1099; //Changed this to 1099 (default)
			serverSocket = new ServerSocket(SkipNode.port);
		} catch(IOException e) {
			e.printStackTrace();
			log("Server Error");
		}
		
		
	}
	
	public void run() {
		
		while(true) {
			
			try {
				socket = serverSocket.accept();
				log("Connection Established with: "+ socket.getRemoteSocketAddress());
				SkipThread skipThread = new SkipThread(socket);
				skipThread.start();
			}catch(IOException e) {
				e.printStackTrace();
				log("Error in accepting a new connection");
			}
		}
	}
	
	
	public void log(String s) {
		System.out.println(s);
	}
	public String get() {
		Scanner in = new Scanner(System.in);
		String input = in.nextLine();
		return input ;
	}
}
