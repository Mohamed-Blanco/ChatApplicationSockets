package com.example.chat_application.Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.example.chat_application.DatabaseManager;
import com.example.chat_application.Client.ClientDAO;

public class ServerController {
    private final Connection connection;
    private final DatabaseManager dbManager;

    public ServerController() {
        dbManager = new DatabaseManager();
        this.connection = dbManager.getConnection();
    }

    public boolean registerUser(String username, String password, String email) {
        if (dbManager.doesUserExist(username)) return false;

        String query = "INSERT INTO Users (username, password, email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ClientDAO getClientByUsername(String username) {
        String query = "SELECT * FROM Users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new ClientDAO(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getDate("created_at")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addFriend(String fromUsername, String toUsername) {
        int fromId = dbManager.getUserIdByUsername(fromUsername);
        int toId = dbManager.getUserIdByUsername(toUsername);

        if (fromId == -1 || toId == -1 || fromId == toId) return false;

        String check = "SELECT * FROM Friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(check)) {
            stmt.setInt(1, fromId);
            stmt.setInt(2, toId);
            stmt.setInt(3, toId);
            stmt.setInt(4, fromId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String insert = "INSERT INTO Friends (user_id, friend_id, status) VALUES (?, ?, 'accepted')";
        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
            stmt.setInt(1, fromId);
            stmt.setInt(2, toId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendPrivateMessage(int senderId, int receiverId, String message) {
        int chatId = getOrCreateChatId(senderId, receiverId);
        if (chatId == -1) return false;

        String insert = "INSERT INTO Messages (chat_id, sender_id, message) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
            stmt.setInt(1, chatId);
            stmt.setInt(2, senderId);
            stmt.setString(3, message);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getOrCreateChatId(int user1Id, int user2Id) {
        String select = "SELECT id FROM Chats WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.setInt(3, user2Id);
            stmt.setInt(4, user1Id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");

            String insert = "INSERT INTO Chats (user1_id, user2_id) VALUES (?, ?)";
            PreparedStatement insertStmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setInt(1, user1Id);
            insertStmt.setInt(2, user2Id);
            insertStmt.executeUpdate();
            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
