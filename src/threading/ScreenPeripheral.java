package threading;

import controller.ScreenPeripheralController;
import debug.registers.PeripheralRegister;
import utils.ApplicationThread;
import utils.Utilities;

public class ScreenPeripheral extends ApplicationThread {

    private ScreenPeripheralController c;
    private PeripheralRegister statusReg;
    private PeripheralRegister controlReg;
    private PeripheralRegister dataReg;
    private StringBuilder content;

    public StringBuilder getScreenContent() {
        return content;
    }

    public ScreenPeripheral()
    {
        setName("Screen Peripheral Thread");
        c = ScreenPeripheralController.getController();
        statusReg = ScreenPeripheralController.StatusRegister;
        controlReg = ScreenPeripheralController.ControlRegister;
        dataReg = ScreenPeripheralController.DataRegister;
        content = new StringBuilder();
        runnable = () -> {
            int val = -1;
            int s;
            try {
                while( !interrupted() )
                {
                    s = controlReg.getBit((char) 2) ? 1 : 0;
                    if ( (val == -1 || s != val) && c != null ) {
                        Utilities.runAndWait(() -> {
                            c.PeripheralTextArea.setDisable( !controlReg.getBit((char) 2) );
                        });
                        val = s;
                    };

                    if ( controlReg.getBit((char) 7) )
                    {
                        if ( c != null )
                           Utilities.runLater(() -> { c.PeripheralTextArea.appendText("\n"); });
                        controlReg.setBit((char) 7, false);
                    };

                    if ( controlReg.getBit((char) 4) )
                    {
                        if ( c != null )
                            c.WorkingPane.setVisible(true);

                        statusReg.setBit((char) 4, true);

                        if ( controlReg.getBit((char) 6) )
                            printDecimal();
                        else
                            printChar();

                        controlReg.setBit((char) 4, false);
                        statusReg.setBit((char) 4, false);

                        if ( c != null )
                            c.WorkingPane.setVisible(false);
                    };

                    if ( controlReg.getBit((char) 1) )
                    {
                        if ( c != null )
                            c.WorkingPane.setVisible(true);

                        content = new StringBuilder();
                        if ( c != null )
                            c.PeripheralTextArea.clear();

                        controlReg.setBit((char) 1, false);

                        if ( c != null )
                            c.WorkingPane.setVisible(false);
                    };

                    sleep(150);

                    c = ScreenPeripheralController.getController();
                };
            } catch (InterruptedException e) {}
        };
    }

    private void printDecimal() throws InterruptedException {
        int number = dataReg.getValue();
        int dig;
        int div = 1, t = 10;
        while( true ) {
            if ( number > 0 ) {
                div *= 10;
            } else
                break;
            number /= 10;
        };
        number = dataReg.getValue();
        while( true ) {
            if ( div == 1 )
                break;
            dig = number * 10 / div;
            dig = dig % 10;
            if ( controlReg.getBit((char) 3) && controlReg.getBit((char) 2) ) {
                content.append(dig);
                if (c != null) {
                    Integer in = Integer.valueOf(dig);
                    Utilities.runAndWait(() -> {
                        c.PeripheralTextArea.appendText(String.valueOf(in.intValue()));
                    });
                }
            };
            sleep((long) (Math.random() * 500 + 100));
            div /= 10;
        };
    }

    private void printChar() throws InterruptedException
    {
        int ich = (char) (dataReg.getValue() & 0xFF);
        char ch = (char) ich;

        if ( controlReg.getBit((char) 3) && controlReg.getBit((char) 2) )
            content.append( ch );

        if ( c != null && controlReg.getBit((char) 3) && controlReg.getBit((char) 2) ) {
            String s = String.valueOf(ch);
            Utilities.runAndWait(() -> {
                c.PeripheralTextArea.appendText( s );
            });
        };

        sleep((long) (Math.random() * 200 + 79));
    }
}
