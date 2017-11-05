import java.io.*;
import java.net.*;

public class Server {

	private static final int port = 4444;
	public static void main( String[] args ) throws Exception {
		
		ServerSocket serverSocket = new ServerSocket(port);
		Socket clientSocket = serverSocket.accept();

		ReceiveCommunications receive = new ReceiveCommunications( clientSocket );
		Thread receiveThread = new Thread( receive );
		receiveThread.start();

		SendCommunications send = new SendCommunications( clientSocket );
		Thread sendThread = new Thread( send );
		sendThread.start();

	}
}