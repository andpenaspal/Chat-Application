import java.io.*;
import java.net.*;

public class ServerAdminSocket implements Runnable{
	
	/*
	 * This class handles the Admin Socket from the Server (Project specifications, Server able to talk...)
	 * Creates a normal Socket (On method, not on constructor) on demand, and lets the Admin on the Server Application to chat
	 * Admin can join or leave the chat not affecting the Server
	 * Admin can close down the server from here
	 * */
	
	//Intance Variables
	private Socket adminSocket;
	private ServerListener listener;
	private final int PORTNUMBER;
	private BufferedReader inputStream;
	private BufferedReader inputConsole;
	private PrintWriter out;
	
	//Constructor to initialize the variables
	public ServerAdminSocket(int portNumber, ServerListener listener){
		this.PORTNUMBER = portNumber;
		this.listener = listener;
		this.inputConsole = new BufferedReader(new InputStreamReader(System.in));
	}
	
	//Called from Initializator after setting up the server
	//Admin console, lets the admin Join/Leave the Chat and close the server properly
		//Commands:
			//"\c" to create the socket and join the chat as a normal user with Broadcast possibilities
			//"\q" to SHUT DOWN THE SERVER <----------- CAREFULL
			//"\q" ON CHAT, will close the Chat session but not the server
	public void addAdminSocket() {
		while(true) {
			try {
				System.out.println("\n\n-----> Welcome to the Admin Console <-----\n");
				System.out.println("Press \"\\c\" to Create a Socket and join the Chat");
				System.out.println("Press \"\\q\" for: \n     On Chat Application: close the socket and leave the Chat\n     On Admin Console: Shut down the Server and close the application\n");
				System.out.println("-----> Special Admin features on Chat: Enter the command \"[\\a] [brdc] 'textMessage'\" to broadcast the 'textMessage'\n");
				
				String input = this.inputConsole.readLine();
				
				//"\c" will create the socket, initiate the Chat Console and start listening on another Thread (run on same class)
				if(input.equalsIgnoreCase("\\c")) {
					createSocket();
					Thread chatSocketListener = new Thread(this);
					chatSocketListener.start();
					chatConsole();
				//Quit here will SHUTDOWN THE SERVER <---------- CAUTION
					//Will close the ServerSocket listening for connection, close the Stream reading from the console
						//From the listener, the serverHandler is called to close all Client Sockets and Streams
				}else if(input.equalsIgnoreCase("\\q")) {
					listener.shutDown();
					System.out.println("Server Socket Closed");
					this.inputConsole.close();
					System.out.println("Application Closed\nSee you soon!");
					System.exit(0);
				}
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//Method to handle the chatConsole of the Admin. Send Input to the socket on the serverside (ServerConnection)
		//Validation to close this socket (client side socket, this one) and goes back to the Admin console
	private void chatConsole() {
		String userInput;
		
		try {
				    
			while ((userInput = inputConsole.readLine()) != null) {
				if(userInput.equalsIgnoreCase("\\q")) {
					closeSocket();
					return;
				}else {
					out.println(userInput);
				}   
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//Method to close the Client-side socket (this one), send QUIT to the server-side socket to close all there and close this socket
	private void closeSocket() {
		
		this.out.println("\\q");
		System.out.println("Admin Chat Session Closed");
		
		try {
			this.inputStream.close(); //(!) See catch on Run
			this.out.close();
			this.adminSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Method to create the client-side socket on demand "\c" on admin console
	private void createSocket() {
		try {
			adminSocket = new Socket("localhost", this.PORTNUMBER);
			this.out = new PrintWriter(adminSocket.getOutputStream(), true);
			this.inputStream = new BufferedReader(new InputStreamReader(adminSocket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Method to listen to the server-socket on the background
	@Override
	public void run() {
		String line;
		
		try {
			while((line = inputStream.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			//REALLY BAD PRACTICE, Don't know how to fix it:
				//Closing the Stream on Closing (Leaving the chat App, but not the server) will trigger it. See (!)
				//Avoided on the ServerSockiet Listner with a Flag, but don't like it either
			//e.printStackTrace();
		}
		
	}
	
}
