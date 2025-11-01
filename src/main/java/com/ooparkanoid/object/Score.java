package com.ooparkanoid.object;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Score extends GameObject {
    private String text;
    private double dy;
    private double life;
    private double initialLife;
    private Color color;
    private static final Font FONT = Font.font("Tahoma", FontWeight.BOLD, 16);

    public Score(String text, double centerX, double topY, Color color) {
        super();
        Text tempText = new Text(text);
        tempText.setFont(FONT);
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();
        this.setWidth(tempText.getLayoutBounds().getWidth());
        this.setHeight(tempText.getLayoutBounds().getHeight());
        this.setX(centerX - this.getWidth() / 2);
        this.setY(topY);

        this.text = text;
        this.color = color;
        this.dy = -30;
        this.life = 0.8;
        this.initialLife = this.life;
    }

    @Override
    public void update(double dt) {
        life -= dt;
        y += dy * dt;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (isFinished()) return;
        double alpha = Math.max(0, life / initialLife);
        gc.save();
        gc.setGlobalAlpha(alpha);
        gc.setFont(FONT);
        gc.setFill(color);
        gc.fillText(text, x, y);
        gc.restore();
    }

    public boolean isFinished() {
        return life <= 0;
    }
}
