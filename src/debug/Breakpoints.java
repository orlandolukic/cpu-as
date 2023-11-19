package debug;

import java.util.LinkedList;
import java.util.function.Consumer;

public class Breakpoints {

    private static Breakpoints instance;
    public static void restart() { instance = null; }
    public static Breakpoints get()
    {
        if ( instance == null )
            instance = new Breakpoints();
        return instance;
    }

    private LinkedList<Integer> list;

    public Breakpoints()
    {
        list = new LinkedList<>();
    }

    public boolean isBraekpointLine( int line )
    {
        return list.contains( line );
    }

    public void addBreakpointLine( int line )
    {
        if ( isBraekpointLine(line) )
            return;

        list.add(line);
    }

    public boolean toggleBreakpointLine( int line )
    {
        if ( isBraekpointLine(line) )
            list.remove( Integer.valueOf(line) );
        else {
            list.add(line);
            return true;
        };

        return false;
    }

    public void removeBreakpointLine( int line )
    {
        if ( !isBraekpointLine(line) )
            return;

        list.remove( Integer.valueOf(line) );
    }

    public void forEach(Consumer<Integer> consumer) {
        list.forEach(consumer);
    }
}
