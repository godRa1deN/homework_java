package com.programm;

import com.programm.User.User;
import com.programm.User.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
        SwingUtilities.invokeLater(() -> {
            ModeSelectionFrame modeSelectionFrame = new ModeSelectionFrame();
            modeSelectionFrame.setVisible(true);
        });
    }
}

class ModeSelectionFrame extends JFrame {
    public ModeSelectionFrame() {
        setTitle("Mode Selection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ModeSelectionPanel modeSelectionPanel = new ModeSelectionPanel();
        add(modeSelectionPanel);

        pack();
        setLocationRelativeTo(null);
    }
}

class RegistrationFrame extends JFrame implements ActionListener {
    private final JTextField usernameField;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JComboBox<String> roleComboBox;
    private final JLabel resultLabel;

    public RegistrationFrame() {
        setTitle("Registration Form");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(6, 2));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        JLabel roleLabel = new JLabel("Role:");
        roleComboBox = new JComboBox<>(new String[]{"Client", "Manager"});
        JButton registerButton = new JButton("Register");
        resultLabel = new JLabel();

        registerButton.addActionListener(this);

        add(usernameLabel);
        add(usernameField);
        add(emailLabel);
        add(emailField);
        add(passwordLabel);
        add(passwordField);
        add(roleLabel);
        add(roleComboBox);
        add(new JLabel());
        add(registerButton);
        add(new JLabel());
        add(resultLabel);

        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Register")) {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();
            User user = new User();
            UserService userService = new UserService();
            user = userService.setUserData(user, username, email, password, role);

            try {
                URL url = new URL("http://localhost:8080/register");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String requestBody = "{ \"username\": \"" + user.getUsername() + "\", \"email\": \"" + user.getEmail() +
                        "\", \"password\": \"" + user.getPasswordHash() + "\", \"role\": \"" + user.getRole() + "\" }";
                byte[] requestBodyBytes = requestBody.getBytes(StandardCharsets.UTF_8);

                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Length", String.valueOf(requestBodyBytes.length));

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(requestBodyBytes);
                outputStream.close();

                int responseCode = connection.getResponseCode();
                String response;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    response = "Registration successful";
                } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                    response = "Wrong email address";
                } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                    response = "Username or email already exists!";
                } else {
                    response = "Unknown error occurred";
                }

                resultLabel.setText(response);

                connection.disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
                resultLabel.setText("Error occurred during registration");
            }
        }
    }
}

class AuthorizationFrame extends JFrame implements ActionListener {
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JLabel resultLabel;

    public AuthorizationFrame() {
        setTitle("Authorization Form");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(4, 2));

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        JButton authorizeButton = new JButton("Authorize");
        resultLabel = new JLabel();

        authorizeButton.addActionListener(this);

        add(emailLabel);
        add(emailField);
        add(passwordLabel);
        add(passwordField);
        add(new JLabel());
        add(authorizeButton);
        add(new JLabel());
        add(resultLabel);

        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Authorize")) {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            try {
                URL url = new URL("http://localhost:8080/authorize");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String requestBody = "{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }";
                byte[] requestBodyBytes = requestBody.getBytes(StandardCharsets.UTF_8);

                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Length", String.valueOf(requestBodyBytes.length));

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(requestBodyBytes);
                outputStream.close();

                int responseCode = connection.getResponseCode();
                String response;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    response = "Authorization successful";
                } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                    response = "Wrong data";
                } else {
                    response = "Unknown error occurred";
                }
                resultLabel.setText(response);
                connection.disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
                resultLabel.setText("Error occurred during registration");
            }
        }
    }
}

class ModeSelectionPanel extends JPanel implements ActionListener {
    private final JButton registrationButton;
    private final JButton authorizationButton;

    public ModeSelectionPanel() {
        setLayout(new FlowLayout());

        registrationButton = new JButton("Registration");
        registrationButton.addActionListener(this);
        authorizationButton = new JButton("Authorization");
        authorizationButton.addActionListener(this);

        add(registrationButton);
        add(authorizationButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registrationButton) {
            RegistrationFrame registrationFrame = new RegistrationFrame();
            registrationFrame.setVisible(true);
        } else if (e.getSource() == authorizationButton) {
            AuthorizationFrame authorizationFrame = new AuthorizationFrame();
            authorizationFrame.setVisible(true);
        }
    }
}