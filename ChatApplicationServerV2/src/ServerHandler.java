import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerHandler {
	//Class that handles the different Sockets for the connections. Starts the ServerListener in a different Thread and gets the connections accepted by the ServerSocket
		//Clients connecting through the ServerListener are initiated in a different Thread
		//Each Socket is passed to a ServerConnection Object that handles the communication with the client in a different thread
	//This class handles the communications performed between ServerConnections and information in common between them
	
	//Static: Shared by all the different ServerConnections
	
	//List to store all the ServerConnections (Online clients)
	private static ArrayList<ServerConnection> connectedList = new ArrayList<ServerConnection>();
	
	//Maps storing relevant data to perform operations between clients
		//These Maps are a relational structure of clients' communications. To whom they are talking, trying to talk or being tried to talk to
	//Structure of the Maps:
		// Key: clientName, Value: List of open chats for the client (name of the client in communication with stored)
	private static HashMap<String, ArrayList<String>> openChatsClients = new HashMap<String, ArrayList<String>>();
	//The values on these lists are deleted when the client accepts/rejects the request for chat
		//One client can ask multiple clients to chat, and being asked by others. Requests he makes are accepted/rejected, and he accepts/rejects the ones he receives
		// Key: clientName, Value: List of Requests to open a new chat with another client (name of the client to whom the request was made stored)
	private static HashMap<String, ArrayList<String>> openChatsRequestsClients = new HashMap<String, ArrayList<String>>();
		// Key: clientName, Value: List of Requested open chats from another client (name of the client whom made the request stored)
	private static HashMap<String, ArrayList<String>> openChatsRequestedClients = new HashMap<String, ArrayList<String>>();
	
	public ServerHandler() {
	}
	
	//This methodis called by the Initializator, instantiates and starts the new Thread to listen for connections. Always listening on a different Thread
	public ServerListener startListening(ServerSocket serverSocket) {
		ServerListener listener = new ServerListener(serverSocket);
		
		Thread listen = new Thread(listener);
		listen.start();
		return listener;
	}
	
	//This method is called by the ServerListener when a connection was successfully made. 
		//Gets the Socket with the communication and instantiates a ServerConnection with it as parameter
		//Starts the Object in a different Thread. Each connection in a different Thread
	public void addClient(Socket clientSocket) {
		ServerConnection clientConnection = new ServerConnection(clientSocket, false);
		Thread client = new Thread(clientConnection);
		client.start();
		
		//Add the connection to the List of connections to keep track of them
		connectedList.add(clientConnection);
	}
	
	//Special "contructor" for admin, priviledges for him/her
	public void addAdmin(Socket adminSocket) {
		ServerConnection adminConnection = new ServerConnection(adminSocket, true);
		Thread admin = new Thread(adminConnection);
		admin.start();
		
		//Add the connection to the List of connections to keep track of them
		connectedList.add(adminConnection);
	}
	
	//From here, all methods called from the ServerConnection objects
	
	//Method to check if the name chosen by the user is available (no repeated names) and add the client (now, with the name) to the Maps
		//Maps use the client name
		//If the name has been already chosen, give back and handle it on the ServerConnection
	public boolean nameIsAvailable(String name) {
		for(ServerConnection client : connectedList) {
			String nameClient = client.getName();
			if(name.equalsIgnoreCase(nameClient)) {
				return false;
			}
		}
		
		ArrayList<String> openChats = new ArrayList<String>();
		openChatsClients.put(name, openChats);
		ArrayList<String> RequestedClients = new ArrayList<String>();
		openChatsRequestedClients.put(name, RequestedClients);
		ArrayList<String> RequestsClients = new ArrayList<String>();
		openChatsRequestsClients.put(name, RequestsClients);
		
		return true;
	}
	
	//Method to loop over all Serverconnections and close sockets and Streams
	public void serverCloseUp() {
		for(ServerConnection user : this.connectedList) {
			user.serverCloseUp();
		}
		System.out.println("All Client's Socket and Streams Closed");
	}
	
	//Method to remove a client
		//Mainly on logoff from client in ServerConnection, but in some other cases too
		//Call a method to do a clean up of the client on the Maps (iterate over maps and the lists deleting its name on them)
	public void removeClient(ServerConnection clienConnection) {
		
		cleanUpClientDisconnected(clienConnection.getName());
		
		if(connectedList.contains(clienConnection)) {
			connectedList.remove(clienConnection);
		}
	}
	
	//Method to the cleanUp of the lists
		//Go to each list, check if the client is there, and if it is, go through all the lists he was in (they are all related) and delete it from them to not be accessible
	private void cleanUpClientDisconnected(String clientDisconnected) {
		//If the list of OpenChats contains the disconnected client
			//Get his list of open chats, go to the list of each of his Open Chats and remove the disconnected client from their lists
			//If it's an Open chat, is bidirectional, the name of the client will be in the OpenChats list of each client he has in its own list
		if(openChatsClients.containsKey(clientDisconnected)) {
			ArrayList<String> listOfOpenChats = openChatsClients.get(clientDisconnected);
			for(String openChatClient : listOfOpenChats) {
				if(openChatsClients.containsKey(openChatClient)) {
					openChatsClients.get(openChatClient).remove(clientDisconnected);
				}
			}
		}
		
		//If the list of Chat Requests contains the disconnected client
			//Get the list of RequestED of each client in the list and remove the disconnected client
			//This list is bidirectional in relation with the REQUESTED list, If a client requests a chat, will be in the list of requested chats for the users he requested to
				//If a name is in his list of the chat REQUEST, he will be in the list of the REQUESTED of these clients
		if(openChatsRequestsClients.containsKey(clientDisconnected)) {
			ArrayList<String> listOfChatRequesTS = openChatsRequestsClients.get(clientDisconnected);
			for(String RequestEDClient : listOfChatRequesTS) {
				if(openChatsRequestedClients.containsKey(RequestEDClient)) {
					openChatsRequestedClients.get(RequestEDClient).remove(clientDisconnected);
				}
			}
		}
		
		//If the list of Chat Requested contains the disconnected client
			//Get the list of RequesTS of each client in the list and remove the disconnected client
			//This list is bidirectional in relation with the REQUEST list, if a client requested a chat, will be in the list of chat requests for the users that requested him
				//If a name is in his list of the REQUESTED chats, he will be in the list of REQUESTS of those clients
		if(openChatsRequestedClients.containsKey(clientDisconnected)) {
			ArrayList<String> listOfClientWhoRequestED = openChatsRequestedClients.get(clientDisconnected);
			for(String clientWhoDidARequeST : listOfClientWhoRequestED) {
				if(openChatsRequestsClients.containsKey(clientWhoDidARequeST)) {
					openChatsRequestsClients.get(clientWhoDidARequeST).remove(clientDisconnected);
				}
			}
		}
	}
	
	//Method get the list of connected clients
		//Avoid nulls (clients connected but didn't pick a name yet)
		//Avoid himself
		//Store the names in an arraylist (from the list of ServerConnections) and send it back
	public ArrayList<String> getConnectedList(String requesterName) {		
		ArrayList<String> connectedClients = new ArrayList<String>();
		for(ServerConnection client : connectedList) {
			//Avoid NullPointerException, check if there's a null value in clientName before getting them
			if(!client.isLogged()) continue;
			//Do not show the user its own name in the list of connected people
			if(client.getName().equals(requesterName)) continue;
			connectedClients.add(client.getName());
		}
		return connectedClients;
	}
	
	//Method to get the ServerConnection of an specific client
		//The ServerConnection is needed to perform relational processes, like send a message to the client (get to his SendMessage() method)
	public ServerConnection getServerConnection(String clientName) {
		for(ServerConnection client : connectedList) {
			//Avoid NullPointerException, check if there's a value in clientName before getting them (Users that still didn't add a name)
			if(!client.isLogged()) continue;
			//Find the client
			if(client.getName().equals(clientName)) return client;
		}
		
		return null;
	}
	
	//These lists are in the ServerHandler and not in the individual ServerConnection to be able to get the data even if the Socket has been closed
		//If a client disconnects by closing the application instead of logging off, when an user tries to send him a message, it will be noted
			//The clean up process will begin, and having here the lists one can check in which lists delete his name without iterating through all
			//Maybe too much exposure for some process savings...
	
	//----------OPEN CHATS
	
	//Method to add a client to the List of the OpenChats of a client
		//Gets the Map key with the name of the client and adds to the list the name of the client the openchat is with
	public void addToOpenChats(String clientName, String newOpenChat) {
		openChatsClients.get(clientName).add(newOpenChat);
	}
	
	//Same as above but to remove an open chat
	public void removeFromOpenChats(String clientName, String removeOpenChat) {
		openChatsClients.get(clientName).remove(removeOpenChat);
	}
	
	//Method to give back a boolean to know if a client is part of the OpenChats of a client
	public boolean isOpenChat(String clientName, String clientToCheck) {
		if(openChatsClients.containsKey(clientName)) {
			if(openChatsClients.get(clientName).contains(clientToCheck)) {
				return true;
			}else {
				return false;
			}
		}
		return false;
	}
	
	//Method to give back the list of all Open Chats of a client
	public ArrayList<String> getOpenChats(String clientName) {
		if(openChatsClients.containsKey(clientName)) {
			return openChatsClients.get(clientName);
		}else {
			return null;
		}
		
	}
	
	//----------REQUESTS
	
	//Method to add a client to the list of RequesTS of a client
	public void addToOpenChatsRequests(String clientName, String newChatRequest) {
		openChatsRequestsClients.get(clientName).add(newChatRequest);
	}
	
	//Method to remove a client from the list of RequesTS of a client
	public void removeFromOpenChatsRequests(String clientName, String removeChatRequest) {
		openChatsRequestsClients.get(clientName).remove(removeChatRequest);
	}
	
	//Method to give back a boolean is the client is or not in the list of RequesTS of a client
	public boolean openChatsRequestsContains(String clientName, String clientNameToCheck) {
		if(openChatsRequestsClients.get(clientName).contains(clientNameToCheck)) {
			return true;
		}else {
			return false;
		}
	}
	
	//Method to get the list of RequesTS chats of a client
	public ArrayList<String> getOpenChatsRequests(String clientName) {
		return openChatsRequestsClients.get(clientName);
	}
	
	//----------REQUESTED
	
	//Method to add a client to the list of RequestED chats of a client
	public void addToOpenChatsRequested(String clientName, String newChatRequested) {
		openChatsRequestedClients.get(clientName).add(newChatRequested);
	}
	
	//Method to remove a client from the list of RequestED chats of a client
	public void removeFromOpenChatsRequested(String clientName, String removeChatRequested) {
		openChatsRequestedClients.get(clientName).remove(removeChatRequested);
	}
	
	//Method to know if a client is in the list of RequestED chats of a client
	public boolean openChatsRequestedContains(String clientName, String clientNameToCheck) {
		if(openChatsRequestedClients.get(clientName).contains(clientNameToCheck)) {
			return true;
		}else {
			return false;
		}
	}
	
	//Method to get the list of RequestED chats of a client
	public ArrayList<String> getOpenChatsRequested(String clientName) {
		return openChatsRequestedClients.get(clientName);
	}
		
}
