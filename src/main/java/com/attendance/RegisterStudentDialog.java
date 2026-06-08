package com.attendance;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.bytedeco.opencv.opencv_core.Mat;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;
import org.bytedeco.javacv.*;

public class RegisterStudentDialog {

    private final Stage dialog;
    private final ImageView imageView;
    private final CameraManager cameraManager;
    private ScheduledExecutorService timer;
    private volatile Mat capturedFrame;
    private boolean success = false;

    private OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
    private Java2DFrameConverter converter = new Java2DFrameConverter();

    public RegisterStudentDialog(Stage parentStage, String studentId, String name) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("📸 Register Student: " + name);

        cameraManager = new CameraManager(); // खात्री कर की तुझा CameraManager तयार आहे

        imageView = new ImageView();
        imageView.setFitWidth(480); // साइज थोडी कमी केलीय जेणेकरून स्क्रीनवर नीट दिसेल
        imageView.setFitHeight(360);
        imageView.setPreserveRatio(true);

        Label label = new Label("Look at camera & click Capture 😎");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Button captureBtn = new Button("📸 Capture");
        Button cancelBtn = new Button("❌ Cancel");

        captureBtn.setStyle("-fx-background-color: #00c6ff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;");
        cancelBtn.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;");

        captureBtn.setOnAction(e -> capture());
        cancelBtn.setOnAction(e -> close(false));

        HBox buttons = new HBox(20, captureBtn, cancelBtn);
        buttons.setStyle("-fx-alignment: center; -fx-padding: 10;");

        VBox root = new VBox(15, label, imageView, buttons);
        root.setStyle("-fx-padding: 20; -fx-background-color: #1e1e1e; -fx-alignment: center;");

        dialog.setScene(new Scene(root));
        dialog.setOnCloseRequest(e -> stopCamera());

        startCamera();
    }

    private void startCamera() {
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(() -> {
            Mat frame = cameraManager.captureFrame();
            if (frame != null && !frame.empty()) {
                BufferedImage img = convert(frame);
                if (img != null) {
                    Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(img, null)));
                }
            }
        }, 0, 33, TimeUnit.MILLISECONDS);
    }

    private BufferedImage convert(Mat frame) {
        try {
            Frame f = matConverter.convert(frame);
            return converter.getBufferedImage(f);
        } catch (Exception e) { return null; }
    }

    private void capture() {
        Mat frame = cameraManager.captureFrame();
        if (frame != null && !frame.empty()) {
            capturedFrame = frame.clone(); // फोटो सेव्ह केला
            close(true);
        }
    }

    private void close(boolean ok) {
        success = ok;
        stopCamera();
        dialog.close();
    }

    private void stopCamera() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
        }
        cameraManager.stopCamera();
    }

    public Mat showAndWait() {
        dialog.showAndWait();
        return success ? capturedFrame : null;
    }
}