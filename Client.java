import java.io.*;
import java.net.*;

public class Client {

	private static final int port = 4444;
	private static final String hostname = "localhost";
	private static byte[] encodedPublicKey;

	public static void main( String[] args ) throws Exception {
		Socket socketToServer = new Socket(hostname, port);

		//Steps 1-4: Handled on Server side
		
		//Step 5: Receive encoded PUBLIC KEY from Server...
		ReceiveByteArray receiveByteArray = new ReceiveByteArray( socketToServer );
		receiveByteArray.run();
		encodedPublicKey = new byte[ receiveByteArray.getIncomingByteArraySize() ];
		encodedPublicKey = receiveByteArray.getByteArray();

		//This portion is only ran if it had received an encoded public key
		//A session key has not yet been established
		if( encodedPublicKey.length > 0 ) {
			System.out.println("Byte array received from server...");
			ReceiveCommunications receive = new ReceiveCommunications( socketToServer );
			Thread receiveThread = new Thread( receive );
			receiveThread.start();

			SendCommunications send = new SendCommunications( socketToServer );
			Thread sendThread = new Thread( send );
			sendThread.start();	
		}
		else {
			System.out.println("Didn't get a byte array from the Server");
		}
	}
}