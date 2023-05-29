package com.programm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import com.programm.User.User;
import com.programm.User.UserDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Server {
    private static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private final UserDatabase userDatabase;
    private HttpServer httpServer;

    public Server() {
        userDatabase = new UserDatabase();
    }

    public void startServer() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
            httpServer.createContext("/register", new RegisterHandler());
            httpServer.createContext("/authorize", new AuthorizationHandler());
            httpServer.setExecutor(null);
            httpServer.start();
            initUserDatabase();
            System.out.println("Server started on port 8080");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initUserDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS users " +
                    "(id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(255) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP(), " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP())";
            statement.executeUpdate(createTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void printUserDatabase() {
        userDatabase.printDatabaseContents();
    }

    public void stopServer() {
        if (httpServer != null) {
            httpServer.stop(0);
            System.out.println("Server stopped");
        }
    }

    private void registerUser(User user) {
        boolean isUnique = userDatabase.isDataUnique(user);
        if (isUnique) {
            userDatabase.addUserToDatabase(user);
            System.out.println("Add user to DB");
        } else {
            System.out.println("Cant add user to DB");
        }
        this.printUserDatabase();
    }

    private boolean isValidEmail(User user) {
        return user.getEmail().contains("@");
    }

    private String extractValueFromJson(String jsonData, String key) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
            return (String) jsonObject.get(key);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("POST")) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                isr.close();

                String requestData = sb.toString().trim();

                User user = new User();
                user.setUsername(extractValueFromJson(requestData, "username"));
                user.setEmail(extractValueFromJson(requestData, "email"));
                user.setPasswordHash(extractValueFromJson(requestData, "password"));
                user.setRole(extractValueFromJson(requestData, "role"));

                String response;
                int responseCode;
                if (userDatabase.isDataUnique(user) && isValidEmail(user)) {
                    response = "Registration successful";
                    responseCode = 200;
                    registerUser(user);
                    userDatabase.updateUserByEmail(user.getEmail(), LocalDateTime.now(), "update");
                } else if (userDatabase.isDataUnique(user)) {
                    response = "Wrong email address";
                    responseCode = 400;
                } else {
                    response = "Username or email already exists!";
                    responseCode = 409;
                }

                exchange.sendResponseHeaders(responseCode, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
    private class AuthorizationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("POST")) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                isr.close();

                String requestData = sb.toString().trim();

                String email = extractValueFromJson(requestData, "email");
                String password = extractValueFromJson(requestData, "password");

                String response;
                int responseCode;
                if (userDatabase.isUserExists(email, password)) {
                    response = "Authorization successful";
                    userDatabase.updateUserByEmail(email, LocalDateTime.now(), "update");
                    responseCode = 200;
                    printUserDatabase();
                } else {
                    response = "Wrong email address";
                    responseCode = 400;
                }
                exchange.sendResponseHeaders(responseCode, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}