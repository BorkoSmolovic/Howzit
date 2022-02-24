package serverMain;

import com.example.howzit.messages.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

	/**
	 * This is a simple client. It can be used to check the server is working, but it is not intended to be deployed
	 */
public class TestClient {
	/**
	 * Socket TCP used to connect to the server
	 */
	private static Socket socket;
	/**
	 * Hardcoded port. TODO: move in a config file
	 */
	private static int serverPort = 2048; //TODO move in a config file
	/**
	 * Main method. Can be used to test the connection. Edit as you like
	 * @param args Not used at the moment
	 */
	public static void main(String[] args) {
		try {
			socket = new Socket("127.0.0.1", serverPort);//TODO customize socket
			
			//Setting up decorators
			
			BufferedReader inputConsole = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Streams created!");
			System.out.println(socket);
			
			//ObjectStream created
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            


			String ip, senderId,requestId;
			
			//Write some guideline in the user console
			System.out.println("Which type of message do you want to send?");
			System.out.println("1) SendIp");
			System.out.println("2) RecieveIp");
			
			String choice=inputConsole.readLine();
			if(choice.equals("1")){
				
				System.out.println("write your name:");
				senderId = inputConsole.readLine();
				System.out.println("write your ip:");
				ip=inputConsole.readLine();
				
				//Create PublishMessage and send to the server
				PublishMessage msg=new PublishMessage(senderId,ip);
				output.writeObject(msg);
				output.flush();
				
				System.out.println("I wrote to server: "+msg);
				
				//read Reply Message From server
				Message stringin =(Message) input.readObject();
				System.out.println("I recieve From Server: "+stringin);
			}
			else if(choice.equals("2")) {
				System.out.println("write the name of person to retrive ip:");
				requestId = inputConsole.readLine();
				//Create QueryMessage and send to the server
				QueryMessage msg = new QueryMessage(requestId);
				output.writeObject(msg);
				output.flush();
				

				System.out.println("I wrote to server: "+msg);
				//read Querry Reply Message From server
				QueryReplyMessage stringin =(QueryReplyMessage) input.readObject();
				System.out.println("I recieve From Server: "+stringin.getRequestedIP());
			}
			
			socket.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		

	}

}
