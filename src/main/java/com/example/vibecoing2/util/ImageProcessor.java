package com.example.vibecoing2.util;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Base64;

@Slf4j
@Component
public class ImageProcessor {

    private static final int STANDARD_WIDTH = 750;
    private static final double QUALITY = 0.95;

    public Mat loadImage(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            throw new RuntimeException("无法加载图片: " + imagePath);
        }
        return image;
    }

    public Mat preprocessImage(Mat source, int targetWidth) {
        Mat processed = new Mat();

        int originalWidth = source.cols();
        int originalHeight = source.rows();
        double scale = (double) targetWidth / originalWidth;
        int targetHeight = (int) (originalHeight * scale);

        Size newSize = new Size(targetWidth, targetHeight);
        Imgproc.resize(source, processed, newSize, 0, 0, Imgproc.INTER_AREA);

        log.info("图片预处理完成: {}x{} -> {}x{}", originalWidth, originalHeight, targetWidth, targetHeight);

        return processed;
    }

    public Mat denoise(Mat source) {
        Mat denoised = new Mat();
        try {
            // 使用高斯模糊代替 fastNlMeansDenoisingColored（Java 版本可能不支持）
            Imgproc.GaussianBlur(source, denoised, new org.opencv.core.Size(5, 5), 0);
        } catch (Exception e) {
            log.warn("去噪处理失败，返回原图: {}", e.getMessage());
            return source.clone();
        }
        return denoised;
    }

    public Mat convertToGray(Mat source) {
        Mat gray = new Mat();
        Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);
        return gray;
    }

    public Mat detectEdges(Mat gray) {
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 50, 150);
        return edges;
    }

    public Mat threshold(Mat gray) {
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        return binary;
    }

    public void saveImage(Mat image, String outputPath) {
        Imgcodecs.imwrite(outputPath, image);
        log.info("图片已保存: {}", outputPath);
    }

    public Size getImageSize(Mat image) {
        return new Size(image.cols(), image.rows());
    }

    public Rect clipRect(Rect rect, Size imageSize) {
        int x = Math.max(0, rect.x);
        int y = Math.max(0, rect.y);
        int width = Math.min((int) imageSize.width - x, rect.width);
        int height = Math.min((int) imageSize.height - y, rect.height);
        return new Rect(x, y, width, height);
    }

    public Mat extractRegion(Mat source, Rect rect) {
        Size imageSize = getImageSize(source);
        Rect clippedRect = clipRect(rect, imageSize);
        return new Mat(source, clippedRect);
    }

    /**
     * 裁剪图片区域
     */
    public Mat cropImageRegion(Mat image, int x, int y, int width, int height) {
        // 边界检查
        if (x < 0 || y < 0 || width <= 0 || height <= 0) {
            log.warn("无效的裁剪区域: x={}, y={}, width={}, height={}", x, y, width, height);
            return new Mat();
        }

        int imageWidth = image.cols();
        int imageHeight = image.rows();

        // 确保裁剪区域在图片范围内
        if (x >= imageWidth || y >= imageHeight) {
            log.warn("裁剪区域超出图片范围: x={}, y={}, 图片尺寸={}x{}", x, y, imageWidth, imageHeight);
            return new Mat();
        }

        // 调整宽高以确保不超出图片边界
        int actualWidth = Math.min(width, imageWidth - x);
        int actualHeight = Math.min(height, imageHeight - y);

        if (actualWidth <= 0 || actualHeight <= 0) {
            log.warn("调整后的裁剪区域无效: actualWidth={}, actualHeight={}", actualWidth, actualHeight);
            return new Mat();
        }

        Rect rect = new Rect(x, y, actualWidth, actualHeight);
        return new Mat(image, rect);
    }

    /**
     * 压缩图片到指定最大尺寸
     */
    public Mat compressImage(Mat image, int maxSize) {
        int width = image.cols();
        int height = image.rows();

        // 如果图片已经小于等于最大尺寸，不需要压缩
        if (width <= maxSize && height <= maxSize) {
            return image;
        }

        // 计算缩放比例
        double scale;
        if (width > height) {
            scale = (double) maxSize / width;
        } else {
            scale = (double) maxSize / height;
        }

        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        Mat compressed = new Mat();
        Size newSize = new Size(newWidth, newHeight);
        Imgproc.resize(image, compressed, newSize, 0, 0, Imgproc.INTER_AREA);

        log.debug("图片压缩: {}x{} -> {}x{}", width, height, newWidth, newHeight);
        return compressed;
    }

    /**
     * 将Mat转换为base64编码的JPEG图片（高压缩比，减少token消耗）
     */
    public String matToBase64(Mat image, int maxSize) {
        return matToBase64(image, maxSize, 75); // 默认质量 75，平衡清晰度和大小
    }

    /**
     * 将Mat转换为base64编码的JPEG图片（可指定质量）
     * @param image 原始图片
     * @param maxSize 最大尺寸（宽或高）
     * @param quality JPEG质量 (0-100)，推荐值：60-80
     * @return base64编码的图片
     */
    public String matToBase64(Mat image, int maxSize, int quality) {
        // 检查图片是否为空
        if (image == null || image.empty()) {
            log.warn("图片为空，无法转换为base64");
            return null;
        }

        // 检查图片尺寸
        if (image.cols() <= 0 || image.rows() <= 0) {
            log.warn("图片尺寸无效: {}x{}", image.cols(), image.rows());
            return null;
        }

        // 压缩图片尺寸
        Mat compressed = compressImage(image, maxSize);

        // 编码为JPEG（更高压缩比）
        MatOfByte buffer = new MatOfByte();
        MatOfInt params = new MatOfInt(
            Imgcodecs.IMWRITE_JPEG_QUALITY, quality,  // JPEG质量
            Imgcodecs.IMWRITE_JPEG_OPTIMIZE, 1        // 优化编码
        );
        boolean success = Imgcodecs.imencode(".jpg", compressed, buffer, params);

        if (!success) {
            log.warn("图片编码失败");
            return null;
        }

        // 转换为base64
        byte[] bytes = buffer.toArray();
        if (bytes == null || bytes.length == 0) {
            log.warn("图片编码结果为空");
            return null;
        }

        String base64 = Base64.getEncoder().encodeToString(bytes);

        // 计算压缩率
        int originalSize = image.cols() * image.rows() * 3; // 估算原始大小
        double compressionRatio = (1 - (double) bytes.length / originalSize) * 100;

        log.debug("图片转base64完成: 尺寸={}x{}, 质量={}, 大小={} KB, 压缩率={:.1f}%",
            compressed.cols(), compressed.rows(), quality, bytes.length / 1024, compressionRatio);

        return "data:image/jpeg;base64," + base64;
    }

    /**
     * 检测区域是否为纯色
     * @return true 如果是纯色，false 如果包含图案
     */
    public boolean isRegionSolidColor(Mat region) {
        // 计算标准差来判断是否为纯色
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(region, mean, stddev);

        // 如果所有通道的标准差都很小，说明是纯色
        double[] stddevArray = stddev.toArray();
        double maxStddev = 0;
        for (double std : stddevArray) {
            maxStddev = Math.max(maxStddev, std);
        }

        // 标准差阈值：小于5认为是纯色
        boolean isSolid = maxStddev < 5.0;
        log.debug("区域颜色检测: 标准差={}, 是否纯色={}", maxStddev, isSolid);
        return isSolid;
    }
}
