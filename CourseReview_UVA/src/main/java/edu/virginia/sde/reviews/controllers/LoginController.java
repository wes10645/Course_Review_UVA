package edu.virginia.sde.reviews.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import edu.virginia.sde.reviews.database.DatabaseManager;
import edu.virginia.sde.reviews.Models.User;
import java.io.IOException;
import edu.virginia.sde.reviews.utils.WindowConstants;

public class LoginController {
    private DatabaseManager databaseManager;

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Label error;

    public LoginController() {
        this.databaseManager = new DatabaseManager();
    }

    @FXML
    private void handleLogin() {
        String usernameText = username.getText();
        String passwordText = password.getText();

        if (usernameText.isEmpty() || passwordText.isEmpty()) {
            error.setText("Please enter both username and password");
            return;
        }

        User user = databaseManager.getUser(usernameText, passwordText);
        if (user != null) {
            databaseManager.setCurrentUser(user);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/search.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) username.getScene().getWindow();
                stage.setScene(new Scene(root, WindowConstants.WINDOW_WIDTH, WindowConstants.WINDOW_HEIGHT));
            } catch (IOException e) {
                error.setText("Error loading search screen");
            }
        } else {
            error.setText("Invalid username or password");
        }
    }

    @FXML
    private void handleCreateAccount() {
        String usernameText = username.getText();
        String passwordText = password.getText();

        if (usernameText.isEmpty() || passwordText.isEmpty()) {
            error.setText("Please enter both username and password");
            return;
        }

        if (passwordText.length() < 8) {
            error.setText("Password must be at least 8 characters long");
            return;
        }

        if (databaseManager.userExists(usernameText)) {
            error.setText("Username already exists");
            return;
        }

        if (databaseManager.createUser(usernameText, passwordText)) {
            error.setText("Account created successfully. Please log in.");
            username.clear();
            password.clear();
        } else {
            error.setText("Error creating account");
        }
    }

    @FXML
    private void handleQuit() {
        Stage stage = (Stage) username.getScene().getWindow();
        stage.close();
    }
}