package project;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
	private Socket socket;
	private ServerSocket serverSocket;
	private BufferedReader input;
	private InputStream inputStream;
	private OutputStream outputStream;
	private DataInputStream fromClient;
	private DataOutputStream toClient;
	//init to zero because the client ID can never be 0
	private int CID;
	private int threadNum = 1;
	public ClientHandler(int CID ,Socket socket, ServerSocket serverSocket,int threadnum)throws IOException {
		this.socket = socket;
		this.serverSocket = serverSocket;
		this.CID = CID;
		this.threadNum = threadnum;
		input  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//for the next thread
		 
		System.out.println("running thread " + threadNum);
		threadNum++;
		try {
		
			// get the output stream from the socket.
	         outputStream = socket.getOutputStream();
	        // create a data output stream from the output stream so we can send data through it
	        toClient = new DataOutputStream(outputStream);
	        
			// get the output stream from the socket.
	        inputStream = socket.getInputStream();
	        // create a data output stream from the output stream so we can send data through it
	        fromClient = new DataInputStream(inputStream);

	        //db init
	        Connection connection = null;	
			try {
				//must include jdbc:sqlite:(insert db name here.db)
				String jbdcUrl = "jdbc:sqlite:CryptoCurrency.db";
				//driver manager looks for the jar we just added in the libary from github
			    connection = DriverManager.getConnection(jbdcUrl);
			    System.out.println("connection established to jdburl");
			    //creates tables
			    Server.createTables(connection);
			   
			}
			catch(SQLException e)
			{
				System.out.println("error connecting to SQLite database");
				e.printStackTrace();
			}
			String id = fromClient.readUTF();
			/*for tracking purposes*/
			CID = Integer.parseInt(id);
		
			
			boolean methodStatus=true;
			boolean loggedin=false;
			while(methodStatus==true) {
	
				if(loggedin==false) {
					try
					{
						while(Server.verifyUser(connection,id)== false){
							toClient.writeUTF("false");
							id=fromClient.readUTF();
						}
						toClient.writeUTF("true");
						Server.login(connection, id);
						loggedin=true;
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
				}
				String action = fromClient.readUTF();
				String answer = null;
				String amount = null;
				//command lists
				switch(action) {
				case "BUY":
			
					String cryptoName = fromClient.readUTF();
					String cryptoAmount = fromClient.readUTF();
					String cryptoPrice = fromClient.readUTF();
					String recieved ="recieved BUY "+ cryptoName +
							" " + cryptoAmount + " " + cryptoPrice;
					toClient.writeUTF(recieved);
					try
					{
						answer = Server.buyCrypto(connection,cryptoName,cryptoAmount,cryptoPrice,id);
						toClient.writeUTF(answer);
						break;
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
					break;
				case "LIST":
					try
					{
						
						//sends results to an array list so the client can get multiple rows of info
						ArrayList<String> resultList = new ArrayList<String>();
						resultList = Server.list(connection,id);
						toClient.writeUTF(""+resultList.size());
							for(int i=0;i<resultList.size();i++)
							{
								toClient.writeUTF(resultList.get(i));
							}
							break;
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
					
				case "SELL":
					cryptoName = fromClient.readUTF();
					cryptoAmount = fromClient.readUTF();
					cryptoPrice = fromClient.readUTF();
				    recieved ="recieved SELL "+ cryptoName +
							" " + cryptoAmount + " " + cryptoPrice;
					toClient.writeUTF(recieved);
					try
					{
						answer = Server.sellCrypto(connection,cryptoName,cryptoAmount,cryptoPrice,id);
						toClient.writeUTF(answer);
						break;
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
				case "QUIT":
					try
					{
						answer =Server.logout(connection,id);
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
					socket.close();
					methodStatus = false;
					break;
				case "SHUTDOWN":
					//socket.close();
					//serverSocket.close();
					methodStatus = false;
					break;
				case "BALANCE":
					System.out.println("balance");
					try
					{
					answer = Server.checkBalance(connection,id);
					//answer = buyCrypto(connection,cryptoName,cryptoAmount,cryptoPrice,id);
					toClient.writeUTF(answer);
					break;
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
					break;
				case "DEPOSIT":
					amount = fromClient.readUTF();
					try
					{
					answer = Server.deposit(connection,id,amount);
					toClient.writeUTF(answer);
					break;
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
					break;
				case "LOGOUT":
					try
					{
						answer =Server.logout(connection,id);
						toClient.writeUTF(answer);
						loggedin = false;
						break;
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
					break;
				case"WHO":
					try
					{
						//sends results to an array list so the client can get multiple rows of info
						ArrayList<String> resultList = new ArrayList<String>();
						resultList = Server.who(connection,id);
						toClient.writeUTF(""+resultList.size());
							for(int i=0;i<resultList.size();i++)
							{
								toClient.writeUTF(resultList.get(i));
							}
							break;
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
					break;
				default:
					System.out.println("invalid action was sent from client");
				
				}
			}
	
	        //clean up time
			input.close();
			socket.close();
			//serverSocket.close();
			toClient.close(); // close the output stream when we're done.
			fromClient.close();
			
			System.out.println("server exiting");
		}
		catch(IOException e)
		{
		  e.printStackTrace();
		}

	}//end runnable
	public int getClientID()
	{
		return CID;
	}
}//end class
