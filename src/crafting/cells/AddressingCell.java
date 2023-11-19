package crafting.cells;

import crafting.LineCell;
import crafting.addressing.*;
import utils.Regex;

import javax.swing.text.AttributeSet;

public class AddressingCell extends LineCell {

    public static final int RegisterDirect = 1 << 0;
    public static final int RegisterIndirect = 1 << 1;
    public static final int RegisterIndirectDisp = 1 << 2;
    public static final int RegisterIndirectPostdecr = 1 << 3;
    public static final int RegisterIndirectPreincr = 1 << 4;
    public static final int MemoryDirect = 1 << 5;
    public static final int MemoryIndirect = 1 << 6;
    public static final int Immediate = 1 << 7;

    public static final int ALLOWED_REGISTER_ADDRESSINGS =
                    RegisterDirect |
                    RegisterIndirect |
                    RegisterIndirectDisp |
                    RegisterIndirectPostdecr |
                    RegisterIndirectPreincr;

    public static final int ALLOWED_MEMORY_ADDRESSINGS = MemoryDirect | MemoryIndirect;

    public static final int ALLOWED_ALL_ADDRESSINGS = ALLOWED_REGISTER_ADDRESSINGS | ALLOWED_MEMORY_ADDRESSINGS | Immediate;

    private Addressing addressing;
    private int allowedAddressings;

    public AddressingCell( int allowedAddressings )
    {
        this.allowedAddressings = allowedAddressings;
        this.addressing = null;
    }



    @Override
    public boolean test(String text) {
        boolean value = false;
        Addressing a = null;
        for (int i=0; i < 8; i++)
        {
            if ( value )
                break;

            if ( ( ( 1 << i ) & allowedAddressings ) == 0 )
                continue;

            switch( 1 << i )
            {
                case RegisterDirect:
                     a = new RegisterDirect();
                     break;

                case RegisterIndirect:
                    a = new RegisterIndirect();
                    break;

                case RegisterIndirectDisp:
                    a = new RegisterIndirectDisp();
                    break;

                case RegisterIndirectPostdecr:
                    a = new RegisterIndirectPostdecr();
                    break;

                case RegisterIndirectPreincr:
                    a = new RegisterIndirectPreincr();
                    break;

                case MemoryDirect:
                    a = new MemoryDirect();
                    break;

                case MemoryIndirect:
                    a = new MemoryDirect();
                    break;

                case Immediate:
                    a = new Immediate();
                    break;

                default:
                    a = null;
            };

            String instruction = getLineChecker().getFromCurrent(-1).getContent();
            if ( !a.isAllowedForInstruction(instruction) ) {
                return false;
            };

            value = a.isAddressingFine(text);
            addressing = a;
        };
        return value;
    }

    @Override
    public AttributeSet _getAttributeSet() {
        return addressing.getAttributeSet();
    }

    public static Addressing createAddressingByText( String text )
    {
        if ( text.matches(Regex.REGEX_ADDRESSING_REGISTER_DIRECT) ) {
            crafting.addressing.RegisterDirect rd = new RegisterDirect();
            try {
                rd.setRegisterNumber(Regex.getRegisterNumber(text));
            } catch (NumberFormatException nfe) {}
            return rd;
        } else if ( text.matches(Regex.REGEX_ADDRESSING_REGISTER_INDIRECT) )
            return new RegisterIndirect();
        else if ( text.matches(Regex.REGEX_ADDRESSING_REGISTER_INDIRECT_DISP) )
            return new RegisterIndirectDisp();
        else if ( text.matches(Regex.REGEX_ADDRESSING_REGISTER_DIRECT_POSTDECR) )
            return new RegisterIndirectPostdecr();
        else if ( text.matches(Regex.REGEX_ADDRESSING_REGISTER_DIRECT_PREINCR) )
            return new RegisterIndirectPreincr();
        else if ( text.matches(Regex.REGEX_ADDRESSING_MEMORY_DIRECT) )
            return new MemoryDirect();
        else if ( text.matches(Regex.REGEX_ADDRESSING_MEMORY_INDIRECT) )
            return new MemoryIndirect();

        return null;
    }
}
