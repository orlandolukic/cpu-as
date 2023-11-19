package gui;

import controller.MainScreenController;
import debug.memory.Memory;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class CreateMemoryRow {
	
	public static VBox MemoryVBOX;
	
	public static int LABEL_ARRAY_LENGTH = 16;

	public Label StartingAddressLabel;
	public String StartingAddress;
	public Label[] AddressValues;
	
	public HBox MainHBOX; // Contains StartingAddressLabel + AddressValuesHBOX
	public HBox AddressValuesHBOX;
	
	public CreateMemoryRow(String StartAddress){
		StartingAddress = StartAddress;

		int startAddress = Integer.parseInt(StartingAddress,16);
		StartingAddress = String.format("%04X", Integer.parseInt(StartingAddress,16));
		StartingAddressLabel = new Label("" +  StartingAddress );
		StartingAddressLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
		
		AddressValues = new Label[LABEL_ARRAY_LENGTH];
		
		MainHBOX = new HBox(20);
		MainHBOX.setAlignment(Pos.CENTER);
		
		AddressValuesHBOX = new HBox(5);
		AddressValuesHBOX.setAlignment(Pos.CENTER_RIGHT);
		
		for(int i=0; i<AddressValues.length; i++) {
			AddressValues[i] = new Label( getAddressAsText(Memory.get().getByteFromAddress( startAddress+i )) );
			AddressValues[i].setFont(Font.font("Courier New", 15));
			if ( startAddress + i == MainScreenController.getInstance().DebugSection.debugControlPanel.PC.getValue() ) {
				Tooltip t = new Tooltip("PC points to this address");
				t.setShowDelay(new Duration(0));
				t.setTextOverrun(OverrunStyle.ELLIPSIS);
				AddressValues[i].setTooltip(t);
				AddressValues[i].getStyleClass().add("pc-address");
			};
			AddressValuesHBOX.getChildren().add(AddressValues[i]);
		}
		
		MainHBOX.getChildren().addAll(StartingAddressLabel, AddressValuesHBOX);
		
		MemoryVBOX.getChildren().add(MainHBOX);
	}

	public String getAddressAsText(int value)
	{
		return String.format("%02x", value & 0xFF).toUpperCase();
	}
}

























