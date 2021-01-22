==============================================================
              CHAT APPLICATION README
==============================================================

Author: Andres Penas Palmeiro
Version 1.0 Jan 2021
Higher Diploma in Science in software Development - GMIT
Network Programming
Final assesment
==============================================================

SUMMARY
--------------------------------------------------------------

This application consists of two different programs: Client 
and Server.

This application is a Final Project for the Module of Network 
Programming in the Higher Diploma in Science in Software 
Development (GMIT). The Chat Application is command based 
Chat that allows multiple users to message each other. The 
application supports private messaging through Chat Rooms.

--------------------------------------------------------------

REQUIREMENTS
--------------------------------------------------------------

JAVA JRE 12 (or superior)
--------------------------------------------------------------

LAUNCH
--------------------------------------------------------------

Compile and run the “Runner” from the Command Prompt
--------------------------------------------------------------

FEATURES
--------------------------------------------------------------

Private messaging
Chat validation
Unread messages saved while not in Chat Room
Unread messages saved on disconnection of the user
Unread system messages saved while in Chat Room
Broadcast messages (admin)
--------------------------------------------------------------

USAGE
--------------------------------------------------------------

(Compile and run the “ClientRunner” or “ServerRunner”)
The Server needs to be running to allow users to connect and 
chat.

Server:

Launch the Server application and select a Port to listen to. 
The server is up and listening and the Administrator in the 
Admin Console. Quit at any moment with the following command:
 “\q” (from Admin Console).

Client:

Launch the Client application and introduce the IP and Port 
of the Server to connect to as requested. Insert a Login 
Username. Insert commands to use the application. Quit at 
any moment with the following command: “\q”; go back to the 
Main console with “\b”.

To start a chat the user needs to open a chat with another 
user (request a chat to another user or accept a chat request 
from another user). To start messaging with an Open Chat, 
enter the Chat Room with this user.

System messages (online/offline) received while in a Chat 
Room will be saved and shown when entering the Main Console 
again. Private messages sent by a user while not in his/her 
Chat Room will be saved and shown when the user enters the 
Chat Room again. Unread messages will be saved even if the 
user who sent them is currently offline.

Administrator:

The Administrator can connect to the Chat through the Server
 application or the Client application. A user connected to 
the Chat through the Client application sharing IP with the 
Server will be Admin automatically. To connect from the Server
 application, the Administrator needs to enter “\c” in the 
Admin Console.

Telnet & Admin Console Chat:

Clients can connect to the chat without the Client application
 through Telnet. Telnet must be set up. Connect to the Server 
with the following command “telnet [IP] [port]”, introduce the 
username and insert commands. Telnet Mode does not support Chat
 Rooms (see list of commands). Telnet does not offer 
Administrator privileges.

The Administrator connected to the Chat through the Admin 
Console can only use the Chat with the same characteristics as
 the Telnet Mode (no Chat Rooms). Enter “\q” at any moment to 
close the Chat (will not affect the Server) and go back to the 
Admin Console.The administrator can run the Client application 
to use the fully featured Chat with Admin privileges (broadcast).

Example of Use:

User named “Patrick” wanting to talk to “John”.
From the main console “chat requestChat John” (John accepts the 
Chat Request). “chat openChatRoom John”, start messaging.

John, on request received: “chat acceptChat Patrick”. 
“chat openChatRoom Patrick”, start messaging.
--------------------------------------------------------------

COMMANDS
--------------------------------------------------------------

Client:
\q		                        
Quit the application

\b		                        
Go back to the Main Console

\h		        
Display List of Commands

help		
Display List of Commands with Description

getOnline		
Get a list of online users

chat openChats		
Get a list of Open Chats

chat requestChat [username]	
Request a Chat to the user

chat requestedChats		
Get a list of Chats Requested by other users

chat removeChat	[username]	
Remove the user from Open Chats (will not be able to 
contact you anymore)

chat chatRequests		
Get a list of sent Chat Requests

chat acceptChat	[username]	
Accept the Chat Request sent by the user

chat rejectChat	[username]	
Reject the Chat Request sent by the user

chat openChatRoom [username]	
Go to the Chat Room with the user (private messaging)

\a brdc	[message]	
Only Admin: Send a Broadcast to all Online users (use on 
Main Console).




Telnet or Admin from Server:
\q		
Quit the Chat

\h		
Display List of Commands

help		
Display List of Commands with Description

getOnline		
Get a list of online users

msg [username] [message]	
Send a private message to the user

\a brdc	[message]	
Only Admin: Send a Broadcast to all Online users
--------------------------------------------------------------

CONTACT INFORMATION
--------------------------------------------------------------

G00376379@gmit.ie (G00376379 at gmit dot ie)
--------------------------------------------------------------

