package gui;

import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import utils.Styles;

public class AdaptedButton extends Button {

    private static PseudoClass PSEUDO_CLASS_ACTIVE = PseudoClass.getPseudoClass("active");

    public static void makeAdaptedButtonFrom( ButtonBase b ) {
        appendListeners(b);
    }

    public AdaptedButton( String text ) {
        super(text);
        appendListeners(this);
    }

    private static void appendListeners( ButtonBase ins ) {
        ins.setOnMousePressed(event -> {
            ins.pseudoClassStateChanged(PSEUDO_CLASS_ACTIVE, true);
        });
        ins.setOnMouseReleased(event -> {
            ins.pseudoClassStateChanged(PSEUDO_CLASS_ACTIVE, false);
        });
    }

}
