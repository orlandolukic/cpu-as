package crafting.cells;

import crafting.LineCell;
import utils.Regex;
import utils.Styles;
import utils.Utilities;

import javax.swing.text.AttributeSet;

public class LabelCell extends LineCell {

    private boolean isLabelDeclaration;

    public LabelCell()
    {
        this(false);
    }

    public LabelCell( boolean isLabelDeclaration )
    {
        this.isLabelDeclaration = isLabelDeclaration;
    }

    public LabelCell( boolean isLabelDeclaration, boolean isMadatory )
    {
        this(isLabelDeclaration);
        this.setMandatory(isMadatory);
    }

    @Override
    public boolean isMandatory() {
        if ( isLabelDeclaration )
            return super.isMandatory();
        else
            return true;
    }

    @Override
    public boolean test(String text) {
        if ( !isLabelDeclaration )
        {
            return text.matches( Regex.REGEX_LABEL_NAME ) && !Utilities.isRegister(text);
        } else
            return text.matches( Regex.REGEX_LABEL_DECL );
    }

    @Override
    public AttributeSet _getAttributeSet() {
        return Styles.attrBlack;
    }
}
