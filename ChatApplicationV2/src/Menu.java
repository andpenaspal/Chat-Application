import java.util.*;

public class Menu {
	
	/*
	 * This class is the Menu. Handles interaction with the user.
	 * Ask for IP and Port, Login, Main Console and chatrooms. Just the interaction, the backbone of the functionalities in ChatHandler.
	 * ChatHandler deals with the server, Menu with the user
	 * This class will take input from the user (check metadata and other Menu-related features) and send it to chatHandler,
	 * 		The Handler will send it to the Server, listen for response, deal with it and send the output to the console
	 * 
	 * Scanner in a specialized class to check for metadata (quit, help)
	 * */
	
	//Instance variables
	private String name;
	private ChatHandler chatHandler;
	
	//Constructor
	public Menu() {
		chatHandler = new ChatHandler();
		System.out.println("Welcome to our Chat Application!");
	}
	
	//Method to start the menu
		//Ask for IP and Port
	public void startMenu() {
		String ip;
		int portNumber;
		//Ask for IP and Port number (Default value is the same in ServerSocket)
		do {
			System.out.println("\nPlease, Introduce the IP Address to connect to the Server:");
			ip = InputValidation.inputString();
			String prompt = ("Please, Introduce the Port to connect to the server or press Enter for Default");
			//Scanner in specialized class to check for metadata (quit, help)
			portNumber = InputValidation.inputIntPortNumber(prompt, "", 9999, 1023, 65535);
			//If the input return a "-1" means the user typed "\q", exit. No need to close socket, not created yet
			if(portNumber == -1) System.exit(0);
			
			System.out.println("Ip selected: [" + ip + "] Port Number selected: [" + portNumber + "]");
			System.out.println("Checking Server Status...");
		//Do it while the Connection cannot be established 
		}while(!chatHandler.createConnection(ip, portNumber));
		
		System.out.println("\nConnection made suscessfully!");
		//Open Streams for the Socket
		chatHandler.openStreams();
		//Got to next Menu
		initMenu();
	}
	
	//Menu for login. Input validation and Server Validation of the Username
	private void initMenu() {
		String prompt = "Please, Introduce Your Name:";
		String warning = "The Username cannot be shorter than 3 chars or longer than 10chars. Also, blank spaces not allowed \n" + prompt;
		String input = "";
		//Variable to control the loop
		boolean loginAccepted = false;
		
		//Infinite loop while Username not validated
		while(!loginAccepted) {
			
			System.out.println(prompt);
			//Ask for Input and check metadata
			input = InputValidation.inputString();
			//On quit, chatHandler will close Socket, Streams and send quit command to server
			if(input.equalsIgnoreCase("\\q")) chatHandler.closeAll();
			
			//Client-side validation. Only if input wrong, input already collected once
				//Loop indefinetely if conditions not meet
			while(input.length() < 3 || input.length() > 10 || input.contains(" ")) {
				System.out.println(warning);
				input = InputValidation.inputString();
				if(input.equalsIgnoreCase("\\q")) chatHandler.closeAll();
			}
			//Boolean on check of validation on server
			//On true will take down the infinite condition of the loop (no more loops, but finish this one)
			loginAccepted = chatHandler.login(input);
			
			if(loginAccepted) {
				name = input;
				System.out.println("\nWelcome " + name + "!!");
			}else {
				System.out.println("Login Error: Username already in use, Please Try again");
				input = "";
			}
		}
		
		//When username valid, create a new Thread to listen to the server and put it to listen on the background
			//Server Input before this point (login) dealt with in chatHandler.login()
		Thread clientListener = new Thread(chatHandler);
		clientListener.start();
		
		//go to next menu
		mainConsole();
	}
	
