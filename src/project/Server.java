package project;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	//required
	static int maxThreadNumber = 3;
	private static final int portNumber = 6410;
  	private static ServerSocket serverSocket;  //connected socket
	private static Socket socket;  //connected socket
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(maxThreadNumber);
    //db init
 
    
	public static void main(String[] args) throws IOException
	{
		int clientID=0;
		int threadNum=1;
		serverSocket = new ServerSocket(portNumber);
		while(true) {
		//server init

		System.out.println("waiting for client");
		serverSocket.setReuseAddress(true);
	    socket = serverSocket.accept();
	    
	  //lets the user know the server and client are connected
		if(socket.isConnected())
		{
			System.out.println("connection established with client");
		}
		else
		{
			System.out.println("connection with client failed");
		}
		
		ClientHandler clientThread = new ClientHandler(clientID,socket,serverSocket,threadNum);
		clients.add(clientThread);
		pool.execute(clientThread);
		}//end while
	
	}//end main

/*testing function to see list of in the user data base*/
	public static ArrayList<String> who(Connection connection,String id)throws SQLException{
		Statement statement = connection.createStatement();
		String row;
		ArrayList<String>answer = new ArrayList();
		
		if(isRootUser(connection,id)==true) {
		ResultSet r = statement.executeQuery("SELECT*FROM Users where isactive = 1");
		int i = 0;
		while(r.next())
			{
				
			String fname = r.getString("first_name");
			String lname = r.getString("last_name");

			row = fname + " " + lname;
			answer.add(row);
			i++;
			
			}//end while
	
		}//end if
		return answer;
	}//end list
/*Returns a Balance after its given the db connection and the UserID*/
public static String checkBalance(Connection connection,String id)throws SQLException{
	
	String query = "SELECT usd_balance,first_name,last_name From Users where id ="+id;
	Statement statement = connection.createStatement();
	statement.executeQuery(query);
	ResultSet r = statement.executeQuery(query);
	String resultant = null;
	while(r.next())
	{
		Double balance = r.getDouble("usd_balance");
		String fname = r.getString("first_name");
		String lname = r.getString("last_name");
		
		resultant = ("200 0K\n"+fname + " " + lname + " has a balance of | $"+ balance);
		
	}
	return resultant;
	
}
/*-initializes tables, inserts a user named john doe with a balance of 100$ who is the root user at 1
 * -users 2 - 7 are for debugging and testing purposes
 * -Clears existing databases and replaces it with default for testing and debugging*/
public static void createTables(Connection connection) throws SQLException{
		Statement statement = connection.createStatement();
		
		//cleans the tables from previous runs
		statement.execute("DROP TABLE IF EXISTS Cryptos");
		statement.execute("DROP TABLE IF EXISTS Users");
		
		String createSqlTableUsers ="create table if not exists Users"
				+ "(ID INTEGER NOT NULL,first_name varchar(255),"
				+ "last_name varchar(255),"
				+ "user_name varchar(255) NOT NULL,"
				+ "password varchar(255),"
				+ "usd_balance DOUBLE NOT NULL,"
				+ "isActive INTEGER NOT NULL CHECK (isActive IN (0, 1)),"
				+ "PRIMARY KEY (ID));";
	
		statement.execute(createSqlTableUsers);
		System.out.println("table created Users");
		
		String createSqlTableCrypto = "create table if not exists Cryptos \r\n"
				+ " (\r\n"
				+ " ID INTEGER NOT NULL,\r\n"
				+ " crypto_name varchar(10) NOT NULL,\r\n"
				+ " crypto_balance DOUBLE,\r\n"
				+ " crypto_amount DOUBLE,\r\n"
				+ " crypto_price DOUBLE,\r\n"
				+ " user_id int, \r\n"
				+ " PRIMARY KEY (ID),\r\n"
				+ " FOREIGN KEY (user_id) REFERENCES Users (id) \r\n"
				+ " );";
	
	    statement = connection.createStatement();
		statement.execute(createSqlTableCrypto);
		System.out.println("table created Crypto");
		
		//inserts a default user 1 and 2
		insertUser(connection,"john","doe","Jdoe@gmail.com","doe19987","100.00",0);
		insertUser(connection,"jane","doe","Jadoe@gmail.com","jane1989","100.00",0);
		//init more users
		//inserts a default user 3 and 7
		insertUser(connection,"Rueben","hodes","rh@gmail.com","rh1998","500.00",0);
		insertUser(connection,"kev","kollovozi","kevKollo@gmail.com","kollo3000","500.00",0);
		insertUser(connection,"marshal","mathers","eminem@gmail.com","Detriot","1000.00",0);  
		insertUser(connection,"kevin","hart","comedian@gmail.com","standup","1000.00",0);  
		insertUser(connection,"dwyane","johnson","theRock@gmail.com","wrestling","1000.00",0);  
		
		/*
		 * Init Cryptos
		 * (connection,"name of crypto", "double", "double", "double","int id of users wallet its in")
		 * Ids only currently go to 7*/
		
		insertCrypto(connection,"bitcoin","10.00","100.00","80.00","2");
		insertCrypto(connection,"dogecoin","10.00","25.00","30.00","2");
		insertCrypto(connection,"etherium","10.00","180.00","90.00","2");
		insertCrypto(connection,"bitcoin","10.00","100.00","80.00","5");
		insertCrypto(connection,"bitcoin","10.00","100.00","80.00","6");
		insertCrypto(connection,"bitcoin","10.00","100.00","80.00","7");
	}
