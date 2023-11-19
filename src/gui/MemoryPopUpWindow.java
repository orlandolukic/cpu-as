package gui;

import java.nio.file.Paths;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MemoryPopUpWindow {
	
	public static void Display() {
		try {
			
			FXMLLoader loader = new FXMLLoader(new DemoClass().getClass().getResource("MemoryWindow.fxml"));
			Parent root = loader.load();
			Stage newWindow = new Stage();
			newWindow.getIcons().add(new Image("file:" + Main.PROGRAM_ICON));
			newWindow.initModality(Modality.APPLICATION_MODAL);
			newWindow.setResizable(false);
			newWindow.setTitle( Main.PROGRAM_NAME + " - Memory content" );
			Scene scene = new Scene(root);
			scene.getStylesheets().add("ThemeCSS.css");
			newWindow.setScene(scene);
			newWindow.show();

			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			newWindow.setX((primScreenBounds.getWidth() - newWindow.getWidth()) / 2);
			newWindow.setY((primScreenBounds.getHeight() - newWindow.getHeight()) / 2);
			
		}catch(Exception e) {
			System.out.print("Display Error");
			e.printStackTrace();
		}
	}
}
