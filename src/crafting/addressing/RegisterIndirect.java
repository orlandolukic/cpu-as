package crafting.addressing;

import utils.Regex;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class RegisterIndirect extends Addressing {

    public RegisterIndirect() {
        super(Regex.REGEX_ADDRESSING_REGISTER_INDIRECT);
    }

    @Override
    public String getAddressingDisplayName() {
        return "register indirect";
    }

    @Override
    public AttributeSet getAttributeSet() {
        return Styles.attrAddressingRegister;
    }
}
