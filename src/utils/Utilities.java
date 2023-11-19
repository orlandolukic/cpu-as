package utils;

import controller.MainScreenController;
import debug.Breakpoints;
import debug.DebugControlPanel;
import debug.registers.PSW;
import debug.registers.Register;
import gui.AppDefaultStyledDocument;
import gui.DebugPopUpWindow;
import gui.DebugSection;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.concurrent.Semaphore;

public class Utilities {

    public static boolean isRegister( String text )
    {
        return text.matches("[rR](0|[1-9]|1[0-5])");
    }

    public static String getFileName( String name )
    {
        return name.replaceAll("\\.[a-zA-Z]+$", "");
    }

    //public static String getFileLocation()
    public static boolean isInstructionLineSelected( int lineNumber )
    {
        String line = getLine(MainScreenController.CurrentTab.TextPane.getText(), lineNumber);
        if ( line == null )
            return false;
        System.out.println(line);
        return line.matches(Regex.REGEX_INSTRUCTION_LINE);
    }

    public static int getRowStart()
    {
        try {
            JTextPane tp = MainScreenController.CurrentTab.TextPane;
            int caretPos = tp.getCaretPosition();
            int rowNum = (caretPos == 0) ? 1 : 0;
            for (int offset = caretPos; offset > 0; ) {
                offset = javax.swing.text.Utilities.getRowStart(tp, offset) - 1;
                rowNum++;
            }
            return rowNum;
        } catch(BadLocationException e) {}
        return 0;
    }

    public static String getLine( String text, int lineNumber )
    {
        StringBuilder str = new StringBuilder();
        int index = 0;
        int line = 1;
        boolean appendChars = line == lineNumber;
        while( true )
        {
            if ( appendChars )
                str.append(text.charAt(index));
            index++;
            if ( text.charAt(index) == '\n' ) {
                if ( appendChars )
                    break;
                if ( line+1 == lineNumber ) {
                    appendChars = true;
                    str.setLength(0);
                } else {
                    appendChars = false;
                };
                line++;
            };
        };

        if ( str.toString().matches("(.*)\\r$") )
            str.deleteCharAt( str.length() - 1 );
        String s = str.toString();
        return s.replaceAll("^[\\r\\n\\t]+", "");
    }

    public static RowInformation getRowStartIndex( String text, int lineNumber )
    {
        int index = 0;
        int line = 1;
        int indexRet = 0;
        RowInformation ri = new RowInformation(0,0);
        ri.line = lineNumber;
        char c;
        while( true )
        {
            if ( line == lineNumber )
                break;

            c = text.charAt(ri.offset);
            if ( c == '\n' ) {
                line++;
            };

            if ( c == '\r' )
                ri.countR++;
            ri.offset++;
        };

        return ri;
    }

    public static int getRowEndIndex( String text, RowInformation rowInfo )
    {
        int index = rowInfo.getStringOffset();
        int indexRet = rowInfo.getRealOffset();
        boolean letter = false;
        String str;
        while( true )
        {
            str = text.charAt(index) + "";
            if ( text.charAt(index) == '\n' )
            {
               break;
            } else if ( str.matches("^[a-zA-Z]$") )
            {
                letter = true;
            };

            if ( !str.equals("\r") )
                indexRet++;
            index++;
        }

        return indexRet;
    }

    public static String getHexadecimalFormat( Register r )
    {
        return getHexadecimalFormat( r.getAppendSizeForHexadecimal(), r.getValue() );
    }

    public static String getDecimalFormat( Register r )
    {
        return String.format("%d", r.getValue() & 0xFFFF);
    }

    public static String getBinaryFormat( Register r )
    {
        String s = String.format("%" + r.getAppendSizeForBinary() + "s",
                Integer.toBinaryString(r.getValue())).replace(' ', '0');

        String b = "";
        String t;
        for (int i=0; i<r.getAppendSizeForHexadecimal(); i++) {
            t = s.substring(i*4, i*4 + 4);
            if ( i > 0 )
                b += " ";
            b += t;
        }
        return b;
    }

    public static String getHexadecimalFormat( int appendSize, int value )
    {
        return "0x" + String.format("%0" + appendSize + "x", value & 0xFFFF).toUpperCase();
    }

    public static void runLater( Runnable r ) {
        if ( Platform.isFxApplicationThread() )
            r.run();
        else
            Platform.runLater(r);
    }

    public static void runAndWait( Runnable r ) {
        if ( Platform.isFxApplicationThread() )
            r.run();
        else {
            Semaphore s = new Semaphore(0);
            Platform.runLater(() -> {
                r.run();
                s.release();
            });
            try {
                s.acquire();
            } catch( InterruptedException e ) {}
        }
    }

