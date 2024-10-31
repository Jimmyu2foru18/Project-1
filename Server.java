import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 5000;
    private static final Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    static class ChatRoom {
        private final String name;
        private String topic;
        private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

        public ChatRoom(String name) {
            this.name = name;
            this.topic = "Welcome to " + name;
        }

        public void setTopic(String topic) {
            this.topic = topic;
            broadcast("Room topic changed to: " + topic, null);
        }

        public String getTopic() {
            return topic;
        }

        public void broadcast(String message, ClientHandler sender) {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            String formattedMessage = String.format("[%s] %s", timestamp, message);
            
            clients.forEach(client -> {
                if (client != sender) {
                    client.sendMessage(formattedMessage);
                }
            });
        }

        public void addClient(ClientHandler client) {
            clients.add(client);
            broadcast(client.getUsername() + " joined the room", client);
        }

        public void removeClient(ClientHandler client) {
            clients.remove(client);
            broadcast(client.getUsername() + " left the room", client);
        }

        public Set<String> getUsers() {
            Set<String> users = new HashSet<>();
            clients.forEach(client -> users.add(client.getUsername()));
            return users;
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        private String username;
        private ChatRoom currentRoom;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
        }

        public String getUsername() {
            return username;
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        @Override
        public void run() {
            try {
                handleClient();
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void handleClient() throws IOException {
            // Handle username setup and commands
            username = in.readLine();
            sendMessage("Welcome " + username + "! Type /help for commands.");

            String input;
            while ((input = in.readLine()) != null) {
                if (input.startsWith("/")) {
                    handleCommand(input);
                } else if (currentRoom != null) {
                    currentRoom.broadcast(username + ": " + input, this);
                } else {
                    sendMessage("Join a room first using /join <room>");
                }
            }
        }

        private void handleCommand(String command) {
            String[] parts = command.split("\\s+", 2);
            switch (parts[0].toLowerCase()) {
                case "/join":
                    if (parts.length < 2) {
                        sendMessage("Usage: /join <room>");
                        return;
                    }
                    joinRoom(parts[1]);
                    break;
                case "/leave":
                    leaveCurrentRoom();
                    break;
                case "/list":
                    listRooms();
                    break;
                case "/users":
                    listUsers();
                    break;
                case "/topic":
                    if (parts.length < 2) {
                        sendMessage("Usage: /topic <new topic>");
                        return;
                    }
                    setRoomTopic(parts[1]);
                    break;
                case "/info":
                    showRoomInfo();
                    break;
                case "/help":
                    showHelp();
                    break;
                default:
                    sendMessage("Unknown command. Type /help for available commands.");
            }
        }

        private void cleanup() {
            try {
                leaveCurrentRoom();
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }

        // Command handler methods...
        private void joinRoom(String roomName) {
            leaveCurrentRoom();
            chatRooms.computeIfAbsent(roomName, ChatRoom::new);
            currentRoom = chatRooms.get(roomName);
            currentRoom.addClient(this);
            sendMessage("Joined room: " + roomName);
        }

        private void leaveCurrentRoom() {
            if (currentRoom != null) {
                currentRoom.removeClient(this);
                currentRoom = null;
            }
        }

        private void listRooms() {
            sendMessage("Available rooms: " + String.join(", ", chatRooms.keySet()));
        }

        private void listUsers() {
            if (currentRoom != null) {
                sendMessage("Users in room: " + String.join(", ", currentRoom.getUsers()));
            } else {
                sendMessage("You're not in any room.");
            }
        }

        private void setRoomTopic(String newTopic) {
            if (currentRoom != null) {
                currentRoom.setTopic(newTopic);
                sendMessage("Room topic updated successfully.");
            } else {
                sendMessage("You must be in a room to set a topic.");
            }
        }

        private void showRoomInfo() {
            if (currentRoom != null) {
                sendMessage("Room: " + currentRoom.name + "\n" +
                           "Topic: " + currentRoom.getTopic() + "\n" +
                           "Users: " + String.join(", ", currentRoom.getUsers()));
            } else {
                sendMessage("You're not in any room.");
            }
        }

        private void showHelp() {
            sendMessage("Available commands:\n" +
                    "/join <room> - Join a chat room\n" +
                    "/leave - Leave current room\n" +
                    "/list - List available rooms\n" +
                    "/users - Show users in current room\n" +
                    "/topic <text> - Set room topic\n" +
                    "/info - Show current room information\n" +
                    "/help - Show this help message");
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                executorService.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }
}
