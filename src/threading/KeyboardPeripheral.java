package threading;

import controller.KeyboardScreenController;
import controller.ScreenPeripheralController;
import debug.Execution;
import debug.registers.PeripheralRegister;
import utils.ApplicationThread;
import utils.Utilities;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class KeyboardPeripheral extends ApplicationThread {

    private KeyboardScreenController c;
    private PeripheralRegister statusReg;
    private PeripheralRegister controlReg;
    private PeripheralRegister dataReg;
    private PeripheralRegister cntdataReg;
    private LinkedList<String> buffer;
    private Semaphore buffSem;
    private LinkedList<String> innerMemory;
    private int memoryCounter;
    private Execution execution;

    public LinkedList<String> getBuffer() {
        return buffer;
    }

    public void insertIntoBuffer( String s ) {
        buffSem.acquireUninterruptibly();
        buffer.add(s);
        buffSem.release();
    }

    public void setExecutionThread( Execution exec ) {
        execution = exec;
    }

    public KeyboardPeripheral()
    {
        setName("Keyboard Peripheral Thread");
        c = KeyboardScreenController.getController();
        statusReg = KeyboardScreenController.StatusRegister;
        controlReg = KeyboardScreenController.ControlRegister;
        dataReg = KeyboardScreenController.DataRegister;
        cntdataReg = KeyboardScreenController.CntDataRegister;
        buffer = new LinkedList<>();
        innerMemory = new LinkedList<>();
        buffSem = new Semaphore(1);
        memoryCounter = 0;
        runnable = () -> {
            int val = -1;
            int s;
            try {
                while( !interrupted() )
                {
                    // Status bits
                    // AVAIL
                    statusReg.setBit((char) 2, buffer.size() > 0);

                    // RST bit
                    if ( controlReg.getBit((char) 6) )
                    {
                        buffer.clear();
                        if ( c != null )
                            c.clearBuffer();
                        boolean ENA = controlReg.getBit((char) 2);
                        controlReg.reset();
                        controlReg.setBit((char) 2, ENA);
                        statusReg.reset();
                        dataReg.reset();
                        cntdataReg.reset();
                        sleep(150);
                    };

                    // To consume from input buffer.
                    if ( buffer.size() > 0 && !statusReg.getBit((char) 6) && controlReg.getBit((char) 4) && controlReg.getBit((char) 2)
                    && statusReg.getBit((char) 2) ) {
                        if ( !statusReg.getBit((char) 1) ) {
                            cntdataReg.reset();
                        };

                        buffSem.acquireUninterruptibly();
                        String ch = buffer.getFirst();
                        buffer.removeFirst();
                        innerMemory.add(ch);
                        if ( c != null )
                            Utilities.runLater(() -> { c.BufferVBOX.getChildren().remove(0); });
                        buffSem.release();

                        statusReg.setBit((char) 1, true);

                        // check if decimal regime is active but char is pressed
                        if ( controlReg.getBit((char) 5) && !ch.equals("<ENTER>") && Character.isAlphabetic(ch.charAt(0)) ) {
                            statusReg.setBit((char) 6, true);
                        } else {

                            if ( c != null )
                                c.WorkingPane.setVisible(true);

                            statusReg.setBit((char) 6, false);

                            // For char regime, increment cntdata.
                            if ( !controlReg.getBit((char) 5) ) {
                                cntdataReg.increment();
                            };

                            // if enter is pressed
                            if ( ch.equals("<ENTER>") ) {
                                statusReg.setBit((char) 1, false);

                                // if device works in interrupt regime
                                if ( controlReg.getBit((char) 7) )   {
                                    // Decimal interrupt regime
                                    if ( controlReg.getBit((char) 5) ) {
                                        cntdataReg.reset();
                                        int number = innerMemory.getFirst().equals("<ENTER>") ? -1 : 0;
                                        while( number != -1 ) {
                                            if ( innerMemory.getFirst().equals("<ENTER>") ) {
                                                innerMemory.removeFirst();
                                                break;
                                            }
                                            number += Integer.parseInt( innerMemory.getFirst() );
                                            number *= 10;
                                            innerMemory.removeFirst();
                                        };

                                        Semaphore wait = new Semaphore(0);
                                        dataReg.setValue( number/10 );
                                        dataReg.setWaitForReadAccessSem( wait );
                                        execution.setInterrupt(0);
                                        wait.acquire();

                                        if ( buffer.size() > 0 ) {
                                          statusReg.setBit((char) 1, false);
                                        };
                                    } else {    // Character interrupt regime
                                        cntdataReg.decrement();
                                        if ( !buffer.getFirst().equals("<ENTER>") ) {
                                            char chx;
                                            while( true ) {
                                                if ( innerMemory.getFirst().equals("<ENTER>") )
                                                    break;
                                                cntdataReg.decrement();
                                                chx = innerMemory.getFirst().charAt(0);
                                                innerMemory.removeFirst();
                                                Semaphore wait = new Semaphore(0);
                                                dataReg.setValue( chx );
                                                dataReg.setWaitForReadAccessSem( wait );
                                                execution.setInterrupt(0);
                                                innerMemory.clear();
                                                wait.acquire();
                                            }

                                            if ( buffer.size() > 0 ) {
                                                statusReg.setBit((char) 1, false);
                                                cntdataReg.reset();
                                                innerMemory.clear();
                                            };
                                        };
                                    };
                                } else {    // Non-interrupt regime
                                    if ( controlReg.getBit((char) 5) ) {
                                       cntdataReg.increment();
                                    } else {
                                        cntdataReg.decrement();
                                    };
                                    innerMemory.clear();
                                    dataReg.reset();
                                    statusReg.setBit((char) 1, false);
                                }
                            } else {    // Enter IS NOT pressed.

                            };

                            if ( c != null )
                                c.WorkingPane.setVisible(false);
                        };
                    };

                    // READ bit
                    if ( controlReg.getBit((char) 0) )
                    {
                        // set ready bit
                        statusReg.setBit((char) 4, false );

                        if ( c != null )
                            c.WorkingPane.setVisible(true);

                        if ( !controlReg.getBit((char) 7) && !statusReg.getBit((char) 6) && innerMemory.size() > 0 ) {
                            // Decimal non-interrupt regime
                            if ( controlReg.getBit((char) 5) ) {
                                cntdataReg.reset();
                                int number = innerMemory.getFirst().equals("<ENTER>") ? 0 : 1;
                                while( number != 0 ) {
                                    if ( innerMemory.getFirst().equals("<ENTER>") )
                                        break;
                                    number *= Integer.parseInt( innerMemory.getFirst() );
                                    innerMemory.remove(0);
                                };
                                dataReg.setValue(number / 10);
                            } else {
                                cntdataReg.decrement();
                                if ( innerMemory.getFirst().equals("<ENTER>") ) {
                                    dataReg.setValue(10);
                                } else {
                                    dataReg.setValue( innerMemory.getFirst().charAt(0) );
                                };
                                innerMemory.removeFirst();
                            }
                        } else {
                            dataReg.reset();
                        };
                        controlReg.setBit((char) 0, false);
                        dataReg.updateLabels();
                        sleep(120);
                        statusReg.setBit((char) 4, true);

                        if ( c != null )
                            c.WorkingPane.setVisible(false);
                    };

                    if ( controlReg.getBit((char) 3) )
                    {
                        if ( c != null )
                            c.WorkingPane.setVisible(true);

                        buffer.clear();
                        if ( c != null )
                            c.clearBuffer();

                        if ( c != null )
                            c.WorkingPane.setVisible(false);
                    };

                    sleep(300);

                    c = KeyboardScreenController.getController();
                };
            } catch (InterruptedException ex) {}
        };
    }
}
