package project;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client{
	//int port num
	private static final int portNumber = 6410;
	
	    private static boolean isActive = true;
	    private static Scanner input = new Scanner(System.in);
	public static void main(String[] args) {
		//init this many threads
		try
		{
			Socket socket = new Socket("localhost",portNumber);
			System.out.println("connecting to server");
			socket.setReuseAddress(isActive);
			//checks to ensure connection worked
			if(socket.isConnected()) {
				System.out.println("connection established with server");	
			}
			else
			{
				System.out.println("connection failed with server");
			}
		
		  //get the input/output stream from the socket.
		 OutputStream outputStream = socket.getOutputStream();
		 InputStream inputStream = socket.getInputStream();
		 //create a data input/output stream from the output stream so we can send data through it
		 DataOutputStream toServer = new DataOutputStream(outputStream);
		 DataInputStream fromServer = new DataInputStream(inputStream);

		boolean methodStatus = true;
		
		String id=null;
		boolean loggedin=false;
		
		//if this ends the program and communication with server ends
		while(methodStatus==true)
		{
			while(loggedin == false)
			{
			System.out.println("Enter a user ID");
			
			id = input.nextLine();
			toServer.writeUTF(id);
			String logginSuccess = fromServer.readUTF();
				if(logginSuccess.equals("true")) {
					loggedin=true;
					System.out.println("login success");
				}
				else
				{
					loggedin=false;
					//todo check error
					System.out.println("error 400 no such user or user is already logged in");
				}//end nested if
			}//end if
			
			System.out.println("Choose an action by entering one of the following"
					+ ": BUY,SELL,LIST,BALANCE,DEPOSIT,LOGOUT,QUIT,SHUTDOWN");
			//full line			
			 BufferedReader buffer = new BufferedReader(
			            new InputStreamReader(System.in));
			 int resultList;
			 String resultListSize;
			 String command[];
			 String action = null;//todo
			 String cryptoName = null;
			 String cryptoAmount = null;
			 String cryptoPrice = null;
			 String amount = null;
			command = buffer.readLine().split(" ");
			action = command[0].toUpperCase();
	
			//only the command portion
			//BUY BITCOIN 2.00 2.00
			//Sends the server the action and Returns the answers
			switch(action.toUpperCase()) {
			case "BUY":
			
				
				cryptoName=command[1];
				cryptoAmount=command[2];
				cryptoPrice=command[3];
				
				toServer.writeUTF(action);
				toServer.writeUTF(cryptoName);
				toServer.writeUTF(cryptoAmount);
				toServer.writeUTF(cryptoPrice);
				System.out.println(fromServer.readUTF());
				System.out.println(fromServer.readUTF());
				break;
			case "SELL":
			
				cryptoName=command[1];
				cryptoAmount=command[2];
				cryptoPrice=command[3];

				toServer.writeUTF(action);
				toServer.writeUTF(cryptoName);
				toServer.writeUTF(cryptoAmount);
				toServer.writeUTF(cryptoPrice);
				System.out.println(fromServer.readUTF());
				System.out.println(fromServer.readUTF());
				break;
			case "LIST":
				if(Integer.parseInt(id)==1)
				{
				System.out.println("displaying crpyto wallet for user:" +id);
				}
				else
				{
					System.out.println("displaying crpyto wallet for all users:" +id);
				}
				toServer.writeUTF(action);
				 resultListSize = fromServer.readUTF();
				 resultList = Integer.parseInt(resultListSize);
					for(int i=0;i<resultList;i++)
					{
						System.out.println(fromServer.readUTF());
					}
					
				break;
			case "QUIT":
				toServer.writeUTF(action);
				System.out.println("Logging out user");
				System.out.println(fromServer.readUTF());
				socket.close();
				methodStatus = false;
				break;
			case "SHUTDOWN":
				toServer.writeUTF(action);
				System.out.println(fromServer.readUTF());
				methodStatus = false;
				break;
			case "BALANCE":
				toServer.writeUTF(action);
				System.out.println(fromServer.readUTF());
				break;
			case "DEPOSIT":
				toServer.writeUTF(action);
				System.out.println("Enter the amount you wish to deposit");
				amount = input.nextLine();
				toServer.writeUTF(amount);
				System.out.println(fromServer.readUTF());
				break;
			case "LOGOUT":
				toServer.writeUTF(action);
				loggedin=false;
				System.out.println(fromServer.readUTF());
				break;
			case "WHO":
				toServer.writeUTF(action);
				 resultListSize = fromServer.readUTF();
				 resultList = Integer.parseInt(resultListSize);
					for(int i=0;i<resultList;i++)
					{
						System.out.println(fromServer.readUTF());
					}
					
				break;
			default:
				System.out.println("incorrect command please try again");
			}//end swtich
		}//while
		socket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	
		System.out.println("client exiting");
		System.exit(0);
	}
}