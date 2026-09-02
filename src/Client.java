import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * CLI Client application for communicating with the Messaging Server.
 * Sends user commands over temporary TCP socket connections.
 */
public class Client {
    public static void main (String [] args) {
        Scanner scanner = new Scanner(System.in);
        String msg = null;
        String []parts=null;

        // Prompt user to enter command formatted as: <IP> <PORT> <FN_ID> [ARGS...]
        msg=scanner.nextLine();
        parts=msg.split( " ");

        // Continue running until user inputs 'exit'
        while (!"exit".equalsIgnoreCase(msg)) {
            // Establishes a new connection to the server for each command
            try (Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]))) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Send the raw command string to the serve
                out.println(msg);
                out.flush();

                // Read server response
                String response= in.readLine();

                // Server sends response lines separated by '@', replace '@' with newlines
                String []responseParts=response.split("@");
                String responseFormat="";
                int pointer=0;
                for(String part:responseParts){
                    pointer+=1;
                    responseFormat=responseFormat+part;
                    if(pointer<responseParts.length){
                        responseFormat=responseFormat+"\n";
                    }
                }

                // Print formatted server response
                System.out.println(responseFormat);
            } catch (IOException e) {
                System.out.println("Wrong connection credentials");
            }
            // Read next command from standard input
            msg=scanner.nextLine();
            parts=msg.split( " ");
        }
        scanner.close();
    }
}