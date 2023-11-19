package gui;

import controller.MainScreenController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class HelpPopUpWindow {
	
	public static void display() {


		Stage newWindow = new Stage();

		try {
			DemoClass d = new DemoClass();
			FXMLLoader loader = new FXMLLoader(d.getClass().getResource("HelpWindow.fxml"));
			Parent root = loader.load();
			newWindow.getIcons().add(new Image("file:" + Main.PROGRAM_ICON));
			newWindow.setResizable(false);
			newWindow.setTitle( Main.PROGRAM_NAME + " - Help" );
			newWindow.setScene(new Scene(root));
			newWindow.initModality(Modality.APPLICATION_MODAL);
			// Window Title
			newWindow.setTitle( Main.PROGRAM_NAME + " - Help");
			newWindow.getIcons().add(new Image("file:" + Main.PROGRAM_ICON));
			newWindow.setResizable(false);
			newWindow.show();

			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			newWindow.setX((primScreenBounds.getWidth() - newWindow.getWidth()) / 2);
			newWindow.setY((primScreenBounds.getHeight() - newWindow.getHeight()) / 2);
		} catch ( Exception e ) {}
	}
	
}
