import java.io.*;
import java.net.*;

public class Client {

	private static final int port = 4444;
	private static final String hostname = "localhost";

	public static void main( String[] args ) throws Exception{
		Socket socketToServer = new Socket(hostname, port);

		ReceiveCommunications receive = new ReceiveCommunications( socketToServer );
		Thread receiveThread = new Thread( receive );
		receiveThread.start();

		SendCommunications send = new SendCommunications( socketToServer );
		Thread sendThread = new Thread( send );
		sendThread.start();

	}
}