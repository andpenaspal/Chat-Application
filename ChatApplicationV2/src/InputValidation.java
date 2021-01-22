import java.util.Scanner;

public class InputValidation {
	/*
	 * This class deals with the User Input directly, with the scanner
	 * Checks for metadata (quit, help)
	 * 
	 * All input is taken as String to check for metadata. If asked for Int, casted catching hypothetical exceptions after checking for metadata
	 * Quit only gives back the same data, but dealt with Int/String problem (-1 for int)
	 * Help Shows the help message and forces another iteration of the loop to keep asking for the data
	 * */
	
	//Scanner
	private static final Scanner scan = new Scanner(System.in);
	
	//Help Messages
	private static final String H = "[] Help: List of commands\n"
			+ "Structure of the list: [command] 'user input'\n\n"
			+ "[\\h]\n"
			+ "[help]\n"
			+ "[\\q]\n"
			+ "[getOnline]\n"
			+ "[\\b]\n"
			+ "[chat] [openChats]\n"
			+ "[chat] [requestChat] 'username'\n"
			+ "[chat] [requestedChats]\n"
			+ "[chat] [removeChat] 'username'\n"
			+ "[chat] [chatRequests]\r\n"
			+ "[chat] [acceptChat] 'username'\n"
			+ "[chat] [rejectChat] 'username' \n"
			+ "[chat] [openChatRoom] 'username'\n";
	
	
	private static final String HELP = "[] Help: List of commands and their function\n"
			+ "Structure of the list: [command] 'user input'\n\n"
			+ "[\\h]: Shows a list of available commands\n\n"
			+ "[help]: Shows a list of available commands and their function\n\n"
			+ "[\\q]: Exit the application\n\n"
			+ "[getOnline]: Get a list of the Users that are currently online\n\n"
			+ "[\\b] When in a Chat Room, go back to the Main Console"
			+ "[chat] [openChats]: Get a list of Open Chats with online users\n\n"
			+ "[chat] [requestChat] 'username': Request a chat to the user specified. The user to whom the"
			+ "\n          the request has been sent can accept or reject the request\n\n"
			+ "[chat] [requestedChats]: Get a list of request made and not answered yet\n\n"
			+ "[chat] [removeChat] 'username': Removes an Open Chat. The user will not be ablo to send or receive"
			+ "\n          messages to/from the user\n\n"
			+ "[chat] [chatRequests]: Get a list of chat requests. Requests made by other users to chat to you\n\n"
			+ "[chat] [acceptChat] 'username': Accepts the chat request made by the user specified. A request "
			+ "\n          needs to have been made to be accepted\n\n"
			+ "[chat] [rejectChat] 'username': Reject the chat request made by the user specified. The user will"
			+ "\n          not be able to contact with or you with him/her\n\n"
			+ "[chat] [openChatRoom] 'username': Opens the Chat Room with the specified user\n\n"
			+ "Message: To send a message, the message needs to be sent from the Chat Room with the receiver\n\n"
			+ "Chat Room: An Open Chat will automatically create a Chat Room with the user\n\n"
			+ "OpenChat: A chat request needs to be sent/received and accepted (by the requested)\n";
	

	//If asked for input, check for metadata and give back. Another iteration on help
	public static String inputString() {
		String input;
		do {
			input = checkForMeta(scan.nextLine(), false);
		} while(input.equalsIgnoreCase("\\h") || input.equalsIgnoreCase("\\help"));
		
		return input;
	}
	
	//When asking for Ports, check if it Default value and it is in bounds. Get the prompt too to show it in each iteration, relevant info
		//Scanner for UserInput on string, check if it is Default Port Value
			//Return if it is, caller handles it
			//If not, cast to int and check metadata
				//Return on Quit, caller handles it
				//Next iteration on Help
		//Check if it is out of bounds (to send message to the user)
		//If input fine, return the value
		
	public static int inputIntPortNumber(String prompt, String defaultInputValue, int defaultOutputValue, int minValue, int maxValue) {
		//Initiate variable to infinite loop
		int input = 0;
		//Variable for the String checking for metadata
		String tempInput = "";
		//Infinite loop while port out of bounds
		while(input < minValue || input > maxValue) {
			//try-catch for the casting String-int
			try {
				System.out.println("\n" + prompt);
				
				tempInput = scan.nextLine();

				if(tempInput.equals(defaultInputValue)) {
					return defaultOutputValue;
				}else {
					input = Integer.parseInt(checkForMeta(tempInput, true));
					if(input == -1) return input;
					if(input == 0) continue;
				}
				
				if(input < minValue || input > maxValue) System.err.println("\n-----> Introduce a valid Port Number between 1023 and 65535 <-----");
				
			}catch(Exception e) {
				System.out.println("\n-----> Invalid format, try again <-----");
				//scan.nextLine();
			}
		}
		return input;
	}
	
	//Method to check for metadata in all input. Bollean on parameter to check if it's int or not (return internal value dependeing on that)
		//On quit, return appropiate type
		//on help, show message and return appropiate type
		//If the user happens to input an internal variable used to handle the internal loops, change it for a non used value in the same bounds (portNumber)
	private static String checkForMeta(String data, boolean isInt) {
		switch (data) {
		case "\\q":
			System.out.println("Thanks for using our Chat! \nSee you soon!");
			if(!isInt) return "\\q";
			if(isInt) return "-1";
			break;
		case "\\h":
		case "\\H":
			System.out.println(H);
			if(!isInt) return "\\help";
			if(isInt) return "0";
			break;
		case "help":
		case "HELP":
		case "Help":
			//On help will show the Help Message and return the appropriate data to keep looping on caller
			System.out.println(HELP);
			if(!isInt) return "\\help";
			if(isInt) return "0";
			break;
		//In case the User inputs one of the "system check" values, replace it for a non-valid port to not perform the system feature (help/quit, check this method above)
		case "0":
		case "-1":
			if(!isInt) return data;
			if(isInt) return "7";
			break;
		//If data is not a MetaData attribute will just return the data
		default:
			return data;
		}
		//Shouldn't get here but...
		return data;	
	}
	
	//Method to close the scanner on Quit, called from the chatHandler
	public static void closeScan() {
		scan.close();
	}

}
