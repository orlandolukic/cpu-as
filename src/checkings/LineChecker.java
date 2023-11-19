package checkings;

import crafting.LineCell;
import crafting.cells.EmptyCell;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LineChecker {

    public static int REPT = 0;

    private int iterator;

    private LineCell emptyCell;
    private LineCell[] cells;

    public LineChecker( LineCell[] cells )
    {
        this.cells = cells;
        this.iterator = 0;
        this.emptyCell = new EmptyCell();
        for (int i=0; i<cells.length; i++)
            cells[i].setLineChekcher(this);
    }

    public void resetIterator()
    {
        iterator = 0;
        for (int i=0; i<cells.length; i++)
            cells[i].start(true);
    }

    public LineCell getCurrent()
    {
        if ( iterator == cells.length )
            return emptyCell;

        return cells[iterator];
    }

    public LineCell getFromCurrent( int offset )
    {
        if ( (iterator + offset) < 0 || (iterator + offset) >= cells.length )
            return null;

        return cells[iterator+offset];
    }

    public boolean isFinished()
    {
        return iterator == cells.length || !cells[iterator].isSticky();
    }

    public void moveToNext()
    {
        moveToNext(false);
    }

    public void moveToNext( boolean force )
    {
        if ( iterator == cells.length )
            return;

        if ( !force ) {
            if ( cells[iterator].isReadyToChangeLineCell() ) {
                iterator++;
                if ( iterator < cells.length )
                    cells[iterator].start( cells[iterator-1].getIsValid() );
            } else
            {
                cells[iterator].moveToNext();
            };
        } else
            iterator++;
    }

    public LineCell getFirstCell(String text)
    {
        int i = iterator;
        while( i < cells.length )
        {
            if ( cells[i].isMandatory() ) {
                iterator = i;
                return cells[i];
            } else
            {
                if ( cells[i].test(text) )
                {
                    iterator = i;
                    return cells[i];
                };
            };

            i++;
        };
        return emptyCell;
    }

    public boolean firstTourEnd()
    {
        return cells[iterator].isMandatory();
    }

    public void each(BiConsumer<Integer, LineCell> consumer) {
        for (int i=0; i<cells.length; i++)
            consumer.accept( i, cells[i] );
    }

}
