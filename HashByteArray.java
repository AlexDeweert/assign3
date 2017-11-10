import java.util.*;
import java.lang.*;

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

	}

	public static byte[] decryptHash(byte[] input, SecretKeySpec sks, AlgorithmParameters ap){
		
	}

	public static void sendHash(byte [] hash, Socket socket){

	}

	
}