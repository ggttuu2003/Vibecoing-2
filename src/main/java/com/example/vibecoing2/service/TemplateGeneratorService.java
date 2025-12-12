package com.example.vibecoing2.service;

import com.example.vibecoing2.config.AppConfig;
import com.example.vibecoing2.domain.*;
import com.example.vibecoing2.util.CoordinateConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Rect;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateGeneratorService {

    private final AppConfig appConfig;
    private final CoordinateConverter coordinateConverter;

    public PageTemplate generateTemplate(List<Component> aiComponents, List<Component> cvComponents,
                                         List<TextComponent> ocrComponents, int imageWidth, int imageHeight) {

        PageTemplate template = new PageTemplate(imageWidth, imageHeight);

        List<Component> mergedComponents = mergeComponents(aiComponents, cvComponents, ocrComponents);

        removeDuplicates(mergedComponents);

        mergedComponents.sort(Comparator.comparingInt(c -> c.getPosition().getY()));

        for (Component component : mergedComponents) {
            template.addComponent(component);
        }

        log.info("生成模板完成，共 {} 个组件", mergedComponents.size());

        return template;
    }

    private List<Component> mergeComponents(List<Component> aiComponents, List<Component> cvComponents,
                                           List<TextComponent> ocrComponents) {
        List<Component> merged = new ArrayList<>();

        merged.addAll(mergeAIAndCV(aiComponents, cvComponents));

        merged.addAll(mergeTextComponents(merged, ocrComponents));

        return merged;
    }

    private List<Component> mergeAIAndCV(List<Component> aiComponents, List<Component> cvComponents) {
        List<Component> merged = new ArrayList<>();
        Set<Integer> usedCVIndices = new HashSet<>();

        for (Component aiComp : aiComponents) {
            Component bestMatch = null;
            int bestMatchIndex = -1;
            double bestIoU = 0.0;

            for (int i = 0; i < cvComponents.size(); i++) {
                if (usedCVIndices.contains(i)) {
                    continue;
                }

                Component cvComp = cvComponents.get(i);

                if (!aiComp.getType().equals(cvComp.getType())) {
                    continue;
                }

                double iou = calculateIoU(aiComp, cvComp);

                if (iou > bestIoU && iou > appConfig.getLayout().getIouThreshold()) {
                    bestIoU = iou;
                    bestMatch = cvComp;
                    bestMatchIndex = i;
                }
            }

            if (bestMatch != null) {
                Component mergedComp = mergeTwoComponents(aiComp, bestMatch);
                merged.add(mergedComp);
                usedCVIndices.add(bestMatchIndex);
            } else {
                merged.add(aiComp);
            }
        }

        for (int i = 0; i < cvComponents.size(); i++) {
            if (!usedCVIndices.contains(i)) {
                merged.add(cvComponents.get(i));
            }
        }

        return merged;
    }

    private double calculateIoU(Component c1, Component c2) {
        Rect r1 = toRect(c1);
        Rect r2 = toRect(c2);

        return coordinateConverter.calculateIoU(r1, r2);
    }

    private Rect toRect(Component component) {
        Position pos = component.getPosition();
        Size size = component.getSize();
        return new Rect(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
    }

    private Component mergeTwoComponents(Component aiComp, Component cvComp) {
        Component merged;

        if (aiComp instanceof TextComponent && cvComp instanceof TextComponent) {
            TextComponent aiText = (TextComponent) aiComp;
            TextComponent cvText = (TextComponent) cvComp;
            TextComponent mergedText = new TextComponent();
            mergedText.setContent(aiText.getContent());
            mergedText.getStyle().putAll(aiText.getStyle());
            mergedText.setPosition(cvText.getPosition());
            mergedText.setSize(cvText.getSize());
            merged = mergedText;
        } else if (aiComp instanceof ButtonComponent && cvComp instanceof ButtonComponent) {
            ButtonComponent aiButton = (ButtonComponent) aiComp;
            ButtonComponent cvButton = (ButtonComponent) cvComp;
            ButtonComponent mergedButton = new ButtonComponent();
            mergedButton.setText(aiButton.getText());
            mergedButton.getStyle().putAll(aiButton.getStyle());
            mergedButton.getStyle().putAll(cvButton.getStyle());
            mergedButton.setPosition(cvButton.getPosition());
            mergedButton.setSize(cvButton.getSize());
            merged = mergedButton;
        } else {
            merged = aiComp;
            merged.setPosition(cvComp.getPosition());
            merged.setSize(cvComp.getSize());
        }

        merged.setId(aiComp.getId());
        merged.setConfidence((aiComp.getConfidence() + cvComp.getConfidence()) / 2);

        return merged;
    }

    private List<TextComponent> mergeTextComponents(List<Component> existingComponents, List<TextComponent> ocrComponents) {
        List<TextComponent> additionalTexts = new ArrayList<>();

        for (TextComponent ocrText : ocrComponents) {
            boolean matched = false;

            for (Component existing : existingComponents) {
                if (existing instanceof TextComponent) {
                    double iou = calculateIoU(existing, ocrText);
                    if (iou > appConfig.getLayout().getIouThreshold()) {
                        matched = true;
                        break;
                    }
                }
            }

            if (!matched) {
                additionalTexts.add(ocrText);
            }
        }

        return additionalTexts;
    }

    private void removeDuplicates(List<Component> components) {
        List<Component> toRemove = new ArrayList<>();

        for (int i = 0; i < components.size(); i++) {
            for (int j = i + 1; j < components.size(); j++) {
                Component c1 = components.get(i);
                Component c2 = components.get(j);

                if (c1.getType().equals(c2.getType())) {
                    double iou = calculateIoU(c1, c2);
                    if (iou > 0.8) {
                        Component toKeep = c1.getConfidence() > c2.getConfidence() ? c1 : c2;
                        Component toDelete = toKeep == c1 ? c2 : c1;
                        if (!toRemove.contains(toDelete)) {
                            toRemove.add(toDelete);
                        }
                    }
                }
            }
        }

        components.removeAll(toRemove);

        log.info("移除 {} 个重复组件", toRemove.size());
    }
}
