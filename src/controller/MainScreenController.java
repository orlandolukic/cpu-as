package controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

import javax.swing.*;

import assembler.EntryPoint;
import checkings.SemanticPassThread;
import gui.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import utils.ApplicationThread;
import utils.Utilities;

import static java.lang.Thread.sleep;

public class MainScreenController implements Initializable {
	
	private static MainScreenController instance;
	public static MainScreenController getInstance() {
		return instance;
	}
	
	// Main Menu
	@FXML
	static public Button NewFileBtn = new AdaptedButton("New file");
	@FXML
	static public Button OpenFileBtn = new AdaptedButton("Open file");
	@FXML
	static public Button SaveAsBtn = new AdaptedButton("Save As");
	@FXML
	static public Button SaveBtn = new AdaptedButton("Save");
	@FXML
	static public Button AssembleBtn = new AdaptedButton("Assemble");
	@FXML
	static public Button DebugBtn = new AdaptedButton("Debug");
	@FXML
	static public Button HelpBtn = new AdaptedButton("Help");
	
	// Scene Builder Items
	@FXML
	public BorderPane RootPane;
	@FXML
	public VBox DebugVBOX;
	@FXML
	public HBox MenuHBOX;
	@FXML
	public TitledPane DebugTitledPane;
	@FXML
	public TabPane CodeTabPane;
	@FXML
	public TextArea OutputTextArea;
	@FXML
	public TitledPane CodeTitledPane;
	@FXML
    public ScrollPane DebugRegistersScrollPane;
	@FXML
	public ProgressIndicator ProgressIndicator;
	@FXML
	public Button MemoryBtn;
	@FXML
	public Button PeripheralsBtn;
	@FXML
	public Label PSW_I_bit;
	@FXML
	public Label PSW_Z_bit;
	@FXML
	public Label PSW_C_bit;
	@FXML
	public Label IMR3_bit;
	@FXML
	public Label IMR2_bit;
	@FXML
	public Label IMR1_bit;
	@FXML
	public Label IMR0_bit;
	@FXML
	public Button StackBtn;
	@FXML
	public Label PRINTR2_bit;
	@FXML
	public Label PRINTR3_bit;

	public DebugSection DebugSection;

	static public CodeTab CurrentTab = null;

	public static SemanticPassThread spt;

	public static void appendOutputText( String text )
	{
		instance.OutputTextArea.appendText(text + "\n");
	}

	public static void appendNewLineWithText( String text )
	{
		instance.OutputTextArea.appendText("===========================================================================================\n");
		instance.OutputTextArea.appendText(text + "\n");
	}

	public static void resetOutputText()
	{
		instance.OutputTextArea.clear();
	}

