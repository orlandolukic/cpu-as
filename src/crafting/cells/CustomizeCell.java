package crafting.cells;

import crafting.LineCell;

import javax.swing.text.AttributeSet;

public class CustomizeCell extends LineCell {

    private AttributeSet attributeSet;
    private String regex;

    public CustomizeCell( String regex, AttributeSet attributeSet )
    {
        this.regex = regex;
        this.attributeSet = attributeSet;
    }

    @Override
    public boolean test(String text) {
        return text.matches(regex);
    }

    @Override
    public AttributeSet _getAttributeSet() {
        return attributeSet;
    }
}
