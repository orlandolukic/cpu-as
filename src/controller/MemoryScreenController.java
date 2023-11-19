package controller;

import java.net.URL;
import java.util.ResourceBundle;

import assembler.AccessRights;
import assembler.EntryPoint;
import assembler.SymbolTableElement;
import debug.memory.Memory;
import gui.AdaptedButton;
import gui.Main;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import org.antlr.v4.runtime.atn.ParseInfo;

import gui.CreateMemoryRow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import utils.Utilities;

public class MemoryScreenController implements Initializable {
	
	// Buttons
	@FXML
	public Button DisplayBtn;
	@FXML
	public Button WriteBtn;
	
	// Text Fields
	@FXML
	public TextField StartAddressTextField;
	@FXML
	public TextField AddressTextField;
	@FXML
	public TextField ValueTextField;
	
	// Other
	@FXML
	public VBox MemoryVBOX;
	
	public CreateMemoryRow[] Memory;
	
	public static final int MaxAddress = Integer.parseInt("ff00", 16);
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {


		DisplayBtn.getStyleClass().add("button");
		WriteBtn.getStyleClass().add("button");
		AdaptedButton.makeAdaptedButtonFrom(DisplayBtn);
		AdaptedButton.makeAdaptedButtonFrom(WriteBtn);
		
		addTextLimiter(ValueTextField, 2);
		addTextLimiter(StartAddressTextField, 4);
		addTextLimiter(AddressTextField, 4);
		
		// Restricting TextFields to Hexadecimal Input ///////////////////////////////////////////
		StartAddressTextField.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue,
	                            String newValue) {
	            if (!newValue.matches("^[0-9A-Fa-f]+$")) {
	            	StartAddressTextField.setText(newValue.replaceAll("[^\\d]", ""));
	            }
	        }
	    });

		ValueTextField.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue,
	                            String newValue) {
	            if (!newValue.matches("^[0-9A-Fa-f]+$")) {
	            	ValueTextField.setText(newValue.replaceAll("[^\\d]", ""));
	            }
	        }
	    });

		AddressTextField.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue,
	                            String newValue) {
	            if (!newValue.matches("^[0-9A-Fa-f]+$")) {
	            	AddressTextField.setText(newValue.replaceAll("[^\\d]", ""));
	            }
	        }
	    });

		StartAddressTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if ( event.getCode().getCode() == 10 ) {
					displayHandler();
				}
			}
		});
		
		///////////////////////////////////////////////////////////////////////////////////////
	    
		
		CreateMemoryRow.MemoryVBOX = MemoryVBOX;
		Memory = new CreateMemoryRow[16];
		
		InitializeMemorytoZero();
		
		// Display Button
		DisplayBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
				displayHandler();
			}
		});
		
		// Write Button
		WriteBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	
		    	
		    	if(AddressTextField.getText().length() == 0 || ValueTextField.getText().length() == 0) {
		    		
		    		Alert alert = new Alert(AlertType.ERROR);		    		
		    		alert.setContentText("Must enter address and value first!");

		    		alert.showAndWait();
		    		return;
		    		
		    	}
		    	int Value = Integer.parseInt(ValueTextField.getText(), 16);

	    		WriteToAddress(
	    				Memory, 
	    				AddressTextField.getText() , 
	    				 Integer.toHexString(Value)
	    				);
		    	
		    }
		});
		
	}

	private void displayHandler()
	{
		if( StartAddressTextField.getText().length() == 0 ) {
			InitializeMemorytoZero();
			return;
		}

		if ( !StartAddressTextField.getText().matches("(.*)0$") )
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Address must end with 0!");
			alert.showAndWait();
			return;
		}

		MemoryVBOX.getChildren().clear();

		int StartAddressHex = Integer.parseInt(StartAddressTextField.getText(), 16);

		if(StartAddressHex <= MaxAddress) {
			InitMemory(StartAddressHex);
		}else {

			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Max Address must be FF00!");
			alert.showAndWait();
			//return;
		};
	}

	
	
	public static void WriteToAddress(CreateMemoryRow[] Memory, String Address, String Value) {
		
		// int Column = Integer.parseInt(Address.substring(0, Address.length() - 1));
		Address = String.format("%04X", Integer.parseInt(Address,16));
		String Column = Address.substring(0, Address.length()-1) + "0";
		int Row = Integer.parseInt(Address.substring(Address.length()-1), 16);
		int value = Integer.parseInt(Value, 16);
		int address = Integer.parseInt(Address, 16);
		
		if(Value.length() == 1) {
			Value = "0" + Value;
		}

		boolean[] allow = new boolean[3];
		SymbolTableElement[] el = new SymbolTableElement[1];
		allow[0] = true;			// grant access
		allow[1] = false;			// if section is found
		allow[2] = true;			// toShow address is not valid modal
		for(int i=0; i<Memory.length; i++) {
			if(Memory[i].StartingAddress.equals(Column)) {
				EntryPoint.getSymbolTable().forEachSection((sec) -> {
					if ( !allow[1] && address >= sec.getSectionAddress() && address < (sec.getSectionAddress() + sec.size) )
					{
						allow[0] = sec.getAccessRights().isAllowedTo( AccessRights.WRITE );
						allow[1] = true;
						el[0] = sec;
					};
				});
				if ( !allow[0] ) {
					Platform.runLater(() -> {
						Alert a = new Alert(Alert.AlertType.ERROR);
						a.setTitle( Main.PROGRAM_NAME + " - Access Violation");
						a.setHeaderText("Access violation at address " + Utilities.getHexadecimalFormat(4, address));
						a.setContentText("Cannot perform 'WRITE' operation for section '" + el[0].name + "' - rights: " + el[0].getAccessRights());
						a.showAndWait();
					});
					allow[2] = false;
					break;
				};
				Memory[i].AddressValues[Row].setText(Value.toUpperCase());
				debug.memory.Memory.get().writeByteAtAddress( address, value );
				return;
			}
		}

		if ( allow[2] ) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Entered address is Invalid!");
			alert.showAndWait();
		};
		return;
		
	}
	
	
	// Set MaxLength to a TextField
	public static void addTextLimiter(final TextField tf, final int maxLength) {
	    tf.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
	            if (tf.getText().length() > maxLength) {
	                String s = tf.getText().substring(0, maxLength);
	                tf.setText(s);
	            }
	        }
	    });
	}
	
	public void InitializeMemorytoZero() {
		MemoryVBOX.getChildren().clear();
    	InitMemory(0);
	}

	public void InitMemory( int start )
	{
		for(int i = 0; i<16; i++) {
			Memory[i] = new CreateMemoryRow( Integer.toHexString(start + i*0x10 ));
		}
	}
	
}
