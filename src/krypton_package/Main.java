package krypton_package;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    private double xOffset,yOffset;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("krypton.fxml"));

        root.setOnMousePressed(mouseEvent -> {
            xOffset= mouseEvent.getSceneX();
            yOffset= mouseEvent.getSceneY();
        });

        root.setOnMouseDragged(mouseEvent -> {
            primaryStage.setX(mouseEvent.getScreenX()-xOffset);
            primaryStage.setY(mouseEvent.getScreenY()-yOffset);
        });


        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("Krypton");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
