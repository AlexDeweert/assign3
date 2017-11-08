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
*	ReceiveByteArray is threadwise class
*	which can be instantiated by Server and Client
*	or any class whicn requires the ability to receive
*	byte data through a socket input stream.
*/
public class ReceiveEncryptedComms implements Runnable {
	
	/*
	*	Init vars
	*	The socket object which binds a port for IP/UDP comms
	*/
	private Socket socket = null;
	private DataInputStream reader = null;
	private byte[] ciphertext;
	private byte[] recovered;
	private int length;
	private String s;
	private Cipher decryptionCipher;

	/*
	*	Set the socket object to the socket parameter which
	*	had been passed in likely by a Server or Client object.
	*	Set the byte array to that which was passed in by a client or server
	*/
	public ReceiveEncryptedComms( Socket socket, Cipher decryptionCipher ) {
		this.socket = socket;
		this.decryptionCipher = decryptionCipher;
	}

	/*
	*	Implement the run function required classes which implement Runnable.
	*	Here we create a reader to receive messages from the socket input stream,
	*/
	@Override
	public void run() {

		try {
			//Set the reader to the socket input stream
			//Gets the length of the incoming message
			
			while( true ) {
				reader = new DataInputStream( socket.getInputStream() );
				this.length = reader.readInt();
				if( length > 0 ) {
					System.out.println( "[ReceiveEncryptedComms Object] receiving byte array of size " + length + "...");
					this.ciphertext = new byte[length];
					reader.readFully(ciphertext, 0, ciphertext.length);
					recovered = decryptionCipher.doFinal(ciphertext);
					s = new String( recovered );
					System.out.println(s);
				}
			}
				
			
		} catch (Exception e) {
			System.out.println( e.toString() );
			e.printStackTrace();
		}
	}
}