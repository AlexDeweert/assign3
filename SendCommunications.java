/*
	SendCommunications.java
	Utilizes threads for chat concurrency
*/

import java.io.*;
import java.net.*;

/**/
public class SendCommunications implements Runnable {

	private Socket socket = null;
	private PrintWriter writer = null;
	private String outgoingMessage = "";

	public SendCommunications( Socket socket ) {
		this.socket = socket;
	}

	@Override
	public void run() {

		try {

			writer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ) );
			BufferedReader userInput = new BufferedReader( new InputStreamReader( System.in ) );

			while( true ) {
				outgoingMessage = userInput.readLine();
				writer.println( outgoingMessage );
				writer.flush();
			}

		} catch (Exception e) {
			System.out.println( e.toString() );
			e.printStackTrace();
		}
	}

}