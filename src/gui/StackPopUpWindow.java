package gui;

import controller.StackScreenController;
import debug.Stack;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class StackPopUpWindow {

    public static Stage window;

    public static void closeWindow() {
        if ( window != null ) {
            window.close();
            window = null;
        }
    }

    public static void display() {

        if (window != null) {
            window.requestFocus();
            return;
        }
        ;

        try {
            Stage newWindow = new Stage();
            window = newWindow;
            DemoClass d = new DemoClass();
            FXMLLoader loader = new FXMLLoader(d.getClass().getResource("StackWindow.fxml"));
            Parent root = loader.load();
            newWindow.getIcons().add(new Image("file:" + Main.PROGRAM_ICON));
            newWindow.setResizable(false);
            newWindow.setTitle(Main.PROGRAM_NAME + " - Stack frame");
            newWindow.setScene(new Scene(root));
            //newWindow.initModality(Modality.APPLICATION_MODAL);
            newWindow.setResizable(false);
            newWindow.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            newWindow.setX((primScreenBounds.getWidth() - newWindow.getWidth()) / 2);
            newWindow.setY((primScreenBounds.getHeight() - newWindow.getHeight()) / 2);

            newWindow.setOnHidden(event -> {
                StackScreenController.instance = null;
                window = null;
            });
        } catch (Exception e) {
        }
    }
}
