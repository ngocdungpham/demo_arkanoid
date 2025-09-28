package com.ooparkanoid;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;


public class AlertBox {
    public static void display(String title, String message){
     Stage window = new Stage();

     window.initModality(Modality.APPLICATION_MODAL);
     window.setTitle(title);
     window.setMinWidth(300);
     window.setMinHeight(200);

     Label label = new Label(message);
     label.setText(message);
     label.setWrapText(true);
     Button closeButton = new Button("Close the window");
     closeButton.setOnAction(e -> window.close());

     VBox layout = new VBox(20);
     layout.getChildren().addAll(label, closeButton);
     layout.setAlignment(Pos.CENTER);

     Scene scene = new Scene(layout);
     window.setScene(scene);
     window.showAndWait();


    }
}
