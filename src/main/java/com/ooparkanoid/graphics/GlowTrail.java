package com.ooparkanoid.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;

import java.util.LinkedList;
import java.util.Queue;

// Hiệu ứng TrailBall
public class GlowTrail {
    private Queue<TrailPoint> points = new LinkedList<>();
    private int maxLength = 30;

    private Color coreColor = Color.WHITE;
    private Color glowColor = Color.CYAN;
    private Color outerGlowColor = Color.BLUE;

    private double ballSize;
    private double glowIntensity = 1.0; // Cường độ
    private boolean enabled = true;

    // Sampling rate (số điểm thêm vào mỗi frame)
    private double lastX = -1, lastY = -1;

    // Inner class
    private static class TrailPoint {
        double x, y;
        long timestamp;         // timer của TrailPoint

        TrailPoint(double x, double y, long timestamp) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
        }
    }

    public GlowTrail(double ballSize) {
        this.ballSize = ballSize;
    }

    public void addPoint(double x, double y) {
        if (!enabled) {
            return;
        }

        if (lastX >= 0 && lastY >= 0) {
            double distance = Math.sqrt(Math.pow(x - lastX, 2) + Math.pow(y - lastY, 2));

            // Nếu khoảng cách > 1 pixels, thêm điểm giữa
            if (distance > 1) {
                int steps = (int) (distance / 5);
                for (int i = 1; i <= steps; i++) {
                    double t = (double) i / (steps + 1);
                    double interpX = lastX + (x - lastX) * t;
                    double interpY = lastY + (y - lastY) * t;
                    points.add(new TrailPoint(interpX, interpY, System.currentTimeMillis()));
                }
            }
        }

        // Thêm điểm hiện tại
        points.add(new TrailPoint(x, y, System.currentTimeMillis()));
        lastX = x;
        lastY = y;

        // Giới hạn độ dài
        while (points.size() > maxLength) {
            points.poll();
        }
    }

    public void render(GraphicsContext gc) {
        if (!enabled || points.isEmpty()) {
            return;
        }
        TrailPoint[] array = points.toArray(new TrailPoint[0]);     // chuyển queue -> Array

        // Vẽ 3 lớp : Outer Glow -> Inner Glow -> Core
        renderOuterGlow(gc, array);
        renderInnerGlow(gc, array);
        renderCore(gc, array);
    }

    // Lớp 1 : Outer Glow : mờ + lớn nhất
    private void renderOuterGlow(GraphicsContext gc, TrailPoint[] points) {
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        for (int i = 0; i < points.length - 1; i++) {
            TrailPoint p1 = points[i];
            TrailPoint p2 = points[i + 1];
            double progress = i * 1.0 / points.length;      // từ 0 -> 1
            double alpha = (0.05 + progress * 0.1) * glowIntensity;    // Rất mờ
            double thickness = ballSize * 1.0 * (0.2 + progress * 0.8);     // Độ dày lớn

            gc.setGlobalAlpha(alpha);
            gc.setStroke(outerGlowColor);
            gc.setLineWidth(thickness);
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }
        gc.setGlobalAlpha(1.0);
    }

    // Lớp 2: Inner Glow
    private void renderInnerGlow(GraphicsContext gc, TrailPoint[] points) {
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        for (int i = 0; i < points.length - 1; i++) {
            TrailPoint p1 = points[i];
            TrailPoint p2 = points[i + 1];

            double progress = i * 1.0 / points.length;
            double alpha = (0.05 + progress * 0.1) * glowIntensity;
            double thickness = ballSize * 0.7 * (0.3 + progress * 0.7);
            ;

            gc.setGlobalAlpha(alpha);
            gc.setStroke(glowColor);
            gc.setLineWidth(thickness);
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }
        gc.setGlobalAlpha(1.0);
    }

    // Lớp 3 : Core
    private void renderCore(GraphicsContext gc, TrailPoint[] points) {
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        for (int i = 0; i < points.length - 1; i++) {
            TrailPoint p1 = points[i];
            TrailPoint p2 = points[i + 1];

            double progress = i * 1.0 / points.length;
            double alpha = (0.1 + progress * 0.4) * glowIntensity;
            double thickness = ballSize * 0.4 * (0.4 + progress * 0.6);
            gc.setGlobalAlpha(alpha);
            gc.setStroke(coreColor);
            gc.setLineWidth(thickness);
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }
        gc.setGlobalAlpha(1.0);
    }

    public void update(double deltaTime) {
        long currentTime = System.currentTimeMillis();
        points.removeIf(p -> (currentTime - p.timestamp) > 1000);
    }

    public void clear() {
        points.clear();
        lastX = -1;
        lastY = -1;
    }

    public void setColor(Color color) {
        this.coreColor = Color.WHITE;
        this.glowColor = color;
        this.outerGlowColor = color.deriveColor(0, 1.0, 0.6, 1.0);
    }

    public void setColors(Color core, Color glow, Color outerGlow) {
        this.coreColor = core;
        this.glowColor = glow;
        this.outerGlowColor = outerGlow;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setGlowIntensity(double intensity) {
        this.glowIntensity = Math.max(0.1, Math.min(2.0, intensity));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}