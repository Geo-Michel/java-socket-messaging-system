/**
 * Represents a direct message sent from one account to another.
 */
public class Message {
    private boolean read=false; // Tracks whether the recipient has read the message
    private String sender, receiver, body;
    private int id; // Account-specific unique identifier for the message0

    /**
     * Constructs a Message instance.
     *
     * @param sender The username of the sender
     * @param receiver The username of the recipient
     * @param body The text content of the message
     */
    public Message(String sender, String receiver, String body) {
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
    }

    /**
     * @return true if the message has been read by the recipient, false otherwise
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Updates the read status of the message.
     *
     * @param read Status indicating whether the message has been read
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * @return The username of the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return The username of the recipient
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * @return The body text of the message
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the unique inbox ID for the message.
     *
     * @param id Message ID inside the recipient's inbox
     */
    public void setId(int id){
        this.id=id;
    }

    /**
     * @return The unique inbox ID of the message
     */
    public int getId(){
        return id;
    }
}
