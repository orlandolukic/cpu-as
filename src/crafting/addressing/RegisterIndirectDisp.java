package crafting.addressing;

import utils.Regex;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class RegisterIndirectDisp extends Addressing {

    public RegisterIndirectDisp() {
        super(Regex.REGEX_ADDRESSING_REGISTER_INDIRECT_DISP);
    }

    @Override
    public String getAddressingDisplayName() {
        return "register indirect with displacement";
    }

    @Override
    public AttributeSet getAttributeSet() {
        return Styles.attrAddressingRegister;
    }
}
