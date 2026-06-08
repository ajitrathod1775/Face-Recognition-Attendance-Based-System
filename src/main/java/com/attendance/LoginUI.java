package com.attendance;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginUI {
    private DatabaseManager dbManager = new DatabaseManager();

    public void start(Stage primaryStage) {
        primaryStage.setTitle("🔐 Teacher Login - AI Attendance");

        Label title = new Label("Teacher Authentication");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setPrefHeight(35);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setPrefHeight(35);

        Button loginBtn = new Button("Login");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Label statusLabel = new Label("");

        loginBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText().trim();

            if (dbManager.validateLogin(user, pass)) {
                // लॉगिन यशस्वी झाले तर डॅशबोर्ड उघडा
                try {
                    new TeacherDashboard().start(new Stage());
                    primaryStage.close();
                } catch (Exception ex) { ex.printStackTrace(); }
            } else {
                statusLabel.setText("❌ Invalid Username or Password!");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        VBox layout = new VBox(15, title, userField, passField, loginBtn, statusLabel);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-width: 2;");

        primaryStage.setScene(new Scene(layout, 350, 400));
        primaryStage.show();
    }
}