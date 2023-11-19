package checkings;

import assembler.EntryPoint;
import controller.MainScreenController;
import crafting.Lines;
import javafx.application.Platform;
import utils.Utilities;

import javax.swing.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import static controller.MainScreenController.CurrentTab;

public class SemanticPassThread extends Thread {

    private boolean block;
    private Semaphore sem;
    private Semaphore mutex;
    private int waiting;
    private Semaphore waitingSem;
    private boolean hasErrors;
    private Semaphore readSemaphore;

    public SemanticPassThread() {
        this.sem = new Semaphore(0);
        this.mutex = new Semaphore(1);
        waiting = 0;
        this.setName("SemanticPassThread");
    }

    public void check()
    {
        sem.release();
    }

    public boolean checkAndWait( Semaphore s )
    {
        boolean val = hasErrors;
        waitingSem = s;
        sem.release();
        try {
            s.acquire();
            val = hasErrors;
            readSemaphore.release();
        } catch (InterruptedException e) {}
        return val;
    }

    private class ContWrapper {
        public void run() throws InterruptedException {
            if (waitingSem != null) {
                readSemaphore = new Semaphore(0);
                waitingSem.release();
                readSemaphore.acquire();
                waitingSem = null;
            }
        }
    }

    @Override
    public void run() {
        Semaphore semarr[] = new Semaphore[2];
        boolean interrupted = false;
        boolean b[] = new boolean[1];
        while( !interrupted() && !interrupted ) {
            semarr[0] = new Semaphore(0);
            semarr[1] = new Semaphore(0);
            try {
                sem.acquire();
                b[0] = false;
                Utilities.runLater(() -> {
                    try {
                        semarr[1].acquire();
                    } catch (InterruptedException ex) {}
                    b[0] = Lines.checkLines();
                    semarr[0].release();
                });
                semarr[1].release();
                semarr[0].acquire();
                if ( !b[0] ) {

                    new ContWrapper().run();
                    continue;
                } else if ( CurrentTab.TextFile == null ) {
                    Utilities.runAndWait(() -> {
                        if (  CurrentTab.CheckedTimes == 0 ) {
                            MainScreenController.appendNewLineWithText("Note: Please save current file");
                            CurrentTab.CheckedTimes++;
                        };
                    });
                    new ContWrapper().run();

                    continue;
                }

                hasErrors = false;
                String filename = Utilities.getFileName( CurrentTab.TextFile.getName() );
                try {
                    EntryPoint.semanticPass(new String[]{
                            CurrentTab.TextFile.getParent() + "\\" + CurrentTab.TextFile.getName(),
                            "-o",
                            CurrentTab.TextFile.getParent() + "\\" + filename + "-memory.txt",
                            "-maddr=16"
                    }, CurrentTab.TextPane.getText());
                } catch (RuntimeException re) {
                    hasErrors = true;
                    if (waitingSem == null) {
                        Semaphore s = new Semaphore(0);
                        Platform.runLater(() -> {
                            MainScreenController.getInstance().OutputTextArea.setText(re.getMessage() + "\n");
                            s.release();
                        });
                        s.acquire();
                    }
                    ;
                }

                if (waitingSem != null) {
                    readSemaphore = new Semaphore(0);
                    waitingSem.release();
                    readSemaphore.acquire();
                    waitingSem = null;
                }
            } catch (InterruptedException e) {
                if (readSemaphore != null)
                    readSemaphore.release();
                interrupted = true;
            }
        }
    }
}
