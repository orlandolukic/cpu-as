package controller;

import debug.registers.PeripheralRegister;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.ResourceBundle;

import gui.CreateRegister;
import utils.Utilities;

public class ScreenPeripheralController implements Initializable {

	public static boolean isInitialized;
	public static ScreenPeripheralController instance;
	public static ScreenPeripheralController getController()
	{
		return instance;
	}

	public static void resetRegisters()
	{
		StatusRegister.reset();
		ControlRegister.reset();
		DataRegister.reset();
		isInitialized = false;
	}

	static {
		StatusRegister = new PeripheralRegister("SCREEN_STATUS", 8, 0xFF00, 0);
		ControlRegister = new PeripheralRegister("SCREEN_CONTROL", 8, 0xFF02, 0b1000);
		DataRegister = new PeripheralRegister("SCREEN_DATA", 16, 0xFF04, 0);

		StatusBitNames = new String[]{
				"", "", "", "BUSY", "", "", "", ""
		};

		ControlBitNames = new String[]{
				"ENTR", "DECIMAL", "", "INIT", "WRE", "ENA", "CLR", ""
		};
	}
	
	// Status
	@FXML
	public HBox StatusHBOX;
	public static Label[] StatusBits;
	public static String[] StatusBitNames;
	@FXML
	public Label StatusBitName;
	
	// Control
	@FXML
	public HBox ControlHBOX;
	@FXML
	public Label ControlBitName;
	public static Label[] ControlBits;
	public static String[] ControlBitNames;
	
	// Data
	@FXML
	public HBox DataHBOX1;
	@FXML
	public HBox DataHBOX2;
	public static Label[] DataBits;
	
	// Peripheral
	@FXML
	public TextArea PeripheralTextArea;
	
	@FXML
	public ProgressIndicator ScreenProgressBar;

	@FXML
	public AnchorPane WorkingPane;

	public static PeripheralRegister StatusRegister;
	public static PeripheralRegister ControlRegister;
	public static PeripheralRegister DataRegister;

	public void resetDebugging() {
		Utilities.runLater(() -> {
			PeripheralTextArea.clear();
			PeripheralTextArea.setDisable(true);
		});
	}
	
    @Override
    public void initialize(URL location, ResourceBundle resources) {

		instance = this;
		PeripheralTextArea.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
		PeripheralTextArea.getStyleClass().add("screen");

		if (isInitialized) {

			StatusRegister.initLabels();
			ControlRegister.initLabels();
			DataRegister.initLabels();

			PeripheralTextArea.setText(
					MainScreenController.getInstance().DebugSection.debugControlPanel.screenPeripheralThread.getScreenContent().toString()
			);
			PeripheralTextArea.setDisable( !ControlRegister.getBit((char) 2) );
			WorkingPane.setVisible(false);

			return;
		}

		StatusBits = new Label[8];
		ControlBits = new Label[8];
		DataBits = new Label[16];
		StatusRegister.setBitLabels(StatusBits);
		ControlRegister.setBitLabels(ControlBits);
		DataRegister.setBitLabels(DataBits);

		CreateRegister.Initialize8BitRegister(StatusBits, StatusBitNames, StatusHBOX, StatusBitName);
		CreateRegister.Initialize8BitRegister(ControlBits, ControlBitNames, ControlHBOX, ControlBitName);
		CreateRegister.Initialize16BitRegister(DataBits, DataHBOX1, DataHBOX2);

		StatusRegister.updateLabels();
		ControlRegister.updateLabels();
		DataRegister.updateLabels();

		StatusRegister.initLabels();
		ControlRegister.initLabels();
		DataRegister.initLabels();

		isInitialized = true;

		PeripheralTextArea.setText(
				MainScreenController.getInstance().DebugSection.debugControlPanel.screenPeripheralThread.getScreenContent().toString()
		);
		PeripheralTextArea.setDisable( !ControlRegister.getBit((char) 2) );
		WorkingPane.setVisible(false);
	}
    
   
}
