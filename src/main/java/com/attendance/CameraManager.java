package com.attendance;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.global.opencv_videoio; // Garjeche aahe
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import org.bytedeco.javacpp.indexer.UByteIndexer;

public class CameraManager {

    private VideoCapture capture;
    private final Mat frame;

    public CameraManager() {
        // CAP_DSHOW mule Windows cha 'cap_msmf' error solve hoto
        capture = new VideoCapture(0, opencv_videoio.CAP_DSHOW);
        frame = new Mat();

        if (!capture.isOpened()) {
            System.err.println("❌ Error: DSHOW ne camera chalu zala nahi, default try karat aahe...");
            capture = new VideoCapture(0);
        }

        if (capture.isOpened()) {
            System.out.println("✅ Camera successfully started!");
            // Resolution fix kelya nantar database la frames pathavne soppe hote
            capture.set(opencv_videoio.CAP_PROP_FRAME_WIDTH, 640);
            capture.set(opencv_videoio.CAP_PROP_FRAME_HEIGHT, 480);
        } else {
            System.err.println("❌ Error: Camera access denied!");
        }
    }

    public synchronized Mat captureFrame() {
        if (capture == null || !capture.isOpened()) return null;

        // Grab aani Retrieve vapara jyane frame crash honar nahi
        if (capture.grab()) {
            capture.retrieve(frame);
            if (frame.empty()) return null;
            return frame.clone();
        }
        return null;
    }

    public Mat grabFrame() {
        return captureFrame();
    }

    public Image matToImage(Mat matrix) {
        if (matrix == null || matrix.empty()) return null;

        int width = matrix.cols();
        int height = matrix.rows();
        WritableImage out = new WritableImage(width, height);
        PixelWriter pw = out.getPixelWriter();

        // Memory safe indexer
        try (UByteIndexer indexer = matrix.createIndexer()) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int b = indexer.get(y, x, 0) & 0xFF;
                    int g = indexer.get(y, x, 1) & 0xFF;
                    int r = indexer.get(y, x, 2) & 0xFF;
                    pw.setColor(x, y, Color.rgb(r, g, b));
                }
            }
        } catch (Exception e) {
            System.err.println("Conversion Error: " + e.getMessage());
        }
        return out;
    }

    public void stopCamera() {
        if (capture != null && capture.isOpened()) {
            capture.release();
            System.out.println("🛑 Camera stopped");
        }
    }
}