/*Inserts a user into database for ease of use*/
public static void insertUser(Connection connection,String firstname,String lastname,
			String username, String password,String balance,int isActive)throws SQLException{
	String query ="INSERT INTO Users(ID, first_name, last_name, user_name,"
    		+ " password, usd_balance,isActive)\n values(null,'"+firstname+"','"+lastname+"','"+username+"','"+password+"',"+balance+","+isActive+");";
	    Statement statement  = connection.createStatement();
		statement.executeUpdate(query);
		System.out.println("inserted");
	}
/*Inserts crypto into a wallet based on thier user ID*/
public static void insertCrypto(Connection connection,String cryptoName,String cryptoAmount,
			String cryptoBalance,String price, String userID)throws SQLException{
		
		String query =
				"INSERT INTO Cryptos("
				+ "crypto_name,"
				+ "crypto_balance,"
				+ "crypto_amount,"
				+ "crypto_price,"
				+ "user_id)"
				+ "values('"+cryptoName+"',"
				+cryptoAmount+","
				+cryptoBalance+","
				+price+","
				+userID+")";
	    Statement statement  = connection.createStatement();
		statement.executeUpdate(query);
		System.out.println("inserted");
	}
/*updated in p2 to add root user function
 * lists all crypto currencies in a wallet
 * currently if root user lists all crypto currencies in all wallets*/
public static ArrayList<String> list(Connection connection,String id)throws SQLException{
	Statement statement = connection.createStatement();
	String row;
	ArrayList<String>answer = new ArrayList();
	
	if(isRootUser(connection,id)==false) {
	ResultSet r = statement.executeQuery("select * from Cryptos where user_id = "+id);
	int i = 0;
		while(r.next())
		{
			Integer user_id = r.getInt("id");
			String fname = r.getString("first_name");
			String lname = r.getString("last_name");
			String uname = r.getString("user_name");
			String pword = r.getString("password");
			Double balance = r.getDouble("usd_balance");
			Integer isactive = r.getInt("isactive");
			
			System.out.println(user_id + "| " + fname + "| " + lname + "| " + uname + "| "+ pword + "| $"+ balance);
		}//endwhile
		return answer;
	}//end if
	else
	{
		ResultSet r = statement.executeQuery("select * from Cryptos");
		int i = 0;
		while(r.next())
			{
				
				String cryptoName = r.getString("crypto_name");
				double cryptoAmount = r.getDouble("crypto_amount");
				double cyrptoBalance = r.getDouble("crypto_balance");
				double cyrptoPrice = r.getDouble("crypto_price");
				int userID = r.getInt("user_id");
				
				row = cryptoName + " " + cryptoAmount + " " + cyrptoBalance;
				answer.add(row);
				i++;
			
			}//end while
			return answer;
	}
}//end list
/*Increases crypto amount from user and decreases usd balance
 * also checks if usd balance is enough to buy it */
public static String buyCrypto(Connection connection,String cryptoname, String amount,String price, String id)throws SQLException{
		
		String answer = null;
		String query = null;
		
		//grabs the balance from the user
		query="SELECT usd_balance FROM Users where id ="+id;
		Statement checkBalance = connection.createStatement();
		ResultSet r = checkBalance.executeQuery(query);
		double user_balance = r.getDouble("usd_balance");
		double amount_to_buy = Double.parseDouble(amount);
		
		//checks if you have enough funds
		if(user_balance >= amount_to_buy*Double.parseDouble(price))
		{
			//subtracts the usd from user
			Statement statement = connection.createStatement();
			query="UPDATE Users\r\n"
					+ "SET usd_balance = usd_balance-"+amount_to_buy*Double.parseDouble(price)+"\r\n"
					+ "WHERE id="+id;
			statement.executeUpdate(query);
			
			//init crypto amount
			insertCrypto(connection,cryptoname,"0.00",price,amount,id);
			
			//updates the balance of the crypto and adds the coin
			query="SELECT*FROM Cryptos where user_id ="+id;//grabs crypto table
			statement = connection.createStatement();
			r = statement.executeQuery(query);
			
			//new crypto balance = old crypto balance + price*amount bought
			double crypto_balance = r.getDouble("crypto_balance");
			crypto_balance += amount_to_buy*r.getDouble("crypto_price");
			
			//total amount = old amount + new amount
			double crypto_amount =+ r.getDouble("crypto_amount");
			query="UPDATE Cryptos\r\n"
					+ "SET crypto_balance ="+crypto_balance +","
					+"crypto_amount="+ crypto_amount
					+ " WHERE user_id="+id;
			statement.executeUpdate(query);
			
			r = statement.executeQuery("select* from Users");
			
			answer ="200 OK\n BOUGHT: New balance:" + " $" +
			r.getDouble("usd_balance");
		}//end if
		else
		{
			answer = "error 400: Not enough Balance";
		}
		r.close();
		return answer;
	}
