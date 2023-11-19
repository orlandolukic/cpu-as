package debug.registers;

import debug.memory.Memory;
import javafx.scene.control.Label;
import utils.Utilities;

import java.util.concurrent.Semaphore;

public class PeripheralRegister extends Register {

    private Label[] bitLabels;
    private int address;
    private int resetValue;
    private Semaphore dataSem;
    private Semaphore waitForReadAccessSem;

    public PeripheralRegister( String name, int size, int address, int resetValue ) {
        super(name, size);
        this.address = address;
        this.resetValue = resetValue;
        Memory.get().linkToRegister( address, this );
        this.value = (short) resetValue;
        dataSem = new Semaphore(1);
    }

    public void setBitLabels( Label[] labels )
    {
        this.updateLabels();
        this.bitLabels = labels;
    }

    public void initLabels()
    {
        setValue( Memory.get().get2BytesFromAddress(address) );
        printLabelText();
    }

    public void setWaitForReadAccessSem( Semaphore s ) {
        waitForReadAccessSem = s;
    }

    @Override
    public int getValue() {
        int v;
        dataSem.acquireUninterruptibly();
        v = super.getValue();
        dataSem.release();
        if ( waitForReadAccessSem != null ) {
            waitForReadAccessSem.release();
            waitForReadAccessSem = null;
        };
        return v;
    }

    @Override
    public void setValue(int value) {
        dataSem.acquireUninterruptibly();
        super.setValue(value);
        dataSem.release();
    }

    @Override
    public void setBit(char bit, boolean value) {
        dataSem.acquireUninterruptibly();
        super.setBit(bit, value);
        dataSem.release();
    }

    @Override
    public void reset() {
        dataSem.acquireUninterruptibly();
        value = (short) resetValue;
        dataSem.release();
        updateLabels();
    }

    public void resetLabels() {
        bitLabels = null;
    }

    @Override
    public void updateLabels() {
        super.updateLabels();
        printLabelText();
    }

    private void printLabelText()
    {
        Utilities.runLater(() -> {
            String s;
            if ( bitLabels != null )
            {
                for (int i=size-1; i>=0; i--)
                {
                    s = getBit((char) i) ? "1" : "0";
                    if ( bitLabels[i] != null )
                        bitLabels[i].setText(s);
                };
            };
        });
    }
}
