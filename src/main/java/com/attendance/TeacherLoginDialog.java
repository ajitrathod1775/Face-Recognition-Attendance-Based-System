package com.attendance;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TeacherLoginDialog {

    private final Stage dialogStage;
    private boolean authenticated = false;

    public TeacherLoginDialog(Stage owner) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setTitle("Teacher Login - AI System");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f4f7f6;");

        Label loginHeader = new Label("🔐 Teacher Login");
        loginHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setText("admin"); // SS 2 प्रमाणे डिफॉल्ट

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginBtn = new Button("Login to Dashboard");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");

        loginBtn.setOnAction(e -> {
            // इथे तुझे लॉगिन लॉजिक (admin/admin)
            if (usernameField.getText().equals("admin") && passwordField.getText() != null) {
                authenticated = true;
                dialogStage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Credentials!");
                alert.show();
            }
        });

        layout.getChildren().addAll(loginHeader, usernameField, passwordField, loginBtn);

        Scene scene = new Scene(layout, 350, 250);
        dialogStage.setScene(scene);
    }

    public boolean showAndWait() {
        dialogStage.showAndWait();
        return authenticated;
    }
}