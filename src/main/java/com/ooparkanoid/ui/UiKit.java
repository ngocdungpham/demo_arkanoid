// src/main/java/com/ooparkanoid/ui/UiKit.java
package com.ooparkanoid.ui;

import javafx.scene.control.Button;

public final class UiKit {
    private UiKit() {}
    public static Button btnPrimary(String text)  { return btn(text, "btn", "btn-primary"); }
    public static Button btnSecondary(String text){ return btn(text, "btn", "btn-secondary"); }
    public static Button btnGhost(String text)    { return btn(text, "btn", "btn-ghost"); }
    private static Button btn(String text, String... classes) {
        Button b = new Button(text);
        b.getStyleClass().addAll(classes);
        b.setMaxWidth(220);
        return b;
    }
}
