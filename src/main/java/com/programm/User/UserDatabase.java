package com.programm.User;

import java.sql.*;
import java.time.LocalDateTime;

public class UserDatabase {
    private static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

    public String getDbUrl() {
        return DB_URL;
    }

    public boolean isDataUnique(User user) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL);
            String checkDataQuery = "SELECT * FROM users WHERE username = ? OR email = ?";
            PreparedStatement statement = connection.prepareStatement(checkDataQuery);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                result.close();
                statement.close();
                connection.close();
                return false;
            }
            result.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true; // Данные уникальны
    }

    public boolean isUserExists(String email, String password) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL);
            UserService userService = new UserService();
            String checkUserQuery = "SELECT * FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(checkUserQuery);
            statement.setString(1, email);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                String passwordHashFromDb = result.getString("password_hash");
                if (userService.isPasswordCorrect(password, passwordHashFromDb)) {
                    result.close();
                    statement.close();
                    connection.close();
                    return true;
                }
            }
            result.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateUserByEmail(String email, LocalDateTime time, String type) {
        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            String updateQuery;
            if (type.equals("update")) {
                updateQuery = "UPDATE users SET updated_at = ? WHERE email = ?";
            } else {
                updateQuery = "UPDATE users SET create_at = ? WHERE email = ?";
            }
            PreparedStatement statement = connection.prepareStatement(updateQuery);
            statement.setString(1, String.valueOf(time));
            statement.setString(2, email);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUserToDatabase(User user) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL);
            String insertQuery = "INSERT INTO users (username, email, password_hash, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getEmail());
                preparedStatement.setString(3, user.getPasswordHash());
                preparedStatement.setString(4, user.getRole());
                preparedStatement.executeUpdate();
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printDatabaseContents() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM users";
            ResultSet resultSet = statement.executeQuery(query);
            System.out.println("Database Contents:");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                String passwordHash = resultSet.getString("password_hash");
                String role = resultSet.getString("role");
                String created_at = resultSet.getString("created_at");
                String updated_at = resultSet.getString("updated_at");

                System.out.println("ID: " + id);
                System.out.println("Username: " + username);
                System.out.println("Email: " + email);
                System.out.println("Password Hash: " + passwordHash);
                System.out.println("Role: " + role);
                System.out.println("Created at: " + created_at);
                System.out.println("Updated at: " + updated_at);
                System.out.println();
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}