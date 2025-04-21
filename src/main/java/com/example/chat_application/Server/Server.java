package com.example.chat_application.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.chat_application.Client.ClientDAO;

public class Server {
    private static final int PORT = 8080;
    private static final int MAX_CLIENTS = 10;
    private static final List<ConnectedClient> clients = Collections.synchronizedList(new ArrayList<>());

    static class ConnectedClient {
        Socket socket;
        ClientDAO user;
        PrintWriter out;
        BufferedReader in;

        public ConnectedClient(Socket socket, ClientDAO user) throws IOException {
            this.socket = socket;
            this.user = user;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public String getUsername() {
            return user.getUsername();
        }

        public int getUserId() {
            return user.getId();
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ServerController controller = new ServerController();

            ClientDAO user = null;
            String line = in.readLine();

            if (line.startsWith("LOGIN:")) {
                String[] parts = line.split(":", 3);
                String username = parts[1];
                String password = parts[2];

                user = controller.getClientByUsername(username);
                if (user != null && user.getPassword().equals(password)) {
                    out.println("LOGIN_SUCCESS");
                } else {
                    out.println("LOGIN_FAIL");
                    socket.close();
                    return;
                }

            } else if (line.startsWith("REGISTER:")) {
                String[] parts = line.split(":", 4);
                String username = parts[1];
                String password = parts[2];
                String email = parts[3];

                if (controller.registerUser(username, password, email)) {
                    user = controller.getClientByUsername(username);
                    out.println("REGISTER_SUCCESS");
                } else {
                    out.println("REGISTER_FAIL");
                    socket.close();
                    return;
                }

            } else {
                out.println("INVALID_COMMAND");
                socket.close();
                return;
            }

            ConnectedClient client = new ConnectedClient(socket, user);
            synchronized (clients) {
                clients.add(client);
            }

            out.println("Bienvenue " + client.getUsername() + " !");

            String message;
            while ((message = in.readLine()) != null) {

                if (message.startsWith("ADD_FRIEND:")) {
                    String friendName = message.split(":", 2)[1];
                    boolean result = controller.addFriend(client.getUsername(), friendName);
                    out.println(result ? "Ami ajouté : " + friendName : "Erreur ajout ami.");
                    continue;
                }

                if (message.startsWith("PRIVATE:")) {
                    String[] parts = message.split(":", 3);
                    String target = parts[1];
                    String msg = parts[2];

                    ClientDAO targetUser = controller.getClientByUsername(target);
                    if (targetUser != null) {
                        controller.sendPrivateMessage(client.getUserId(), targetUser.getId(), msg);
                        out.println("Message privé envoyé à " + target);
                    } else {
                        out.println("Utilisateur non trouvé.");
                    }
                    continue;
                }

                broadcast(client.getUsername() + ": " + message, client.socket);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        }
    }

    private static void broadcast(String message, Socket sender) {
        synchronized (clients) {
            for (ConnectedClient c : clients) {
                if (!c.socket.equals(sender)) {
                    c.out.println(message);
                }
            }
        }
    }
}
