package com.example.vibecoing2.service;

import com.example.vibecoing2.config.OCRConfig;
import com.example.vibecoing2.domain.Position;
import com.example.vibecoing2.domain.Size;
import com.example.vibecoing2.domain.TextComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OCRService {

    private final OCRConfig ocrConfig;
    private volatile ITesseract tesseract;
    private volatile boolean initialized = false;
    private volatile boolean initFailed = false;

    private synchronized ITesseract getTesseract() {
        if (initFailed) {
            return null;
        }

        if (tesseract == null && !initialized) {
            try {
                log.info("初始化 Tesseract OCR...");
                tesseract = new Tesseract();
                tesseract.setDatapath(ocrConfig.getTessdataPath());
                tesseract.setLanguage(ocrConfig.getLanguage());
                tesseract.setPageSegMode(ocrConfig.getPageSegmentationMode());
                initialized = true;
                log.info("Tesseract OCR 初始化成功");
            } catch (UnsatisfiedLinkError e) {
                log.warn("Tesseract 库未安装，OCR 功能不可用。macOS 请使用: brew install tesseract");
                initFailed = true;
                return null;
            } catch (Exception e) {
                log.error("Tesseract 初始化失败", e);
                initFailed = true;
                return null;
            }
        }

        return tesseract;
    }

    public List<TextComponent> extractText(String imagePath) {
        List<TextComponent> textComponents = new ArrayList<>();

        ITesseract tesseractInstance = getTesseract();
        if (tesseractInstance == null) {
            log.warn("Tesseract 不可用，跳过 OCR 识别");
            return textComponents;
        }

        try {

            // 将文件转换为 BufferedImage
            File imageFile = new File(imagePath);
            BufferedImage bufferedImage = ImageIO.read(imageFile);

            if (bufferedImage == null) {
                log.warn("无法读取图片: {}", imagePath);
                return textComponents;
            }

            List<Word> words = tesseractInstance.getWords(bufferedImage, 0);

            log.info("OCR 识别到 {} 个文字区域", words.size());

            for (Word word : words) {
                String text = word.getText().trim();
                if (text.isEmpty()) {
                    continue;
                }

                TextComponent component = new TextComponent();
                component.setId("text-ocr-" + System.nanoTime());
                component.setContent(text);

                java.awt.Rectangle rect = word.getBoundingBox();
                component.setPosition(new Position(rect.x, rect.y));
                component.setSize(new Size(rect.width, rect.height));

                component.setFontSize(estimateFontSize(rect.height));
                component.setConfidence(word.getConfidence() / 100.0);

                textComponents.add(component);
            }

        } catch (Exception e) {
            log.error("OCR 识别失败", e);
        }

        return textComponents;
    }

    private int estimateFontSize(int height) {
        return Math.max(12, Math.min(48, height));
    }
}
