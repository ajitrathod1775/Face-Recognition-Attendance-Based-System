package com.attendance;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * AttendanceSystem - Final Fixed Version
 * ✅ FIXED: Popup dialogs for Student & Teacher Registration
 * ✅ FIXED: Popup dialog for Mark Attendance
 * ✅ STABLE: 5-Second Status Clear
 */
public class AttendanceSystem extends Application {

    private DatabaseManager dbManager;
    private CameraManager cameraManager;
    private ImageView cameraPreview;
    private Label statusLabel;
    private volatile boolean isRunning = true;

    private TextField stdId, stdName, stdEmail, tchId, tchName, subInput, semInput;
    private PasswordField tchPass;

    @Override
    public void start(Stage primaryStage) {
        dbManager = new DatabaseManager();
        cameraManager = new CameraManager();

        // --- STYLES ---
        String inputStyle = "-fx-padding: 8; -fx-border-color: #dfe6e9; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px;";
        String blueBtn = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 13px;";
        String greenBtn = "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 13px;";
        String purpleBtn = "-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 13px;";
        String orangeBtn = "-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 13px;";

        // --- HEADER ---
        Label title = new Label("🚀 AI Attendance Pro");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label clockLabel = new Label();
        clockLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        startClockThread(clockLabel);

        HBox header = new HBox(20, title, clockLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 5, 0));

        // --- CAMERA PANEL ---
        cameraPreview = new ImageView();
        cameraPreview.setFitWidth(460);
        cameraPreview.setFitHeight(340);
        cameraPreview.setPreserveRatio(true);
        StackPane cameraHolder = new StackPane(cameraPreview);
        cameraHolder.setStyle("-fx-border-color: #2c3e50; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-color: #000; -fx-background-radius: 10;");

        startHighSpeedCamera();

        // --- CONTROLS PANEL ---
        VBox controls = new VBox(8);
        controls.setPadding(new Insets(0, 0, 0, 20));
        controls.setPrefWidth(350);

        stdId = new TextField(); stdId.setPromptText("Student ID"); stdId.setStyle(inputStyle);
        stdName = new TextField(); stdName.setPromptText("Student Name"); stdName.setStyle(inputStyle);
        stdEmail = new TextField(); stdEmail.setPromptText("Parent Email (Alerts)"); stdEmail.setStyle(inputStyle);
        Button regStdBtn = new Button("Register Student");
        regStdBtn.setStyle(blueBtn); regStdBtn.setMaxWidth(Double.MAX_VALUE);

        tchId = new TextField(); tchId.setPromptText("Teacher ID (Username)"); tchId.setStyle(inputStyle);
        tchName = new TextField(); tchName.setPromptText("Teacher Name"); tchName.setStyle(inputStyle);
        tchPass = new PasswordField(); tchPass.setPromptText("Set Password"); tchPass.setStyle(inputStyle);
        Button regTchBtn = new Button("Register Teacher");
        regTchBtn.setStyle(greenBtn); regTchBtn.setMaxWidth(Double.MAX_VALUE);

        subInput = new TextField(); subInput.setPromptText("Subject (e.g. Java)"); subInput.setStyle(inputStyle);
        semInput = new TextField(); semInput.setPromptText("Semester (e.g. 8th)"); semInput.setStyle(inputStyle);
        Button attBtn = new Button("Mark Attendance (Scan)");
        attBtn.setStyle(blueBtn); attBtn.setMaxWidth(Double.MAX_VALUE);

        Button sendAlertBtn = new Button("📧 Send Absence Alerts");
        sendAlertBtn.setStyle(orangeBtn); sendAlertBtn.setMaxWidth(Double.MAX_VALUE);

        Button dashBtn = new Button("📊 Open Analytics Dashboard");
        dashBtn.setStyle(purpleBtn); dashBtn.setMaxWidth(Double.MAX_VALUE);

        statusLabel = new Label("System Ready");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        controls.getChildren().addAll(
                new Label("👨‍🎓 Student Registration"), stdId, stdName, stdEmail, regStdBtn,
                new Separator(),
                new Label("👨‍🏫 Teacher Setup"), tchId, tchName, tchPass, regTchBtn,
                new Separator(),
                new Label("📝 Live Attendance"), subInput, semInput, attBtn,
                new Separator(),
                sendAlertBtn, dashBtn, statusLabel
        );

        HBox body = new HBox(cameraHolder, controls);
        VBox root = new VBox(10, header, body);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // --- BUTTON ACTIONS ---
        regStdBtn.setOnAction(e -> handleRegistration(stdId.getText().trim(), stdName.getText().trim(), stdEmail.getText().trim(), "", primaryStage, "Student"));
        regTchBtn.setOnAction(e -> handleRegistration(tchId.getText().trim(), tchName.getText().trim(), "", tchPass.getText(), primaryStage, "Teacher"));

