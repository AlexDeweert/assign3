import java.util.Date;
import java.text.SimpleDateFormat;
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
	private DataInputStream fingerprintReader = null;
	private String incomingMessage = "";
	private byte[] recovered;
	private byte[] decryptedHashFingerprint;
	private byte[] finalHash;
	private byte[] encryptedHashFingerprint;
	private byte[] hashNeverTransmitted;
	private Cipher decryptionCipher;
	private Cipher encryptionCipher;
	private boolean receiveFingerprintFirst;
	private int length;

	/*
	*	Set the socket object to the socket parameter which
	*	had been passed in likely by a Server or Client object.
	*	Both the Server and Client utilise regular Socket objects (not ServerSocket's)
	*/
	public ReceiveCommunications( Socket socket, Cipher decryptionCipher, Cipher encryptionCipher, boolean receiveFingerprintFirst ) {
		this.socket = socket;
		this.decryptionCipher = decryptionCipher;
		this.encryptionCipher = encryptionCipher;
		this.receiveFingerprintFirst = receiveFingerprintFirst;
	}

	/*
	*	Implement the run function required classes which implement Runnable.
	*	Here we create a reader to receive messages from the socket input stream,
	*/
	@Override
	public void run() {

		try {


			if( receiveFingerprintFirst ) {
					fingerprintReader = new DataInputStream( socket.getInputStream() );
					this.length = fingerprintReader.readInt();
					if( length > 0 ) {
						this.encryptedHashFingerprint = new byte[length];

						fingerprintReader.readFully(encryptedHashFingerprint, 0, encryptedHashFingerprint.length);
						//Decrypt the has fingerprint
						decryptedHashFingerprint = decryptionCipher.doFinal(encryptedHashFingerprint);
						finalHash = HashByteArray.decryptHash( decryptedHashFingerprint, decryptionCipher );

						//Receive the actual message
						// this.length = fingerprintReader.readInt();
						// this.ciphertext = new byte[length];
						// fingerprintReader.readFully(ciphertext, 0, ciphertext.length);
						// recovered = decryptionCipher.doFinal(ciphertext);
						// s = new String( recovered );
						//Set the reader (BufferedReader object) to the socket input stream
						reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
						while( ( incomingMessage = reader.readLine()) != null ) {
							System.out.println("Received a message: " + incomingMessage );
							recovered = incomingMessage.getBytes();
						}
						
						//Rehash the message
						hashNeverTransmitted = new byte[ (HashByteArray.encryptHash( recovered, encryptionCipher )).length ];

						//Compare the two results
						if( !Arrays.equals( hashNeverTransmitted, finalHash ) ) {
							System.out.println("[WARNING]: Message integrity comprimised. Hash fingerprint varies.");
							System.out.println("HashNeverTransmitted: " + Arrays.toString( hashNeverTransmitted ));
							System.out.println("decryptedHashFingerprint: " + Arrays.toString( finalHash ));
						}



						//Print the message anyway
						System.out.println( "[" + getCurrentTimeStamp() + " Received]: " + incomingMessage );
					}	
				}
				//Don't need to verify message integrity
				else {
					//Set the reader (BufferedReader object) to the socket input stream
					reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
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