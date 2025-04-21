package com.example.chat_application.Server;


import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Server { // Capitalized class name
    private static final int PORT = 8080;
    private static final int MAX_CLIENTS = 10;
    private static final List<Client> clients = Collections.synchronizedList(new ArrayList<>());
    private static final Random random = new Random();

    static class Client {
        Socket socket;
        String name;
        PrintWriter out;
        BufferedReader in;
        int count; // Number of attempts made

        Client(Socket socket, String name) throws IOException {
            this.socket = socket;
            this.name = name;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.count = 0;
        }
    }

    static class ClientHandler extends Thread {
        private final Client client;
        private final int randomNumber; // Unique random number for this client
        private static final int MAX_ATTEMPTS = 3;

        ClientHandler(Client client) {
            this.client = client;
            this.randomNumber = random.nextInt(10); // 0-9, matching your original range
        }

        private void broadcastMessage(String message, Socket senderSocket) {
            synchronized (clients) {
                for (Client c : clients) {
                    if (!c.socket.equals(senderSocket)) {
                        c.out.println(message);
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                System.out.println(client.name + " has joined the chat.");
                broadcastMessage(client.name + " has joined the chat.", client.socket);
                client.out.println("Guess a number between 0 and 9. You have " + MAX_ATTEMPTS + " attempts.");

                String message;
                while ((message = client.in.readLine()) != null) { // Keep connection alive
                    String fullMessage = client.name + ": " + message;
                    System.out.print(fullMessage);
                    broadcastMessage(fullMessage, client.socket);

                    if (client.count < MAX_ATTEMPTS) {
                        try {
                            int clientNumber = Integer.parseInt(message);
                            boolean response = (clientNumber == randomNumber);
                            client.out.println(response); // Send boolean response

                            if (response) {
                                client.out.println("Correct! The number was " + randomNumber);
                                client.count = MAX_ATTEMPTS; // End guessing
                            } else {
                                client.count++;
                                int attemptsLeft = MAX_ATTEMPTS - client.count;
                                if (attemptsLeft > 0) {
                                    client.out.println("Wrong! " + attemptsLeft + " attempts left.");
                                    System.out.println("\nThe number is not correct. " + client.name + " has " + attemptsLeft + " chances left.");
                                } else {
                                    client.out.println("Game over! The number was " + randomNumber);
                                    System.out.println("\n" + client.name + " is out of attempts.");
                                }
                            }
                        } catch (NumberFormatException e) {
                            client.out.println("false");
                            client.out.println("Invalid input. Please enter a number.");
                        }
                    } else {
                        client.out.println("You've used all attempts. You can now chat.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    synchronized (clients) {
                        clients.remove(client);
                    }
                    String leaveMessage = client.name + " has left the chat.";
                    System.out.print(leaveMessage);
                    broadcastMessage(leaveMessage, client.socket);

                    client.out.close();
                    client.in.close();
                    client.socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing client resources: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader tempIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String name = tempIn.readLine();

                Client client = new Client(clientSocket, name);

                synchronized (clients) {
                    if (clients.size() < MAX_CLIENTS) {
                        clients.add(client);
                        ClientHandler handler = new ClientHandler(client);
                        handler.start();
                        System.out.println("Generated number for " + name + ": " + ((ClientHandler) handler).randomNumber);
                    } else {
                        PrintWriter tempOut = new PrintWriter(clientSocket.getOutputStream(), true);
                        tempOut.println("Server full. Connection rejected.");
                        clientSocket.close();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}