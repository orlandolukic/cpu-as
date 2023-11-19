package crafting.cells;

import crafting.LineCell;

import javax.swing.text.AttributeSet;

public class RepetitionCommaCell extends LineCell {

    public static final int INFINITE = -1;

    private boolean toCheck;
    private boolean isComma;
    private LineCell[] innerCells;
    private int repetition;
    private int repetitionCount;
    private int currentIteration;
    private boolean isPreviouslyDeletedComma;
    private boolean isPreviousComma;

    public RepetitionCommaCell(LineCell[] innerCells, int repetition )
    {
        this.isComma = false;
        this.innerCells = innerCells;
        this.repetition = repetition;
        this.isPreviouslyDeletedComma = false;
        this.currentIteration = 0;
        this.repetitionCount = 0;
        this.toCheck = repetition > 0 || repetition == INFINITE;
        this.isPreviousComma = false;
    }

    @Override
    public void start(boolean startValue) {
        super.start(startValue);
        isComma = false;
        this.isPreviouslyDeletedComma = false;
        this.currentIteration = 0;
        this.repetitionCount = 0;
        this.toCheck = repetition > 0 || repetition == INFINITE;
        this.isPreviousComma = false;
    }

    @Override
    public boolean test(String text) {

        boolean toSetComma = true;

        if ( !toCheck )
            return false;

        if ( isComma && !text.matches("^,(.+)$") ) {
            isComma = false;
            isPreviouslyDeletedComma = false;
            isPreviousComma = true;
            return text.matches("^,$");
        } else
        {
            isComma = false;
            if ( text.matches(",(.*)$") && (isPreviouslyDeletedComma || isPreviousComma) )
                return false;

            if ( text.matches("((.*),{2,}$|^,{2,}(.*))") )
                return false;

            // Comma is at the end of the string.
            if ( text.matches("^(,?(((.+),)+((.+)),?))") )
            {
                boolean val1 = text.matches("(.*),$");
                boolean val2 = text.matches("^,(.*)");
                boolean isPreviouslyDeletedCommaContext = isPreviouslyDeletedComma;
                if ( val1 )
                {
                    isPreviouslyDeletedComma = true;
                    text = text.replaceAll(",$", "");
                }

                if ( val2 )
                {
                    if ( isPreviousComma || isPreviouslyDeletedCommaContext )
                        return false;
                    text = text.replaceAll("^,", "");
                }

                if ( !val1 && !val2 )
                {
                    isPreviouslyDeletedComma = false;
                };
                isComma = !val1;
                isPreviousComma = false;

                String arr[] = text.split(",");
                boolean ok = true;
                for (int i=0; i<arr.length; i++)
                    ok = ok && innerCells[currentIteration].test(arr[i]);

                return ok;
            } else if ( text.matches("(.+),$") )
            {
                text = text.replaceAll(",$", "");
                toSetComma = false;
                isPreviouslyDeletedComma = true;
            } else  if ( text.matches( "^,(.+)" ) ) {  // Comma is at the start of the string.
                text = text.replaceAll("^,", "");
                toSetComma = true;
                isPreviouslyDeletedComma = false;
            } else
            {
                isPreviouslyDeletedComma = false;
            }


            boolean valid = innerCells[currentIteration].test(text);
            if ( toSetComma )
                isComma = true;
            isPreviousComma = false;
            return valid;
        }
    }

    @Override
    public boolean isMandatory() {
        return this.repetition != INFINITE;
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
    public boolean finishTesting() {

        if ( isPreviousComma || isPreviouslyDeletedComma )
            return false;
        else
            return true;

    }

    @Override
    public String getExplanation() {
        if ( isPreviousComma || isPreviouslyDeletedComma )
            return "Comma is set on the previous number.";
        return super.getExplanation();
    }

    @Override
    public AttributeSet _getAttributeSet() {
        return innerCells[currentIteration]._getAttributeSet();
    }
}