/*Reduces crypto ammount from user and adds fuinds to usd balance*/
public static String sellCrypto(Connection connection,String cryptoname, String amount,String price, String id)throws SQLException{
	//Init
	Statement statement = connection.createStatement();
	String query;
	//updates the balance of the User 
	//total amount = old amount + new amount
	double profit =+(Double.parseDouble(amount)*Double.parseDouble(price));
	query="UPDATE Users\r\n"
			+ "SET usd_balance = + "+ profit
			+ " WHERE id="+id;
	statement.executeUpdate(query);
	//grabs users table
		query ="Select*From Users";
		ResultSet rs = statement.executeQuery(query);
	String answer="200 ok\n SOLD: New balance: "+amount + " " + cryptoname + 
			" USD "+rs.getDouble("usd_balance");
	rs.close();
	return answer;
}
/*Error handling, if user DNE*/
public static Boolean verifyUser(Connection connection,String id)throws SQLException{
	
	String query ="SELECT COUNT(1) AS RC FROM Users WHERE id= "+ id;
	Statement statement = connection.createStatement();
	ResultSet rs = statement.executeQuery(query);
	int answer= rs.getInt("RC");
	//does exist?
	if(answer==1 )
	{
		//user is not logged in
		if(isActive(connection,id)==false)
		{
			rs.close();
			return true;
		}
		else
		{
			rs.close();
			return false;
		}
	}
	else
	{
		//didnt exist
		rs.close();
		return false;
	}
}//end verify user
/*allows to easily check root users*/
public static Boolean isRootUser(Connection connection,String id)throws SQLException{
	{
		int num = Integer.parseInt(id);
		if(num==1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
//todo p2

/*increase users balance*/
public static String deposit(Connection connection,String id,String amount)throws SQLException
{
	//Init
	Statement statement = connection.createStatement();
	String query;
	//updates the balance of the User 
	//total balance = old amount + new amount
	query="UPDATE Users\r\n"
			+ "SET usd_balance = usd_balance +"+amount
			+ " WHERE id="+id;
	statement.executeUpdate(query);
	//grabs users table
		query ="Select*From Users where id=" +id;
		ResultSet rs = statement.executeQuery(query);
		rs.close();
	String answer="200 ok\n deposited "+amount+" New balance: "+rs.getDouble("usd_balance");
	return answer;
}

/*allows a user to logout, once logged out other users can login with the same id*/
public static String logout(Connection connection,String id) throws SQLException {
	Statement statement = connection.createStatement();
	String query;
	//updates the balance of the User 
	//total balance = old amount + new amount
	query="UPDATE Users\r\n"
			+ "SET isActive = 0"
			+ " WHERE id="+id;
	statement.executeUpdate(query);
	//grabs users table
		query ="Select*From Users where id=" +id;
		ResultSet rs = statement.executeQuery(query);
		rs.close();
	String answer="200 ok logged out";
	return answer;
}
/*checks that once logged in other users cannot login with the same id*/
public static String login(Connection connection,String id) throws SQLException
{
	//Init
		Statement statement = connection.createStatement();
		String query;
		//updates the balance of the User 
		//total balance = old amount + new amount
		query="UPDATE Users\r\n"
				+ "SET isActive = 1"
				+ " WHERE id="+id;
		statement.executeUpdate(query);
		//grabs users table
			query ="Select*From Users where id=" +id;
			ResultSet rs = statement.executeQuery(query);
		String answer="200 ok logged in";
		rs.close();
		return answer;
}
public static boolean isActive(Connection connection,String id) throws SQLException
{
	//Init
	Statement statement = connection.createStatement();
	String query ="Select*From Users where id=" +id;
	ResultSet rs = statement.executeQuery(query);
	if(rs.getInt("isActive")==1)	
	{
		rs.close();
		return true;
	}
	else
	{	
		rs.close();
		return false;
	}
}
//checks to see if the client needs to log back in
}//end Server class




