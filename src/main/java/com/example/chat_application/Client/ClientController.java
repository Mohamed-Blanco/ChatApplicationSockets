package com.example.chat_application.Client;

import java.io.*;
import java.net.Socket;

public class ClientController {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientController(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.username = username;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }


    public void addFriend(String friendUsername) {
        out.println("ADD_FRIEND:" + friendUsername);
    }

    public void sendPrivateMessage(String friendUsername, String message) {
        out.println("PRIVATE:" + friendUsername + ":" + message);
    }

    public void receiveMessages() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("\n[Server] " + message);
                    System.out.print("you: ");
                }
            } catch (IOException e) {
                System.out.println("Déconnecté du serveur.");
            }
        }).start();
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void close() {
        try {
            socket.close();
            out.close();
            in.close();
        } catch (IOException e) {
            System.out.println("Erreur lors de la fermeture : " + e.getMessage());
        }
    }
}
