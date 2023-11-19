package controller;

import debug.registers.PeripheralRegister;
import gui.AdaptedButton;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ResourceBundle;

import gui.CreateRegister;
import utils.Utilities;

public class KeyboardScreenController implements Initializable {

	private static KeyboardScreenController instance;
	public static KeyboardScreenController getController() {
		return instance;
	}
	public static boolean isInitialized;

	static {
		StatusRegister = new PeripheralRegister("KEYBOARD_STATUS", 8, 0xFF10, 0);
		ControlRegister = new PeripheralRegister("KEYBOARD_CONTROL", 8, 0xFF12, 0);
		DataRegister = new PeripheralRegister("KEYBOARD_DATA", 16, 0xFF14, 0);
		CntDataRegister = new PeripheralRegister("KEYBOARD_CNTDATA", 8, 0xFF16, 0);

		StatusBitNames = new String[]{
				"", "ERROR", "", "READY", "", "AVAIL", "LOADED", ""
		};

		ControlBitNames = new String[]{
				"INTR", "RST", "DECIMAL", "CONSUME", "CLEAR", "ENA", "CONF", "READ"
		};

		CntDataBitNames = null;
	}

	public static void resetRegisters()
	{
		StatusRegister.reset();
		ControlRegister.reset();
		DataRegister.reset();
		CntDataRegister.reset();
		isInitialized = false;
	}

	public void resetDebugging() {
		Utilities.runLater(() -> {
			clearBuffer();
		});
	}
	
	// Status
	@FXML
	public HBox StatusHBOX;
	@FXML
	public Label StatusBitName;
	public static Label[] StatusBits;
	public static String[] StatusBitNames;
	
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
	
	// CntData
	@FXML
	public HBox CntDataHBOX;
	@FXML
	public Label CntDataBitName;
	public static Label[] CntDataBits;
	public static String[] CntDataBitNames;
	
	// Keyboard Peripheral
	@FXML
	public VBox BufferVBOX;
	@FXML
	public ToggleButton KeyboardToggleBtn;
	
	@FXML
	public ProgressIndicator KeyboardProgressBar;

	@FXML
	public AnchorPane WorkingPane;

	@FXML
	public BorderPane RootNode;

	public static PeripheralRegister StatusRegister;
	public static PeripheralRegister ControlRegister;
	public static PeripheralRegister DataRegister;
	public static PeripheralRegister CntDataRegister;
	
    @Override
    public void initialize(URL location, ResourceBundle resources) {

		instance = this;
    	BufferVBOX.setSpacing(0);
    	KeyboardToggleBtn.getStyleClass().add("button");
		AdaptedButton.makeAdaptedButtonFrom(KeyboardToggleBtn);
    	KeyboardToggleBtn.setFocusTraversable(false);

		if (isInitialized) {

			StatusRegister.initLabels();
			ControlRegister.initLabels();
			DataRegister.initLabels();
			CntDataRegister.initLabels();

			MainScreenController.getInstance().DebugSection.debugControlPanel.keyboardPeripheralThread.getBuffer()
					.forEach((str) -> {
				addIntoBuffer(str);
			});
			WorkingPane.setVisible(false);

			return;
		};

    	StatusBits = new Label[8];
		ControlBits = new Label[8];
		DataBits = new Label[16];
		CntDataBits = new Label[8];
		StatusRegister.setBitLabels(StatusBits);
		ControlRegister.setBitLabels(ControlBits);
		DataRegister.setBitLabels(DataBits);
		CntDataRegister.setBitLabels(CntDataBits);

    	CreateRegister.Initialize8BitRegister(StatusBits, StatusBitNames, StatusHBOX, StatusBitName);
    	CreateRegister.Initialize8BitRegister(ControlBits, ControlBitNames, ControlHBOX, ControlBitName);
    	CreateRegister.Initialize16BitRegister(DataBits, DataHBOX1, DataHBOX2);
    	CreateRegister.Initialize8BitRegister(CntDataBits, CntDataBitNames, CntDataHBOX, CntDataBitName);

		StatusRegister.initLabels();
		ControlRegister.initLabels();
		DataRegister.initLabels();
		CntDataRegister.initLabels();

		MainScreenController.getInstance().DebugSection.debugControlPanel.keyboardPeripheralThread.getBuffer()
				.forEach((str) -> {
					addIntoBuffer(str);
				});

    	isInitialized = true;
		WorkingPane.setVisible(false);
    }
    
    public void ToggleButtonAction(KeyEvent event) {

    	if ( KeyboardToggleBtn.isSelected() && !event.isShiftDown() && !event.isControlDown() ) {
    		String str = event.getCode().equals(KeyCode.ENTER) ? "<ENTER>" : event.getText();
    		if ( str.equals("") )
    			return;
			MainScreenController.getInstance().DebugSection.debugControlPanel.keyboardPeripheralThread.insertIntoBuffer(str);
			addIntoBuffer(str);
		};
    	
    }

    public void addIntoBuffer( String content ) {
		Utilities.runAndWait(() -> {
			Label NewLabel = new Label(content);
			NewLabel.setFont(Font.font("Courier New", FontWeight.NORMAL, 18));
			NewLabel.setPadding(new Insets(0,0,0,0));
			BufferVBOX.getChildren().add(NewLabel);
		});
	}

	public void clearBuffer() {
    	Utilities.runAndWait(() -> {
			BufferVBOX.getChildren().clear();
		});
	}
    
}
