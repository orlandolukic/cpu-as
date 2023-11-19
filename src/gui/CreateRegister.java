package gui;

import controller.ScreenPeripheralController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CreateRegister {

	public static void Initialize8BitRegister(
    		Label[] RegisterBits, 
    		String[] RegisterBitNames, 
    		HBox RegisterHBOX,
    		Label RegBitName
    	) {

    	RegisterHBOX.setAlignment(Pos.CENTER);
    	RegisterHBOX.setSpacing(15);
    	for(int i=7; i >= 0; i--) {
    		RegisterBits[i] = new Label("0");
    		RegisterBits[i].setFont(Font.font("Courier New", FontWeight.BOLD, 20));
    		RegisterBits[i].setPadding(new Insets(0, 3, 0, 3));

    		if ( RegisterBitNames != null ) {
				RegisterBits[i].setCursor(Cursor.HAND);
				String BitName = RegisterBitNames[7 - i];
				if (!BitName.equals("")) {
					RegisterBits[i].setOnMouseEntered(event -> {
						RegBitName.setText(BitName);
						RegBitName.setVisible(true);
					});
					RegisterBits[i].setOnMouseExited(event -> {
						RegBitName.setText("");
						RegBitName.setVisible(false);
					});
				} else {
					RegisterBits[i].setTextFill(Color.valueOf("#A1A1A1"));
				}
			};
    		
    		
    		RegisterHBOX.getChildren().add(RegisterBits[i]);
    	}
    }
	
	
	 public static void Initialize16BitRegister(
	    		Label[] RegisterBits, 
	    		HBox RegisterHBOX1,
	    		HBox RegisterHBOX2
	    	) {
	    	
	    	RegisterHBOX1.setAlignment(Pos.CENTER);
	    	RegisterHBOX1.setSpacing(15);
	    	RegisterHBOX2.setAlignment(Pos.CENTER);
	    	RegisterHBOX2.setSpacing(15);
	    	
	    	for(int i=15; i >= 8; i--) {
	    		RegisterBits[i] = new Label("0");
	    		RegisterBits[i].setFont(Font.font("Courier New", FontWeight.BOLD, 20));
	    		
	    		RegisterHBOX1.getChildren().add(RegisterBits[i]);
	    	}
	    	
	    	for(int i=7; i >= 0; i--) {
	    		RegisterBits[i] = new Label("0");
	    		RegisterBits[i].setFont(Font.font("Courier New", FontWeight.BOLD, 20));
	    		
	    		RegisterHBOX2.getChildren().add(RegisterBits[i]);
	    	}
	    	
	    }
	
}
