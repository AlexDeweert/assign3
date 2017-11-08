import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.lang.*;

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
	private static byte[] clientEncodedPublicKey;

	//REMOVE THIS AFTER TESTING
	private static byte[] serverSharedSecret;

	public static void main( String[] args ) throws Exception {

		/*
		*	Establish sockets since they are required first for byte exchange
		*/
		System.out.println("[SERVER] Generating serverSocket and clientSocket...");
		ServerSocket serverSocket = new ServerSocket(port);
		Socket clientSocket = serverSocket.accept();



		/*	KEYGEN
		*	Here we attempt to create secure communications with the
		*	Java Crypto Architecture example Appendix D:
		*	https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html#AppD
		*/
		//Step 1: Server first generates a keypair
		System.out.println("[SERVER] Generating keypair...");
		KeyPair serverKeypair = generateKeypair();

		//Step 2: Server generates and initializes a KeyAgreement object
		System.out.println("[SERVER] Generating and initializing a KeyAgreement object...");
		KeyAgreement serverKeyAgree = initKeyAgreementObject( serverKeypair );

		//Step 3: Encode the public key from the keypair
		System.out.println("[SERVER] Generating and initializing a KeyAgreement object...");
		byte[] serversEncodedPublicKey = encodePublicKey( serverKeypair );

		//Step 4: Server sends the ENCODED PUBLIC KEY to the Client with a SendByteArray object
		System.out.println("[SERVER] sends the ENCODED PUBLIC KEY to the Client with a SendByteArray object...");
		SendByteArray sendByteArray = new SendByteArray( clientSocket, serversEncodedPublicKey );
		sendByteArray.run();

		//Step 5: Client now has the encoded public key byte array (see client code for step 5)
		//We wait for the client to generate it's own public key and send it back to server

		//Step 6 - 11 (See Client code)

		//Step 12: Receive encoded PUBLIC KEY from Client...
		System.out.println("[SERVER] Receiveing encoded PUBLIC KEY from Client...");
		ReceiveByteArray receiveByteArray = new ReceiveByteArray( clientSocket );
		receiveByteArray.run();
		clientEncodedPublicKey = new byte[ receiveByteArray.getIncomingByteArraySize() ];
		clientEncodedPublicKey = receiveByteArray.getByteArray();

		//Step 13:
        //Server uses client's public key for the first (and only) phase
        //of it's version of the DH protocol. Before it can do so, it
        //has to instantiate a DH public key from client's encoded key material.
        KeyFactory serverKeyFactory = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientEncodedPublicKey);
        PublicKey clientPublicKey = serverKeyFactory.generatePublic(x509KeySpec);
        System.out.println("[SERVER]: Execute PHASE1 ...");
        //Note here this means that TRUE means this is the last key agreement phase to be executed
        serverKeyAgree.doPhase(clientPublicKey, true);
        System.out.println("[SERVER]: ServerPublicKey AGREES with ClientPublicKey ...");


        //TESTING STEP - REMOVE THIS FROM FINAL VERSION - ONLY TO CONFIRM
        //THAT THE SHARED SECRETS AGREE:
        //At this stage, both Server and Client have completed the DH key
        //agreement protocol. Both generate the (same) shared secret.
        try {
            serverSharedSecret = serverKeyAgree.generateSecret();
            int serverLen = serverSharedSecret.length;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }        // provide output buffer of required size
        System.out.println("[SERVER]: TESTING REMOVE THIS EVENTUALLY Shared secret: " + toHexString(serverSharedSecret));




        //TODO: What follows next is just a test....
        //EVENTUALLY we need to make these steps into a realtime back-and-forth chat.
        //If the user of the program decides they wish for encrypted chat streams:
        //	1) Generate SecretKeys for the AES Algorithm with the raw shared secret data
        //	2) Encrypt a plaintext message using AES/CipherBlockChaining, generating a ciphertext
        //	3) Encode the parameters based on the ciphertext and TRANSMIT those to the server
        //	4) TRANSMIT the ciphertext byte array to the server
        //	5) Now the server has the ciphertext parameters AND the byte array ciphertext
        //	6) Alice uses the parameters to decrypt the ciphertext into plaintext


        
        authentication.authenticationProcess(clientSocket);


		/*	UNENCRYPTED COMMUNICATIONS
		*/
		//We only start talking if the public key length is greater than 0 (ie it exists)
		//We havent yet started a session key for message encryption
		if( clientEncodedPublicKey.length > 0 ) {
			System.out.println("[SERVER] Beginning communications with Client...");
			//Start a thread to send communications
			SendCommunications send = new SendCommunications( clientSocket );
			Thread sendThread = new Thread( send );
			sendThread.start();

			//Start a thread to receive communications
			ReceiveCommunications receive = new ReceiveCommunications( clientSocket );
			Thread receiveThread = new Thread( receive );
			receiveThread.start();
		}
		else {
			System.out.println("[SERVER] Didn't get a byte array from the Client");
		}
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


	//Utility functions
	/*
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /*
     * Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }



}