package com.example.vibecoing2.util;

import com.example.vibecoing2.domain.Position;
import com.example.vibecoing2.domain.Size;
import org.opencv.core.Rect;
import org.springframework.stereotype.Component;

@Component
public class CoordinateConverter {

    public Position toPosition(Rect rect) {
        return new Position(rect.x, rect.y, "px", null);
    }

    public Size toSize(Rect rect) {
        return new Size(rect.width, rect.height);
    }

    public Position toRelativePosition(Rect rect, int containerWidth, int containerHeight) {
        double xPercent = (double) rect.x / containerWidth * 100;
        double yPercent = (double) rect.y / containerHeight * 100;

        Position position = new Position();
        position.setX((int) xPercent);
        position.setY((int) yPercent);
        position.setUnit("%");

        return position;
    }

    public Size toRelativeSize(Rect rect, int containerWidth, int containerHeight) {
        double widthPercent = (double) rect.width / containerWidth * 100;
        double heightPercent = (double) rect.height / containerHeight * 100;

        return new Size((int) widthPercent, (int) heightPercent);
    }

    public String determineAlignment(Rect rect, int containerWidth) {
        int center = rect.x + rect.width / 2;
        int leftBound = containerWidth / 3;
        int rightBound = containerWidth * 2 / 3;

        if (center < leftBound) {
            return "left";
        } else if (center > rightBound) {
            return "right";
        } else {
            return "center";
        }
    }

    public double calculateIoU(Rect r1, Rect r2) {
        int x1 = Math.max(r1.x, r2.x);
        int y1 = Math.max(r1.y, r2.y);
        int x2 = Math.min(r1.x + r1.width, r2.x + r2.width);
        int y2 = Math.min(r1.y + r1.height, r2.y + r2.height);

        int intersectionArea = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);
        int unionArea = (int)r1.area() + (int)r2.area() - intersectionArea;

        if (unionArea == 0) {
            return 0.0;
        }

        return (double) intersectionArea / unionArea;
    }

    public boolean isOverlapping(Rect r1, Rect r2, double threshold) {
        return calculateIoU(r1, r2) > threshold;
    }

    public Rect mergeRects(Rect r1, Rect r2) {
        int x = Math.min(r1.x, r2.x);
        int y = Math.min(r1.y, r2.y);
        int x2 = Math.max(r1.x + r1.width, r2.x + r2.width);
        int y2 = Math.max(r1.y + r1.height, r2.y + r2.height);

        return new Rect(x, y, x2 - x, y2 - y);
    }
}
