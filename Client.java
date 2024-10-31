import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 5000;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private static volatile boolean running = true;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream())), true);
             BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.print("Enter your username: ");
            String username = consoleIn.readLine();
            out.println(username);

            // Message sender
            Future<?> senderFuture = executorService.submit(() -> {
                try {
                    while (running) {
                        String message = consoleIn.readLine();
                        if (message == null || message.equalsIgnoreCase("/quit")) {
                            running = false;
                            break;
                        }
                        out.println(message);
                    }
                } catch (IOException e) {
                    System.err.println("Error sending message: " + e.getMessage());
                    running = false;
                }
            });

            // Message receiver
            Future<?> receiverFuture = executorService.submit(() -> {
                try {
                    String message;
                    while (running && (message = in.readLine()) != null) {
                        if (message.contains("joined the room")) {
                            System.out.println(ANSI_GREEN + message + ANSI_RESET);
                        } else if (message.contains("left the room")) {
                            System.out.println(ANSI_YELLOW + message + ANSI_RESET);
                        } else if (message.startsWith("Available commands:") || 
                                 message.startsWith("Users in room:") ||
                                 message.startsWith("Available rooms:")) {
                            System.out.println(ANSI_BLUE + message + ANSI_RESET);
                        } else {
                            System.out.println(message);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error receiving message: " + e.getMessage());
                    running = false;
                }
            });

            // Wait for either thread to complete
            try {
                senderFuture.get();
                receiverFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error in message handling: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }
}
