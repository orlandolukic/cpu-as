package crafting.addressing;

import utils.Regex;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class RegisterIndirectPostdecr extends Addressing {

    public RegisterIndirectPostdecr() {
        super(Regex.REGEX_ADDRESSING_REGISTER_DIRECT_POSTDECR);
    }

    @Override
    public String getAddressingDisplayName() {
        return "register indirect post-decrement";
    }

    @Override
    public AttributeSet getAttributeSet() {
        return Styles.attrAddressingRegister;
    }
}
