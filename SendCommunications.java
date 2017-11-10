import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import com.sun.crypto.provider.SunJCE;

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
	private Cipher encryptionCipher;
	private boolean sendFingerprintFirst;

	/*
	*	Set the socket object to the socket parameter which
	*	had been passed in likely by a Server or Client object.
	*	Both the Server and Client utilise regular Socket objects (not ServerSocket's)
	*/
	public SendCommunications( Socket socket, Cipher encryptionCipher, boolean sendFingerprintFirst ) {
		this.socket = socket;
		this.encryptionCipher = encryptionCipher;
		this.sendFingerprintFirst = sendFingerprintFirst;
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
			BufferedReader userInput = new BufferedReader( new InputStreamReader( System.in ) );

			//We're always ready to send a message
			while( true ) {


				//If we're sending a fingerprint, we send that ahead of the message
				if( sendFingerprintFirst ) {

					//Set the outgoing message string to whatever the user enters in terminal
					outgoingMessage = userInput.readLine();

					//First send an encrypted hashed version of the plaintext
					HashByteArray.sendHash( (HashByteArray.hashByteArray(outgoingMessage.getBytes())), socket, encryptionCipher );

					//Print what the user entered into the socket's output stream
					writer.println( outgoingMessage );
					//Flush the message
					writer.flush();	
				}
				else {
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

}