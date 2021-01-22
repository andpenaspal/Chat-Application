import java.io.*;
import java.net.*;
import java.util.*;

public class ChatHandler implements Runnable{
	
	/*
	 * This class handles interaction with the server and returns to Menu or outputs on console after processing the data
	 * It handles all internal values for saving unread messages and system messages when on chatroom
	 * Listens to the server on a separate Thread (started on Menu)
	 * 
	 * This class listens from the server:
	 * 		if the user is in main console, just print system messages but save all messages from openchats
	 * 		if the user in an chatroom, print messages from the user the chatroom is with, but save all other messages (system or other chats)
	 * 		if an user disconnects but there are unread messages, save them, delete openchat after read
	 * */
	
	//Variables for the Socket
	private Socket clientSocket;
	private OutputStream outputStream;
	private InputStream inputStream;
	private BufferedReader reader;
	
	//Variables to handle internal data
		//Map to track the username and unread messages (only of openchats)
	private HashMap<String, ArrayList<String>> openChats;
		//Map to track if an user is online or not
	private HashMap<String, Boolean> reachable;
		//Map to track if there are unread messages
	private HashMap<String, Boolean> newMessages;
		//List to store System messages while on chatroom
	private ArrayList<String> systemMsg;
		//Flag to check if there is an chatroom active (value, username of the chatroom or "")
	private String openChatRoomActive;
	
	//Method to create the socket, return boolean on success/fail
	public boolean createConnection(String ip, int portNumber) {
		try {
			this.clientSocket = new Socket(ip, portNumber);
			return true;
		} catch (IOException e) {
			System.out.println("\n-----> Impossible to connect to the Server <-----\n-----> Double check the IP Address and Port Number <-----");
			return false;
		}
	}

	//Listen on the background
		//Four variables from the server:
			//[]OpenChats: regulation of openchats. Compare current and new list, save Offline (not new list) if messages unread, delete if not unread messages,
				//or just update if new openchats. The user asks for it, for the list of openchats
			//[]ChatRequest: that means is a new Openchat. []Openchats will only check the list stored on server (basically double check and output to show new messages/offline)
				//[]ChatRequest will indicate new chats, add to the lists with pertinent values (sent on accepting or accepted new chat requests)
			//msg: new messages, check if its in active chatroom or store them in the list of the sender (Map)
			//[]: Other system messages (online, offline, chat requests, broadcasts...)
				//If it is Offline, check if the user can be deleted from lists (no unread messages) or just shown as not reachable with unread messages
	@Override
	public void run() {
		try {
			String line;
			
			while((line = reader.readLine()) != null) {
				
				String[] tokens = line.split(" ", 2);
				
				if(tokens.length > 0) {
					if(tokens[0].equalsIgnoreCase("[]OpenChats:")) {
						handleOpenChats(tokens[1]);
					}else if(tokens[0].equalsIgnoreCase("[]ChatRequest:")) {
						handleNewOpenChat(tokens[1]);
					}else if(tokens[0].equalsIgnoreCase("msg>")){
						handleMsg(tokens[1]);
					}else if(tokens[0].equalsIgnoreCase("[]")) {
						handleSystemMsg(tokens[1]);
					}else {
						//Shouldn't get here, but if not catched, show it
						String msg = "";
						for(String z : tokens) {
							msg += z;
						}
						System.out.println(msg);
					}
				}				
			}
		} catch (IOException e) {
			//Exception if Server closes the socket or goes down abruptly (shouldn't happen, only if Server does not close properly (\q))
			System.err.println("\n----------> The Server is down <----------\nApologies and See you soon!");
			//This is because the Scanner in InputValidation cannot close (closeAllLocal()), it is blocked waiting for input
				//This Thread is in the background, when this Exception may happen the Console will be always looking for input and waiting for it
				//Only exception to this situation: on login, Listener still not active. Exception would be Catch by "sendCmd()" on user input
			System.out.println("\n Please, Enter any key to Close the program");
			//Close all local Socket Streams... Do not send anything to the server (on normal quit closeAll(), send the Quit), here the server is down, only close on client
			closeAllLocal();
		}
		
	}
	
