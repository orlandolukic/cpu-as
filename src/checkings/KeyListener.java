package checkings;

import controller.MainScreenController;

import java.awt.event.KeyEvent;

public class KeyListener implements java.awt.event.KeyListener {
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if ( e.getKeyCode() == 10 ) {
            if ( MainScreenController.CurrentTab.TextFile != null ) {
                MainScreenController.spt.check();
            };
        };
    }
}
