package crafting.addressing;

import utils.Regex;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class RegisterIndirectPreincr extends Addressing {

    public RegisterIndirectPreincr() {
        super(Regex.REGEX_ADDRESSING_REGISTER_DIRECT_PREINCR);
    }

    @Override
    public String getAddressingDisplayName() {
        return "register indirect pre-increment";
    }

    @Override
    public AttributeSet getAttributeSet() {
        return Styles.attrAddressingRegister;
    }
}
