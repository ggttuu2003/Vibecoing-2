package com.example.vibecoing2.service;

import com.example.vibecoing2.config.OpenCVConfig;
import com.example.vibecoing2.domain.*;
import com.example.vibecoing2.util.ColorExtractor;
import com.example.vibecoing2.util.CoordinateConverter;
import com.example.vibecoing2.util.ImageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentDetectionService {

    private final OpenCVConfig openCVConfig;
    private final ImageProcessor imageProcessor;
    private final ColorExtractor colorExtractor;
    private final CoordinateConverter coordinateConverter;

    public List<Component> detectComponents(String imagePath) {
        List<Component> components = new ArrayList<>();

        Mat image = imageProcessor.loadImage(imagePath);

        List<ButtonComponent> buttons = detectButtons(image);
        components.addAll(buttons);

        List<ImageComponent> images = detectImageRegions(image);
        components.addAll(images);

        log.info("OpenCV 检测完成：{} 个按钮，{} 个图片区域", buttons.size(), images.size());

        return components;
    }

    private List<ButtonComponent> detectButtons(Mat image) {
        List<ButtonComponent> buttons = new ArrayList<>();

        Mat gray = imageProcessor.convertToGray(image);
        Mat edges = imageProcessor.detectEdges(gray);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);

            if (isButtonShape(rect)) {
                ButtonComponent button = createButtonComponent(image, rect);
                buttons.add(button);
            }
        }

        log.info("检测到 {} 个按钮", buttons.size());

        return buttons;
    }

    private boolean isButtonShape(Rect rect) {
        double aspectRatio = (double) rect.width / rect.height;

        boolean widthOk = rect.width >= openCVConfig.getMinButtonWidth();
        boolean heightOk = rect.height >= openCVConfig.getMinButtonHeight();
        boolean aspectRatioOk = aspectRatio >= openCVConfig.getButtonAspectRatioMin()
                && aspectRatio <= openCVConfig.getButtonAspectRatioMax();

        return widthOk && heightOk && aspectRatioOk;
    }

    private ButtonComponent createButtonComponent(Mat image, Rect rect) {
        ButtonComponent button = new ButtonComponent();
        button.setId("button-cv-" + System.nanoTime());
        button.setText("按钮");
        button.setPosition(coordinateConverter.toPosition(rect));
        button.setSize(coordinateConverter.toSize(rect));
        button.setConfidence(0.7);

        Mat buttonRegion = imageProcessor.extractRegion(image, rect);
        String backgroundColor = colorExtractor.extractDominantColor(buttonRegion);
        String textColor = colorExtractor.getContrastColor(backgroundColor);

        button.setBackgroundColor(backgroundColor);
        button.setTextColor(textColor);
        button.setBorderRadius(estimateBorderRadius(rect));

        return button;
    }

    private int estimateBorderRadius(Rect rect) {
        return Math.min(rect.height / 2, 25);
    }

    private List<ImageComponent> detectImageRegions(Mat image) {
        List<ImageComponent> imageComponents = new ArrayList<>();

        Mat gray = imageProcessor.convertToGray(image);

        Mat binary = imageProcessor.threshold(gray);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);

            if (isImageRegion(rect, image.size())) {
                ImageComponent imageComponent = createImageComponent(image, rect);
                imageComponents.add(imageComponent);
            }
        }

        log.info("检测到 {} 个图片区域", imageComponents.size());

        return imageComponents;
    }

    private boolean isImageRegion(Rect rect, org.opencv.core.Size imageSize) {
        double areaRatio = (double) rect.area() / (imageSize.width * imageSize.height);

        boolean largeEnough = rect.width > 100 && rect.height > 100;
        boolean notTooLarge = areaRatio < 0.8;
        boolean aspectRatioOk = (double) rect.width / rect.height > 0.5 && (double) rect.width / rect.height < 3.0;

        return largeEnough && notTooLarge && aspectRatioOk;
    }

    private ImageComponent createImageComponent(Mat image, Rect rect) {
        ImageComponent imageComponent = new ImageComponent();
        imageComponent.setId("image-cv-" + System.nanoTime());
        imageComponent.setPosition(coordinateConverter.toPosition(rect));
        imageComponent.setSize(coordinateConverter.toSize(rect));
        imageComponent.setConfidence(0.7);

        Mat imageRegion = imageProcessor.extractRegion(image, rect);
        String dominantColor = colorExtractor.extractDominantColor(imageRegion);

        imageComponent.setPlaceholderUrl("https://placehold.co/" + rect.width + "x" + rect.height);
        imageComponent.setPlaceholderAlt("图片占位符");
        imageComponent.setDominantColor(dominantColor);
        imageComponent.setObjectFit("cover");

        return imageComponent;
    }
}
