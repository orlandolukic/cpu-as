package crafting.cells;

import crafting.LineCell;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class EmptyCell extends LineCell {
    @Override
    public boolean test(String text) {
        return false;
    }

    @Override
    public AttributeSet _getAttributeSet() {
        return Styles.attrBlack;
    }
}
