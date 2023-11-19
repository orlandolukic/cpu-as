package gui;

import controller.KeyboardScreenController;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class KeyboardPeripheralWindow {
	
	public static Stage StageInstance = null;

    public static void closeWindow() {
        if ( StageInstance != null ) {
            StageInstance.close();
            StageInstance = null;
        };
    }

    public static void display()
    {
        try {
        	
        	if(StageInstance == null) {
                FXMLLoader loader = new FXMLLoader(new DemoClass().getClass().getResource("KeyboardPeripheralWindow.fxml"));
                Parent root = loader.load();
                Stage newWindow = new Stage();
                newWindow.getIcons().add(new Image("file:" + Main.PROGRAM_ICON));
                newWindow.setResizable(false);
                newWindow.setTitle( Main.PROGRAM_NAME + " - Peripheral: Keyboard" );
                newWindow.setScene(new Scene(root));
                newWindow.show();
                newWindow.getScene().getStylesheets().add("ThemeCSS.css");

                Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
                double x = (primScreenBounds.getWidth() - newWindow.getWidth()) / 2;
                double y = (primScreenBounds.getHeight() - newWindow.getHeight()) / 2;
                newWindow.setX(x+250);
                newWindow.setY(y+180);
                
                newWindow.setOnHiding(new EventHandler<WindowEvent>() {
    				
    				@Override
    				public void handle(WindowEvent event) {
    					StageInstance = null;
    				}
    			});
                
                StageInstance = newWindow;

                StageInstance.setOnHidden(event -> {
                    KeyboardScreenController.isInitialized = false;
                });

                StageInstance.getScene().setOnKeyPressed(event -> {
                    KeyboardScreenController.getController().ToggleButtonAction(event);
                });

        	}
        	else {
        		StageInstance.requestFocus();
        	}


        }catch(Exception e) {
            System.out.print("Display Error");
            e.printStackTrace();
        }
    }
}
