/*
*	SendCommunications.java
*	Utilizes threads for chat concurrency
*/

import java.io.*;
import java.net.*;

/*
*	SendCommunications is threadwise class
*	which can be instantiated by Server and Client
*	or any class whicn requires the ability to send
*	Text data through a socket output stream.
*/
public class SendCommunications implements Runnable {

	/*
	*	Init vars
	*	The socket object which binds a port for IP/UDP comms
	*/
	private Socket socket = null;
	private PrintWriter writer = null;
	private String outgoingMessage = "";
	private String message = "";

	/*
	*	Set the socket object to the socket parameter which
	*	had been passed in likely by a Server or Client object.
	*	Both the Server and Client utilise regular Socket objects (not ServerSocket's)
	*/
	public SendCommunications( Socket socket ) {
		this.socket = socket;
	}

	public SendCommunications(Socket socket, String message){
		this.socket = socket;
		this.message = message;
	}

	/*
	*	Implement the run function required classes which implement Runnable.
	*	Here we create a writer to send messages to the output socket
	* 	and a reader to get user input from the terminal.
	*/
	@Override
	public void run() {

		try {

			//Set the writer variable (PrintWriter) to the socket outputstream
			//The writer sends lines to the socket output based on user input
			writer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ) );
			if(!message.equals("")){
				writer.print( message );
				writer.flush();
			}else{
				BufferedReader userInput = new BufferedReader( new InputStreamReader( System.in ) );

				//We're always ready to send a message
				while( true ) {
					//Set the outgoing message string to whatever the user enters in terminal
					outgoingMessage = userInput.readLine();
					//Print what the user entered into the socket's output stream
					writer.println( outgoingMessage );
					//Flush the message
					writer.flush();
				}
			}

		} catch (Exception e) {
			System.out.println( e.toString() );
			e.printStackTrace();
		}
	}

	public void setMessage(String message){
		this.message = message;
	}

}