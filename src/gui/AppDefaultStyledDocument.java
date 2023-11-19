package gui;

import javax.swing.*;
import javax.swing.text.*;

import checkings.LineChecker;
import controller.MainScreenController;
import crafting.Lines;
import utils.Regex;
import utils.Styles;

public class AppDefaultStyledDocument extends DefaultStyledDocument {

	private static final String REGEX_BEFORE = "(\\W)*";
	private static final long serialVersionUID = 1L;

	public static final int NT_RegisterDirect = 1 << 0;
	public static final int NT_RegisterIndirect = 1 << 1;
	public static final int NT_Immediate = 1 << 2;
	public static final int NT_Instruction = 1 << 3;
	public static final int NT_Directive = 1 << 4;
	public static final int NT_LabelDeclaration = 1 << 5;
	public static final int NT_LabelName = 1 << 6;
	public static final int NT_SectionName = 1 << 7;
	public static final int NT_RegisterDirectPreincr = 1 << 8;
    public static final int NT_RegisterDirectPostdecr = 1 << 9;
    public static final int NT_RegisterIndirectDisp = 1 << 10;

    public static final int NT_Addressing = NT_Immediate | NT_RegisterDirect | NT_RegisterIndirect | NT_LabelName
            | NT_RegisterDirectPreincr | NT_RegisterDirectPostdecr;

    private static AppDefaultStyledDocument instance;
    public static AppDefaultStyledDocument getInstance()
    {
        return instance;
    }
	
	/**
	 * Attributes
	 */
	
	private StyleContext cont;	
	private AttributeSet attrBlack;
	private AttributeSet attrGreen;
	private AttributeSet attrComment;
	private AttributeSet attrDirective;
	private AttributeSet attrError;
	private AttributeSet attrAddressingRegister;
	private AttributeSet attrImmediate;
	
	private boolean toGoThrough;
	private int expectation;
	private boolean isError;
    private JTextPane TextPane;
    private LineChecker checker;


    public AppDefaultStyledDocument()
	{
	    expectation = -1;
	    isError = false;
	    checker = null;
		instance = this;
		toGoThrough = true;
	}
    
    public void setTextPane( JTextPane textPane )
    {
        this.TextPane = textPane;
    }
    
    public void setToGoThrough(boolean val)
    {
    	toGoThrough = val;
    }
    
    private boolean check( String substring, String regex )
    {
    	return substring.matches( REGEX_BEFORE + regex );
    }
	
	
	@Override
	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
		super.insertString(offset, str, a);
		
		String text = getText(0, getLength());

