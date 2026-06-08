package com.attendance;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginWindow {

    private DatabaseManager dbManager = new DatabaseManager();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Teacher Login - AI System");

        // --- UI Elements ---
        Label title = new Label("🔐 Teacher Login");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Teacher ID / Username");
        usernameField.setStyle("-fx-padding: 10; -fx-border-radius: 5;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-padding: 10; -fx-border-radius: 5;");

        Button loginBtn = new Button("Login to Dashboard");
        loginBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // --- Login Logic ---
        loginBtn.setOnAction(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();

            if (dbManager.validateLogin(user, pass)) {
                stage.close(); // लॉगिन यशस्वी झाले की लॉगिन विंडो बंद करा
                try {
                    new TeacherDashboard().start(new Stage()); // डॅशबोर्ड उघडा
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                errorLabel.setText("❌ Invalid ID or Password!");
            }
        });

        VBox layout = new VBox(15, title, usernameField, passwordField, loginBtn, errorLabel);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f8f9fa;");

        stage.setScene(new Scene(layout, 350, 300));
        stage.show();
    }
}