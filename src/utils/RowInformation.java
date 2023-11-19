package utils;

public class RowInformation {

    public int offset;
    public int countR;
    public int line;

    public RowInformation( int offset, int countR )
    {
        this.offset = offset;
        this.countR = countR;
    }

    public int getRealOffset()
    {
        return offset - countR;
    }

    public int getStringOffset()
    {
        return offset;
    }
}
