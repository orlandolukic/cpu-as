package debug.memory;

import assembler.EntryPoint;
import debug.registers.Register;

import java.util.HashMap;
import java.util.LinkedList;

public class Memory {

    private static Memory instance;

    public static Memory get() {
        if ( instance == null )
            instance = new Memory();
        return instance;
    }

    public static void load()
    {
        get();
        EntryPoint.getSymbolTable().forEachSection((s) -> {
            s.forEachByte((b, i) -> {
                instance.writeByteAtAddress( s.getSectionAddress() + i, b );
            });
        });
    }

    private MemoryCell mem;
    private HashMap<Integer, Register> linkToRegister;

    private Memory()
    {
        mem = new MemoryCell(0);
        linkToRegister = new HashMap<>();
    }

    private int[] getAddressAsArray( int address )
    {
        String h = Integer.toHexString(address);
        int[] addr = new int[4];
        for (int i=0; i<4; i++)
            addr[i] = 0;
        int m = 3;
        for (int i=h.length()-1; i>=0; i--)
        {
            addr[m] = Integer.parseInt("" + h.charAt(i), 16 );
            m--;
        };
        while ( m >= 0 )
            addr[m--] = 0;
        return addr;
    }

    private int[][] getAddressesForTwoArrays( int address )
    {
        int[][] a = new int[2][4];

        a[0] = getAddressAsArray(address);
        address++;
        a[1] = getAddressAsArray(address);
        return a;
    }

    public int getByteFromAddress( int address )
    {
        if ( linkToRegister.containsKey(address) )
        {
            return linkToRegister.get(address).getValue();
        } else {
            String h = Integer.toHexString(address);
            int[] addr = getAddressAsArray(address);
            return mem.getByteFromAddress(addr) & 0xFF;
        }
    }

    public void writeByteAtAddress( int address, int value )
    {
        String h = Integer.toHexString(address);
        int[] addr = getAddressAsArray(address);

        if ( linkToRegister.containsKey(address) )
        {
            Register r = linkToRegister.get(address);
            int v;
            if ( address % 2 == 1 )
            {
                v = (value & 0xFF) << 8;
                r.setValue( (r.getValue() & ~0xFF00) | v );
            } else {
                v = value & 0xFF;
                r.setValue( (r.getValue() & ~0xFF) | v );
            };
        } else
            mem.writeByteAtAddress(addr, ((byte) (value & 0xFF) ));
    }

    public void write2BytesAtAddress( int address, Register r )
    {
        write2BytesAtAddress(address, r.getValue());
    }

    public void write2BytesAtAddress( int address, int value )
    {
        value = value & 0xFFFF;
        int lower = value & 0xFF;
        int higher = (value & 0xFF00) >> 8;

        if ( linkToRegister.containsKey(address) )
        {
            linkToRegister.get(address).setValue( higher << 8 | lower );
        } else {
            int[][] s = getAddressesForTwoArrays(address);
            mem.writeByteAtAddress(s[0], ((byte) (lower & 0xFF)));
            mem.writeByteAtAddress(s[1], ((byte) (higher & 0xFF)));
        };
    }

    public int get2BytesFromAddress( Register r )
    {
        return get2BytesFromAddress( r.getValue() );
    }

    public int get2BytesFromAddress( int address )
    {
        int ret = 0;
        if ( linkToRegister.containsKey(address) )
        {
            ret = linkToRegister.get(address).getValue();
        } else {
            int[][] s = getAddressesForTwoArrays(address);
            ret |= mem.getByteFromAddress(s[1]);
            ret = (ret << 8) & 0xFF00;
            ret |= mem.getByteFromAddress(s[0]) & 0xFF;
        };

        return ret ;
    }

    public void linkToRegister( int address, Register r )
    {
        linkToRegister.put(address, r);
    }

    public void clear()
    {
        mem = new MemoryCell(0);
        linkToRegister.forEach((i, reg) -> {
            reg.reset();
        });
    }

    public static void main(String[] varg)
    {
        Memory m = new Memory();
        m.writeByteAtAddress( 0xABCE, 0xfb);
        m.write2BytesAtAddress( 0xCFFF, 0xAABB );
        System.out.println( Integer.toHexString(m.getByteFromAddress(0xABCD)) );
        System.out.println( Integer.toHexString(m.getByteFromAddress(0xCFFF)) );
        System.out.println( Integer.toHexString(m.getByteFromAddress(0xD000)) );
        System.out.println( Integer.toHexString(m.get2BytesFromAddress(0xCFFF)) );
    }

}
