package crafting.addressing;

import utils.Regex;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class RegisterDirect extends Addressing {

    private int number;

    public RegisterDirect() {
        super(Regex.REGEX_ADDRESSING_REGISTER_DIRECT);
    }

    @Override
    public String getAddressingDisplayName() {
        return "register direct";
    }

    @Override
    public AttributeSet getAttributeSet() {
        return Styles.attrAddressingRegister;
    }

    public int getRegisterNumber() {
        return number;
    }

    public void setRegisterNumber( int number ) {
        this.number = number;
    }
}
