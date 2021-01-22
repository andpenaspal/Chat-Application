import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class ServerConnection implements Runnable{
	
	//Class to Store and handle the ClientConnections: Sockets and communications
		//Listen to commands and process the petitions
		//Links to ServerHandler for inter-connection processes
		//Storage of data in the ServerHandler
	
	/*
	 * Flow of operation:
	 * User logs in
	 * Can see the list of online users
	 * Chat: To be able to chat with an user, the user needs to:
	 * 		- Request a Chat to an online user
	 * 		- The request need to be accepted
	 * Users can see the list of chats requested that hasn't been yet answered
	 * Users can see the list of chat requests other users made and can accept or reject it
	 * Once an user accepts or gets accepted, the chat will be open
	 * Users can see the list of open chats
	 * Users can send and receive messages from open chats
	 * Users can remove the open chat with another user, communication will be then closed between them
	 * */
	
	//Storage of main data of the class	
	private final Socket clientSocket;
	private final ServerHandler serverHandler;
	private boolean isLogged;
	private String clientName;
	private OutputStream outputStream;
	//private PrintWriter outputStream;
	private InputStream inputStream;
	
	//Variable for the Admin priviledges........
	private final boolean isAdmin;
	
	//Constructor
		//isLogged to false -> Change on logged correctly, infinite loop to log in
	public ServerConnection(Socket clientSocket, boolean isAdmin) {
		this.clientSocket = clientSocket;
		this.serverHandler = new ServerHandler();
		this.isLogged = false;
		this.isAdmin = isAdmin;
	}
	
	//Start the run
	@Override
	public void run() {
		clientListener();		
	}
	
	private void clientListener() {
		try {
			//Open Streams for the socket, get a buffer to read the whole line
			this.inputStream = clientSocket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			this.outputStream = clientSocket.getOutputStream();
			//this.outputStream = new PrintWriter(this.clientSocket.getOutputStream());
			
			//Variable to store the Input
				//All input to uppercase to make it more user-friendly
			String line;
			//Welcoming message
			String helloMessage = "\r\n----> Welcome to the Chat <-----\r\n\r\nPlease, Introduce your Username - No blanck spaces allowed in the username";
			
			//Inifnite loop to force the login
			while(!this.isLogged) {
				//Handle Exception if User disconnects abruptly on login Menu
				try {
					sendMsg(helloMessage);
					line = reader.readLine();			
					//String[] tokens = line.split(" ");
					//if(!tokens[0].equalsIgnoreCase("login")) {
						//sendMsg("\r\n-----> Command not recognized. Example of in put: \"login patrick\"\r\n");
					//}
					//Handle Quit and Help
					if(line.equals("\\q")) {
						//Close all properly
						closeUp();
						return;
					}
					if(line.equals("\\h")) handleSimpleHelp();
					if(line.equals("help")) handleHelp();
					//Send to method to validate the Username
					handleLogin(line);
				}catch (Exception e) {
					//On abrupt close down, close all properly
					closeUp();
					return;
				}
			}
			
			//Send notice of Online to everybody
			sendNoticeToAll(true);
			
			//Inifnite loop toi read from the Stream
			socketLoop: while((line = reader.readLine()) != null) {
				//Split the input in tokens for the Switch
				String[] tokens = line.split(" ", 3);
				
				String cmd = tokens[0];
				String cmd2 = "";
				if(tokens.length > 1) cmd2 = tokens[1];
				
				if(tokens != null && tokens.length > 0) {
					//Command to Quit
					if(cmd.equalsIgnoreCase("\\q")) {
						handleLoggedOff();
						//Break the infinite loop to close streams and socket
						break socketLoop;
					//Command to log in
					//}else if(cmd.equalsIgnoreCase("login")) {
						//handleLogin(tokens);
					//Command to get online users
					}else if(cmd.equalsIgnoreCase("getOnline")) {
						handleGetOnline();
					//Command to access the "chat" Switch
					}else if(cmd.equalsIgnoreCase("chat")) {
						handleChat(tokens);
					//Command to send a message
					}else if(cmd.equalsIgnoreCase("msg")) {
						handleMessage(tokens);
					//Commands to get help
					}else if(cmd.equalsIgnoreCase("\\h")) {
						handleSimpleHelp();
					}else if(cmd.equalsIgnoreCase("help")) {
						handleHelp();
					//If nothing, something wrong
					}else if(cmd.equalsIgnoreCase("\\a") && cmd2.equalsIgnoreCase("brdc")) {
						handleAdminMessages(tokens);
					}else {
						sendMsg("[] Command not recognized. \r\n[] Enter \"\\h\" for a list of commands or \"help\" for a list of commands with explanations");
					}
				}
			}
			//Method to close Streams and Socket
			closeUp();
			
		} catch (IOException e) {	
			try {
				closeUp();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//e.printStackTrace();
		}
	}
	
	//Method to give the Name of the Username of this ServerConnection
	public String getName() {
		return clientName;
	}
	//Method to check if the client is logged in or not
	public boolean isLogged() {
		return this.isLogged;
	}
	//Method to close Streams and Socket
	private void closeUp() throws IOException {
		
		this.inputStream.close();
		this.outputStream.close();
		this.clientSocket.close();
		if(this.clientName != null) {
			serverHandler.removeClient(this);
			sendNoticeToAll(false);
			System.out.println("Disconnected from " + this.clientName + " With connection: " + this.clientSocket);
		}else {
			System.out.println("Disconnected from Guest With connection: " + this.clientSocket);
		}
	}
	
	//Method to send every Online user a notice about this user connecting/disconnecting
	private void sendNoticeToAll(boolean isOnline) throws IOException {
		String msg = "[] ";
		if(isOnline) msg += "Online: ";
		if(!isOnline) msg += "Offline: ";
		msg += this.clientName;
		ArrayList<String> listOnlineUsers = serverHandler.getConnectedList(this.clientName);
		for(String onlineUser : listOnlineUsers) {
			ServerConnection connection = serverHandler.getServerConnection(onlineUser);
			connection.sendMsg(msg);
		}
	}
	
	//Method to perform the login validation
		//Check if the name is available from the list of connected users (no repetition allowed)
	private void handleLogin(String username) throws IOException {
		//"login: " + clientName + " loginOK"
		String msg = "login: ";
		if(username.length() >= 3) {
			if(!username.contains(" ")) {
				if(serverHandler.nameIsAvailable(username)) {
					this.clientName = username;
					this.isLogged = true;
					msg += username + " loginOK";
					sendMsg(msg);				
				}else {
					msg += "loginERROR --> Name already in use, try again";
					sendMsg(msg);
				}
			}else {
				msg += "loginERROR --> No blankspaces allowed in the username";
				sendMsg(msg);
			}
		}else {
			msg += "loginERROR --> Username cannot be shorter than 3 chars";
			sendMsg(msg);
		}
		
	}
	
	//Method to remove a client from the list of connected users
		//Calls removeClient in ServerHandler to remove the client from all the lists/maps
	private void handleLoggedOff() throws IOException {
		String msg = "[] ";
		if(this.isLogged) {
			msg = this.clientName;
		}else {
			msg = "Guest";
		}
		
		msg += " has been successfully logged off\r\nSee you soon!";
		sendMsg(msg);
		//Will break the Listener Infinite loop and call closeUP() after it
		//serverHandler.removeClient(this);
	}
	
	//Called when closing the server, loop from handler to close all ServerConnections Streams and socket
	public void serverCloseUp() {
		try {
			this.inputStream.close();
			this.outputStream.close();
			this.clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Method to get the connected users and format the list
	private void handleGetOnline() throws IOException {
		ArrayList<String> connectedClients = serverHandler.getConnectedList(this.clientName);
				
		String msg = "[] ";
		
		if(connectedClients.size() == 0) {
			msg += "No online Users";
		}else {
			msg += "Online users: ";
			for(String clientName : connectedClients) {
				msg += clientName;
				msg += " ";
			}
		}

		sendMsg(msg);
	}
	
	//Method to handle the "chat" command
		//Switch to the different options
	private void handleChat(String[] tokens) throws IOException {
		
		if(tokens.length > 1) {
		
			String cmd = tokens[1];
			
			if(cmd.equalsIgnoreCase("OPENCHATS")) {
				handleOpenChats();
			}else if(cmd.equalsIgnoreCase("REQUESTEDCHATS")){
				handleRequestedChats();
			}else if(cmd.equalsIgnoreCase("REQUESTCHAT")){
				//Validation of the commands, needs to have 3 tokens
				if(tokens.length > 2) {
					handleRequestChat(tokens[2]);
				}else {
					String msg = "[] requestchat: Error on Command";
					sendMsg(msg);
				}
			}else if(cmd.equalsIgnoreCase("REMOVECHAT")){
				if(tokens.length > 2) {
					handleRemoveChat(tokens[2]);
				}else {
					String msg = "[] removeChat: Error on Command";
					sendMsg(msg);
				}
			}else if(cmd.equalsIgnoreCase("CHATREQUESTS")){
				handleChatRequests();
			}else if(cmd.equalsIgnoreCase("ACCEPTCHAT")){
				if(tokens.length > 2) {
					handleAcceptChat(tokens[2]);
				}else {
					String msg = "[] acceptChat: Error on Command";
					sendMsg(msg);
				}
			}else if(cmd.equalsIgnoreCase("REJECTCHAT")){
				if(tokens.length > 2) {
					handleRejectChat(tokens[2]);
				}else {
					String msg = "[] rejectChat: Error on Command";
					sendMsg(msg);
				}
			}else {
				sendMsg("[] Command not recognized. \r\n[] Enter \"\\h\" for a list of commands or \"help\" for a list of commands with explanations\r\n");
			}
		}else {
			sendMsg("[] Command not recognized. \r\n[] Enter \"\\h\" for a list of commands or \"help\" for a list of commands with explanations\r\n");
		}
	}
	
	//Method to send back the list of open chats
	private void handleOpenChats() throws IOException {
		String msg = "[]OpenChats: ";
		
		if(serverHandler.getOpenChats(this.clientName).size() != 0) {
			for(String connection : serverHandler.getOpenChats(this.clientName)) {
				msg += connection;
				msg += " ";
			}
		}else {
			msg += "No Open Chats";
		}
		sendMsg(msg);
	}
	
	//Method to send back the list of Requested chats (not answered yet)
	private void handleRequestedChats() throws IOException {
		String msg = "[] Pending chat requests: ";
		
		for(String requestedName : serverHandler.getOpenChatsRequested(this.clientName)) {
			msg += requestedName;
			msg += " ";
		}
		
		if(serverHandler.getOpenChatsRequested(this.clientName).size() == 0) msg += "No Requested Chats";
		
		sendMsg(msg);
	}
	
	//Method to make a request for chat
	private void handleRequestChat(String clientNameToRequest) throws IOException {
		
		String msg = "[] ";
		
		if(serverHandler.isOpenChat(this.clientName, clientNameToRequest)) {
			msg += clientNameToRequest + " already in Open Chats";
		}else if(clientNameToRequest.equals(this.clientName)) {
			msg += "You cannot chat with yourself...";
		}else if(isOnline(clientNameToRequest)) {
			//Add to the list on the ServerHandler
			serverHandler.addToOpenChatsRequested(this.clientName, clientNameToRequest);
			serverHandler.addToOpenChatsRequests(clientNameToRequest, this.clientName);
			
			msg += "Chat request sent to " + clientNameToRequest;
			ServerConnection clientRequested = serverHandler.getServerConnection(clientNameToRequest);
			String msg2 = "[] New chat request from " + this.clientName;
			clientRequested.sendMsg(msg2);
		}else {
			 msg += clientNameToRequest + " is Offline";
		}
		
		sendMsg(msg);
		
		
	}

	//Method to remove chat from openchats
	private void handleRemoveChat(String clientNameToRemove) throws IOException {
		
		if(serverHandler.isOpenChat(this.clientName, clientNameToRemove)){
			serverHandler.removeFromOpenChats(this.clientName, clientNameToRemove);
			serverHandler.removeFromOpenChats(clientNameToRemove, this.clientName);
			
			ServerConnection connectionClientToRemove = serverHandler.getServerConnection(clientNameToRemove);
			String msg = "[] " + this.clientName + " has removed your chat from open chats";
			connectionClientToRemove.sendMsg(msg);
			
			String msg2 = "[] " + clientNameToRemove + " removed from open chats";
			sendMsg(msg2);
		}else {
			String msg = "[] " + clientNameToRemove + " is not an open chat";
			sendMsg(msg);
		}
		
	}
	
	//Method to handle the chat requests
	private void handleChatRequests() throws IOException {
		String msg = "[] ";
		if(serverHandler.getOpenChatsRequests(this.clientName).size() == 0) {
			msg += "No Chat Requests from other users";
		}else {
			msg += "Chat requests from: ";
			
			for(String request : serverHandler.getOpenChatsRequests(this.clientName)) {
				msg += request;
				msg += " ";
			}
		}
		
		sendMsg(msg);
	}
	//Method to handle chat request accepted
	private void handleAcceptChat(String clientNameToAccept) throws IOException {
		if(isOnline(clientNameToAccept) && serverHandler.openChatsRequestsContains(this.clientName, clientNameToAccept)) {
			
			//On chat request accepted, delete the name of the requester of the list of requesTS and add it to open chats
			serverHandler.removeFromOpenChatsRequests(this.clientName, clientNameToAccept);
			serverHandler.addToOpenChats(this.clientName, clientNameToAccept);
			
			//On chat request accepted, delete the name of the one who accepted of the list of requestED and add it to open chats (list of the one made the request)
			serverHandler.removeFromOpenChatsRequested(clientNameToAccept, this.clientName);
			serverHandler.addToOpenChats(clientNameToAccept, this.clientName);
			
			//Get the one who requested and send a message letting now it has been accepted
			ServerConnection acceptedClient = serverHandler.getServerConnection(clientNameToAccept);
			String msg = "[]ChatRequest: " + this.clientName + " accepted your chat request";
			acceptedClient.sendMsg(msg);
			
			//Send a message to the one who accepted confirming it
			String msg2 = "[]ChatRequest: " + clientNameToAccept + " chat request accepted";
			sendMsg(msg2);
		}else {
			String msg = "[] " + clientNameToAccept + " did not request a chat";
			sendMsg(msg);
		}
	}
	
	//Method to handle rejected chat requests
	private void handleRejectChat(String clientToReject) throws IOException {
		if(isOnline(clientToReject) && serverHandler.openChatsRequestsContains(this.clientName, clientToReject)){
			
			
			//Remove from the list of Request the one that has been rejected
			serverHandler.removeFromOpenChatsRequests(this.clientName, clientToReject);
			//Remove the rejector from the list of requestED of the one who has been rejected
			serverHandler.removeFromOpenChatsRequested(clientToReject, this.clientName);
			
			ServerConnection rejectedClient = serverHandler.getServerConnection(clientToReject);
			String msg = "[] The request of chat with " + this.clientName + " has been rejected";
			rejectedClient.sendMsg(msg);
			
			
			String msg2 = "[] Request from " + clientToReject + " rejected";
			sendMsg(msg2);
		}else {
			String msg = "[] " + clientToReject + " did not request a chat";
			sendMsg(msg);
		}
	}
		
	
	//Method to handle messages
	private void handleMessage(String[] tokens) throws IOException {
		if(tokens.length > 2) {
			String receiver = tokens[1];
			String body = tokens[2];
			
			String msg = "msg> ";
			
			if(isOnline(receiver)) {
				if(serverHandler.isOpenChat(this.clientName, receiver)){
					ServerConnection receiverConnection = serverHandler.getServerConnection(receiver);
					try {
						msg += this.clientName + " --> " + body;
											
						receiverConnection.sendMsg(msg);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (NullPointerException e2) {
						//NOT add the "[] ", this messages get to the chat room
						msg += receiver + " --> [] Message cannot be sent, " + receiver + " is Offline";
						sendMsg(msg);
						serverHandler.removeClient(receiverConnection);
					}
				}else {
					msg += receiver + " --> [] Message cannot be sent, " + receiver + " is not an Open Chat";
					sendMsg(msg);
				}
			}else if(receiver.equalsIgnoreCase(this.clientName)) {
				msg += receiver + " --> [] You cannot chat with yourself";
				sendMsg(msg);
			}else {
				msg += receiver + " --> [] Message cannot be sent, " + receiver + " is Offline";
				sendMsg(msg);
			}
			
		}else {
			String msg = "[] Receiver not specified";
			sendMsg(msg);
		}
	}
	
	//Method to handle Broadcast from the Admin
	private void handleAdminMessages(String[] tokens) throws IOException {
		
		String msg;
		
		if(tokens.length > 2) {
			msg = tokens[2];
		}else {
			return;
		}
		//If it is admin (variable on constructor from serverHandler), broadcast
		if(this.isAdmin) {
			ArrayList<String> connectedList = serverHandler.getConnectedList(this.clientName);
			for(String connectedUser : connectedList) {
				ServerConnection connectionUser = serverHandler.getServerConnection(connectedUser);
				connectionUser.sendMsg("[] [AdminBrdC] " + msg);
			}
		}else {
			System.err.println("----------> " + this.clientName + " is trying to use Admin Features");
			System.err.println("IP: " + this.clientSocket.getInetAddress().getHostAddress());
		}
	}
	
	//Method to check if an user is online (list on serverhandler)
	private boolean isOnline(String clientName) {
		ArrayList<String> connectedClients = serverHandler.getConnectedList(this.clientName);
		
		if(connectedClients.contains(clientName)) {
			return true;
		}else {
			return false;
		}
	}
	
	//Method to send through the stream
	private synchronized void sendMsg(String msg) throws IOException {
		msg += "\r\n";
		
		this.outputStream.write(msg.getBytes());
	}
	
	//Mthod to handle Help
	private void handleHelp() throws IOException {
		String msg = "[] Help: List of commands and their function\r\n"
				+ "Structure of the list: [command] 'user input'\r\n\r\n\r\n"
				+ "[\\h]: Shows a list of available commands\r\n\r\n"
				+ "[help]: Shows a list of available commands and their function\r\n\r\n"
				+ "[\\q]: Exit the application\r\n\r\n"
				+ "[getOnline]: Get a list of the Users that are currently online\r\n\r\n"
				+ "[msg] 'ReceiverOfTheMessage' 'Body of the message': Send a message to the specified user. "
				+ "\r\n          The receiver needs to be in the list of Open Chats of the user sending the message."
				+ "\r\n          Eg: \"msg Andres Hello World!\": Will send the message \"Hello World!\" to Andres\r\n\r\n"
				+ "[chat] [openChats]: Get a list of Open Chats with online users\r\n\r\n"
				+ "[chat] [requestChat] 'username': Request a chat to the user specified. The user to whom the"
				+ "\r\n          the request has been sent can accept or reject the request\r\n\r\n"
				+ "[chat] [requestedChats]: Get a list of request made and not answered yet\r\n\r\n"
				+ "[chat] [removeChat] 'username': Removes an Open Chat. The user will not be ablo to send or receive"
				+ "\r\n          messages to/from the user\r\n\r\n"
				+ "[chat] [chatRequests]: Get a list of chat requests. Requests made by other users to chat to you\r\n\r\n"
				+ "[chat] [acceptChat] 'username': Accepts the chat request made by the user specified. A request "
				+ "\r\n          needs to have been made to be accepted\r\n\r\n"
				+ "[chat] [rejectChat] 'username': Reject the chat request made by the user specified. The user will"
				+ "\r\n          not be able to contact with or you with him/her";
		sendMsg(msg);
	}
	
	private void handleSimpleHelp() throws IOException {
		String msg = "[] Help: List of commands\r\n"
				+ "Structure of the list: [command] 'user input'\r\n\r\n"
				+ "[\\h]\r\n"
				+ "[help]\r\n"
				+ "[\\q]\r\n"
				+ "[getOnline]\r\n"
				+ "[msg] 'ReceiverOfTheMessage' 'Body of the message'\r\n"
				+ "[chat] [openChats]\r\n"
				+ "[chat] [requestChat] 'username'\r\n"
				+ "[chat] [requestedChats]\r\n"
				+ "[chat] [removeChat] 'username'\r\n"
				+ "[chat] [chatRequests]\r\n"
				+ "[chat] [acceptChat] 'username'\r\n"
				+ "[chat] [rejectChat] 'username'";
		sendMsg(msg);
	}

}
