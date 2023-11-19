package utils;

import javax.swing.text.*;
import java.awt.*;

public class Styles {

    public static final AttributeSet attrBlack;
    public static final AttributeSet attrGreen;
    public static final AttributeSet attrComment;
    public static final AttributeSet attrDirective;
    public static final AttributeSet attrError;
    public static final AttributeSet attrAddressingRegister;
    public static final AttributeSet attrImmediate;
    public static final AttributeSet attrLineBackground;
    public static final AttributeSet attrWhiteLineBackground;
    public static final AttributeSet attrDebugLineBackground;
    public static final AttributeSet attrBreakpointLineBackground;
    public static final AttributeSet attrBreakpointAndDebugLineBackground;

    public static final Color gray;
    public static final Color colorLineBackground;
    public static final Color colorDebugLineBackground;
    public static final Color colorBreakpointLineBackground;
    public static final Color colorBreakpointAndDebugLineBackground;

    static {
        // Line background.
        gray = new Color(250,250,250);
        colorLineBackground = gray;
        colorDebugLineBackground = new Color(52, 187, 113, 70);
        colorBreakpointLineBackground = new Color(219,108,30,70);
        colorBreakpointAndDebugLineBackground = new Color(80,60,60,60);

        MutableAttributeSet m = new SimpleAttributeSet();
        StyleConstants.setBold(m, false);
        StyleConstants.setForeground(m, Color.BLACK);
        attrBlack = m.copyAttributes();

        m = new SimpleAttributeSet();
        StyleConstants.setBold(m, true);
        StyleConstants.setForeground(m, new Color(107,12,33));
        attrDirective = m.copyAttributes();

        m = new SimpleAttributeSet();
        StyleConstants.setForeground(m, new Color(0,0,0));
        StyleConstants.setBackground(m, new Color(242, 72, 80, 40));
        attrError = m.copyAttributes();

        m = new SimpleAttributeSet();
        StyleConstants.setForeground(m, new Color(143, 171, 255));
        attrAddressingRegister = m.copyAttributes();

        m = new SimpleAttributeSet();
        StyleConstants.setForeground(m, new Color(255, 17, 110, 255));
        attrImmediate = m.copyAttributes();

        m = new SimpleAttributeSet();
        StyleConstants.setForeground(m, new Color(56,138,24));
        attrGreen = m.copyAttributes();

        m = new SimpleAttributeSet();
        StyleConstants.setForeground(m, new Color(126,119,119));
        attrComment = m.copyAttributes();

        m = new SimpleAttributeSet();
        StyleConstants.setBackground(m, colorLineBackground);
        attrLineBackground = m.copyAttributes();

        m = new SimpleAttributeSet();
        StyleConstants.setBackground(m, new Color(255,255,255, 0));
        attrWhiteLineBackground = m.copyAttributes();

        m = new SimpleAttributeSet();
        StyleConstants.setBackground(m, colorDebugLineBackground);
        attrDebugLineBackground = m.copyAttributes();

        m = new SimpleAttributeSet();
        StyleConstants.setBackground(m, colorBreakpointLineBackground);
        attrBreakpointLineBackground = m.copyAttributes();

        m = new SimpleAttributeSet();
        StyleConstants.setBackground(m, colorBreakpointAndDebugLineBackground);
        attrBreakpointAndDebugLineBackground = m.copyAttributes();
    }

}
