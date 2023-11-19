package crafting;

import checkings.LineChecker;
import utils.Styles;

import javax.swing.text.AttributeSet;
import java.util.concurrent.Callable;

public abstract class LineCell {

    protected boolean isSticky;
    protected String content;
    protected String errorMessage;
    private LineChecker lineChecker;
    private boolean isValid;
    private boolean isMandatory;
    private Runnable errorRunnable;
    private Callable<Boolean> beforeEnd;
    private AttributeSet newAttrSet;

    protected LineCell()
    {
        this.isSticky = false;
        this.isValid = true;
        this.isMandatory = true;
        errorRunnable = () -> {
            setErrorMessage("Unrecognized error");
        };
        beforeEnd = () -> {
            return true;
        };
        newAttrSet = null;
    }

    public void setContent( String content )
    {
        this.content = content;
    }

    public String getContent()
    {
        return this.content;
    }

    public void setNewAttrSet( AttributeSet as ) {
        newAttrSet = as;
    }

    public AttributeSet getNewAttrSet() {
        return newAttrSet;
    }

    /**
     * Test line cell.
     * @param text outside text.
     * @return indicator whether given text is ok with current line cell.
     */
    public abstract boolean test( String text );

    public void setMandatory(boolean mandatory)
    {
        this.isMandatory = mandatory;
    }

    public boolean isMandatory()
    {
        return isMandatory;
    }

    public boolean isSticky()
    {
        return isSticky;
    }

    public void setLineChekcher(LineChecker checker)
    {
        this.lineChecker = checker;
    }

    protected LineChecker getLineChecker()
    {
        return lineChecker;
    }

    public void moveToNextLineCell()
    {
        lineChecker.moveToNext(true);
    }

    public boolean isReadyToChangeLineCell()
    {
        return isSticky ? false : true;
    }

    public void moveToNext() {}

    public boolean testLineCell( String text )
    {
        if ( content == null )
        {
            isValid = false;
            return isValid;
        };

        boolean b;
        if ( !isValid )
            return false;
        isValid = test(text);
        b = isValid;
        try {
            isValid &= beforeEnd.call();
        } catch( Exception e ) { isValid = b; }
        return isValid;
    }

    public void start( boolean startValue )
    {
        this.isValid = startValue;
        this.content = null;
    }

    public boolean getIsValid()
    {
        return isValid;
    }

    public boolean finishTesting()
    {
        return isValid;
    }

    public String getExplanation()
    {
        return isValid ? "Everything is fine!" : "Testing failed...";
    }

    /**
     * Gets attribute set for the given line cell.
     * Implementation for each line cell.
     * @return attribute set
     */
    public abstract AttributeSet _getAttributeSet();

    public AttributeSet getAttributeSet()
    {
        if ( !isValid )
            return Styles.attrError;
        else
            return newAttrSet == null ? _getAttributeSet() : newAttrSet;
    }

    public void setErrorMessage( String str ) {
        this.errorMessage = str;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorRunnable( Runnable r ) {
        errorRunnable = r;
    }

    public void execErrorRunnable() {
        errorRunnable.run();
    }

    public void setBeforeEndCheck( Callable<Boolean> callable ) {
        beforeEnd = callable;
    }

    public LineCell getPrevious()
    {
        return this.lineChecker.getFromCurrent(-1);
    }


}
