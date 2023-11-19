package utils;

import controller.MainScreenController;
import gui.AppDefaultStyledDocument;
import gui.DebugPopUpWindow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/*
 *  Track the movement of the Caret by painting a background line at the
 *  current caret position.
 */
public class LinePainter
	implements Highlighter.HighlightPainter, CaretListener, MouseListener, MouseMotionListener
{
	private JTextComponent component;

	private Color color;

	private Rectangle lastView;

	private boolean isAllowedToPaint;

	private boolean clearAllow;

	private int currentRow;
	private int rowStart;
	private int rowEnd;
	private boolean isBreakpoint;

	/*
	 *  The line color will be calculated automatically by attempting
	 *  to make the current selection lighter by a factor of 1.2.
	 *
	 *  @param component  text component that requires background line painting
	 */
	public LinePainter(JTextComponent component)
	{
		this(component, null);
		setLighter(component.getSelectionColor());
	}

	/*
	 *  Manually control the line color
	 *
	 *  @param component  text component that requires background line painting
	 *  @param color      the color of the background line
	 */
	public LinePainter(JTextComponent component, Color color)
	{
		this.component = component;
		setColor( color );

		//  Add listeners so we know when to change highlighting

		component.addCaretListener( this );
		component.addMouseListener( this );
		component.addMouseMotionListener( this );

		//  Turn highlighting on by adding a dummy highlight

		try
		{
			component.getHighlighter().addHighlight(0, 0, this);
		}
		catch(BadLocationException ble) {}

		isAllowedToPaint = true;
		clearAllow = false;
		rowStart = -1;
		rowEnd = -1;
	}

	public void setClearAllow( boolean value )
	{
		clearAllow = value;
	}

	public boolean getClearAllow()
	{
		return clearAllow;
	}

	public boolean isAllowedToPaint()
	{
		return isAllowedToPaint;
	}

	public void setAllowedToPaint( boolean toPaint )
	{
		isAllowedToPaint = toPaint;
	}

	/*
	 *	You can reset the line color at any time
	 *
	 *  @param color  the color of the background line
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}

	/*
	 *  Calculate the line color by making the selection color lighter
	 *
	 *  @return the color of the background line
	 */
	public void setLighter(Color color)
	{
		int red   = Math.min(255, (int)(color.getRed() * 1.2));
		int green = Math.min(255, (int)(color.getGreen() * 1.2));
		int blue  = Math.min(255, (int)(color.getBlue() * 1.2));
		setColor(new Color(red, green, blue));
	}

	//  Paint the background highlight
	@Override
	public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c)
	{
		if ( !isAllowedToPaint )
			return;
		try
		{
			Graphics2D g1 = (Graphics2D) g;
			g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			Rectangle r = c.modelToView(c.getCaretPosition());
			g.setColor( color );
			g.fillRect(0, r.y, c.getWidth(), r.height);

			if (lastView == null)
				lastView = r;
		}
		catch(BadLocationException ble) {System.out.println(ble);}
		if ( clearAllow )
			isAllowedToPaint = false;
	}

	public void paintBackground( RowInformation ri )
	{
		paintBackground(ri, false);
	}

	public void paintBackground( RowInformation ri, boolean isCurrentLine )
	{
		SwingUtilities.invokeLater(() -> {
			// Repaint previous line to Background.NONE
			if ( rowStart != -1 && rowEnd != -1 ) {
				if ( !isBreakpoint ) {
					((JTextPane) component).getStyledDocument()
							.setCharacterAttributes(rowStart, rowEnd - rowStart, Styles.attrWhiteLineBackground, false);
				} else {
					((JTextPane) component).getStyledDocument()
							.setCharacterAttributes(rowStart, rowEnd - rowStart, Styles.attrBreakpointLineBackground, false);
				}
			};

			rowStart = component.getCaretPosition();
			rowEnd = Utilities.getRowEndIndex( component.getText(), ri );
			((JTextPane) component).getStyledDocument()
					.setCharacterAttributes(rowStart, rowEnd - rowStart, !isCurrentLine ?
							Styles.attrDebugLineBackground : Styles.attrBreakpointAndDebugLineBackground, false);
			isBreakpoint = isCurrentLine;
			currentRow = ri.line;
		});
	}

	public void paintDebugLine( int line )
	{
		SwingUtilities.invokeLater(() -> {
			RowInformation ri = Utilities.getRowStartIndex( component.getText(), line );
			component.setCaretPosition(ri.getRealOffset());
			paintBackground(ri);
		});
	}

	public void paintBreakpointLine( RowInformation ri, boolean isCleared )
	{
		int rowStart = component.getCaretPosition();
		int rowEnd = Utilities.getRowEndIndex( component.getText(), ri );
		AttributeSet s;
		if ( isCleared ) {
			if ( ri.line == currentRow )
				s = Styles.attrDebugLineBackground;
			else
				s = Styles.attrWhiteLineBackground;
		} else {
			if (ri.line == currentRow)
				s = Styles.attrBreakpointAndDebugLineBackground;
			else
				s = Styles.attrBreakpointLineBackground;
		};
		((JTextPane) component).getStyledDocument().setCharacterAttributes(rowStart, rowEnd - rowStart, s, false);
	}

	public void clearRowParameters()
	{
		rowStart = -1;
		rowEnd = -1;
	}

	public void resetPaintedBackground()
	{
		SwingUtilities.invokeLater(() -> {
			// Repaint whole document to Background.NONE
			((JTextPane) component).getStyledDocument()
					.setCharacterAttributes(0, ((JTextPane) component).getText().length(), Styles.attrWhiteLineBackground, false);
			rowStart = -1;
			rowEnd = -1;
		});
	}

	/*
	*   Caret position has changed, remove the highlight
	*/
	public void resetHighlight()
	{
		//  Use invokeLater to make sure updates to the Document are completed,
		//  otherwise Undo processing causes the modelToView method to loop.

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					int offset =  component.getCaretPosition();
					Rectangle currentView = component.modelToView(offset);

					//  Remove the highlighting from the previously highlighted line
					if (lastView != null && lastView.y != currentView.y)
					{
						component.repaint(0, lastView.y, component.getWidth(), lastView.height);
						lastView = currentView;
					}
				}
				catch(BadLocationException ble) {}
			}
		});
	}

	//  Implement CaretListener

	public void caretUpdate(CaretEvent e)
	{
		if ( !DebugPopUpWindow.IS_DEBUGGING )
			resetHighlight();
	}

	//  Implement MouseListener

	public void mousePressed(MouseEvent e)
	{
		if ( !DebugPopUpWindow.IS_DEBUGGING )
			resetHighlight();
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	//  Implement MouseMotionListener

	public void mouseDragged(MouseEvent e)
	{
		if ( !DebugPopUpWindow.IS_DEBUGGING )
			resetHighlight();
	}

	public void mouseMoved(MouseEvent e) {}
}