	public static void setOutputText( String text )
	{
		instance.OutputTextArea.setText( text + "\n" );
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		instance = this;
		spt = new SemanticPassThread();
		spt.start();
		DebugSection = new DebugSection();
		RootPane.getStyleClass().add("bg-white");
		CodeTitledPane.getStyleClass().add("bg-white");
		CodeTabPane.getStyleClass().add("bg-white");

		DebugPopUpWindow.DebugTitledPane = DebugTitledPane;
		DebugPopUpWindow.CodeTitledPane = CodeTitledPane;
		DebugPopUpWindow.DebugVBOX = DebugVBOX;
		DebugPopUpWindow.MenuHBOX = MenuHBOX;
		DebugPopUpWindow.DebugRegistersScrollPane = DebugRegistersScrollPane;

		ProgressIndicator.setOpacity(0);		
		OutputTextArea.setFont(new Font("Courier New", 15));
        OutputTextArea.setCache(false);
		MainScreenController.getInstance().setDisableActionButtons(true);
		
		// New File Button
		NewFileBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
		NewFileBtn.getStyleClass().add("button");
		NewFileBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	
		    	CreateAndAddNewTab(null);
		    }// [INFO] ...
		    // [ERROR] ...
		});
		
		// Open File Button
		OpenFileBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
		OpenFileBtn.getStyleClass().add("button");
		OpenFileBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	FileChooser fileChooser = new FileChooser();
		    	fileChooser.setTitle("Open New File");
		    	File OpenedFile = fileChooser.showOpenDialog(Main.MainStage);
		    	
		    	try {
			    	String fileName = OpenedFile.getName();
			    	String ext = fileName.replaceAll("^[a-zA-Z\\-0-9_]+\\.", "");
			    	
			    	if ( (ext.equals("txt") || ext.equals("s")) ) {
			    		Utilities.runAndWait(() -> {
							ProgressIndicator.setOpacity(1);
							disableAllButtons();
						});
			    		if ( CurrentTab != null )
			    			CurrentTab.TextPane.setEnabled(false);
						ApplicationThread t = new ApplicationThread( () ->  {
			    			CreateAndAddNewTab(OpenedFile);
			    			Platform.runLater(() -> {
			    				ProgressIndicator.setOpacity(0);
			    				enableAllButtons();
								CurrentTab.StyleDocument.highlightAll();
			    			});
			    		} );
			    		t.start();			    				    		
			    	} else 
			    	{		    		
			    		OutputTextArea.appendText("Wrong file format\n");
			    	}
		    	} catch( NullPointerException w ) {}
		    }
		});
		
		// Save As Button
		SaveAsBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
		SaveAsBtn.getStyleClass().add("button");
		SaveAsBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	
		    	if(CurrentTab == null) {
		    		OutputTextArea.appendText("You must create new file first!\n");
		    	}
		    	else {
		    		SaveAsFunction();
		    	}
		    }
		});
		
		// Save Button
		SaveBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
		SaveBtn.getStyleClass().add("button");
		SaveBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	
		    	saveButtonFunction();
		    	
		    }
		});
		
		// Assemble Button
		AssembleBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
		AssembleBtn.getStyleClass().add("button");
		AssembleBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	Assemble(null, null);
		    }
		});
		
		// Debug Button
		DebugBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
		DebugBtn.getStyleClass().add("button");
		DebugBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	
		    	if ( CurrentTab == null )
		    		OutputTextArea.appendText("You must create new file first!\n");
		    	else {
					ApplicationThread t1 = new ApplicationThread(() -> {
						Semaphore s = new Semaphore(0);
						boolean[] b = new boolean[1];
						MainScreenController.getInstance().Assemble( s, b );
						try {
							s.acquire();
						} catch( InterruptedException q ) {
							return;
						}
						if ( !b[0] ) {
							Platform.runLater(() -> {
								Alert a = new Alert(Alert.AlertType.ERROR);
								a.setTitle( Main.PROGRAM_NAME + " - Error occurred");
								a.setHeaderText("Assembly file is not valid");
								a.setContentText("Please check input file and correct error(s)");
								a.showAndWait();
							});
							return;
						} else
							DebugPopUpWindow.display();
					});
		    		t1.start();
		    	};		    	
		    }
		});
		
		// Help Button
		HelpBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
		HelpBtn.getStyleClass().add("button");
		HelpBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	
		    	HelpPopUpWindow.display();
		    	// CurrentTab.ScrollPane.repaint();
		    	
		    }
		});

		CodeTitledPane.setPrefWidth(1024);
		
		// Menu HBox Initialization
		MenuHBOX.getChildren().addAll(MainScreenController.NewFileBtn, 
				MainScreenController.OpenFileBtn,
				MainScreenController.SaveAsBtn,
				MainScreenController.SaveBtn,
				MainScreenController.AssembleBtn,
				MainScreenController.DebugBtn,
				MainScreenController.HelpBtn);
		
		DebugTitledPane.setVisible(false);

		DebugSection.init();
	}
	// } initialize ...

	public void Assemble( Semaphore waitSem, boolean[] ret )
	{
		if ( CurrentTab == null ) {
			OutputTextArea.appendText("You must create new file first!\n");
			return;
		} else {
			if ( CurrentTab.TextFile == null ) {
				if ( !SaveAsFunction() ) {
					if ( waitSem != null ) {
						ret[0] = false;
						waitSem.release();
					}
					return;
				};
			};

			// Check if file is correctly written!
			MainScreenController.getInstance().ProgressIndicator.setOpacity(1);
			Semaphore s = new Semaphore(0);
			Semaphore s1 = new Semaphore(0);
			Semaphore s2 = new Semaphore(0);
			boolean[] retval = new boolean[1];
			retval[0] = true;
			ApplicationThread t = new ApplicationThread( () -> {
				boolean r;
				boolean hasErrors = spt.checkAndWait(s);
				if ( hasErrors ) {
					if ( waitSem != null ) {
						ret[0] = false;
						waitSem.release();
					};
					return;
				};

				Platform.runLater(() -> {
					boolean x;
					SaveFunction(CurrentTab.TextFile);
					OutputTextArea.appendText("Assemble started...\n");

					String filename = Utilities.getFileName( CurrentTab.TextFile.getName() );

					try {
						EntryPoint.assemble(new String[]{
								CurrentTab.TextFile.getParent() + "\\" + CurrentTab.TextFile.getName(),
								"-o",
								CurrentTab.TextFile.getParent() + "\\" + filename + "-mem.txt",
								"-maddr=16",
								"--dump"
						});
					} catch( RuntimeException re ) {
						OutputTextArea.appendText(re.getMessage() + "\n");
					}

					Platform.runLater(() -> {
						MainScreenController.getInstance().ProgressIndicator.setOpacity(0);
						MainScreenController.getInstance().setDisableActionButtons(false);
					});

					if ( !EntryPoint.isErrorOccurred() )
					{
						OutputTextArea.appendText( EntryPoint.getSymbolTable().dump() + "\n");
						OutputTextArea.appendText( EntryPoint.getSymbolTable().getAllSectionsContent() + "\n" );
						x = true;
					} else
						x = false;

					if ( waitSem != null )
					{
						ret[0] = x;
						waitSem.release();
					};
				});
			} );
			t.start();
		}
	}
	
    static public void saveTextToFile(String content, File file) {

		if ( CurrentTab.text == null || content.hashCode() != CurrentTab.text.hashCode() ) {
			try {
				PrintWriter writer;
				writer = new PrintWriter(file);
				writer.println(content);
				writer.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			CurrentTab.hash = CurrentTab.TextPane.getText().hashCode();
			CurrentTab.text = CurrentTab.TextPane.getText();
		}
    }
    
    static public boolean SaveAsFunction() {

		if ( !Platform.isFxApplicationThread() ) {
			Semaphore s = new Semaphore(0);
			boolean[] b = new boolean[1];
			Platform.runLater(() -> {
				b[0] = SaveAsFunctionInside();
				s.release();
			});
			try {
				s.acquire();
			} catch ( InterruptedException e ) {}
			return b[0];
		} else
			return SaveAsFunctionInside();

    }

    private static boolean SaveAsFunctionInside()
	{
		FileChooser fileChooser = new FileChooser();

		//Set extension filter for text files
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("S files (*.s)", "*.txt", "*.s");
		fileChooser.getExtensionFilters().add(extFilter);

		//Show save file dialog
		File file = fileChooser.showSaveDialog(Main.MainStage);
		if ( file != null )
		{
			CurrentTab.TextFile = file;
			CurrentTab.NewTab.setText(file.getName());
			SwingUtilities.invokeLater(() -> {
				CurrentTab.TextPane.grabFocus();
			});

			return SaveFunction(file);
		} else
			return false;
	}
    
    static public boolean SaveFunction(File SaveFile) {
        if (SaveFile != null) {
            saveTextToFile(CurrentTab.TextPane.getText(), SaveFile);
            CurrentTab.FileSaved = true;
            return true;
        }else {
        	return SaveAsFunction();        	
        }
    }
    
    public void CreateAndAddNewTab(File file) {

		// Save data before opening new file.
		if ( CurrentTab != null )
		{
			CurrentTab.outputText = MainScreenController.getInstance().OutputTextArea.getText();
		};
    	
    	CodeTab NewCodeTab = new CodeTab("New tab");
    	NewCodeTab.TextFile = file;
    	if(file != null) {
    		NewCodeTab.StyleDocument.setToGoThrough(false);
        	NewCodeTab.TextPane.setText( ReadFromFile(file) );
        	NewCodeTab.StyleDocument.setToGoThrough(true);
        	NewCodeTab.FileSaved = true;
			Utilities.runLater(() -> {
				NewCodeTab.NewTab.setText(file.getName());
				NewCodeTab.hash = NewCodeTab.TextPane.getText().hashCode();
				NewCodeTab.text = NewCodeTab.TextPane.getText();
				MainScreenController.spt.check();
			} );
    	}
    	CurrentTab = NewCodeTab;
		MainScreenController.getInstance().setDisableActionButtons(false);
    	
    	if ( !Platform.isFxApplicationThread() ) {
	    	Semaphore s = new Semaphore(0);
	    	Platform.runLater( () -> {    		
	    		addTab(NewCodeTab);
	        	s.release();
	    	} );    	
	    	try {
	    		s.acquire();
	    	} catch( InterruptedException x ) {}
    	} else
    	{
			addTab(NewCodeTab);
    	};   	
    }

    private void addTab(CodeTab NewCodeTab)
	{
		CodeTabPane.getTabs().add(NewCodeTab.NewTab);
		NewCodeTab.setTabIndex( CodeTabPane.getTabs().size()-1 );
		CurrentTab.NewTab.getTabPane().getSelectionModel().select(CodeTabPane.getTabs().size()-1);
	}
    
    public String ReadFromFile(File file) {
    	String text = "";
    	
    	try {
			
    		text = new String( Files.readAllBytes(Paths.get(file.toString())) );
    		
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    	return text;
    }
    
    public void saveButtonFunction() {

    	if(CurrentTab == null) {
    		OutputTextArea.appendText("You must create new file first!\n");
    	}
    	else {
	    	if( (CurrentTab == null) || !CurrentTab.FileSaved) {
	    		OutputTextArea.appendText("You have to save file first!\n");
	    		SaveAsFunction();
	    	}
	    	else {
	    		SaveFunction(CurrentTab.TextFile);
				OutputTextArea.appendText("File successfully saved!\n");
	    	}
    	}
    }
    
    public void disableAllButtons()
    {
    	_setDisableButtons(true);
    }
    
    public void enableAllButtons()
    {
    	_setDisableButtons(false);
    }
    
    private void _setDisableButtons(boolean dis)
    {
    	NewFileBtn.setDisable(dis);
    	OpenFileBtn.setDisable(dis);
    	SaveAsBtn.setDisable(dis);
    	SaveBtn.setDisable(dis);
    	AssembleBtn.setDisable(dis);
    	DebugBtn.setDisable(dis);
    	HelpBtn.setDisable(dis);    	
    }

    public void setDisableActionButtons( boolean dis )
	{
		SaveAsBtn.setDisable(dis);
		SaveBtn.setDisable(dis);
		AssembleBtn.setDisable(dis);
		DebugBtn.setDisable(dis);
	}

	static public void RefreshScrollPane(long SleepTime) {
		RefreshScrollPane(SleepTime, null);
	}
    
    static public void RefreshScrollPane(long SleepTime, Runnable after) {
    	Runnable r = new Runnable() {
    		@Override public void run() {
    			try {
					ApplicationThread t = new ApplicationThread(() -> {
						try {
							sleep(SleepTime);
						} catch( InterruptedException e ) {}
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								if ( CurrentTab != null ) {
									CurrentTab.ScrollPane.repaint();
									CurrentTab.TextPane.repaint();

									if ( after != null ) {
										try {
											sleep(SleepTime);
										} catch( InterruptedException e ) {}
										after.run();
									}
								}
							}
						});
					});
        			t.start();
        		} catch (Exception e1) {			
        			e1.printStackTrace();
        		}
    		}
    	};
    	if ( Platform.isFxApplicationThread() )
    		r.run();
    	else
    		Platform.runLater(r);
    }
	
}
