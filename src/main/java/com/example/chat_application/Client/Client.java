package com.example.chat_application.Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (
                Socket clientSocket = new Socket(SERVER_IP, PORT);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {

            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.print("Choix: ");
            String choice = scanner.nextLine();

            String username;
            if (choice.equals("1")) {
                System.out.print("Nom d'utilisateur: ");
                username = scanner.nextLine();
                System.out.print("Mot de passe: ");
                String password = scanner.nextLine();
                out.println("LOGIN:" + username + ":" + password);
            } else {
                System.out.print("Nom d'utilisateur: ");
                username = scanner.nextLine();
                System.out.print("Mot de passe: ");
                String password = scanner.nextLine();
                System.out.print("Email: ");
                String email = scanner.nextLine();
                out.println("REGISTER:" + username + ":" + password + ":" + email);
            }

            String response = in.readLine();
            if (!response.equals("LOGIN_SUCCESS") && !response.equals("REGISTER_SUCCESS")) {
                System.out.println("❌ " + response);
                return;
            }

            System.out.println("✅ Connecté au serveur en tant que " + username);
            ClientController controller = new ClientController(clientSocket, username);
            controller.receiveMessages();
            while (true) {
                System.out.println("\n1. Envoyer message général");
                System.out.println("2. Ajouter un ami");
                System.out.println("3. Envoyer un message privé");
                System.out.print("Choix: ");
                String action = scanner.nextLine();

                switch (action) {
                    case "1" -> {
                        System.out.print("Message : ");
                        String msg = scanner.nextLine();
                        controller.sendMessage(msg);
                    }
                    case "2" -> {
                        System.out.print("Nom de l'ami : ");
                        String friend = scanner.nextLine();
                        controller.addFriend(friend);
                    }
                    case "3" -> {
                        System.out.print("Nom de l'ami : ");
                        String friend = scanner.nextLine();
                        System.out.print("Message privé : ");
                        String msg = scanner.nextLine();
                        controller.sendPrivateMessage(friend, msg);
                    }
                    default -> System.out.println("⛔ Choix invalide !");
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Connexion échouée : " + e.getMessage());
        }
    }
}
