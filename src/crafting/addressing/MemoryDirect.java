package crafting.addressing;

import utils.Regex;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class MemoryDirect extends Addressing {

    public MemoryDirect() {
        super(Regex.REGEX_ADDRESSING_MEMORY_DIRECT);
    }

    @Override
    public String getAddressingDisplayName() {
        return "memory direct";
    }

    public boolean isLabel()
    {
        return addressing.matches( Regex.REGEX_LABEL_NAME );
    }

    public boolean isHexadecimalNumber()
    {
        return !isLabel();
    }

    @Override
    public AttributeSet getAttributeSet() {
        return Styles.attrBlack;
    }
}
