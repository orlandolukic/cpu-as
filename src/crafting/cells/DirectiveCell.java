package crafting.cells;

import crafting.LineCell;
import utils.Regex;
import utils.Styles;

import javax.swing.text.AttributeSet;

public class DirectiveCell extends LineCell {

    private String regex;
    public DirectiveCell(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean test(String text) {
        return text.matches(regex);
    }

    @Override
    public AttributeSet _getAttributeSet() {
        return Styles.attrDirective;
    }
}
