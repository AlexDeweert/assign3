import java.io.*;
import java.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;

/*
*	ReceiveCommunications is threadwise class
*	which can be instantiated by Server and Client
*	or any class whicn requires the ability to receive
*	text data through a socket input stream.
*/
public class ReceiveCommunications implements Runnable {
	
	/*
	*	Init vars
	*	The socket object which binds a port for IP/UDP comms
	*/
	private Socket socket = null;
	private BufferedReader reader = null;
	private String incomingMessage = "";

	/*
	*	Set the socket object to the socket parameter which
	*	had been passed in likely by a Server or Client object.
	*	Both the Server and Client utilise regular Socket objects (not ServerSocket's)
	*/
	public ReceiveCommunications( Socket socket ) {
		this.socket = socket;
	}

	/*
	*	Implement the run function required classes which implement Runnable.
	*	Here we create a reader to receive messages from the socket input stream,
	*/
	@Override
	public void run() {

		try {

			//Set the reader (BufferedReader object) to the socket input stream
			reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			while( true ) {
				//While there exists data in the inputstream print it out
				while( ( incomingMessage = reader.readLine()) != null ) {
					System.out.println( "[" + getCurrentTimeStamp() + " Received]: " + incomingMessage );
				}
			}
		} catch (Exception e) {
			System.out.println( e.toString() );
			e.printStackTrace();
		}
	}

	public static String getCurrentTimeStamp() {
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	    Date now = new Date();
	    String strDate = sdf.format(now);
	    return strDate;
	}
}