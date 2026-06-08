package com.attendance;

import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class FaceRecognizer {

    private CascadeClassifier faceDetector;

    private static final int FACE_WIDTH = 96;
    private static final int FACE_HEIGHT = 96;

    public FaceRecognizer() {

        faceDetector = new CascadeClassifier();

        try {
            URL url = getClass().getResource("/haarcascade_frontalface_default.xml");

            File temp = File.createTempFile("cascade", ".xml");
            temp.deleteOnExit();

            Files.copy(url.openStream(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);

            faceDetector.load(temp.getAbsolutePath());

            System.out.println("Face detector loaded ✅");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public INDArray generateFaceVector(Mat frame) {

        if (frame == null || frame.empty()) return null;

        Mat gray = new Mat();

        try {
            cvtColor(frame, gray, COLOR_BGR2GRAY);
            equalizeHist(gray, gray);

            RectVector faces = new RectVector();
            faceDetector.detectMultiScale(gray, faces);

            if (faces.size() == 0) return null;

            Rect face = faces.get(0);

            Mat faceRegion = new Mat(gray, face);
            Mat resized = new Mat();

            resize(faceRegion, resized, new Size(FACE_WIDTH, FACE_HEIGHT));

            float[] pixels = new float[FACE_WIDTH * FACE_HEIGHT];

            UByteIndexer indexer = resized.createIndexer();

            int idx = 0;
            for (int i = 0; i < FACE_HEIGHT; i++) {
                for (int j = 0; j < FACE_WIDTH; j++) {
                    pixels[idx++] = indexer.get(i, j) / 255.0f;
                }
            }

            indexer.release();

            INDArray vector = Nd4j.create(pixels);

            // normalize
            double norm = vector.norm2Number().doubleValue();
            if (norm != 0) vector = vector.div(norm);

            System.out.println("Vector generated ✅");

            return vector;

        } finally {
            gray.release();
        }
    }
}