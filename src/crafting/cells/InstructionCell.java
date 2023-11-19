package crafting.cells;

import crafting.LineCell;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class InstructionCell extends LineCell {

    private String instruction;
    private String regex;

    public InstructionCell( String regex )
    {
        this.regex = regex;
    }

    public void setInstruction( String instruction )
    {
        this.instruction = instruction;
    }

    @Override
    public boolean test(String text) {
        return text.matches(regex);
    }

    @Override
    public AttributeSet _getAttributeSet() {
        return Styles.attrGreen;
    }
}