	//Method to handle the new Openchats list
	private void handleOpenChats(String token) {
		//Check if there's no open chats, or if there's none but there are openchats locally (asked by the user, so show the openchats)
			//If there's none on Server, could be locally (user logged off, but with unread messsages here), so check if there is locally even if no on server
		if((!token.equalsIgnoreCase("No Open Chats")) || (this.openChats.keySet().size() != 0 && token.equalsIgnoreCase("No Open Chats"))) {		
			//Because of the second conditional option, this situation can happen here
			//If there's none on server, chage the token to not analyse the single words of the system message
			if(token.equalsIgnoreCase("No Open Chats")) {
				token = "";
			}
			//Send to update the lists
			handleOpenChatsUpdateLists(token);
			
			//Print the Openchats with the flags
				//(N) if new messages unread
				//* if Offline (only with (N), cannot be offline without new messages)
			System.out.println("[] Openchats: ");
			for(String user : this.openChats.keySet()) {
				String extra = "";
				if(this.areNewMessages(user)) extra += "(N)";
				if(!this.reachable.get(user)) extra += "*";
				
				System.out.print(user + extra);
				System.out.print(" ");
			}
			System.out.println("");
		}else {
			System.out.println("[] No Open Chats");
		}
	}
	
	//Method to update system variables (if no in new list, check if unread messages, if so, stablish as unreachable, if not,
		//delete from the lists(double checking, should be done in []Offline))
	private void handleOpenChatsUpdateLists(String token) {
		String[] newOpenChats = token.split(" ");
		
		//Make an ArrayList, easier to manage
		ArrayList<String> newOpenChatsList = new ArrayList<String>();
		for(String item : newOpenChats) {
			newOpenChatsList.add(item);
		}
		
		//If not in local list but in server list, add it to the lists (double check)
		for(String newOpenChatsItem : newOpenChats) {
			if(!this.openChats.containsKey(newOpenChatsItem)) {
				handleNewOpenChatInternalVariables(newOpenChatsItem);
			}
		}
		
		//If in local list but not in server list, check if new messages. 
			//If unread mesages, set it as not reachable and nothing else (will be deleted when the user exits the chatroom after reading, Menu)
			//if not unread messages, delete from lists
		for(String openChat : this.openChats.keySet()) {
			if(!newOpenChatsList.contains(openChat)) {
				if(this.areNewMessages(openChat)) {
					this.reachable.replace(openChat, false);
				}else {
					this.openChats.remove(openChat);
					this.reachable.remove(openChat);
					this.newMessages.remove(openChat);
				}
			}
		}
	}
	
	//Method to handle when the Server sends a notice that a new openchat has been created
		//Show it to the user
		//send to create the internal lists
	private void handleNewOpenChat(String newOpenChatMessage) {
		String msg = "[]ChatRequest: ";
		System.out.println(msg + newOpenChatMessage);
		
		String[] temp = newOpenChatMessage.split(" ", 2);
		String newOpenChatName = temp[0];
		
		handleNewOpenChatInternalVariables(newOpenChatName);
	}
	
	//Method to add users to internal lists (if new openchat)
		//Add to map and initialize variables to default initial settings
	private void handleNewOpenChatInternalVariables(String newOpenChat) {
		ArrayList<String> messages = new ArrayList<String>();
		this.openChats.put(newOpenChat, messages);
		boolean reachable = true;
		this.reachable.put(newOpenChat, reachable);
		boolean readed = false;
		this.newMessages.put(newOpenChat, readed);
	}
	
	
	//Method to handle new messages from other users
		//If the cahtrrom with the sender is active, show it
		//If not, save on the lists
	//Structure: clientName --> body
	private void handleMsg(String senderAndMessage) {
		String[] tokens = senderAndMessage.split(" --> ", 2);
		String sender = tokens[0];
		String message = tokens[1];
		
		if(sender.equals(this.openChatRoomActive)) {
			System.out.println(sender + ": " + message);
		}else {
			this.openChats.get(sender).add(message);
			
			if(!this.newMessages.get(sender)) {
				this.newMessages.replace(sender, true);
			}
		}
	}
	
	//Method to handle system messages
		//If it's online/offline, send to another method
		//If not:
			//If in chatroom, save them
			//If not, show them
	private void handleSystemMsg(String token) {
		
		checkChangeStatusOpenChats(token);
		
		String sysMsg = "[] ";
		sysMsg += token;
		if(this.openChatRoomActive.equals("")) {
			System.out.println(sysMsg);
		}else {
			this.systemMsg.add(sysMsg);
		}
	}
	
	//Method to deal with online/Offline messages
		//If it is online and is in the Openchats list (offline with unread), change it to online
		//If it is offline:
			//If unread messages, make it unreachable and that's all (deleter from Menu after reading them)
			//If not unread messages, delete from lists
	private void checkChangeStatusOpenChats(String token) {
		
		String[] tokens = token.split(" ");
		
		if(tokens.length == 2) {
			if(tokens[0].equalsIgnoreCase("Online:")) {
				if(this.openChats.keySet().contains(tokens[1])) {
					this.reachable.replace(tokens[1], true);
				}
			}else if(tokens[0].equalsIgnoreCase("Offline:")) {
				if(this.openChats.keySet().contains(tokens[1])) {
					this.reachable.replace(tokens[1], false);
					if(!(this.newMessages.get(tokens[1]) || tokens[1].equalsIgnoreCase(openChatRoomActive))) {
						this.openChats.remove(tokens[1]);
						this.reachable.remove(tokens[1]);
						this.newMessages.remove(tokens[1]);
					}
				}
			}
		}
		
	}
	
