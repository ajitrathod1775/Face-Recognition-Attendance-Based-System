package com.attendance;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * StudentRegistration - UI for Registering Students
 * ✅ Added: Email Input for Parent Alerts
 * ✅ Fixed: DatabaseManager parameter mismatch
 */
public class StudentRegistration {

    private DatabaseManager dbManager;

    public StudentRegistration() {
        this.dbManager = new DatabaseManager();
    }

    public void display() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Student Registration");
        window.setMinWidth(400);

        Label label = new Label("Register New Student");
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField idInput = new TextField();
        idInput.setPromptText("Enter Student ID (e.g. S101)");
        idInput.setPrefHeight(35);

        TextField nameInput = new TextField();
        nameInput.setPromptText("Enter Full Name");
        nameInput.setPrefHeight(35);

        // ✅ NEW: पालकांचा ईमेल घेण्यासाठी फील्ड (ईमेल अलर्टसाठी आवश्यक)
        TextField emailInput = new TextField();
        emailInput.setPromptText("Enter Parent Email (for alerts)");
        emailInput.setPrefHeight(35);

        Button registerBtn = new Button("Register Student");
        registerBtn.setPrefWidth(200);
        registerBtn.setPrefHeight(40);
        registerBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        registerBtn.setOnAction(e -> {
            String id = idInput.getText().trim();
            String name = nameInput.getText().trim();
            String email = emailInput.getText().trim();

            if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Error", "Please fill all fields, including Email!");
                return;
            }

            // ईमेल व्हॅलिडेशन (Basic Check)
            if (!email.contains("@") || !email.contains(".")) {
                showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please enter a valid email address!");
                return;
            }

            // ✅ CALL: आता ३ पॅरामीटर्स पाठवले जात आहेत, ज्यामुळे DatabaseManager ला एरर येणार नाही
            boolean success = dbManager.registerStudent(id, name, email);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student Registered Successfully: " + name);
                window.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save student in Database.");
            }
        });

        VBox layout = new VBox(20, label, idInput, nameInput, emailInput, registerBtn);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #ecf0f1;");

        window.setScene(new Scene(layout));
        window.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}