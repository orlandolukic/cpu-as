package crafting.addressing;

import utils.Regex;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class MemoryIndirect extends Addressing {

    public MemoryIndirect() {
        super(Regex.REGEX_ADDRESSING_MEMORY_INDIRECT);
    }

    @Override
    public String getAddressingDisplayName() {
        return "memory indirect";
    }

    @Override
    public AttributeSet getAttributeSet() {
        return Styles.attrAddressingRegister;
    }
}
