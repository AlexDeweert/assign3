import java.io.*;
import java.net.*;
import java.util.*;
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
    private static byte[] serverCipherParameters;

	//REMOVE THIS AFTER TESTING
	private static byte[] clientSharedSecret;

    private static String paramString = "...";
    private static boolean encrypt_chat = false;
    private static boolean veryify_message_integrity = false;
    private static boolean use_password_authentication = false;

	public static void main( String[] args ) throws Exception {



		//First you gotta open a connection to the server
		System.out.println("[CLIENT] Generating socketToServer...");
		Socket socketToServer = new Socket(hostname, port);


        /* We must first ask the Server what kind of parameters it would like to run
        *  1) Confidentiality: Encrypted chat messages (AES encryption)
        *  2) Integrity: Verifying that the messages received have not been altered (Checksum)
        *  3) Authentication: Both the Client and Server enter have a username and password
        */
        checkUserChoices();
        //The server first sends it's chat parameters and we get it here
        ReceiveByteArray receiveChatParameters = new ReceiveByteArray( socketToServer );
        receiveChatParameters.run();
        byte[] serverChatParamsBytes;
        serverChatParamsBytes = Arrays.copyOf( receiveChatParameters.getByteArray(), receiveChatParameters.getIncomingByteArraySize() );
        String serverChatParams = new String( serverChatParamsBytes );
        if( serverChatParams.equals( paramString ) ) {
            System.out.println("[CLIENT] Client's chat parameters MATCH server's chat parameters");
        }
        else {
            System.out.println("[CLIENT] Client's chat parameters DO NOT MATCH server's chat parameters");
        }

        //Now that we received the params from the server, we send our own for the server to verify it's match too
        System.out.println("[CLIENT] Sending chat parameter choices to server...");
        byte[] paramBytes = paramString.getBytes();
        SendByteArray sendChatParameters = new SendByteArray( socketToServer, paramBytes );
        sendChatParameters.run();


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
		serversEncodedPublicKey = Arrays.copyOf( receiveByteArray.getByteArray(), receiveByteArray.getIncomingByteArraySize() );

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
        //Client created a shared secret
        clientSharedSecret = clientKeyAgree.generateSecret();


        //ESTABLISH AES KEY AND CIPHER PARAMETERS
        //Step 1: Client uses the shared secret to create an AES key
        System.out.println("[CLIENT]: Using shared secret as SecretKey object...");
        SecretKeySpec clientAesKey = new SecretKeySpec(clientSharedSecret, 0, 16, "AES");
        //Step 2: Client creates an AES CipherBlockChain with PKCS5Padding spec then
        //initializes that cipher utiliizing the client AES key
        System.out.println("[CLIENT]: Created AES CBC PKCS5 spec...");
        Cipher clientEncryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        System.out.println("[CLIENT]: Initialized AES key...");
        clientEncryptionCipher.init(Cipher.ENCRYPT_MODE, clientAesKey);
        

        //Generate encoded cipher parameters to exchange with Server
        System.out.println("[CLIENT]: Generating cipher parameters...");
        byte[] encodedParams = clientEncryptionCipher.getParameters().getEncoded();
        //Step 3: TRANSMIT  cipher parameters to SERVER
        System.out.println("[CLIENT]: TRASMITTING CIPHER PARAMETERS BYTE ARRAY...");
        sendByteArray = new SendByteArray( socketToServer, encodedParams );
        sendByteArray.run();

        Cipher clientDecryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        System.out.println("[CLIENT]: Initialized AES key...");
        clientDecryptionCipher.init(Cipher.DECRYPT_MODE, clientAesKey, clientEncryptionCipher.getParameters() );


        /*  ENCRYPTED COMMUNICATIONS
        *   If the encryption handshake was successful we begin comms with the server
        */
        if( encrypt_chat ) {
            System.out.println("[CLIENT] Beginning communications with server...");
            ReceiveEncryptedComms encryptedReceive = new ReceiveEncryptedComms( socketToServer, clientDecryptionCipher );
            Thread encryptedReceiveThread = new Thread( encryptedReceive );
            encryptedReceiveThread.start();

            SendEncryptedComms encryptedSend = new SendEncryptedComms( socketToServer, clientEncryptionCipher );
            Thread encryptedSendThread = new Thread( encryptedSend );
            encryptedSendThread.start();    
        }

        /*   UNENCRYPTED COMMUNICATIONS
        *    If the encryption handshake was successful we begin comms with the server
        */
        else {
            System.out.println("[CLIENT] Beginning communications with server...");
            ReceiveCommunications receive = new ReceiveCommunications( socketToServer );
            Thread receiveThread = new Thread( receive );
            receiveThread.start();

            SendCommunications send = new SendCommunications( socketToServer );
            Thread sendThread = new Thread( send );
            sendThread.start(); 
        }
	}

    public static void checkUserChoices() {
        Scanner in = new Scanner( System.in );

        System.out.println("Would you like encrypted communication? Type (y) or (n)...");
        String choice = in.nextLine();
        if( choice.equals("y") ) {
            encrypt_chat = true;
            paramString = paramString.substring(0,0)+'y'+paramString.substring(1);
            System.out.println(paramString);
        } 

        System.out.println("Would you like message integrity verified? Type (y) or (n)...");
        choice = in.nextLine();
        if( choice.equals("y") ) {
            veryify_message_integrity = true;
            paramString = paramString.substring(0,1)+'y'+paramString.substring(2);
            System.out.println(paramString);
        }

        System.out.println("Would you like to use password authentication? Type (y) or (n)...");
        choice = in.nextLine();
        if( choice.equals("y") ) {
            use_password_authentication = true;
            paramString = paramString.substring(0,2)+'y'+paramString.substring(3);
            System.out.println(paramString);
        }

        System.out.println( "You chose the following parameters for chat:");
        System.out.println("Param choice string: " + paramString );

        if( encrypt_chat ) System.out.println("Encrypted chat");
        else System.out.println("Unencrypted chat.");
        if( veryify_message_integrity ) System.out.println("Verified message integrity");
        else System.out.println("Message integrity not verified.");
        if( use_password_authentication ) System.out.println("Password authentication");
        else System.out.println("No password authentication.");
        System.out.println();
    }
}