    public static void applyChangeOnClick(DebugControlPanel cp, Label l, Register r, int bit) {
        l.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if ( cp.isActiveSession ) {
                    char c = (char) bit;
                    boolean val = r.getBit(c);
                    r.setBit(c, !val);
                };
            }
        });
        Tooltip t = new Tooltip("Click me to toggle this bit");
        t.setShowDelay(new Duration(0));
        l.setTooltip(t);
    }

    public static void applyChangeForPRINTROnClick( DebugControlPanel cp, Label l, int entry ) {
        l.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if ( cp.isActiveSession && cp.execution != null ) {
                    boolean b = cp.execution.toggleInterrupt(entry);
                    l.setText( b ? "1" : "0" );
                };
            }
        });
        cp.execution.setPRINTRLabel(entry, l);
        Tooltip t = new Tooltip("Click me to toggle interrupt on entry " + entry);
        t.setShowDelay(new Duration(0));
        l.setTooltip(t);
    }

    public static void clearTooltipFromAllControlBits(DebugSection ds ) {
        ds.PSW_I_bit.setTooltip(null);
        ds.PSW_C_bit.setTooltip(null);
        ds.PSW_Z_bit.setTooltip(null);
        ds.IMR3_bit.setTooltip(null);
        ds.IMR2_bit.setTooltip(null);
        ds.IMR1_bit.setTooltip(null);
        ds.IMR0_bit.setTooltip(null);
        ds.PRINTR2_bit.setTooltip(null);
        ds.PRINTR3_bit.setTooltip(null);

        ds.PRINTR2_bit.setOnMouseClicked(null);
        ds.PRINTR3_bit.setOnMouseClicked(null);
        ds.PSW_I_bit.setOnMouseClicked(null);
        ds.PSW_C_bit.setOnMouseClicked(null);
        ds.PSW_Z_bit.setOnMouseClicked(null);
        ds.IMR3_bit.setOnMouseClicked(null);
        ds.IMR2_bit.setOnMouseClicked(null);
        ds.IMR1_bit.setOnMouseClicked(null);
        ds.IMR0_bit.setOnMouseClicked(null);

    }

    public static void applyTooltipToAllControlBits( DebugSection ds ) {
        applyChangeOnClick( ds.debugControlPanel, ds.PSW_I_bit, ds.debugControlPanel.PSW, PSW.I_BIT );
        applyChangeOnClick( ds.debugControlPanel, ds.PSW_C_bit, ds.debugControlPanel.PSW, PSW.C_BIT );
        applyChangeOnClick( ds.debugControlPanel, ds.PSW_Z_bit, ds.debugControlPanel.PSW, PSW.Z_BIT );
        applyChangeOnClick( ds.debugControlPanel, ds.IMR3_bit, ds.debugControlPanel.IMR, 3 );
        applyChangeOnClick( ds.debugControlPanel, ds.IMR2_bit, ds.debugControlPanel.IMR, 2 );
        applyChangeOnClick( ds.debugControlPanel, ds.IMR1_bit, ds.debugControlPanel.IMR, 1 );
        applyChangeOnClick( ds.debugControlPanel, ds.IMR0_bit, ds.debugControlPanel.IMR, 0 );
        applyChangeForPRINTROnClick( ds.debugControlPanel, ds.PRINTR2_bit, 2 );
        applyChangeForPRINTROnClick( ds.debugControlPanel, ds.PRINTR3_bit, 3 );
    }

    public static void paintLine( int line ) {
        runLater(() -> {
            RowInformation ri = Utilities.getRowStartIndex( MainScreenController.CurrentTab.TextPane.getText(), line );
            MainScreenController.CurrentTab.TextPane.setCaretPosition( ri.getRealOffset() );
            MainScreenController.CurrentTab.LinePainter.paintBackground( ri );
        });
    }

    public static void paintLine( int line, boolean isCurrentLine ) {
        runLater(() -> {
            RowInformation ri = Utilities.getRowStartIndex( MainScreenController.CurrentTab.TextPane.getText(), line );
            MainScreenController.CurrentTab.TextPane.setCaretPosition( ri.getRealOffset() );
            MainScreenController.CurrentTab.LinePainter.paintBackground( ri, isCurrentLine );
        });
    }

    public static void paintLineAsCurrentLineWithBreakpoint( int line ) {
        runLater(() -> {
            RowInformation ri = Utilities.getRowStartIndex( MainScreenController.CurrentTab.TextPane.getText(), line );
            MainScreenController.CurrentTab.TextPane.setCaretPosition( ri.getRealOffset() );
            MainScreenController.CurrentTab.LinePainter.paintBreakpointLine( ri, false );
        });
    }

    public static void clearLinePainting() {
        runLater(() -> {
            MainScreenController.CurrentTab.LinePainter.resetPaintedBackground();
            Breakpoints.get().forEach((i) -> {
                Utilities.paintLine( i );
            });
        });
    }

    public static Register getRegisterByName( String name ) {
        return MainScreenController.getInstance().DebugSection.debugControlPanel.group.getRegister(name);
    }

}
