import java.util.ArrayList;
/**
 * Represents a user account within the messaging system.
 * Holds user credentials (username, auth token) and manages the user's inbox.
 */
public class Account {
    private String username;
    final private int authToken;
    ArrayList<Message> messageBox;  // List storing incoming messages for this account
    int id =0;  // Internal counter used to assign unique IDs to incoming messages

    /**
     * Constructs a new Account with a username and authentication token.
     *
     * @param username The unique username for the user
     * @param authToken The generated authentication token
     */
    public Account(String username, int authToken) {
        this.username = username;
        this.authToken = authToken;
        messageBox=new ArrayList<>();
    }

    /**
     * @return The username of the account owner
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return The unique authentication token assigned to this account
     */
    public int getAuthToken() {
        return authToken;
    }

    /**
     * Adds a new message to the user's inbox and assigns it an incremental ID.
     *
     * @param message The Message object to be added
     */
    public void addMessage(Message message){
        id +=1;
        message.setId(id);
        messageBox.add(message);
    }

    /**
     * @return The list of all messages in the user's inbox
     */
    public ArrayList<Message> getInbox(){
        return messageBox;
    }

    /**
     * Removes a message from the inbox matching the given ID.
     *
     * @param id The ID of the message to remove
     * @return "OK" if successfully deleted, or "Message does not exist" if not found
     */
    public String removeMessage(int id) {
        int pointer=0;
        for(Message message:messageBox){
            if (message.getId()==id){
                messageBox.remove(pointer);
                return "OK";
            }
            pointer+=1;
        }
        return "Message does not exist";
    }
}