        int offsetStart = getRowStart(text, offset);
        int offsetEnd = getRowEnd(text, offset);
        highlightRow( text, offsetStart, offsetEnd );
	}
	
	@Override
	public void remove (int offs, int len) throws BadLocationException {
        super.remove(offs, len);

        String text = getText(0, getLength());

        int offsetStart = getRowStart(text, offs);
        int offsetEnd = getRowEnd(text, offs);
        highlightRow( text, offsetStart, offsetEnd );
    }

    public void highlightAll()
    {
        try {
            String text = getText(0, getLength());
            String rows[] = text.split("\\r\\n");
            String row;
            int start = 0, end = getLength() - 1;
            int rowStart, rowEnd;

            while( start <= end )
            {
                rowStart = getRowStart(text, start);
                if ( rowStart < 0 ) rowStart = 0;
                rowEnd = getRowEnd(text, rowStart);
                if ( rowEnd < 0 ) rowEnd = 0;
                if ( rowEnd - rowStart > 0 ) {
                    row = text.substring(rowStart, rowEnd);
                    highlightRow(text, rowStart, rowEnd);
                    start = getWordStart(text, rowEnd);
                };
                checker = null;
                start = getWordStart(text, rowEnd);
            };
        } catch ( BadLocationException e ) {}
    }

    private void highlightRow( String text, int start, int end ) {
        if ( end - start == 0 )
            return;

        String substring;

        int index = getWordStart(text, start);
        int row = getRowNumber(this, index);
        int itRow;
        int wordEnd;

        int rowStart = getRowStart(text, index);
        int rowEnd = getRowEnd(text, index);
        int rowLength = rowEnd - rowStart;

        boolean isComment = false;
        boolean isEnd = false;
        boolean test = true;
        boolean isSet = false;
        boolean isLabelDecl = false;
        expectation = -1;
        AttributeSet s = attrBlack;

        while (index < end) {

            wordEnd = getWordEnd(text, index);
            substring = text.substring(index, wordEnd);

            // If checker is not set.
            if ( !isSet || checker == null )
            {
                if ( substring.matches(Regex.REGEX_LABEL_DECL) ) {
                    s = Styles.attrBlack;
                    test = false;
                } else if ( substring.matches(Regex.REGEX_COMMENT_START) ) {
                    s = Styles.attrBlack;
                    checker = Lines.COMMENT;
                    checker.getFirstCell(substring);
                    checker.resetIterator();
                    isSet = true;
                    test = true;
                } else {
                    s = Styles.attrBlack;
                    checker = Lines.getLineChecker(substring);
                    if ( checker != null )
                        checker.resetIterator();
                    isSet = true;
                    test = true;
                }
            } else if ( checker != null ) {
                if ( substring.matches(Regex.REGEX_LABEL_DECL) ) {
                    s = Styles.attrBlack;
                    test = false;
                    checker = null;
                } else if ( substring.matches(Regex.REGEX_COMMENT_START) ) {
                    s = Styles.attrBlack;
                    checker = Lines.COMMENT;
                    checker.getFirstCell(substring);
                    checker.resetIterator();
                    test = true;
                }
            }

            if ( test ) {
                checker.getCurrent().setContent(substring);
                checker.getCurrent().testLineCell(substring);
                s = checker.getCurrent().getAttributeSet();

                checker.moveToNext();
            } else {
                s = Styles.attrBlack;
            };

            //clearSpaces( text, rowStart, rowEnd );
            setCharacterAttributes(wordEnd, rowEnd - wordEnd, Styles.attrBlack, true);
            setCharacterAttributes(index, wordEnd - index, s, true);
            index = getWordStart(text, wordEnd);
        };
    }

    public static void paintSpaces( AttributeSet attributeSet, int start, int end )
    {
        try {
            String text = instance.getText(0, instance.getLength());
            int indexer = start;
            while (indexer < end) {
                if ((text.charAt(indexer) == ' ' || text.charAt(indexer) == '\t')) {
                    instance.setCharacterAttributes(indexer, 1, attributeSet, true);
                };
                indexer++;
            }
        } catch( Exception e ) {}
    }

    public static void paintSpacesWholeDocument( AttributeSet attributeSet )
    {
        paintSpaces( attributeSet, 0, instance.getLength() );
    }

    public static int getWordStart( String text, int start )
    {
        int indexer = start;
        while( indexer < text.length() )
        {
            if ( (text.charAt(indexer) == ' ' || text.charAt(indexer) == '\t') )
            {
                //instance.setCharacterAttributes(indexer, 1, Styles.attrLineBackground, true);
            };
            if ( text.charAt(indexer) == ' ' || text.charAt(indexer) == '\t' || text.charAt(indexer) == '\n' )
                indexer++;
            else
                return indexer;
        }
        return indexer;
    }

    public static int getWordEnd( String text, int start )
    {
        int indexer = start;
        while( indexer < text.length() )
        {
            if ( text.charAt(indexer) == ' ' || text.charAt(indexer) == '\t' || text.charAt(indexer) == '\n' )
                return indexer;
            else
                indexer++;
        }
        return indexer;
    }

    public static int getRowStart( String text, int start )
    {
        int indexer = start-1;
        while( indexer >= 0 )
        {
            if ( text.charAt(indexer) == '\n' )
                return getWordStart(text, indexer+1);
            else
                indexer--;
        };
        if ( indexer < 0 )
            return 0;
        return indexer;
    }

    public static int getRowEnd( String text, int start )
    {
        int indexer = start;
        while( indexer < text.length() )
        {
            if ( text.charAt(indexer) == '\n' )
                return indexer;
            else
                indexer++;
        }
        return indexer;
    }

    public static int getRowNumber( AppDefaultStyledDocument doc, int currentPosition )
    {
        int index = 0, len = doc.getLength();
        int row = 1;
        try {
            String text = doc.getText(0, len);
            while ( index < currentPosition && index < len ) {
                if ( text.charAt(index) == '\n' )
                    row++;
                index++;
            };
            return row;
        } catch( Exception e ) {}
        return 1;
    }

}
