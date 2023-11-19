package threading;

import debug.DebugControlPanel;
import debug.Execution;
import utils.ApplicationThread;

public class TimerPeripheral extends ApplicationThread {

    private DebugControlPanel cp;

    public TimerPeripheral(DebugControlPanel cp) {
        this.cp = cp;
        runnable = () -> {
            try {
                while (!interrupted()) {
                    sleep((long) 1200);
                    if ( cp.execution != null )
                        cp.execution.setInterrupt(1);
                }
            } catch ( InterruptedException e ) {}
        };
    }
}
