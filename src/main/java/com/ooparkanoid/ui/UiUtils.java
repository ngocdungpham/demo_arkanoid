package com.ooparkanoid.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

/** label extension (glow when texts change). */
public final class UiUtils {
    private UiUtils() {}

    public static void addCardGlowOnChange(Label valueLbl) {
        valueLbl.textProperty().addListener((obs, o, n) -> {
            Timeline t = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(valueLbl.opacityProperty(), 0.65),
                            new KeyValue(valueLbl.scaleXProperty(), 1.00),
                            new KeyValue(valueLbl.scaleYProperty(), 1.00)),
                    new KeyFrame(Duration.millis(120),
                            new KeyValue(valueLbl.opacityProperty(), 1.0),
                            new KeyValue(valueLbl.scaleXProperty(), 1.06),
                            new KeyValue(valueLbl.scaleYProperty(), 1.06)),
                    new KeyFrame(Duration.millis(260),
                            new KeyValue(valueLbl.opacityProperty(), 1.0),
                            new KeyValue(valueLbl.scaleXProperty(), 1.00),
                            new KeyValue(valueLbl.scaleYProperty(), 1.00))
            );
            t.play();
        });
    }
}
