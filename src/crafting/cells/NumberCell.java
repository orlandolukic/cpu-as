package crafting.cells;

import crafting.LineCell;
import utils.Regex;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class NumberCell extends LineCell {

    public static final int BINARY = 1 << 0;
    public static final int HEXADECIMAL = 1 << 1;
    public static final int DECIMAL = 1 << 2;
    public static final int ALL = BINARY | HEXADECIMAL | DECIMAL;

    private int allowedNumberMask;

    public NumberCell( int allowedNumberMask )
    {
        this(false);
        this.allowedNumberMask = allowedNumberMask;
    }

    public NumberCell(boolean isSticky)
    {
        this.isSticky = isSticky;
    }

    public boolean isDecimal()
    {
        return content != null ? content.matches(Regex.REGEX_NUMBER_DECIMAL) : false;
    }

    public boolean isHexadecimal()
    {
        return content != null ? content.matches(Regex.REGEX_NUMBER_HEXADECIMAL) : false;
    }

    public boolean isBinary()
    {
        return content != null ? content.matches(Regex.REGEX_NUMBER_BINARY) : false;
    }

    @Override
    public boolean test(String text) {

        boolean value = false;

        if ( (allowedNumberMask & BINARY) > 0 )
            value = text.matches( Regex.REGEX_NUMBER_BINARY );

        if ( value )
            return value;

        if ( (allowedNumberMask & HEXADECIMAL) > 0 )
            value = text.matches( Regex.REGEX_NUMBER_HEXADECIMAL );

        if ( value )
            return value;

        if ( (allowedNumberMask & DECIMAL) > 0 )
            value = text.matches( Regex.REGEX_NUMBER_DECIMAL );

        return value;
    }

    @Override
    public AttributeSet _getAttributeSet() {
        return Styles.attrImmediate;
    }

    public int getContentAsInteger() {
        int radix = content.matches(Regex.REGEX_NUMBER_DECIMAL) ? 10 : content.matches(Regex.REGEX_NUMBER_HEXADECIMAL) ? 16 : 2;
        String s = this.content;
        s = s.replaceAll("^0[bBxX][0]*", "");
        return Integer.parseInt(s, radix);
    }
}
