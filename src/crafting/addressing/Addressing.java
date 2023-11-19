package crafting.addressing;

import javax.swing.text.AttributeSet;

public abstract class Addressing {

    protected String addressing;
    protected String regex;

    public Addressing( String regex )
    {
        this.regex = regex;
    }

    /**
     * Gets name of the addressing.
     * @return name
     */
    public abstract String getAddressingDisplayName();

    /**
     * Gets attribute set for this addressing type.
     * @return
     */
    public abstract AttributeSet getAttributeSet();

    public void setAddressingString( String addr )
    {
        this.addressing = addr;
    }

    public boolean isAddressingFine( String text )
    {
        return text.matches(regex);
    }

    public boolean isAllowedForInstruction( String instruction )
    {
        switch( instruction.toLowerCase() )
        {
            case "ld":
            case "st":
            case "add":
            case "and":
            case "or":
            case "sub":
            case "xor":
            case "mul":
            case "cmp":
                return true;

            case "call":
            case "jmp":
            case "jgt":
            case "jge":
            case "jlt":
            case "jle":
            case "jeq":
            case "jneq":
                if ( this instanceof MemoryDirect )
                    return ((MemoryDirect) this).isLabel();
                else
                    return false;

            case "push":
            case "pop":
            case "iret":
            case "ret":
            case "halt":
            case "inc":
            case "dec":
            case "cli":
            case "sti":
            case "ldsp":
            case "stsp":
            case "ldimr":
            case "stimr":
            case "not":
                return false;

            default:
                return false;
        }
    }
}
