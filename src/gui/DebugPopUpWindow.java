package gui;

import java.util.Iterator;

import assembler.EntryPoint;
import controller.MainScreenController;
import debug.DebugInfo;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import jdk.jshell.execution.Util;
import utils.Utilities;

import javax.swing.event.CaretListener;

import static controller.MainScreenController.CurrentTab;

public class DebugPopUpWindow {

	static public final double CODE_PANE_WIDTH = 540; // After Debugging
	public static final double DEBUG_PANE_WIDTH = 480;
	public static boolean IS_DEBUGGING = false;
	public static boolean IS_VISIBLE = false;
	
	static public TitledPane DebugTitledPane;
	static public TitledPane CodeTitledPane;
	static public VBox DebugVBOX;
	static public HBox MenuHBOX;
	public static Stage window;
	public static ScrollPane DebugRegistersScrollPane;

	private static TextField textField;
	
	public static void OkBtnClicked(Stage newWindow) {

		CodeTab tab = MainScreenController.CurrentTab;
		String text = textField.getText();
		if ( text.matches("^(0|[1-9][0-9]*)$") )
		{
			int ln = Integer.parseInt(text);
			if ( DebugInfo.getInstance().getLinesWithInstructionPC().get( Integer.valueOf(ln) ) == null && ln != 0 ) {
				Platform.runLater(() -> {
					Alert a = new Alert(Alert.AlertType.ERROR);
					a.setTitle( Main.PROGRAM_NAME + " - Start line error");
					a.setHeaderText("Provided line is not line which contains instruction");
					a.setContentText("Please enter line again.");
					a.showAndWait();
					textField.clear();
					textField.requestFocus();
				});
				return;
			} else if ( ln == 0 ) {
				Integer in = DebugInfo.getInstance().getInstructionPCWithLines().get( EntryPoint.getMainPC() );
				ln = in.intValue();
			}
			MainScreenController.getInstance().DebugSection.debugControlPanel.startLine = ln;
			Integer in = Integer.valueOf(ln);

			MenuHBOX.getChildren().clear();
			DebugSection.SwapMenu(MenuHBOX, 1);
			newWindow.close();

			Platform.runLater(() -> {
				tab.TextLineNumber.resetLineMap();
				DebugTitledPane.setVisible(true);
				MainScreenController.getInstance().DebugVBOX.getChildren().clear();
				MainScreenController.getInstance().DebugSection.debugControlPanel.group.resetAllRegisters();
				MainScreenController.getInstance().DebugSection.debugControlPanel.group.forEachRegister((r) -> {
					DebugSection.AddToDebugPane( DebugVBOX, r.getName(), r );
				});
				CodeTitledPane.setPrefWidth(CODE_PANE_WIDTH);
				DebugTitledPane.setPrefWidth(DEBUG_PANE_WIDTH);
				DebugVBOX.setPrefWidth(DEBUG_PANE_WIDTH - 3);
				DebugRegistersScrollPane.setPrefWidth(DEBUG_PANE_WIDTH - 3);
				MainScreenController.CurrentTab.TextPane.setEditable(false);
				MainScreenController.CurrentTab.TextPane.setFocusable(false);
				MainScreenController.CurrentTab.LinePainter.setAllowedToPaint(false);
				CurrentTab.LinePainter.resetPaintedBackground();
				activateDebugUtils(true);

				tab.removeAllCaretListeners();
				tab.LinePainter.clearRowParameters();

				MainScreenController.RefreshScrollPane(70);

				tab.LinePainter.paintDebugLine( in );

				// Start debug session!
				MainScreenController.getInstance().DebugSection.startDebugSession();
			});
		} else
		{
			Platform.runLater(() -> {
				Alert a = new Alert(Alert.AlertType.ERROR);
				a.setTitle( Main.PROGRAM_NAME + " - Error occurred");
				a.setHeaderText("Start line format error");
				a.setContentText("Please provide number as a start line.");
				a.showAndWait();
				textField.clear();
				textField.requestFocus();
			});
		};
	}