	//Main Console
		//Infinite Loop. Main place to be. 
		//Here the user sends basic commands or opens chatrooms
		//Never stops, chat rooms will come back to the infinite loop when closing
	private void mainConsole() {
		String prompt = "\n-----> Main Console <----- \n\nThis is a command based interface, please enter \"\\h\" or \"help\" for help\n\n";
		String input;
		System.out.println(prompt);
		
		//infinte loop
		while(true) {
			//Ask for input and check metadata
			input = InputValidation.inputString();
			//On "Go Back", explain you can't
			if(input.equals("\\b")) System.out.println("You cannot Go Back on the Main Conlose\n"
					+ "Enter \"help\" to get a list of the commands and its actions\n"
					+ "Enter \"\\h\" to get a list of the commands\n"
					+ "Enter \"\\q\" to EXIT the program\n");
			//On Quit, close socket, streams and send the command to the server
			if(input.equalsIgnoreCase("\\q")) chatHandler.closeAll();
			//Split the input to check for commands
			String[] tokens = input.split(" ");
			//Catch "msg" command, not valid on Client (Only on Telnet/Admin)
				//On client ChatRooms to talk
				//Catch and go next iteration of the loop
			if(tokens[0].equalsIgnoreCase("msg")) {
				System.out.println("Please, open a chat room to send a message");
				continue;
			}
			
			//First if: check if there's something in the input
			//Second if: check if input is 3 tokens
				//If it is:
					//Third if: check if it's for chatrooms (Client-side)
						//If it is, deal with it
						//If not, is server-side
				//If not: is server side
			if(tokens.length != 0) {
				if(tokens.length == 3) {
					//Structure: chat openChatRoom usernameToOpenChatRoomWith
					if(tokens[0].equalsIgnoreCase("chat") && tokens[1].equalsIgnoreCase("openChatRoom")) {
						handleChatRoom(tokens[2]);
					}else {
						chatHandler.sendCmd(input);
					}
				}else {
					chatHandler.sendCmd(input);
				}
			}
		}
	}
	
	//If the user wants to open chatroom
		//Validation of the ChatRoom and Unread messages
	private void handleChatRoom(String clientName) {
		//Separation to make clear to the user that it is in a chatroom
		System.out.println("\n\n\n\n\n\n\n\n\n\n");
		//Validation if the user specified is an openchat
			//Only can open chatrooms with openchats
			//If not, go back to main console
		boolean isOpenChat = chatHandler.checkUser(clientName);	
		if(!isOpenChat) {
			System.out.println("[] " + clientName + " is not an open chat");
			System.out.println("\n-----> Main Console <----- \n");
			return;
		}
		
		System.out.println("-----> Chat Room: " + clientName + " <-----\n");
		//Check if the are new messages unread in the "buffer" for this client
			//If there is, take them and output them
		if(chatHandler.areNewMessages(clientName)) {
			ArrayList<String> unreadMessages = chatHandler.openChatRoom(clientName);
			//System.out.println("Unread size2: " + unreadMessages.size());
			System.out.println("Unread messages: \n");
			for(String message : unreadMessages) {
				String msg = clientName + ": " + message;
				System.out.println(msg);
			}
			System.out.println("");
		}
		
		handleChat(clientName);		
		return;
	}
	
	//Method to handle the chatRoom itself
	private void handleChat(String sendTo) {
		
		String msg = "";
		//Activate flag on handler:
			//Block and save system messages to read when back on main console
		chatHandler.openChatRoomFlagActivate(sendTo);
		
		//Infinite loop to handle user input-chat
			//Validation to go back to Main Console or quit (Break the loop on "go back")
			//Validation if the client is reachable (Online)
				//If it is not online, was to read unread messages saved on chatroom, de not let talk
				//If it is online, send messages to the user
					//Will be instant if the another user in chatroom too, if not, saved on the "buffer"
		while(true) {
			msg =  InputValidation.inputString();
			
			if(msg.equalsIgnoreCase("\\b")) break;
			if(msg.equalsIgnoreCase("\\q")) chatHandler.closeAll();
			
			if(!(chatHandler.isReachable(sendTo))) {
				System.out.println("[] " + sendTo + " is Offline");
			}else {
				chatHandler.sendCmd("msg " + sendTo + " " + msg);
			}
		}
		//Loop broken, user wants to go back
		//Set down the flag of ChatRoom active, allow system messages to go to the console and save any new messages from the user
		chatHandler.openChatRoomFlagDeactivate();
		
		//If it's not reachable (offline while talking or only unread messages)
		//Delete from all lists
			//would be on list and offline only if there was unread messages, now are read
			//If it is online, is reachable, so do not delete
		if(!(chatHandler.isReachable(sendTo))){
			chatHandler.removeFromLists(sendTo);
		}
		//Communicate the user is going back to main console
		System.out.println("\n-----> Main Console <----- \n");
		return;
	}
	
}