	//Method called when back on Main Console, show System messages if there's any
		//Delete the list after showing them
	private void openSystemMsgs() {
		if(this.systemMsg.size() != 0) {
			System.out.println("\n[] Unread System Messages: \n");
			for(String sysMsg : this.systemMsg) {
				System.out.println(sysMsg);
			}
			this.systemMsg.clear();
		}
	}
	
	//Method called when accessing a chatroom, send unread messages if there's any for this isername (delete after sending them)
	public ArrayList<String> openChatRoom(String clientName) {
		if(this.newMessages.get(clientName)) { 
			ArrayList<String> unreadMessages = new ArrayList<String>();
			for(String message : this.openChats.get(clientName)) {
				unreadMessages.add(message);
			}
			
			this.openChats.get(clientName).clear(); 
			this.newMessages.replace(clientName, false);
			
			return unreadMessages;
		}else {
			return null;
		}
		
	}
	
	//Method to check if there's unread messages (called from Menu)
	public boolean areNewMessages(String clientName) {
		return this.newMessages.get(clientName);
	}
	
	//Method to Activate the flag of the Chatroom
		//The flag will make system msgs to be saved
	public void openChatRoomFlagActivate(String clientName) {
		this.openChatRoomActive = clientName;
	}
	
	//Method to deactivate the flag of the chatroom
	public void openChatRoomFlagDeactivate() {
		this.openChatRoomActive = "";
		openSystemMsgs();
	}
	
	//Method to let know if a client is reachable or not (allow/not send messages from Menu)
	public boolean isReachable(String clientNameToCheck) {
		boolean reachable;
		if(this.reachable.get(clientNameToCheck)) {
			reachable = true;
		}else {
			reachable = false;
		}
		return reachable;
	}
	
	//Method to remove from lists and user
	public void removeFromLists(String clientNameToDelete) {
		this.openChats.remove(clientNameToDelete);
		this.newMessages.remove(clientNameToDelete);
		this.reachable.remove(clientNameToDelete);
	}
	
	//Method to open the Streams for the socket and initialize internal lists (when socket created)
	public void openStreams() {
		try {
			this.inputStream = clientSocket.getInputStream();
			this.reader = new BufferedReader(new InputStreamReader(inputStream));
			this.outputStream = (clientSocket.getOutputStream());
			this.openChats = new HashMap<String, ArrayList<String>>();
			this.reachable = new HashMap<String, Boolean>();
			this.newMessages = new HashMap<String, Boolean>();
			this.openChatRoomActive = "";
			this.systemMsg = new ArrayList<String>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Reject Welcome line from the Server......
		try {
			reader.readLine();
			reader.readLine();
			reader.readLine();
			reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//Method to handle Server-side Login validation
	public boolean login(String clientName) {
		try {
			String msg = clientName;
			sendCmd(msg);
			String input;
			input = reader.readLine();
			if(input.equalsIgnoreCase("login: " + clientName + " loginOK")) {
				return true;
			}else {
				reader.readLine();
				return false;
			}
		} catch (IOException e1) {
			System.out.println("-----> Server not available, please try again later <------");
			closeAll();
		}
		return false;
	}
	
	//Method to check if the user is an openchat (from the Menu)
	public boolean checkUser(String clientNameToCheck) {
		if(this.openChats.keySet().contains(clientNameToCheck)) {
			return true;
		}else {
			return false;
		}
	}
	//Method to send commands to the Server
	public void sendCmd(String command) {
		
		command += "\n";
		
		try {
			this.outputStream.write(command.getBytes());
			//System.out.println("Command sent"+command);
		} catch (IOException e2) {
			System.out.println("-----> Server not available, please try again later <------");
			System.out.println("\nThe Application will close automatically in 10 seconds\n");
			//No need to press any key, Scanner not blocked (See Catch on Run())
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			closeAllLocal();
		}
	}
	
	//Method to close only Locally (Server down, no need to close there)
	private void closeAllLocal() {
		try {
			this.inputStream.close();
			this.outputStream.close();
			this.clientSocket.close();
			InputValidation.closeScan();
			System.exit(0);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//Method to close all on Quit, close all locally and send Quit to the server to close it there too
	public void closeAll() {
		try {
			sendCmd("\\q");
			this.inputStream.close();
			this.outputStream.close();
			this.clientSocket.close();
			InputValidation.closeScan();
			System.exit(0);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