	public static void activateDebugUtils( boolean active )
	{
		MainScreenController.CurrentTab.TextPane.setEditable(!active);
		MainScreenController.CurrentTab.NewTab.setClosable(!active);
		IS_DEBUGGING = active;
		TabPane tp = MainScreenController.getInstance().CodeTabPane;
		ObservableList<Tab> tabs = tp.getTabs();
		Iterator<Tab> it = tabs.iterator();
		Tab t;
		while( it.hasNext() )
		{
			t = it.next();
			if ( !active ) {
				t.setDisable(false);
				t.setClosable(true);
			} else if ( MainScreenController.CurrentTab.NewTab != t ) {
				t.setDisable(active);
				t.setClosable(!active);
			}
		};
	}

	public static void display() {

		if ( Platform.isFxApplicationThread() )
			displayMethod();
		else
			Platform.runLater(() -> {
				displayMethod();
			});
	}

	public static void displayMethod()
	{
		if ( IS_VISIBLE )
		{
			window.requestFocus();
			return;
		};

		Stage newWindow = new Stage();
		window = newWindow;
		 //

		newWindow.initModality(Modality.APPLICATION_MODAL);
		newWindow.getIcons().add(new Image("file:" + Main.PROGRAM_ICON));
		// Window Title
		newWindow.setTitle( Main.PROGRAM_NAME + " - Choose start line" );

		// Window Message
		Font f = new Font("Courier New", 14);
		Font f1 = new Font("Courier New", 12);
		Label label1= new Label("Choose start line of the program:");
		label1.setFont(f1);

		Button OkBtn = new AdaptedButton("Confirm");
		Button CloseBtn= new AdaptedButton("Dismiss");
		OkBtn.setFont(f);
		CloseBtn.setFont(f);

		// OK Button Action
		OkBtn.setOnAction(e ->  {
			OkBtnClicked(newWindow);
		});

		// Close Button Action
		CloseBtn.setOnAction(e -> {
			closeHandler();
		});

		VBox GeneralLayout = new VBox(10);

		// Message to be displayed
		VBox TextLayout = new VBox(10);
		TextLayout.getChildren().addAll(label1);
		TextLayout.setAlignment(Pos.CENTER);

		// Input Field
		textField = new TextField();
		HBox TextInputLayout = new HBox();
		TextInputLayout.getChildren().addAll(textField);
		TextInputLayout.setAlignment(Pos.CENTER);
		textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if ( event.getCode().equals(KeyCode.ENTER) )
					OkBtnClicked(window);
				else if ( event.getCode().equals(KeyCode.ESCAPE) )
					newWindow.close();
			}
		});

		// Buttons
		HBox ButtonLayout = new HBox(10, OkBtn, CloseBtn);
		ButtonLayout.setAlignment(Pos.BOTTOM_CENTER);

		GeneralLayout.getChildren().addAll(TextLayout, TextInputLayout, ButtonLayout);
		GeneralLayout.setAlignment(Pos.CENTER);

		Scene scene1= new Scene(GeneralLayout, 300, 100);
		scene1.getStylesheets().add("ThemeCSS.css");
		OkBtn.getStyleClass().add("button");
		CloseBtn.getStyleClass().add("button");

		newWindow.setResizable(false);
		newWindow.setScene(scene1);
		newWindow.show();
		newWindow.setOnHiding((event) -> {
			closeHandler();
		});

		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		newWindow.setX((primScreenBounds.getWidth() - newWindow.getWidth()) / 2);
		newWindow.setY((primScreenBounds.getHeight() - newWindow.getHeight()) / 2);

		IS_VISIBLE = true;
	}

	private static void closeHandler()
	{
		IS_VISIBLE = false;
		window.close();
	}

}
