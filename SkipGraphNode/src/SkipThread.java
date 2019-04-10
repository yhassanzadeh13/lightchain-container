import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SkipThread extends Thread{
	
	BufferedReader reader = null;
	PrintWriter writer = null;
	Socket socket = null;
	
	public SkipThread(Socket s) {
		this.socket = s;
	}
	
	public void run() {
		
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream());
		}catch(IOException e) {
			log("IO Error in server thread");
		}
		
		try {
			String line = reader.readLine();
			
			log("Recieved "+line+" from "+socket.getRemoteSocketAddress());
			
		}catch(IOException e) {
			log("client connection error");
		}catch(NullPointerException e) {
			log("Null Pointer error");
		}finally {
			
			try {
				log("Closing Connection...");
				if(reader != null) {
					reader.close();
					log("Reader Closed");
				}
				
				if(writer != null) {
					writer.close();
					log("Writer Closed");
				}
				
				if(socket != null) {
					socket.close();
					log("Socket Closed");
				}
				
			}catch(IOException e) {
				log("Closing Error");
			}
			
		}
		
		
	}
	
	public void log(String s) {
		System.out.println(s);
	}
}
