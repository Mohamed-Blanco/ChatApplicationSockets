package com.example.chat_application.Client;

import java.sql.Date;

public class ClientDAO {
    private int id;
    private String username;
    private String password;
    private String email;
    private Date date;
    public ClientDAO(int id, String username, String password, String email, Date date) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.date = new Date(date.getTime());
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    @Override
    public String toString() {
        return "ClientDAO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", date=" + date +
                '}';
    }

}