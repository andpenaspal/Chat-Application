import java.io.*;
import java.net.*;

public class ServerListener implements Runnable{
	//This class is the Listener
		//Separate Thread that is always listening for connections (infinite loop)
		//New connections (sockets) handed to the ServerHandler
	
	private final ServerSocket SERVERSOCKET;
	private final ServerHandler serverhandler;
	//Flag to avoid the Exception closing the ServerSocket............
	private boolean closerServerSocket = false;
	
	//Get the port number to listen to
	public ServerListener(ServerSocket serverSocket) {
		this.SERVERSOCKET = serverSocket;
		this.serverhandler = new ServerHandler();
	}
	
	//ServerListener, listen for new connections
		//If the new connection is local, is the Admin (same IP as Server), create
	public void run() {
		try {
			//ServerSocket serverSocket = new ServerSocket(serverPort);
			while(true) {
				System.out.println("\nWaiting for connections...\n");
				Socket clientSocket = this.SERVERSOCKET.accept();
				System.out.println("\nConnected to: " + clientSocket);
				
				//If the IP of the Socket is the same as the server, especial "constructor" on serverHandler to give admin priviledges
				if(clientSocket.getInetAddress().getHostAddress().equals("127.0.0.1")) {
					serverhandler.addAdmin(clientSocket);
				}else {
					serverhandler.addClient(clientSocket);
				}				
			}
		}catch (IOException e) {
			if(!this.closerServerSocket) {
				e.printStackTrace();
			}
		}
	}
	
	//Method to close down the ServerSocket (called from ServeradminSocket)
		//Calls the handler to close all scokets and streams
	public void shutDown() {
		try {
			//Flag to avoid exception...................
			this.closerServerSocket = true;
			this.SERVERSOCKET.close();
			this.serverhandler.serverCloseUp();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
