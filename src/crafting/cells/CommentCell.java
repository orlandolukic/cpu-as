package crafting.cells;

import crafting.LineCell;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class CommentCell extends LineCell {
    @Override
    public boolean test(String text) {
        return true;
    }

    @Override
    public AttributeSet _getAttributeSet() {
        return Styles.attrComment;
    }
}
