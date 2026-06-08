package com.attendance;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Main Selection - Optimized UI
 * साईज कमी केली आहे जेणेकरून खालचे बटन्स टास्कबारमुळे लपणार नाहीत.
 */
public class MainSelection extends Application {

    @Override
    public void start(Stage primaryStage) {
        // --- UI Header ---
        Label title = new Label("🎯 AI Attendance System");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subTitle = new Label("Select a module to proceed");
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        // --- Styles & Effects ---
        DropShadow shadow = new DropShadow(5, Color.GRAY);
        String blueBtn = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-size: 16px;";
        String greenBtn = "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-size: 16px;";

        // १. अटेंडन्स विंडो बटण
        Button liveAttendanceBtn = new Button("📸 Start Live Attendance");
        liveAttendanceBtn.setPrefSize(350, 60); // हाईट ८० वरून ६० केली
        liveAttendanceBtn.setStyle(blueBtn);
        liveAttendanceBtn.setEffect(shadow);

        liveAttendanceBtn.setOnAction(e -> {
            try {
                primaryStage.hide();
                new AttendanceSystem().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // २. विद्यार्थी नोंदणी बटण
        Button studentBtn = new Button("👨‍🎓 Manage Registration");
        studentBtn.setPrefSize(350, 60); // हाईट ६० केली
        studentBtn.setStyle(greenBtn);
        studentBtn.setEffect(shadow);

        studentBtn.setOnAction(e -> {
            System.out.println("ℹ️ Registration Section clicked.");
        });

        // --- Layout Design (Optimized) ---
        // Spacing ३० वरून १५ केली जेणेकरून लेआउट कॉम्पॅक्ट होईल
        VBox layout = new VBox(15, title, subTitle, liveAttendanceBtn, studentBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30)); // Padding कमी केली
        layout.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ecf0f1; -fx-border-width: 2; -fx-border-radius: 15; -fx-background-radius: 15;");

        // विंडोची साईज ७००x६०० वरून ६००x५०० केली
        Scene scene = new Scene(layout, 600, 500);
        primaryStage.setTitle("Smart Attendance Pro - Home Menu");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}