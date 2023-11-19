package controller;

import debug.DebugControlPanel;
import debug.registers.Register;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import utils.Utilities;

import java.net.URL;
import java.util.ResourceBundle;

import gui.CreateStack;

public class StackScreenController implements Initializable {

	public static StackScreenController instance;
	public static CreateStack Stack;
	
	@FXML
	public VBox AddrValues_VBOX; // Address Contents
	@FXML
	public VBox SP_VBOX; // SP
	@FXML
	public VBox Addr_VBOX; // Memory Address

	private Register sp;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
		instance = this;

		DebugControlPanel dcp = MainScreenController.getInstance().DebugSection.debugControlPanel;
    	sp = MainScreenController.getInstance().DebugSection.debugControlPanel.SP;
		Stack = new CreateStack( dcp.stack, Addr_VBOX, SP_VBOX, AddrValues_VBOX);
//		Stack.ChangeStartAddress(sp.getValue());

    	
    	//Stack.Push(16);
    	//Stack.Pop();
    	Stack.DrawStack();
    }

}
