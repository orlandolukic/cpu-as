package debug.registers;

import debug.DebugInfo;
import javafx.application.Platform;
import javafx.scene.control.Label;
import utils.Utilities;

import java.util.concurrent.Semaphore;

public class Register {

    public static final int LABEL_BINARY = 0,
                            LABEL_HEXADECIMAL = 1,
                            LABEL_DECIMAL = 2;

    protected int size;
    protected String name;
    protected short value;
    protected Label[] labels;

    public Register( String name, int size )
    {
        this.name = name;
        this.size = size;
        this.value = 0;
    }

    public int getSize()
    {
        return size;
    }

    public String getName()
    {
        return name;
    }

    public int getValue()
    {
        return value & 0xFFFF;
    }

    public void setValue( Register r )
    {
        setValue( r.getValue() );
    }

    public void setValue( int value )
    {
        this.value = (short) (value & 0xFFFF);
        updateLabels();
    }

    public int getAppendSizeForBinary()
    {
        return size;
    }

    public int getAppendSizeForHexadecimal()
    {
        return size / 4;
    }

    public boolean getBit( char bit ) {
        return (value & (1 << bit)) != 0;
    }

    public void setBit( char bit, boolean value )
    {
        if ( bit >= 16 || bit < 0 )
            return;

        if ( value )
            this.value |= 1 << bit;
        else
            this.value &= ~(1 << bit);

        updateLabels();
    }

    public void reset()
    {
        value = 0;
        updateLabels();
    }

    public void setLabels(Label[] labels )
    {
        this.labels = labels;
    }

    public Label getLabel( int type )
    {
        if ( labels == null )
            return null;

        if ( type == LABEL_BINARY )
            return labels[0];
        else if ( type == LABEL_HEXADECIMAL )
            return labels[1];
        else if ( type == LABEL_DECIMAL )
            return labels[2];
        else
            return null;
    }

    public int getByte( int byteNo )
    {
        return (value & ( 0xFF << 8*(byteNo-1) )) >> 8*(byteNo-1) & 0xFF;
    }

    public int getInverted()
    {
        int val = ~value;
        val &= 0xFFFF;
        return val;
    }

    public void increment()
    {
        value = (short) (value + 1);
        value = (short) (value & 0xFFFF);
        updateLabels();
    }

    public void decrement()
    {
        int v = value & 0xFFFF;
        value = (short) (v - 1);
        updateLabels();
    }

    public boolean canNumberBeWrittenByThisRegister( int number ) {
        int max = (int) (Math.pow(2, size) - 1);
        return number <= max;
    }

    public void initialize() {
        if ( !DebugInfo.getInstance().isRegisterInitialized( name ) )
            return;
        int value = DebugInfo.getInstance().getRegisterStartValue( name );
        setValue( value );
    }

    protected void updateLabels()
    {
        if ( labels != null )
        {
            Utilities.runLater(() -> {
                if (labels[LABEL_BINARY] != null)
                    labels[LABEL_BINARY].setText(Utilities.getBinaryFormat(this));

                if (labels[LABEL_DECIMAL] != null)
                    labels[LABEL_DECIMAL].setText(Utilities.getDecimalFormat(this));

                if (labels[LABEL_HEXADECIMAL] != null)
                    labels[LABEL_HEXADECIMAL].setText(Utilities.getHexadecimalFormat(this));
            });
        }
    }
}
