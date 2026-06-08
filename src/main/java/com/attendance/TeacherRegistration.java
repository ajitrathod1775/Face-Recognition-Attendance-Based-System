package com.attendance;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Teacher Registration - Updated with Success Pop-up
 */
public class TeacherRegistration extends Application {
    private DatabaseManager dbManager;

    @Override
    public void start(Stage primaryStage) {
        dbManager = new DatabaseManager();

        Label header = new Label("Teacher Registration");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField idField = new TextField();
        idField.setPromptText("Enter Teacher ID");
        idField.setPrefHeight(40);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter Full Name");
        nameField.setPrefHeight(40);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Create Login Password");
        passField.setPrefHeight(40);

        Button regBtn = new Button("Register Teacher");
        regBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        regBtn.setPrefWidth(200);
        regBtn.setPrefHeight(45);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-weight: bold;");

        regBtn.setOnAction(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String password = passField.getText().trim();

            if (id.isEmpty() || name.isEmpty() || password.isEmpty()) {
                statusLabel.setText("❌ All fields are required!");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            // Database मध्ये माहिती सेव्ह करण्याचा प्रयत्न
            boolean success = dbManager.registerTeacher(id, name, password);

            if (success) {
                // ✅ यशस्वी झाल्यावर स्टेटस लेबल अपडेट करा
                statusLabel.setText("✅ Teacher Registered Successfully!");
                statusLabel.setStyle("-fx-text-fill: #27ae60;");

                // ✅ पॉप-अप मेसेज दाखवा
                showDialog(Alert.AlertType.INFORMATION, "Success", "Teacher Registered Successfully!");

                idField.clear();
                nameField.clear();
                passField.clear();
            } else {
                statusLabel.setText("❌ Registration Failed. Try again.");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                showDialog(Alert.AlertType.ERROR, "Failed", "Registration failed. Check Database connection.");
            }
        });

        VBox layout = new VBox(20, header, idField, nameField, passField, regBtn, statusLabel);
        layout.setPadding(new Insets(50));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #ecf0f1;");

        primaryStage.setTitle("Smart Attendance - Teacher Registration");
        primaryStage.setScene(new Scene(layout, 450, 500));
        primaryStage.show();
    }

    // पॉप-अप विंडो दाखवण्यासाठी हेल्पपर मेथड
    private void showDialog(Alert.AlertType type, String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}