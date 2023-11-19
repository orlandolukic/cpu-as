package debug.registers;

import javafx.scene.control.Label;

public class IMR extends Register {

    private Label[] labels;

    public IMR() {
        super("IMR", 4);
    }

    public void setAdditionalLabels( Label[] labels ) {
        this.labels = labels;
    }

    @Override
    public void reset() {
        value = 0xF;
    }

    @Override
    public void updateLabels() {
        super.updateLabels();
        if ( labels != null ) {
            labels[0].setText(!getBit((char) 0) ? "0" : "1");
            labels[1].setText(!getBit((char) 1) ? "0" : "1");
            labels[2].setText(!getBit((char) 2) ? "0" : "1");
            labels[3].setText(!getBit((char) 3) ? "0" : "1");
        };
    }
}
