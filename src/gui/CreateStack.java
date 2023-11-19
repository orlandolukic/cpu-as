package gui;

import controller.StackScreenController;
import debug.DebugControlPanel;
import debug.Stack;
import debug.memory.Memory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import utils.Utilities;

public class CreateStack {
	// VBOXs
	public VBox AddrValues_VBOX; 	// VBOX for StackValues (Center Pane)
	public VBox SP_VBOX; 			// VBOX for SP --> (Left Pane)
	public VBox Addr_VBOX; 			// VBOX for Addresses(Right Pane)
	private Memory mem;
	private Stack stack;
	
	public static final int STACK_SIZE = (int) Math.pow(2, 10);
	public static final double HBOX_HEIGHT = 37.5;
	
	public int StackPointerAddress; 
	public int StartAddress; // Starting Address of DrawStack

	//public int[] stack;
	
	public CreateStack(Stack stack, VBox Addr, VBox SP, VBox Val) {
		Addr_VBOX = Addr; 
		SP_VBOX = SP; 
		AddrValues_VBOX = Val;
		mem = Memory.get();
		this.stack = stack;
		
		StartAddress = STACK_SIZE - 1;
	}

	public void DrawStack() {
		Utilities.runLater(() -> {
			Addr_VBOX.getChildren().clear();
			SP_VBOX.getChildren().clear();
			AddrValues_VBOX.getChildren().clear();

			int elems = stack.elements();
			StartAddress = stack.getStackPointer() + Math.min(elems, 7);
			StackPointerAddress = stack.getStackPointer();
			boolean below = false;
			boolean forceEmpty = true;
			if ( elems < 8 ) {
				StartAddress--;
				forceEmpty = false;
			}
			below = elems == 0;
			boolean t;

			for (int i=0; i<8; i++) {

				// Address
				HBox AddrHbox = CreateHBOX(Utilities.getHexadecimalFormat(4, StartAddress - i), 0);
				Addr_VBOX.getChildren().add(AddrHbox);


				// Stack Pointer
				HBox SPHbox;
				t = false;
				if( (StartAddress - i) == StackPointerAddress ) {
					SPHbox = CreateHBOX("SP -- >", 1);
					t = true;
				}
				else {
					SPHbox = CreateHBOX("", 1);
				}
				SP_VBOX.getChildren().add(SPHbox);

				// Address Values
				HBox AddrValueHbox;
				String text;
				if ( below )
					text = "";
				else
					text = Utilities.getHexadecimalFormat(2, mem.getByteFromAddress(StartAddress-i));
				AddrValueHbox = CreateHBOX( text , 2 );
				AddrValues_VBOX.getChildren().add(AddrValueHbox);

				if ( t ) {
					t = false;
					below = true;
				};

			};
		});
	}
	
	
	// IsAddress - Is the created HBOX used for the CenterVBOX 
	public HBox CreateHBOX( String LabelText, int Type ) {
		// Type = 0 - Address
		// Type = 1 - SP
		// Type = 2 - Value
		
		HBox hbox = new HBox();
		hbox.setPrefHeight(HBOX_HEIGHT);
		hbox.setAlignment(Pos.CENTER);
		if(Type == 2) {
			hbox.setStyle("-fx-border-color: black; -fx-background-color:  #EBEBEB");
		}
		
		Label newLabel = new Label(LabelText);
		
		if(Type == 2 || Type == 1) {
			newLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 20));
		}
		else {
			newLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
		}
		
		hbox.getChildren().add(newLabel);
		
		return hbox;
	}
	
	public void ChangeStartAddress(int Val) {
		if(Val < 7 ) {
    		Alert alert = new Alert(AlertType.ERROR);		    		
    		alert.setContentText("StartAddress Not Valid!");

    		alert.showAndWait();
    		return;
		}
		StartAddress = Val;
	}

	public void ChangeStackPointerAddress( int addr ) {
		StackPointerAddress = addr;
	}

}
