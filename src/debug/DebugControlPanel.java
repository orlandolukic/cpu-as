package debug;

import controller.MainScreenController;
import threading.KeyboardPeripheral;
import threading.ScreenPeripheral;
import debug.registers.IMR;
import debug.registers.PSW;
import debug.registers.Register;
import debug.registers.RegisterGroup;
import gui.DebugSection;
import threading.TimerPeripheral;
import utils.Utilities;

import java.util.HashMap;
import java.util.LinkedList;

public class DebugControlPanel {

    public int startLine;
    public RegisterGroup group;
    public Register PC;
    public Register IVTP;
    public debug.registers.SP SP;
    public Register AX;
    public debug.registers.IMR IMR;
    public debug.registers.PSW PSW;
    public HashMap<Integer, Integer> instructionWithLines;
    public Execution execution;
    public boolean isActiveSession;
    public DebugSection debugSection;
    public Stack stack;

    public ScreenPeripheral screenPeripheralThread;
    public KeyboardPeripheral keyboardPeripheralThread;
    public TimerPeripheral timerPeripheralThread;

    public DebugControlPanel(DebugSection ds)
    {
        debugSection = ds;

        PC = new Register( "PC", 16 );
        IVTP = new Register( "IVTP", 16 );
        SP = new debug.registers.SP();
        IMR = new IMR();
        PSW = new PSW();
        AX = new Register( "AX", 16 );

        LinkedList<Register> l = new LinkedList<>();
        l.add(PC);
        l.add(IVTP);
        l.add(SP);
        l.add(IMR);
        l.add(PSW);
        l.add(AX);
        for (int i=0; i<32; i++)
            l.add(new Register( "r" + i, 16 ));
        group = new RegisterGroup(l.toArray( new Register[l.size()] ));
        stack = new Stack(SP);

        isActiveSession = false;
    }

    public void terminateDebugSession( String terminateMessage ) {
        Utilities.runLater(() -> {
            debugSection.ContinueBtn.setDisable(true);
            debugSection.StepBtn.setDisable(true);
            debugSection.ResetBtn.setDisable( false );
            isActiveSession = false;
            Utilities.clearTooltipFromAllControlBits(debugSection);
            MainScreenController.CurrentTab.LinePainter.resetPaintedBackground();
            if ( terminateMessage != null )
                MainScreenController.getInstance().appendNewLineWithText("DEBUG session is terminated. Reason: " + terminateMessage);
            else
                MainScreenController.getInstance().appendNewLineWithText("DEBUG session is terminated.");
        });
    }

    public void stopDebugSession() {
        screenPeripheralThread.interrupt();
    }

    public void startDebugSession() {

        // Reset stack
        stack.reset();

        if ( screenPeripheralThread != null ) {
            screenPeripheralThread.interrupt();
            screenPeripheralThread = null;
        };
        screenPeripheralThread = new ScreenPeripheral();
        screenPeripheralThread.start();

        if ( keyboardPeripheralThread != null ) {
            keyboardPeripheralThread.interrupt();
            keyboardPeripheralThread = null;
        };
        keyboardPeripheralThread = new KeyboardPeripheral();
        keyboardPeripheralThread.start();

        if ( timerPeripheralThread != null ) {
            timerPeripheralThread.interrupt();
            timerPeripheralThread = null;
        };
        timerPeripheralThread = new TimerPeripheral(this);
        timerPeripheralThread.start();
    }

    public void setInstructionWithLines( HashMap<Integer, Integer> map )
    {
        this.instructionWithLines = map;
    }
}
