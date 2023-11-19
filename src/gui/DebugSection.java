package gui;

import assembler.EntryPoint;
import controller.KeyboardScreenController;
import controller.MainScreenController;
import controller.ScreenPeripheralController;
import debug.Breakpoints;
import debug.DebugControlPanel;
import debug.DebugInfo;
import debug.Execution;
import debug.memory.Memory;
import debug.registers.PSW;
import debug.registers.Register;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jdk.jshell.execution.Util;
import utils.*;

import javax.swing.*;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import static controller.MainScreenController.CurrentTab;
import static controller.MainScreenController.RefreshScrollPane;
import static gui.DebugPopUpWindow.*;
import static java.lang.Thread.interrupted;
import static java.lang.Thread.sleep;

public class DebugSection {

    public static final Font FONT_BOLD = Font.font("Courier New", FontWeight.BOLD, 18);
    public static final Font FONT_NORMAL = Font.font("Courier New", FontWeight.NORMAL, 18);
    public static final Font FONT_REG_NAME = Font.font("Courier New", FontWeight.BOLD, 18);

    public static void AddToDebugPane(VBox vbox, String LabelText, Register r ) {

        Label label = new Label(LabelText);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setFont(FONT_REG_NAME);
        label.setPrefWidth(90);

        String binaryNumber = Utilities.getBinaryFormat(r);

        Label value2 = new Label(binaryNumber);
        value2.setAlignment(Pos.CENTER_RIGHT);
        value2.setFont(FONT_NORMAL);
        value2.setPrefWidth(280);

        Label value3 = new Label( Utilities.getHexadecimalFormat(r) );
        value3.setAlignment(Pos.CENTER_RIGHT);
        value3.setFont(FONT_NORMAL);
        value3.setPrefWidth(110);

        Label value4 = new Label( Utilities.getDecimalFormat(r) );
        value4.setAlignment(Pos.CENTER_RIGHT);
        value4.setFont(FONT_NORMAL);
        value4.setPrefWidth(100);

        // Set labels for this register.
        Label[] labels = new Label[3];
        labels[Register.LABEL_BINARY] = value2;
        labels[Register.LABEL_HEXADECIMAL] = value3;
        labels[Register.LABEL_DECIMAL] = value4;
        r.setLabels(labels);

        // Spacing = 30
        HBox hbox = new HBox(10, label, value2, value3, value4);
        hbox.setPadding(new Insets(5, 10, 5, 10));
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Color c = Color.rgb(250,250,250);
                hbox.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
                hbox.getChildren().forEach(new Consumer<Node>() {

                    private int i = 0;

                    @Override
                    public void accept(Node node) {
                        if ( i > 0 && node instanceof Label )
                            ((Label) node).setFont( FONT_BOLD );
                        i++;
                    }

                });

            }
        });
        hbox.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hbox.setBackground(Background.EMPTY);
                hbox.getChildren().forEach(new Consumer<Node>() {

                    private int i = 0;

                    @Override
                    public void accept(Node node) {
                        if ( i > 0 && node instanceof Label )
                            ((Label) node).setFont( FONT_NORMAL );
                        i++;
                    }

                });
            }
        });

        vbox.getChildren().add(hbox);
    }

    // Debug Menu
    public Button StepBtn = new AdaptedButton("Step (F6)");
    public Button ContinueBtn = new AdaptedButton("Continue (F7)");
    public Button ResetBtn = new AdaptedButton("Reset Debugging (F8)");
    public Button StopBtn = new AdaptedButton("Stop Debugging (F9)");
    public Button MemoryBtn;
    public Button PeripheralsBtn;
    public Button StackBtn;
    public Label PSW_I_bit;
    public Label PSW_C_bit;
    public Label PSW_Z_bit;
    public Label IMR3_bit;
    public Label IMR2_bit;
    public Label IMR1_bit;
    public Label IMR0_bit;
    public Label PRINTR2_bit;
    public Label PRINTR3_bit;
    public DebugControlPanel debugControlPanel;
    public Consumer<Integer> ConsumerOnLineClick;
    public int PC;

    public DebugSection()
    {
        MemoryBtn = MainScreenController.getInstance().MemoryBtn;
        PeripheralsBtn = MainScreenController.getInstance().PeripheralsBtn;
        StackBtn = MainScreenController.getInstance().StackBtn;
        AdaptedButton.makeAdaptedButtonFrom(MemoryBtn);
        AdaptedButton.makeAdaptedButtonFrom(PeripheralsBtn);
        AdaptedButton.makeAdaptedButtonFrom(StackBtn);

        PSW_I_bit = MainScreenController.getInstance().PSW_I_bit;
        PSW_C_bit = MainScreenController.getInstance().PSW_C_bit;
        PSW_Z_bit = MainScreenController.getInstance().PSW_Z_bit;
        IMR3_bit = MainScreenController.getInstance().IMR3_bit;
        IMR2_bit = MainScreenController.getInstance().IMR2_bit;
        IMR1_bit = MainScreenController.getInstance().IMR1_bit;
        IMR0_bit = MainScreenController.getInstance().IMR0_bit;
        PRINTR2_bit = MainScreenController.getInstance().PRINTR2_bit;
        PRINTR3_bit = MainScreenController.getInstance().PRINTR3_bit;

        debugControlPanel = new DebugControlPanel(this);
    }

    private LinePainter painter()
    {
        return MainScreenController.CurrentTab.LinePainter;
    }

    public void init()
    {
        // Memory Button
        MemoryBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                MemoryPopUpWindow.Display();
            }
        });

        // Continue Button
        ContinueBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
        ContinueBtn.setStyle("-fx-focus-traversable: false;");
        ContinueBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                MainScreenController.appendOutputText("DEBUG: Continuing...");
                debugControlPanel.execution.continueExecution();
                ContinueBtn.setDisable(true);
                StepBtn.setDisable(true);
                ResetBtn.setDisable(true);
            }
        });

        // Step Button
        StepBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
        StepBtn.setStyle("-fx-focus-traversable: false;");
        StepBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                debugControlPanel.execution.step();
                ResetBtn.setDisable(false);
                ContinueBtn.setDisable(false);
            }
        });

        // Reset Button
        ResetBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
        ResetBtn.setStyle("-fx-focus-traversable: false;");
        ResetBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                MainScreenController.appendNewLineWithText("Restarting debug session...");
                DebugPopUpWindow.display();
            }
        });

        // Stop Button
        StopBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
        StopBtn.setStyle("-fx-focus-traversable: false;");
        StopBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                CodeTab tab = MainScreenController.CurrentTab;
                MainScreenController.appendOutputText("Debugging panel closed.");
                SwapMenu(MenuHBOX, 0);
                MainScreenController.getInstance().DebugVBOX.getChildren().clear();
                MainScreenController.getInstance().DebugTitledPane.setVisible(false);
                tab.TextPane.setEditable(true);
                tab.TextPane.setFocusable(true);
                tab.restoreAllCaretListeners();
                CodeTitledPane.setPrefWidth(1024);
                RefreshScrollPane(50);
                activateDebugUtils(false);
                IS_DEBUGGING = false;
                tab.LinePainter.setClearAllow(false);
                tab.LinePainter.setAllowedToPaint(true);
                tab.LinePainter.setColor(Styles.colorLineBackground);
                ApplicationThread t = new ApplicationThread(()->{
                    try {
                        sleep(100);
                    } catch( InterruptedException exx ) {}
                    tab.TextPane.requestFocus();
                    tab.LinePainter.resetPaintedBackground();
                });
                MainScreenController.getInstance().DebugSection.finishDebugSession(true);
                MainScreenController.getInstance().DebugSection.debugControlPanel.stopDebugSession();
                ScreenPeripheralWindow.closeWindow();
                KeyboardPeripheralWindow.closeWindow();
                StackPopUpWindow.closeWindow();
                t.start();
            }
        });

        StackBtn.setOnAction(event -> {
            StackPopUpWindow.display();
        });

        PeripheralsBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ScreenPeripheralWindow.display();
                KeyboardPeripheralWindow.display();
            }
        });

        ConsumerOnLineClick = new Consumer<Integer>() {
            @Override
            public void accept(Integer ln) {

                if ( !IS_DEBUGGING )
                    return;

                ApplicationThread t1 = new ApplicationThread(() -> {

                    RowInformation ri = Utilities.getRowStartIndex( MainScreenController.CurrentTab.TextPane.getText(), ln);
                    String line = Utilities.getLine( MainScreenController.CurrentTab.TextPane.getText(), ln );

                    line = line.replaceAll("([\\r\\n\\t]*$)|(^[ \\t]+)", "");
                    line = line.replaceAll("([ \\t]*\\@(.*))?$", "");

                    if ( !line.matches( "^" + Regex.REGEX_INSTRUCTION_LINE + "$" ) || line.matches("^[ \\t]*$") )
                        return;

                    boolean isBreakpoint = Breakpoints.get().toggleBreakpointLine( ln );

                    SwingUtilities.invokeLater(() -> {
                        MainScreenController.CurrentTab.TextPane.setCaretPosition(ri.getRealOffset());
                        CurrentTab.LinePainter.paintBreakpointLine(ri, !isBreakpoint);
                    });
                });
                t1.start();
            }
        };
    }

    public void startDebugSession()
    {
        MainScreenController.getInstance().appendNewLineWithText("DEBUG session is initiating...");

        ApplicationThread app = new ApplicationThread(() -> {
            if ( this.debugControlPanel.execution != null && this.debugControlPanel.execution.isWorking() ) {
                this.debugControlPanel.execution.stopExecution();
            }
        });
        app.start();

        this.debugControlPanel.PC.setValue( DebugInfo.getInstance().getLinesWithInstructionPC().get(this.debugControlPanel.startLine) );

        Breakpoints.restart();
        this.debugControlPanel.isActiveSession = true;

        StepBtn.setDisable(false);
        ContinueBtn.setDisable(false);
        ResetBtn.setDisable(false);

        Memory.get().clear();
        Memory.load();
        Label[] labels = new Label[] {
                IMR0_bit, IMR1_bit, IMR2_bit, IMR3_bit
        };
        this.debugControlPanel.IMR.setAdditionalLabels(labels);
        labels = new Label[] {
                PSW_Z_bit, PSW_C_bit, PSW_I_bit
        };
        this.debugControlPanel.PSW.setAdditionalLabels(labels);
        this.debugControlPanel.group.initialize();

        if ( !DebugInfo.getInstance().getInstructionPCWithLines().containsKey(this.debugControlPanel.PC.getValue()) )
        {
            Utilities.runLater(() -> {
                String s = "No instruction is found on address " + Utilities.getHexadecimalFormat(this.debugControlPanel.PC);
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle( Main.PROGRAM_NAME + " - Error occurred");
                a.setHeaderText("No instruction found");
                a.setContentText(s);
                a.showAndWait();
                MainScreenController.appendNewLineWithText("Error: " + s);
                StopBtn.getOnAction().handle(null);
            });
            return;
        };
        CurrentTab.LinePainter.paintDebugLine(
                DebugInfo.getInstance().getInstructionPCWithLines().get(this.debugControlPanel.PC.getValue() )
        );

        if (ScreenPeripheralController.getController() != null)
        {
            ScreenPeripheralController.resetRegisters();
            ScreenPeripheralController.getController().resetDebugging();
        };

        if (KeyboardScreenController.getController() != null)
        {
            KeyboardScreenController.resetRegisters();
            KeyboardScreenController.getController().resetDebugging();
        };

        this.debugControlPanel.startDebugSession();
        if ( this.debugControlPanel.execution != null ) {
            this.debugControlPanel.execution.interrupt();
            this.debugControlPanel.execution = null;
        }
        this.debugControlPanel.execution = new Execution(this.debugControlPanel.PC, this.debugControlPanel);
        this.debugControlPanel.keyboardPeripheralThread.setExecutionThread(this.debugControlPanel.execution);
        this.debugControlPanel.execution.prepareForStart();

        Utilities.applyTooltipToAllControlBits(this);

        MainScreenController.getInstance().appendOutputText("DEBUG session is initiated.");
    }

    public void finishDebugSession( boolean isFinished )
    {
        StepBtn.setDisable( isFinished );
        ContinueBtn.setDisable( isFinished );
        this.debugControlPanel.isActiveSession = false;
        Utilities.clearTooltipFromAllControlBits(this);

        this.debugControlPanel.execution.stopExecution();
    }

    public static void SwapMenu(HBox hbox, int Type) {
        // 0 - Main Menu
        // other - Debug Menu

        if(Type == 0) {

            hbox.getChildren().clear();
            hbox.getChildren().addAll(MainScreenController.NewFileBtn,
                    MainScreenController.OpenFileBtn,
                    MainScreenController.SaveAsBtn,
                    MainScreenController.SaveBtn,
                    MainScreenController.AssembleBtn,
                    MainScreenController.DebugBtn,
                    MainScreenController.HelpBtn);

        }else {
            hbox.getChildren().clear();
            hbox.getChildren().addAll(
                    MainScreenController.getInstance().DebugSection.StepBtn,
                    MainScreenController.getInstance().DebugSection.ContinueBtn,
                    MainScreenController.getInstance().DebugSection.ResetBtn,
                    MainScreenController.getInstance().DebugSection.StopBtn,
                    MainScreenController.HelpBtn);
        }
    }

}
