import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class Initializator {
	//This class initiates the program and lets the user choose a Port for the server to listen to
	
	public void init() {
		//Scanner to read input from the user
		Scanner scan = new Scanner(System.in);
		
		ServerHandler serverHandler = new ServerHandler();
		ServerListener listener;
		//String to check later for an empty line
		String checkDefaultValue;
		//Default value to initialize the Loop
		int portNumber = 0;
		
		//Loop while the Port selected is not valid (Not well-known ports and a valid Port number)
		while(portNumber < 1023 || portNumber > 65535) {
			//Message for the user
			System.out.println("Please, Introduce the Port to listen or press Enter for Default");
			//Prompt, it's fancy..
			System.out.print(">>");
			try {
				
				checkDefaultValue = scan.nextLine();
				if(checkDefaultValue.equals("")) {
					portNumber = 9999;
				}else {
					portNumber = Integer.parseInt(checkDefaultValue);
				}
				
				if(portNumber < 1023 || portNumber > 65535) {
					System.out.println("----->> The Port [" + portNumber + "] is not valid. Introduce a valid Port (between 1023 and 65535)");
				}else {
					//Inform the user about the selection and go to the ServerHandler
					System.out.println("Selected Port: " + portNumber);
					System.out.println("Trying to open a Server Socket...");
					
					try {
						ServerSocket serverSocket = new ServerSocket(portNumber);
						
						listener = serverHandler.startListening(serverSocket);
						
						System.out.println("\n--> Server opened successfully<--\n");
						
						//Creates and initiates the AdminConsole
						ServerAdminSocket serverAdmin = new ServerAdminSocket(portNumber, listener);
						serverAdmin.addAdminSocket();
					} catch (IOException e) {
						System.out.println("\n--> Error on ServerSocket: Please, Introduce an available Port Number <--\n");
						portNumber = 0;
						//e.printStackTrace();
					}
				}
				
			//If the value is not an int
			}catch(NumberFormatException e) {
				System.out.println("-----> Select a numerical value between 1023 and 65535");
			}
		}	
	}
}
