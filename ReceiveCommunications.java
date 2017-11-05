import java.io.*;
import java.net.*;

public class ReceiveCommunications implements Runnable {
	
	private Socket socket = null;
	private BufferedReader reader = null;
	private String incomingMessage = "";

	public ReceiveCommunications( Socket socket ) {
		this.socket = socket;
	}

	@Override
	public void run() {

		try {

			reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			while( true ) {

				while( ( incomingMessage = reader.readLine()) != null ) {
					System.out.println( "Received: " + incomingMessage );
				}
			}

		} catch (Exception e) {
			System.out.println( e.toString() );
			e.printStackTrace();
		}
	}

}