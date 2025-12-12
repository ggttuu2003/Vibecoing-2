package com.example.vibecoing2.util;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ColorExtractor {

    public String extractDominantColor(Mat region) {
        if (region.empty() || region.rows() < 1 || region.cols() < 1) {
            return "#FFFFFF";
        }

        Mat reshapedImage = region.reshape(1, region.rows() * region.cols());
        Mat reshapedImageFloat = new Mat();
        reshapedImage.convertTo(reshapedImageFloat, CvType.CV_32F);

        int clusterCount = 3;
        Mat labels = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 100, 0.2);
        Mat centers = new Mat();

        Core.kmeans(reshapedImageFloat, clusterCount, labels, criteria, 3, Core.KMEANS_PP_CENTERS, centers);

        int[] labelCounts = new int[clusterCount];
        for (int i = 0; i < labels.rows(); i++) {
            int label = (int) labels.get(i, 0)[0];
            labelCounts[label]++;
        }

        int dominantCluster = 0;
        int maxCount = labelCounts[0];
        for (int i = 1; i < clusterCount; i++) {
            if (labelCounts[i] > maxCount) {
                maxCount = labelCounts[i];
                dominantCluster = i;
            }
        }

        double[] dominantColor = centers.get(dominantCluster, 0);

        int b = (int) dominantColor[0];
        int g = (int) dominantColor[1];
        int r = (int) dominantColor[2];

        return String.format("#%02X%02X%02X", r, g, b);
    }

    public String extractCenterColor(Mat region) {
        if (region.empty() || region.rows() < 1 || region.cols() < 1) {
            return "#FFFFFF";
        }

        int centerX = region.cols() / 2;
        int centerY = region.rows() / 2;

        int sampleSize = 5;
        int startX = Math.max(0, centerX - sampleSize / 2);
        int startY = Math.max(0, centerY - sampleSize / 2);
        int endX = Math.min(region.cols(), centerX + sampleSize / 2);
        int endY = Math.min(region.rows(), centerY + sampleSize / 2);

        int totalR = 0, totalG = 0, totalB = 0, count = 0;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                double[] pixel = region.get(y, x);
                totalB += pixel[0];
                totalG += pixel[1];
                totalR += pixel[2];
                count++;
            }
        }

        if (count == 0) {
            return "#FFFFFF";
        }

        int avgR = totalR / count;
        int avgG = totalG / count;
        int avgB = totalB / count;

        return String.format("#%02X%02X%02X", avgR, avgG, avgB);
    }

    public boolean isLightColor(String hexColor) {
        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);

        double luminance = 0.299 * r + 0.587 * g + 0.114 * b;

        return luminance > 128;
    }

    public String getContrastColor(String hexColor) {
        return isLightColor(hexColor) ? "#000000" : "#FFFFFF";
    }
}
