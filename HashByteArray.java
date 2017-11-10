import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import com.sun.crypto.provider.SunJCE;

public class HashByteArray{


	public static byte[] hashByteArray(byte[] input){
		MessageDigest sha = MessageDigest.getInstance("SHA-256");
		byte [] val = sha.digest(input);
		return val;
	}

	public static byte[] hashString(String input){
		byte [] val = input.getBytes();
		MessageDigest sha = MessageDigest.getInstance("SHA-256");
		byte returnVal = sha.digest(val);
		return returnVal;
	}

	public static byte[] encryptHash(byte[] input, SecretKeySpec sks, AlgorithmParameters ap){

		Cipher encryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		encryptionCipher.init(Cipher.ENCRYPT_MODE, sks, ap);
		byte [] arr = hashByteArray(input);
		byte [] encryptedHash = encryptionCipher.doFinal(arr);
		return encryptedHash;

	}

	public static byte[] decryptHash(byte[] input, SecretKeySpec sks, AlgorithmParameters ap){
		Cipher decryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		decryptionCipher.init(Cipher.DECRYPT_MODE, sks, ap);
		byte [] decryptedHash = decryptionCipher.doFinal(input);
		return decryptedHash;
	}

	public static void sendHash(byte [] hash, Socket socket, SecretKeySpec sks, AlgorithmParameters ap){
		try{

			byte [] fingerprint = encryptionHash(hash, sks, ap);
			writer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ) );
			writer.println(fingerprint);

		}catch (Exception e) {
			System.out.println( e.toString() );
			e.printStackTrace();
		}
	}


}