package serverMain;

import com.example.howzit.messages.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;


/**
 * <p>This class acts as the discovery server for the Howzit application.</p>
 * 
 * <p>The server is single threaded. Therefore, all of its logic is contained in this class.
 * The server uses the JSON file format to store data in a file.</p>
 * 
 * @author Alessandro, Matteo
 *
 */

 //prova
public class Main {
	/**
	 * HashMap containing the pairs &lt;ID, address&gt; of the users.
	 */
	private static HashMap<String, String> addressBook;
	/**
	 * Hardcoded port on which the server is listening. TODO: move it in a config file.
	 */
	private static int port = 2048;//TODO move in a config file
	/**
	 * Hardcoded filepath of the database JSON file. TODO: move it in a config file
	 */
	//private static String databaseFilePath = "C:\\Users\\Il Cesna\\Documents\\howzit\\server\\database.json";
	private static String databaseFilePath = "C:\\Users\\iacop\\AndroidStudioProjects\\howzit\\server\\database.json";
	/**
	 * ServerSocket TCP used to accept connections.
	 */
	private static ServerSocket serverSocket;
	/**
	 * Hardcoded variable to properly handle JSON. I really don't know why it is there
	 */
	public final static Charset charset = Charset.forName("US-ASCII");
	
	
	
	
	/**
	 * This method reads from file json the content of the internal address book
	 * 
	 * @param filePath Name of the file JSON of the database
	 * 
	 * @return true if the reading succeeded, false if an error occurred
	 */
	private static boolean readDatabase(String filePath) {

		System.out.println("Reading database " + filePath + "...");
		try {
			BufferedReader databaseIn = new BufferedReader(new FileReader(filePath));
			JSONTokener jsont = new JSONTokener(databaseIn);
			JSONObject jsono = new JSONObject(jsont);
			Set<String> keys = jsono.keySet();
			for(String key : keys) {
				addressBook.put(key, jsono.getString(key));
			}
			System.out.println("Database read!");
			databaseIn.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println("Error reading database");
		return false;
	}
	
	
	/**
	 * <p>This method saves the content of the internal address book to a file.</p>
	 * <p>A boolean value is returned indicating success or failure of the operation</p>
	 * 
	 * @param filePath Path of the file JSON of the database
	 * 
	 * @return true if the writing succeeded, false if an error occurred
	 */
	private static boolean writeDatabase(String filePath) {
		System.out.println("Writing database to " + filePath + "...");
		try {
			BufferedWriter databaseOut = new BufferedWriter(new FileWriter(filePath));
			JSONWriter jsonw = new JSONWriter(databaseOut);
			jsonw.object();
			Set<Map.Entry<String, String>> addressSet = addressBook.entrySet();
			for(Map.Entry<String, String> entry : addressSet) {
				jsonw.key(entry.getKey()).value(entry.getValue());
			}
			jsonw.endObject();
			System.out.println("Database wrote");
			databaseOut.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println("Error during database writing");
		return false;
	}

	/**
	 * Main method. Right now, it creates the server and the socket connection. It performs the exchange of object between the client
	 * @param args Not used right now
	 */
	public static void main(String[] args) {
		
		System.out.println("Starting server...");		
		addressBook = new HashMap<String, String>();
		readDatabase(databaseFilePath);
		
		try{
			serverSocket = new ServerSocket(port);
			System.out.println("ServerSocket created");
		} catch(IOException e){
			e.printStackTrace();
			System.out.println("Error while creating the ServerSocket");
			return;
		}
		
		System.out.println("Server started, accepting connection");
		while(true) {
			try {
				
				System.out.println("Server Ready to accept connection...");
				
				//Accepting the connection
				Socket socket = serverSocket.accept();
				System.out.println("Connection accepted");
				
				//Setting up Stream to read the Object on Socket				
				ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

				//Now server can read
				System.out.println("Streams created!");
				System.out.println("");
				
				Message instring = (Message) input.readObject();	
				
				
				//If message recieved is of type 0 : TEXT
				if(instring.getType()==0) {
					
					//Server code if recieve a text message
					
				}
				//If message recieved is of type 1 :PUBLISH
				else if(instring.getType()==1)  {
					//Create PublishMessage readed from the Stream
					PublishMessage msg=(PublishMessage) instring;
					
					//add the contact in the hashmap and then write in the database
					addressBook.put(msg.getSenderID(), msg.getSenderIP());
					writeDatabase(databaseFilePath);
					System.out.println("è arrivato");
					
					//Create a SimpleMessage
					TextMessage reply=new TextMessage(0, 0, "Hello "+msg.getSenderID()+" your Ip is saved in the DB\n");

					output.writeObject(reply);
					output.flush();  	
				}
				//If message recieved is of type 2 :QUERY
				else if(instring.getType()==2)  {
					//Create QueryMessage readed from the Stream
					QueryMessage msg=(QueryMessage) instring;
					
					//Search Ip in the hashmap
					String ip=addressBook.get(msg.getRequestedID());
					
					//Create a QueryReplyMessage
					QueryReplyMessage reply= new QueryReplyMessage(msg.getRequestedID(),ip);

					output.writeObject(reply);
					output.flush(); 	
				}

				System.out.println("Released connection");
				} catch(IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
		}

	}

}

