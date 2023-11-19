package debug.registers;

import javafx.scene.control.Label;
import utils.Utilities;

public class PSW extends Register {

    public static final char I_BIT = 2;
    public static final char C_BIT = 6;
    public static final char Z_BIT = 7;

    private Label[] labels;

    public PSW()
    {
        super("PSW", 8);
    }

    @Override
    public void setBit(char bit, boolean value) {
        if ( bit == I_BIT || bit == C_BIT || bit == Z_BIT )
            super.setBit(bit, value);
    }

    public void setInterruptFlag()
    {
        setBit(I_BIT, true);
    }

    public void resetInterruptFlag()
    {
        setBit(I_BIT, false);
    }

    public void setCarryBit()
    {
        setBit(C_BIT, true);
    }

    public void resetCarryBit()
    {
        setBit(C_BIT, !true);
    }

    public void setZeroBit()
    {
        setBit(Z_BIT, true);
    }

    public void resetZeroBit()
    {
        setBit(Z_BIT, false);
    }

    @Override
    public void updateLabels() {
        super.updateLabels();
        Utilities.runLater(() -> {
            if ( labels != null ) {
                labels[0].setText(!getBit(Z_BIT) ? "0" : "1");
                labels[1].setText(!getBit(C_BIT) ? "0" : "1");
                labels[2].setText(!getBit(I_BIT) ? "0" : "1");
            }
        });
    }

    public void setAdditionalLabels(Label[] labels ) {
        this.labels = null;
        this.labels = labels;
    }
}
