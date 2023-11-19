package crafting.cells;

import crafting.LineCell;

import javax.swing.text.AttributeSet;

public class RepetitionCell extends LineCell {

    public static final int INFINITE = -1;

    private boolean toCheck;
    private boolean isComma;
    private LineCell[] innerCells;
    private int repetition;
    private int repetitionCount;
    private int currentIteration;

    public RepetitionCell(LineCell[] innerCells, int repetition )
    {
        this.isComma = false;
        this.innerCells = innerCells;
        this.repetition = repetition;
        this.currentIteration = 0;
        this.repetitionCount = 0;
        this.toCheck = repetition > 0 || repetition == INFINITE;
    }

    @Override
    public boolean test(String text) {

        boolean valid = innerCells[currentIteration].test(text);
        return valid;
    }

    @Override
    public boolean isReadyToChangeLineCell() {

        if ( !super.isReadyToChangeLineCell() || repetition == INFINITE )
            return false;

        return repetition == repetitionCount;
    }

    @Override
    public void moveToNext() {

        currentIteration++;
        if ( currentIteration == innerCells.length ) {
            currentIteration = 0;
            repetitionCount++;
            if ( repetition != INFINITE && repetitionCount == repetition )
                toCheck = false;
        };
    }

    @Override
    public AttributeSet _getAttributeSet() {
        return innerCells[currentIteration]._getAttributeSet();
    }

}
