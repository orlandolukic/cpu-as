package start;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class AppJTextPane extends JTextPane {

    public AppJTextPane(StyledDocument document) {
        super(document);
    }

    // Override getScrollableTracksViewportWidth
    // to preserve the full width of the text
    @Override
    public boolean getScrollableTracksViewportWidth() {
        Component parent = getParent();
        ComponentUI ui = getUI();

        return parent != null ? (ui.getPreferredSize(this).width <= parent
                .getSize().width) : true;
    }

    @Override
    public Dimension getPreferredSize() {
        // Avoid substituting the minimum width for the preferred width when the viewport is too narrow
        return getUI().getPreferredSize(this);
    };
}
