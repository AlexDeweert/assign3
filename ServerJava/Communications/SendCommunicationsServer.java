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
public class SendCommunicationsServer implements Runnable {

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
	public SendCommunicationsServer( Socket socket ) {
		this.socket = socket;
	}

	public SendCommunicationsServer(Socket socket, String message){
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

			writer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ) );

			BufferedReader userInput = new BufferedReader( new InputStreamReader( System.in ) );

			outgoingMessage = userInput.readLine();

			writer.println( outgoingMessageHash );

			writer.flush();

		} catch (Exception e) {
			System.out.println( e.toString() );
			e.printStackTrace();
		}
	}

	public void sendMessageInit(){
			writer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ) );
			BufferedReader userInput = new BufferedReader( new InputStreamReader( System.in ) );
			outgoingMessage = userInput.readLine();
			writer.println( outgoingMessage );
			writer.flush();
	}

	public void sendMessageAuthentication(){
			writer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ) );
			BufferedReader userInput = new BufferedReader( new InputStreamReader( System.in ) );
			if(message.equals()){ //new user

			}else if(message.equals()){//log in

			}else if(message.equals()){//guest user

			}
			outgoingMessage = userInput.readLine();
			writer.println( outgoingMessage );
			writer.flush();
	}


	public void setMessage(String message){
		this.message = message;
	}

}