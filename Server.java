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

public class Server {

	private static final int port = 4444;
	public static void main( String[] args ) throws Exception {

		/*
		*	Establish sockets since they are required first for byte exchange
		*/
		ServerSocket serverSocket = new ServerSocket(port);
		Socket clientSocket = serverSocket.accept();

		/*	KEYGEN
		*	Here we attempt to create secure communications with the
		*	Java Crypto Architecture example Appendix D:
		*	https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html#AppD
		*/
		//Step 1: Server first generates a diffie-hellman keypair
		KeyPair serverKeypair = generateKeypair();

		//Step 2: Server generates and initializes a Diffie-Hellman KeyAgreement object
		KeyAgreement serverKeyAgree = initKeyAgreementObject( serverKeypair );

		//Step 3: Encode the public key from the keypair
		byte[] encodedPublicKey = encodePublicKey( serverKeypair );

		//Step 4: Server sends the ENCODED PUBLIC KEY to the Client with a SendByteArry object
		SendByteArray sendByteArray = new SendByteArray( clientSocket, encodedPublicKey );
		sendByteArray.run();

		//Step 5: Client now has the encoded public key byte array (see client code for step 5)

		/*	COMMUNICATIONS
		*	If the encryption handshake was successful we begin comms with the client
		*/
		//Start a thread to send communications
		SendCommunications send = new SendCommunications( clientSocket );
		Thread sendThread = new Thread( send );
		sendThread.start();

		//Start a thread to receive communications
		ReceiveCommunications receive = new ReceiveCommunications( clientSocket );
		Thread receiveThread = new Thread( receive );
		receiveThread.start();

	}

	/*
	*	Generate a keypair based on the Diffie Hellman protocol
	*/
	public static KeyPair generateKeypair() throws NoSuchAlgorithmException {
		KeyPairGenerator serverKeypairGen = KeyPairGenerator.getInstance("DH");
        serverKeypairGen.initialize(2048);
        KeyPair serverKeypair = serverKeypairGen.generateKeyPair();
        return serverKeypair;
	}

	/*
	*	Create and Initialize a Diffie-Hellman KeyAgreement Object
	*/
	public static KeyAgreement initKeyAgreementObject( KeyPair serverKeypair ) throws Exception {
		System.out.println("SERVER: Initialization of KeyAgreement Object...");
        KeyAgreement serverKeyAgree = KeyAgreement.getInstance("DH");
        serverKeyAgree.init(serverKeypair.getPrivate());
        return serverKeyAgree;
	}

	/*
	*	Server encodes it's public key
	*/
	public static byte[] encodePublicKey ( KeyPair serverKeypair ){
		return serverKeypair.getPublic().getEncoded();
	}



}