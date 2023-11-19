package gui;

import controller.MainScreenController;
import controller.ScreenPeripheralController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import utils.ApplicationThread;
import utils.Regex;

import java.util.Calendar;
import java.util.Date;

public class Main extends Application{
	
	public static final String PROGRAM_NAME = "cpuASM";
	public static final String PROGRAM_ICON = "res/logo.png";
	
	@FXML 
	static public Stage MainStage;
	
	@Override
    public void start(Stage primaryStage) throws Exception{

		MainStage = primaryStage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ApplicationWindow.fxml"));
        Parent root = loader.load();

        // Stage Setup
        primaryStage.setTitle(PROGRAM_NAME);
        primaryStage.getIcons().add(new Image( "file:" + PROGRAM_ICON ) );
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        scene.getStylesheets().add("ThemeCSS.css");

        primaryStage.show();
        primaryStage.setResizable(false);


        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);

        primaryStage.getScene().getAccelerators().put(KeyCombination.keyCombination("CTRL+S"), KeyCombinationRunnables.CTRLS);
        primaryStage.getScene().getAccelerators().put(KeyCombination.keyCombination("CTRL+V"), KeyCombinationRunnables.CTRLV);
        primaryStage.getScene().getAccelerators().put(KeyCombination.keyCombination("CTRL+O"), KeyCombinationRunnables.CTRLO);
        primaryStage.getScene().getAccelerators().put(KeyCombination.keyCombination("CTRL+Q"), KeyCombinationRunnables.CTRLQ);
        primaryStage.getScene().getAccelerators().put(KeyCombination.keyCombination("CTRL+N"), KeyCombinationRunnables.CTRLN);
        primaryStage.getScene().getAccelerators().put(KeyCombination.keyCombination("CTRL+D"), KeyCombinationRunnables.CTRLD);
        primaryStage.getScene().getAccelerators().put(KeyCombination.keyCombination("F6"), KeyCombinationRunnables.F6);
        primaryStage.getScene().getAccelerators().put(KeyCombination.keyCombination("F7"), KeyCombinationRunnables.F7);
        primaryStage.getScene().getAccelerators().put(KeyCombination.keyCombination("F8"), KeyCombinationRunnables.F8);
        primaryStage.getScene().getAccelerators().put(KeyCombination.keyCombination("F9"), KeyCombinationRunnables.F9);

        primaryStage.setOnHidden((event) -> {
            MainScreenController.spt.interrupt();
            ApplicationThread.shutdownAllThreads();
            if ( ScreenPeripheralWindow.StageInstance != null ) {
                ScreenPeripheralWindow.StageInstance.close();
            }
            if ( KeyboardPeripheralWindow.StageInstance != null )
                KeyboardPeripheralWindow.StageInstance.close();
            if ( MainScreenController.getInstance().DebugSection.debugControlPanel.execution != null ) {
                MainScreenController.getInstance().DebugSection.debugControlPanel.execution.stopExecution();
            };

            if ( MainScreenController.getInstance().DebugSection.debugControlPanel.keyboardPeripheralThread != null )
                MainScreenController.getInstance().DebugSection.debugControlPanel.keyboardPeripheralThread.interrupt();
            if ( MainScreenController.getInstance().DebugSection.debugControlPanel.screenPeripheralThread != null )
                MainScreenController.getInstance().DebugSection.debugControlPanel.screenPeripheralThread.interrupt();
        });
    }


    public static void main(String[] args) {
	    launch(args);
    }

}
