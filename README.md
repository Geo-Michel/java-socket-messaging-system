# Multi-Threaded Messaging Application

A Java-based client-server messaging system utilizing TCP Sockets and multi-threading. The application enables users to register accounts, authenticate using generated authentication tokens, list registered users, send direct messages, view inbox messages, read specific messages, and delete messages.

---

## Architecture Overview

The system consists of two primary runtime components (Server and Client) and two data structures (`Account` and `Message`):

- **Server (`Server.java`)**: Listens for incoming TCP connections on a specified port. Spawns a new worker thread (`ClientHandler`) for every client request to allow concurrent client processing.
- **Client (`Client.java`)**: A CLI application that connects to the server on a per-command basis, transmits user input, and displays responses.
- **Account (`Account.java`)**: Represents a registered user with a unique username, an auto-generated authentication token (`authToken`), and an inbox message list.
- **Message (`Message.java`)**: Encapsulates message data, including sender, recipient, content body, unique message ID, and read status.

---

## Server Protocol & Features

Client input commands are parsed by space-separated arguments. The expected syntax for running commands from the client is:

```
<host> <port> <FN_ID> [args...]
```

### Supported Functions (`FN_ID`)

| FN_ID | Action | Arguments | Description |
|---|---|---|---|
| **1** | Create Account | `<username>` | Registers a new user with an alphanumeric username and returns a unique 5-digit authentication token. |
| **2** | Show Accounts | `<authToken>` | Returns a list of all registered accounts except the requester's. |
| **3** | Send Message | `<authToken> <recipient> <body...>` | Sends a message to the specified recipient. |
| **4** | Show Inbox | `<authToken>` | Lists all messages in the user's inbox (marked with `*` if unread). |
| **5** | Read Message | `<authToken> <message_id>` | Reads a specific message by ID and marks it as read (`read = true`). |
| **6** | Delete Message | `<authToken> <message_id>` | Deletes a specific message from the user's inbox. |

> **Note:** The server responds using `@` as a line separator, which the client converts into standard multi-line console output.

---

## Getting Started

### Prerequisites
- **Java Development Kit (JDK)** version 8 or higher installed.

### Compilation
Compile all Java source files:
```bash
javac *.java
```

### Running the Application

1. **Start the Server:**
   ```bash
   java Server
   ```
   *The server will prompt you to enter a port number (e.g., `8080`).*

2. **Start the Client:**
   In a separate terminal window, launch the client:
   ```bash
   java Client
   ```

---

## Usage Examples

Below are typical interactive commands executed in the client CLI:

1. **Create an Account:**
   ```
   localhost 8080 1 Alice
   ```
   *Output:* `48291` *(Authentication Token)*

2. **Create a Second Account:**
   ```
   localhost 8080 1 Bob
   ```
   *Output:* `91024` *(Authentication Token)*

3. **List Accounts (as Alice):**
   ```
   localhost 8080 2 48291
   ```
   *Output:*
   `1. Bob`

4. **Send a Message from Alice to Bob:**
   ```
   localhost 8080 3 48291 Bob Hello Bob, welcome to the platform!
   ```
   *Output:* `OK`

5. **Show Bob's Inbox:**
   ```
   localhost 8080 4 91024
   ```
   *Output:* `1. from: Alice*` *(The `*` denotes an unread message)*

6. **Read Message (Message ID: 1):**
   ```
   localhost 8080 5 91024 1
   ```
   *Output:* `(Alice)Hello Bob, welcome to the platform!`

7. **Delete Message (Message ID: 1):**
   ```
   localhost 8080 6 91024 1
   ```
   *Output:* `OK`

8. **Exit Client:**
   ```
   exit
   ```