package crafting.cells;

import crafting.LineCell;

import javax.swing.text.AttributeSet;

public class CombinedCell extends LineCell {

    private int ind;
    private String removeRegex;
    private LineCell[] combinedCells;

    public CombinedCell( LineCell[] combinedCells, String removeRegex )
    {
        this.combinedCells = combinedCells;
        this.removeRegex = removeRegex;
    }

    @Override
    public boolean test(String text) {

        if ( removeRegex != null )
            text = text.replaceAll(removeRegex, "");

        for (int i=0; i<combinedCells.length; i++)
            if ( combinedCells[i].test(text) ) {
                ind = i;
                return true;
            };

        return false;
    }

    @Override
    public AttributeSet _getAttributeSet() {
        return combinedCells[ind]._getAttributeSet();
    }
}
