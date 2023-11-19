package crafting.addressing;

import utils.Regex;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class Immediate extends Addressing {

    public Immediate() {
        super(Regex.REGEX_ADDRESSING_IMMEDIATE);
    }

    @Override
    public String getAddressingDisplayName() {
        return "immediate";
    }

    @Override
    public AttributeSet getAttributeSet() {
        return Styles.attrImmediate;
    }
}
