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

public class authenticationServer{

	//need an authenticator
	//username: hash Password: hash

	//need aes key
	public static void authenticationProcess(Socket connection){
		Scanner console = new Scanner(System.in);
		String choice = "";
		boolean invalid = true;
		SendCommunications ask = new SendCommunications(connection);
		ReceiveCommunications tell = new ReceiveCommunications(connection, false);
		while(invalid){
			invalid = false;
			ask.setMessage("Log In: 1\nSign Up: 2\nProceed As Guest: press any key\n> ");
			ask.run();
			tell.run();
			choice = tell.getMessage();
			//ask = new SendCommunications(connection);
			if(choice.contains("1") && choice.length() < 2){
				ask.setMessage("your logging in\n");
				ask.run();
			}else if(choice.contains("2") && choice.length() < 2){
				ask.setMessage("your signing up \n");
				ask.run();
			}else{
				ask.setMessage("your proceeding as a guest\n");
				ask.run();
			}

		}

	}

	public static void loginUser(Scanner console){
		String username;
		String password;
		System.out.print("Please enter your username: ");
		username = console.nextLine();

		System.out.print("Please enter your password: ");
		password = console.nextLine();




	}

	public static void signUpUser(){

	}

	public static void loginAsGuest(){

	}	

}