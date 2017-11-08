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

public class Client {

	private static final int port = 4444;
	private static final String hostname = "localhost";
	private static byte[] serversEncodedPublicKey;

	//REMOVE THIS AFTER TESTING
	private static byte[] clientSharedSecret;

	public static void main( String[] args ) throws Exception {

		//First you gotta open a connection to the server
		System.out.println("[CLIENT] Generating socketToServer...");
        boolean looper = true;
        while(looper){
            looper = false;
            try{
    		  Socket socketToServer = new Socket(hostname, port);
            }catch(Exception e){
                looper = true;
                Thread.sleep(100);
            }
        }

		/*	KEYGEN
		*	Here we attempt to create secure communications with the
		*	Java Crypto Architecture example Appendix D:
		*	https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html#AppD
		*/
		//Steps 1-4: Handled on Server side - we're waiting for the server to generate it's public key
		System.out.println("[CLIENT] Waiting for Server to generate it's public key...");

		//Step 5: Received encoded PUBLIC KEY from Server...
		System.out.println("[CLIENT] Receiving encoded PUBLIC KEY from Server...");
		ReceiveByteArray receiveByteArray = new ReceiveByteArray( socketToServer );
		receiveByteArray.run();
		serversEncodedPublicKey = new byte[ receiveByteArray.getIncomingByteArraySize() ];
		serversEncodedPublicKey = receiveByteArray.getByteArray();

		//Step 6: Client instantiates a public key from the encoded bytes sent by server
		System.out.println("[CLIENT] Instantiating a public key from the encoded bytes sent by server...");
		KeyFactory clientKeyFactory = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serversEncodedPublicKey);
        PublicKey serverPublicKey = clientKeyFactory.generatePublic(x509KeySpec);

        //Step 7: Per Diffie-Hellman protocol, the client must generate their own keypair
        System.out.println("[CLIENT] Generating keypair using the parameters associated with the Server's public key...");
        //using the parameters associated with the Server's public key
        DHParameterSpec dhParamFromServerPubKey = ((DHPublicKey)serverPublicKey).getParams();

		//Step 8: Client creates their own DH keypair
        System.out.println("[CLIENT] Creating own Diffie-Hellman keypair...");
        System.out.println("[CLIENT] generating DH keypair...");
        KeyPairGenerator clientKeypairGen = KeyPairGenerator.getInstance("DH");
        clientKeypairGen.initialize(dhParamFromServerPubKey);
        KeyPair clientKeypair = clientKeypairGen.generateKeyPair();

        //Step 9: Client creates and initializes their DH KeyAgreement object
        System.out.println("[CLIENT] Creating and initializing Diffie-Hellman KeyAgreement object...");
        System.out.println("[CLIENT] initialization of DH KeyAgreement object...");
        KeyAgreement clientKeyAgree = KeyAgreement.getInstance("DH");
        clientKeyAgree.init(clientKeypair.getPrivate());

        //Step 10: Client encodes their public key, and sends it over to Server.
        System.out.println("[CLIENT] Encoding own public key...");
        byte[] clientEncodedPublicKey = clientKeypair.getPublic().getEncoded();

        //Step 11: Send the encoded public key to Server
        System.out.println("[CLIENT] Sending encoded public key to Server...");
        SendByteArray sendByteArray = new SendByteArray( socketToServer, clientEncodedPublicKey );
		sendByteArray.run();

		//Step 12-13: (See Server code)
		//Waiting for the Server to instantiate it's own Diffie-Hellman public key (which it hasn't done up until now)

		//Step 14
		//Client uses Server's public key for the first (and only) phase
        //of Client's version of the DH protocol.
        System.out.println("[CLIENT]: Execute PHASE1 ...");
        //Note here this means that TRUE means this is the last key agreement phase to be executed
        clientKeyAgree.doPhase(serverPublicKey, true);
        System.out.println("[CLIENT]: ClientPublicKey AGREES with ServerPublicKey...");


        //TESTING STEP - REMOVE THIS FROM FINAL VERSION - ONLY TO CONFIRM
        //THAT THE SHARED SECRETS AGREE:
        //At this stage, both Server and Client have completed the DH key
        //agreement protocol. Both generate the (same) shared secret.
        try {
            clientSharedSecret = clientKeyAgree.generateSecret();
            int clientLen = clientSharedSecret.length;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }        // provide output buffer of required size
        System.out.println("[CLIENT]: TESTING REMOVE THIS EVENTUALLY Shared secret: " + toHexString(clientSharedSecret));



        //TODO: What follows next is just a test....
        //EVENTUALLY we need to make these steps into a realtime back-and-forth chat.
        //If the user of the program decides they wish for encrypted chat streams:
        //	1) Generate SecretKeys for the AES Algorithm with the raw shared secret data
        //	2) Encrypt a plaintext message using AES/CipherBlockChaining, generating a ciphertext
        //	3) Encode the parameters based on the ciphertext and TRANSMIT those to the server
        //	4) TRANSMIT the ciphertext byte array to the server
        //	5) Now the server has the ciphertext parameters AND the byte array ciphertext
        //	6) Server uses the parameters to decrypt the ciphertext into plaintext




        /*
			initial connection with server, establishing security protocols.
        */
		ReceiveCommunications wait = new ReceiveCommunications(socketToServer, true, false);
		


		/*	UNENCRYPTED COMMUNICATIONS
		*	If the encryption handshake was successful we begin comms with the server
		*/
		//This portion is only ran if it had received an encoded public key
		//A session key has not yet been established
		if( serversEncodedPublicKey.length > 0 ) {
			System.out.println("[CLIENT] Beginning communications with server...");
			ReceiveCommunications receive = new ReceiveCommunications( socketToServer );
			Thread receiveThread = new Thread( receive );
			receiveThread.start();

			SendCommunications send = new SendCommunications( socketToServer );
			Thread sendThread = new Thread( send );
			sendThread.start();	
		}
		else {
			System.out.println("[CLIENT] Didn't get a byte array from the Server");
		}


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