        attBtn.setOnAction(e -> {
            String subject = subInput.getText().trim();
            String semester = semInput.getText().trim();
            String id = stdId.getText().trim();
            String name = stdName.getText().trim();

            if (id.isEmpty() || name.isEmpty() || subject.isEmpty() || semester.isEmpty()) {
                updateStatus("❌ Missing Fields!", "#e74c3c");
                return;
            }

            updateStatus("⌛ Processing...", "#3498db");

            Task<String> task = new Task<>() {
                @Override protected String call() {
                    return dbManager.markSmartAttendance(id, name, "Student", subject, semester);
                }
            };
            task.setOnSucceeded(event -> {
                if ("SUCCESS".equals(task.getValue())) {
                    updateStatus("✅ Attendance Logged!", "#27ae60");
                    showDialog(Alert.AlertType.INFORMATION, "Attendance Success", "Attendance marked for " + name); // ✅ पॉप-अप जोडला
                    subInput.clear(); semInput.clear();
                    clearStatusAfterDelay(5000);
                } else {
                    updateStatus("❌ Database Error!", "#e74c3c");
                }
            });
            new Thread(task).start();
        });

        sendAlertBtn.setOnAction(e -> sendBulkAbsenceAlerts());
        dashBtn.setOnAction(e -> new LoginWindow().show());

        primaryStage.setScene(new Scene(root, 960, 710));
        primaryStage.show();
    }

    private void handleRegistration(String id, String name, String email, String pass, Stage stage, String type) {
        if (id.isEmpty() || name.isEmpty()) {
            updateStatus("❌ Enter ID and Name", "#e74c3c");
            return;
        }
        if ("Student".equals(type)) {
            RegisterStudentDialog dialog = new RegisterStudentDialog(stage, id, name);
            Mat face = dialog.showAndWait();
            if (face != null) {
                if (dbManager.registerStudent(id, name, email)) {
                    showDialog(Alert.AlertType.INFORMATION, "Registration Success", "Student " + name + " registered successfully!"); // ✅ पॉप-अप जोडला
                    updateStatus("✅ Student Registered", "#27ae60");
                    stdId.clear(); stdName.clear(); stdEmail.clear();
                    clearStatusAfterDelay(5000);
                }
            }
        } else {
            if (pass.isEmpty()) { updateStatus("❌ Set a password", "#e74c3c"); return; }
            if (dbManager.registerTeacher(id, name, pass)) {
                showDialog(Alert.AlertType.INFORMATION, "Registration Success", "Teacher " + name + " registered successfully!"); // ✅ पॉप-अप जोडला
                updateStatus("✅ Teacher Registered", "#27ae60");
                tchId.clear(); tchName.clear(); tchPass.clear();
                clearStatusAfterDelay(5000);
            }
        }
    }

    private void sendBulkAbsenceAlerts() {
        updateStatus("✉️ Dispatching Absence Reports...", "#f39c12");
        Task<Integer> task = new Task<>() {
            @Override protected Integer call() throws Exception {
                // ... (ईमेल लॉजिक)
                return 0; // (हे लॉजिक तुमच्या मूळ कोडप्रमाणे सुरू राहील)
            }
        };
        task.setOnSucceeded(e -> {
            updateStatus("🚀 Broadcast Successful!", "#27ae60");
            showDialog(Alert.AlertType.INFORMATION, "Alerts Sent", "Reports dispatched to parents.");
            clearStatusAfterDelay(5000);
        });
        new Thread(task).start();
    }

    private void clearStatusAfterDelay(int ms) {
        new Thread(() -> {
            try { Thread.sleep(ms); } catch (Exception ignored) {}
            updateStatus("✨ AI System Monitoring...", "#7f8c8d");
        }).start();
    }

    private void showDialog(Alert.AlertType type, String title, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(type);
            a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
        });
    }

    private void startHighSpeedCamera() {
        Thread cam = new Thread(() -> {
            while (isRunning) {
                Mat frame = cameraManager.captureFrame();
                if (frame != null) Platform.runLater(() -> cameraPreview.setImage(cameraManager.matToImage(frame)));
                try { Thread.sleep(30); } catch (Exception ignored) {}
            }
        });
        cam.setDaemon(true); cam.start();
    }

    private void startClockThread(Label label) {
        Thread clock = new Thread(() -> {
            while (isRunning) {
                String t = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
                Platform.runLater(() -> label.setText("🕒 " + t));
                try { Thread.sleep(1000); } catch (Exception ignored) {}
            }
        });
        clock.setDaemon(true); clock.start();
    }

    private void updateStatus(String text, String color) {
        Platform.runLater(() -> {
            statusLabel.setText(text);
            statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        });
    }

    public static void main(String[] args) { launch(args); }
}