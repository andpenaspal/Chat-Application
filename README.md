# Chat Application

##### Author: Andres Penas Palmeiro
##### Final Project for the Module "Network Programming" of the H.Dip in Computer Science in Software Development - GMIT
##### Version 1.0 (Jan 2021)

## Summary
This application consists of two different programs: Client and Server.

The Chat Application is a command based Chat that allows multiple users to message each other.

Stateful Server Application.

## Technologies
Java, TCP (Sockets)

## Requirements
JAVA JRE 12 (or superior)

## Launch
Compile and run the "ServerRunner" or "ClientRunner" from the Command Prompt.

## Special Features
### Private Messages
Users can chat with each other privately.
### Open Chats System
In order to chat with another user, a user needs to accept or have accepted a chat request.
### Chat Rooms
All Open Chats have their own Chat Room.
No commands needed to chat, once in a Chat room, all text entered will be sent automatically.
### Saved Unread messages
* Messages sent when the receiver is not in the Chat Room will be saved and shown once the receiver enters the Chat Room again.
* Unread messages saved on Chat Room will be available to be read even on disconnection of the sender.
* System messages received while in a Chat Room will be saved and shown once the user enters the Main Console Again.
### Broadcast messages (admin)
The administrator can send Broadcast messages that will be sent to all users and shown as a Special.

## Usage
The Server needs to be running to allow users to connect and chat.

### Server:

Launch the Server application and select a Port to listen to. The server is up and listening and the Administrator in the Admin Console. Quit at any moment with the following command: “\q” (from Admin Console).

### Client:

Launch the Client application and introduce the IP and Port of the Server to connect to as requested. Insert a Login Username. Insert commands to use the application. Quit at any moment with the following command: “\q”; go back to the Main console with “\b”.

To start a chat the user needs to open a chat with another user (request a chat to another user or accept a chat request from another user). To start messaging with an Open Chat, enter the Chat Room with this user.

System messages (online/offline) received while in a Chat Room will be saved and shown when entering the Main Console again. Private messages sent by a user while not in his/her Chat Room will be saved and shown when the user enters the Chat Room again. Unread messages will be saved even if the user who sent them is currently offline.

### Administrator:

The Administrator can connect to the Chat through the Server application or the Client application. A user connected to the Chat through the Client application sharing IP with the Server will be Admin automatically. To connect from the Server application, the Administrator needs to enter “\c” in the Admin Console.

### Telnet & Admin Console Chat:

Clients can connect to the chat without the Client application through Telnet. Telnet must be set up. Connect to the Server with the following command “telnet [IP] [port]”, introduce the username and insert commands. Telnet Mode does not support Chat Rooms (see list of commands). Telnet does not offer Administrator privileges.

The Administrator connected to the Chat through the Admin Console can only use the Chat with the same characteristics as the Telnet Mode (no Chat Rooms). Enter “\q” at any moment to close the Chat (will not affect the Server) and go back to the Admin Console.The administrator can run the Client application to use the fully featured Chat with Admin privileges (broadcast).

## Example of Use

Chat between “Patrick” and “John” through the Client Application (commands are shown between square brackets []).

Patrick:
1. Open Chat Application.
2. Login: [Patrick].

John:
1. Open Chat Application.
2. Login: [John].

Patrick:
(In Main Console)
1. Get list of Online users: \[getOnline]. Optional. Shows "John".
2. Request John to chat with him: \[chat requestChat John].

John:
(In Main Console)
1. Gets a System message: "[] New chat request from Patrick".
2. Accept chat request: \[chat acceptChat Patrick].
3. Gets a System message: "[]ChatRequest: Patrick chat request accepted".
4. Open the Chat Room: \[chat openChatRoom Patrick].
5. Start messaging.

#### Telnet and Admin Console Chat
Same steps until the "ChatRoom". For John to message Patrick: \[msg Patrick Hello World!].

## Commands
### Client Application

| COMMAND | VARIABLE | DESCRIPTION |
| ------- | -------- | ----------- |
| \q |  | Quit the application |
| \b |  | Go back to Main Console |
| \h |  | Display List of Commands |
| help |  | Display List of Commands with Description |
| [getOnline] |  | Get a list of online users |
| [chat] [openChats] |  | Get a list of Open Chats |
| [chat] [requestChat] | [username] | Request a chat to the user |
| [chat] [requestedChats] |  | Get a list of requests made and not answered yet |
| [chat] [removeChat] | [username] | Remove the Open Chat with the user |
| [chat] [chatRequests] |  | Get a list of chat requests made by other users to chat to you |
| [chat] [acceptChat] | [username] | Accepts the chat request made by the user specified |
| [chat] [rejectChat] | [username] | Rejects the chat request made by the user specified |
| [chat] [openChatRoom] | [username] | Opens the Chat Room with the specified user |

### Telnet and Admin Console Chat

| COMMAND | VARIABLE | DESCRIPTION |
| ------- | -------- | ----------- |
| \q |  | Quit the application |
| \h |  | Display List of Commands |
| help |  | Display List of Commands with Description |
| [getOnline] |  | Get a list of online users |
| [chat] [openChats] |  | Get a list of Open Chats |
| [chat] [requestChat] | [username] | Request a chat to the user |
| [chat] [requestedChats] |  | Get a list of requests made and not answered yet |
| [chat] [removeChat] | [username] | Remove the Open Chat with the user |
| [chat] [chatRequests] |  | Get a list of chat requests made by other users to chat to you |
| [chat] [acceptChat] | [username] | Accepts the chat request made by the user specified |
| [chat] [rejectChat] | [username] | Rejects the chat request made by the user specified |
| [msg] | [username] [message] | Sends a message to the specified user |
| [\a] [brdc] | [message] | Sends a Broadcast message to all the users (admin) |

## License
MIT License. See "LICENSE" for further information.

## Contact Information
G00376379@gmit.ie