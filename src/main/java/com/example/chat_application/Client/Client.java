package com.example.chat_application.Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client { // Capitalized class name
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 8080;
    private static Socket clientSocket;
    private static String name;
    private static PrintWriter out;
    private static BufferedReader in;

    // Thread to receive messages
    static class MessageReceiver extends Thread {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.print("\r\033[K"); // Clear line first
                    if (message.equals("true")) {
                        System.out.println("Yes, you guessed the correct number! You are the winner!");
                    } else if (message.equals("false")) {
                        System.out.println("No, the number is incorrect.");
                    } else {
                        System.out.println(message); // Print other server messages
                    }
                    System.out.print("you: ");
                    System.out.flush();
                }
            } catch (IOException e) {
                System.out.println("Error receiving message: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Establish connection
            clientSocket = new Socket(SERVER_IP, PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Get user name and send it to server
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your name: ");
            name = scanner.nextLine();
            out.println(name); // Send name to server

            // Start receiver thread
            MessageReceiver receiver = new MessageReceiver();
            receiver.start();

            // Main sending loop
            while (true) {
                System.out.print("you: ");
                System.out.flush();
                String message = scanner.nextLine();
                out.println(message);
            }

        } catch (IOException e) {
            System.out.println("Connection failed: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) clientSocket.close();
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}