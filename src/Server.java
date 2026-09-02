import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;

/**
 * Multi-threaded TCP Messaging Server.
 * Listens on a user-defined port and delegates client connections to ClientHandler threads.
 */
public class Server {
    public static void main(String[] args) {
        ServerSocket server = null;
        // Shared list across all client requests storing all registered accounts
        ArrayList<Account> accounts = new ArrayList<>();
        Scanner scanner=new Scanner(System.in);
        String port= scanner.nextLine();
        try {
            server = new ServerSocket(Integer.parseInt(port));
            // Main server loop accepting client requests indefinitely
            while (true) {
                Socket client = server.accept();
                // Spawn a new thread to handle each client connection concurrently
                ClientHandler clientHandler = new ClientHandler(client, accounts);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runnable worker class for processing individual client commands.
     */
    private static class ClientHandler implements Runnable {
        private ArrayList<Account> accounts = new ArrayList<>();
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket, ArrayList<Account> accounts) {
            this.clientSocket = clientSocket;
            this.accounts = accounts;
        }
        public void run(){
            PrintWriter out=null;
            BufferedReader in=null;
            try{
                out=new PrintWriter(clientSocket.getOutputStream(), true);
                in=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Read client input command string
                String command=in.readLine();
                String []commandParts=command.split(" ");

                // Execute command and return response to client
                out.println(executeCommand(commandParts));
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        /**
         * Parses and executes commands synchronously to prevent race conditions on shared account data.
         * Expected command structure: commandParts[0] = IP, commandParts[1] = Port, commandParts[2] = FN_ID
         *
         * @param commandParts Tokenized input command array
         * @return Protocol response string
         */
        private synchronized String executeCommand(String []commandParts){
            try {
                // FN_ID 1: Create Account -> [IP, PORT, 1, username]
                if ("1".equals(commandParts[2])) {
                    boolean exists = false;
                    for (Account accounts : accounts) {
                        if (accounts.getUsername().equalsIgnoreCase(commandParts[3])) {
                            exists = true;
                        }
                    }
                    if (exists) {
                        return "Sorry, the user already exists";
                    } else if (!commandParts[3].matches("[\\w]+")) {
                        return "Invalid Username";
                    } else {
                        // Generate a unique integer authentication token
                        Random random = new Random();
                        int authToken = random.nextInt(100000);
                        boolean unique = false;
                        while (!unique) {
                            unique = true;
                            for (Account account : accounts) {
                                if (account.getAuthToken() == authToken) {
                                    unique = false;
                                }
                            }
                            if (!unique) {
                                authToken = random.nextInt(100000);
                            }
                        }
                        Account account = new Account(commandParts[3], authToken);
                        String token = String.valueOf(authToken);
                        accounts.add(account);
                        return token;
                    }
                // FN_ID 2: Show Accounts -> [IP, PORT, 2, authToken]
                } else if ("2".equals(commandParts[2])) {
                    boolean exists = false;
                    int c = 0;
                    int pointer = -1;
                    for (Account account : accounts) {
                        // Exclude the requesting user's own username from output
                        if (commandParts[3].equals(String.valueOf(account.getAuthToken()))) {
                            pointer = c;
                            exists = true;
                        }
                        c += 1;
                    }
                    if (exists) {
                        String usernames = "";
                        int u = 0;
                        for (Account account : accounts) {
                            if (!account.getUsername().equals(accounts.get(pointer).getUsername())) {
                                u += 1;
                                usernames = usernames + u + ". " + account.getUsername() + "@";
                            }
                        }
                        return usernames;
                    } else {
                        return "Invalid Auth Token";
                    }
                // FN_ID 3: Send Message -> [IP, PORT, 3, authToken, recipient, body...]
                } else if ("3".equals(commandParts[2])) {
                    boolean exists = false;
                    int c = 0;
                    int sender = -1;
                    int receiver = -1;
                    for (Account account : accounts) {
                        if (commandParts[3].equals(String.valueOf(account.getAuthToken()))) {
                            exists = true;
                            sender = c;
                        }
                        if (account.getUsername().equals(commandParts[4])) {
                            receiver = c;
                        }
                        c += 1;
                    }
                    if (exists) {
                        if (receiver >= 0) {
                            // Reconstruct full message body from remaining arguments
                            int pointer = 5;
                            String body = "";
                            while (pointer < commandParts.length) {
                                body = body + commandParts[pointer];
                                pointer += 1;
                                if (pointer < commandParts.length) {
                                    body = body + " ";
                                }
                            }
                            Message message = new Message(accounts.get(sender).getUsername(), commandParts[4], body);
                            accounts.get(receiver).addMessage(message);
                            return ("OK");
                        } else {
                            return "User does not exist";
                        }
                    } else {
                        return "Invalid Auth Token";
                    }
                // FN_ID 4: Show Inbox -> [IP, PORT, 4, authToken]
                } else if ("4".equals(commandParts[2])) {
                    boolean exists = false;
                    int pointer = -1;
                    int c = 0;
                    for (Account account : accounts) {
                        if (commandParts[3].equals(String.valueOf(account.getAuthToken()))) {
                            exists = true;
                            pointer = c;
                        }
                        c += 1;
                    }
                    if (exists) {
                        ArrayList<Message> inbox = accounts.get(pointer).getInbox();
                        String inboxFormat = "";
                        for (Message message : inbox) {
                            inboxFormat = inboxFormat + message.getId() + ". from: " + message.getSender();
                            // Append '*' if the message is unread
                            if (!message.isRead()) {
                                inboxFormat = inboxFormat + "*";
                            }
                            inboxFormat = inboxFormat + "@";
                        }
                        return inboxFormat;
                    } else {
                        return "Invalid Auth Token";
                    }
                // FN_ID 5: Read Message -> [IP, PORT, 5, authToken, message_id]
                } else if ("5".equals(commandParts[2])) {
                    boolean exists = false;
                    int pointer = -1;
                    int c = 0;
                    for (Account account : accounts) {
                        if (commandParts[3].equals(String.valueOf(account.getAuthToken()))) {
                            exists = true;
                            pointer = c;
                        }
                        c += 1;
                    }
                    if (exists) {
                        ArrayList<Message> inbox = accounts.get(pointer).getInbox();
                        boolean messageExists = false;
                        int messagePointer = -1;
                        int c2 = 0;
                        for (Message message : inbox) {
                            if (commandParts[4].equals(String.valueOf(message.getId()))) {
                                messageExists = true;
                                messagePointer = c2;
                            }
                            c2 += 1;
                        }
                        if (messageExists) {
                            // Mark message as read and return content
                            accounts.get(pointer).getInbox().get(messagePointer).setRead(true);
                            return "(" + accounts.get(pointer).getInbox().get(messagePointer).getSender() + ")" + accounts.get(pointer).getInbox().get(messagePointer).getBody();//sender not the receiver
                        } else {
                            return "Message ID does not exist";
                        }
                    } else {
                        return "Invalid Auth Token";
                    }
                // FN_ID 6: Delete Message -> [IP, PORT, 6, authToken, message_id]
                } else if ("6".equals(commandParts[2])) {
                    boolean exists = false;
                    int pointer = -1;
                    int c = 0;
                    for (Account account : accounts) {
                        if (commandParts[3].equals(String.valueOf(account.getAuthToken()))) {
                            exists = true;
                            pointer = c;
                        }
                        c += 1;
                    }
                    if (exists) {
                        ArrayList<Message> inbox = accounts.get(pointer).getInbox();
                        boolean messageExists = false;
                        for (Message message : inbox) {
                            if (commandParts[4].equals(String.valueOf(message.getId()))) {
                                messageExists = true;
                            }
                        }
                        if (messageExists) {
                            return accounts.get(pointer).removeMessage(Integer.parseInt(commandParts[4]));
                        } else {
                            return "Message does not exist";
                        }
                    } else {
                        return "Invalid Auth Token";
                    }
                }
                return "Invalid FN_ID";
            }catch (ArrayIndexOutOfBoundsException e){
                return "Missing arguments";
            }
        }
    }
}