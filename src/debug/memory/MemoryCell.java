package debug.memory;

public class MemoryCell {

    private int index;
    private byte[] arr;
    private MemoryCell[] cells;

    public MemoryCell( int index )
    {
        this.index = index;
        if ( index == 3 )
            arr = new byte[16];
        else {
            cells = new MemoryCell[16];
        };
    }

    public byte getByteFromAddress( int[] address )
    {
        if ( address[index] < 0 || address[index] >= 16 )
            return (byte)0;

        if ( this.index == 3 )
        {
            return arr[address[index]];
        } else
        {
            if ( cells[address[index]] == null )
                return (byte) 0;
            else
                return cells[address[index]].getByteFromAddress(address);
        }
    }

    public void writeByteAtAddress(int[] address, byte value )
    {
        if ( this.index == 3 )
        {
            arr[address[index]] = value;
        } else
        {
            if ( cells[address[index]] == null )
            {
                cells[address[index]] = new MemoryCell(index+1);
            };
            cells[address[index]].writeByteAtAddress(address, value);
        }
    }
}
