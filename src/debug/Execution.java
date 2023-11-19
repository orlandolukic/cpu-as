package debug;

import assembler.Instructions;
import controller.MainScreenController;
import debug.memory.Memory;
import debug.registers.PSW;
import debug.registers.Register;
import gui.Main;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import utils.ApplicationThread;
import utils.RowInformation;
import utils.Utilities;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class Execution extends Thread {

    private Register PC;
    private Memory mem;

    private Semaphore readyToStart;
    private Semaphore start;
    private Semaphore mutex;
    private Semaphore breakpointWait;
    private Semaphore interruptSem;
    private boolean PRINTR[];
    private Label PRINTR_LABELS[];
    private boolean isIRET;
    private boolean isRET;

    private boolean isStepping;
    private boolean stop;
    private boolean isStep;
    private boolean skipDecode;
    private int IR0;
    private int IR1;
    private int IR2;
    private int IR3;
    private int B;
    private boolean error;
    private boolean halted;
    private int addressingType;
    private DebugControlPanel dcp;
    private boolean working;
    private LinkedList<Semaphore> stopSemaphoreList;
    private Semaphore temp;

    public Execution( Register PC, DebugControlPanel dcp )
    {
        this.PC = PC;
        this.mem = Memory.get();
        this.start = new Semaphore(0);
        this.mutex = new Semaphore(1);
        this.breakpointWait = new Semaphore(0);
        stop = false;
        this.dcp = dcp;
        isStepping = false;
        error = false;
        working = false;
        stopSemaphoreList = new LinkedList<>();
        this.setName("CPU Execution Thread");
        temp = new Semaphore(0);
        interruptSem = new Semaphore(1);
        PRINTR = new boolean[4];
        PRINTR_LABELS = new Label[4];
    }

    public void setInterrupt( int entry ) {
        if ( entry < 0 || entry >= 4 )
            return;
        interruptSem.acquireUninterruptibly();
        PRINTR[entry] = true;
        interruptSem.release();
    }

    public void resetInterrupt( int entry ) {
        if ( entry < 0 || entry >= 4 )
            return;
        interruptSem.acquireUninterruptibly();
        PRINTR[entry] = true;
        interruptSem.release();
    }

    public boolean isActiveInterrupt( int entry ) {
        boolean b;
        interruptSem.acquireUninterruptibly();
        b = PRINTR[entry];
        interruptSem.release();
        return b;
    }

    public boolean toggleInterrupt( int entry ) {
        boolean b;
        interruptSem.acquireUninterruptibly();
        b = PRINTR[entry] = !PRINTR[entry];
        interruptSem.release();
        return b;
    }

    public void setPRINTRLabel(int entry, Label l) {
        PRINTR_LABELS[entry] = l;
    }

    @Override
    public synchronized void start() {
        ApplicationThread.addApplicationThread(this);
        super.start();
    }

    public int getIR0() { return IR0; }
    public int getIR1() { return IR1; }
    public int getIR2() { return IR2; }
    public int getIR3() { return IR3; }

    /**
     * First phase of instruction execution
     */
    private void fetchInstruction() {
        IR0 = mem.getByteFromAddress( PC.getValue() );
        if ( !Instructions.isValidInstruction((byte)IR0) ) {
            Utilities.runLater(() -> {
                String s = "Invalid instruction at address " + Utilities.getHexadecimalFormat(PC);
                dcp.terminateDebugSession( s );
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(Main.PROGRAM_NAME + " - Invalid instruction");
                alert.setContentText(s);
                alert.showAndWait();
            });
            return;
        };
        skipDecode = Instructions.isJumpInstruction((byte) IR0) || !Instructions.requiresAddressing((byte) IR0);
        PC.increment();
        if ( !Instructions.requiresAddressing( (byte)IR0 ) )
            return;

        IR1 = mem.getByteFromAddress( PC.getValue() );
        PC.increment();
        if ( Instructions.isJumpInstruction( (byte)IR0 ) ) {
            IR2 = mem.getByteFromAddress( PC.getValue() );
            PC.increment();
        } else {
            if (!Instructions.isValidAddressing((byte) IR1)) {
                Utilities.runLater(() -> {
                    String s = "Invalid addressing type at address " + Utilities.getHexadecimalFormat(PC);
                    dcp.terminateDebugSession(s);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(Main.PROGRAM_NAME + " - Invalid addressing type");
                    alert.setContentText(s);
                    alert.showAndWait();
                });
                return;
            } else {
                int addr = Instructions.getAddressingFromByte( (byte)IR1 );
                if ( addr == Instructions.IMMED || addr == Instructions.MEMDIR || addr == Instructions.MEMIND ) {
                    IR2 = mem.getByteFromAddress( PC.getValue() );
                    PC.increment();
                    IR3 = mem.getByteFromAddress( PC.getValue() );
                    PC.increment();
                } else if ( addr == Instructions.REGINDPOM ) {
                    IR2 = mem.getByteFromAddress( PC.getValue() );
                    PC.increment();
                };
            };
        };
    }

    private void decodeInstruction() throws InterruptedException {
        if ( skipDecode )
            return;
        int addr = Instructions.getAddressingFromByte( (byte)IR1 );
        int reg;
        Register r;
        addressingType = Instructions.getAddressingFromByte((byte) IR1);
        switch( addr )
        {
            case Instructions.REGDIR:
                B = dcp.group.getRegister( "r" + ( IR1 & 0b11111 ) ).getValue();
                break;

            case Instructions.REGIND:
                reg = dcp.group.getRegister( "r" + ( IR1 & 0b11111 ) ).getValue();
                B = mem.get2BytesFromAddress( reg );
                break;

            case Instructions.REGINDPOM:
                reg = dcp.group.getRegister( "r" + ( IR1 & 0b11111 ) ).getValue();
                B = mem.get2BytesFromAddress( reg + IR2 );
                break;

            case Instructions.PREINCR:
                r = dcp.group.getRegister( "r" + ( IR1 & 0b11111 ) );
                r.increment();
                r.increment();
                B = mem.get2BytesFromAddress( r.getValue() );
                break;

            case Instructions.POSTDECR:
                r = dcp.group.getRegister( "r" + ( IR1 & 0b11111 ) );
                reg = r.getValue();
                r.decrement();
                r.decrement();
                B = mem.get2BytesFromAddress( reg );
                break;

            case Instructions.IMMED:
                B = IR3 << 8 | IR2;
                break;

            case Instructions.MEMDIR:
//                if ( (IR3 << 8 | IR2) >= 0xFF00 )
//                    sleep(100);
                B = mem.get2BytesFromAddress( IR3 << 8 | IR2 );
                break;

            case Instructions.MEMIND:
                B = mem.get2BytesFromAddress( IR3 << 8 | IR2 );
                B = mem.get2BytesFromAddress( B );
                break;
        }
    }

    private void executeInstruction() throws InterruptedException {
        int regnum, addr, carry, val;
        Register r;
        byte b = (byte)IR0;
        switch( b ) {
            case Instructions.PUSH:
                dcp.SP.decrement();
                mem.writeByteAtAddress(dcp.SP.getValue(), (dcp.AX.getValue() >> 8) & 0xFF);
                dcp.stack.push( (dcp.AX.getValue() >> 8) & 0xFF );
                dcp.SP.decrement();
                mem.writeByteAtAddress(dcp.SP.getValue(), dcp.AX.getValue() & 0xFF);
                dcp.stack.push( dcp.AX.getValue() & 0xFF );
                break;

            case Instructions.POP:
                int value = mem.get2BytesFromAddress(dcp.SP.getValue());
                dcp.SP.increment();
                dcp.SP.increment();
                dcp.AX.setValue(value);
                dcp.stack.pop();
                dcp.stack.pop();
                break;

            case Instructions.RET:
                int retAddr = mem.get2BytesFromAddress(dcp.SP);
                dcp.PC.setValue(retAddr);
                dcp.SP.increment();
                dcp.SP.increment();
                dcp.stack.pop();
                dcp.stack.pop();
                isRET = true;
                break;

            case Instructions.IRET:
                this.isIRET = true;
                int a;
                a = mem.get2BytesFromAddress(dcp.SP.getValue());
                dcp.AX.setValue(a);
                dcp.SP.increment();
                dcp.SP.increment();
                dcp.stack.pop();
                dcp.stack.pop();

                a = mem.getByteFromAddress(dcp.SP.getValue());
                dcp.PSW.setValue(a);
                dcp.SP.increment();
                dcp.stack.pop();

                a = mem.get2BytesFromAddress(dcp.SP.getValue());
                dcp.PC.setValue(a);
                dcp.SP.increment();
                dcp.SP.increment();
                dcp.stack.pop();
                dcp.stack.pop();

                break;

            case Instructions.HALT:
                this.dcp.terminateDebugSession("Processor is halted");
                halted = true;
                break;

            case Instructions.INC:
                val = dcp.AX.getValue();
                if (val == 0xFFFF) {
                    dcp.PSW.setCarryBit();
                    dcp.PSW.setZeroBit();
                }
                ;
                dcp.AX.increment();
                break;

            case Instructions.DEC:
                dcp.AX.decrement();
                if (B == 0)
                    dcp.PSW.setZeroBit();
                break;

            case Instructions.LD:
                dcp.AX.setValue(B);
                if (B == 0)
                    dcp.PSW.setZeroBit();
                break;

            case Instructions.ST:
                switch (addressingType) {
                    case Instructions.REGDIR:
                    case Instructions.PREINCR:
                    case Instructions.POSTDECR:
                        regnum = IR1 & 0b11111;
                        Utilities.getRegisterByName("r" + regnum).setValue(dcp.AX.getValue());
                        break;

                    case Instructions.REGIND:
                        regnum = IR1 & 0b11111;
                        addr = Utilities.getRegisterByName("r" + regnum).getValue();
                        mem.write2BytesAtAddress(addr, dcp.AX);
                        break;

                    case Instructions.MEMDIR:
                        addr = IR3 << 8 | IR2;
//                        if ( addr >= 0xFF00 )
//                            sleep(70);
                        mem.write2BytesAtAddress(addr, dcp.AX);
                        break;

                    case Instructions.MEMIND:
                        addr = mem.get2BytesFromAddress(dcp.AX);
                        addr = mem.get2BytesFromAddress(addr);
                        mem.write2BytesAtAddress(addr, dcp.AX);
                        break;

                    case Instructions.REGINDPOM:
                        regnum = IR1 & 0b11111;
                        addr = mem.get2BytesFromAddress(Utilities.getRegisterByName("r" + regnum));
                        addr += IR2;
                        mem.write2BytesAtAddress(addr, dcp.AX);
                        break;
                }
                ;
                break;

            case Instructions.ADD:
                val = dcp.AX.getValue() + B;
                if ((val & 0x10000) != 0) {
                    dcp.PSW.setCarryBit();
                } else
                    dcp.PSW.resetCarryBit();

                dcp.AX.setValue(dcp.AX.getValue() + B);
                if (dcp.AX.getValue() == 0)
                    dcp.PSW.setZeroBit();
                else
                    dcp.PSW.resetZeroBit();
                break;

            case Instructions.AND:
                dcp.AX.setValue(dcp.AX.getValue() & B);
                if (dcp.AX.getValue() == 0)
                    dcp.PSW.setZeroBit();
                else
                    dcp.PSW.resetZeroBit();
                break;

            case Instructions.OR:
                dcp.AX.setValue(dcp.AX.getValue() | B);
                if (dcp.AX.getValue() == 0)
                    dcp.PSW.setZeroBit();
                else
                    dcp.PSW.resetZeroBit();
                break;

            case Instructions.NOT:
                dcp.AX.setValue(dcp.AX.getInverted());
                if (dcp.AX.getValue() == 0)
                    dcp.PSW.setZeroBit();
                else
                    dcp.PSW.resetZeroBit();
                break;

            case Instructions.XOR:
                dcp.AX.setValue(dcp.AX.getValue() ^ B);
                if (dcp.AX.getValue() == 0)
                    dcp.PSW.setZeroBit();
                else
                    dcp.PSW.resetZeroBit();

                break;

            case Instructions.SUB:
                val = dcp.AX.getValue() - B;
                dcp.AX.setValue( dcp.AX.getValue() - B );
                if ( dcp.AX.getValue() == 0 )
                    dcp.PSW.setZeroBit();
                else
                    dcp.PSW.resetZeroBit();

                if ( val < 0 )
                    dcp.PSW.setCarryBit();
                else
                    dcp.PSW.resetCarryBit();
                break;

            case Instructions.MUL:
                dcp.AX.setValue( dcp.AX.getValue() * B );
                if ( dcp.AX.getValue() == 0 )
                    dcp.PSW.setZeroBit();
                else
                    dcp.PSW.resetZeroBit();
                break;

            case Instructions.CMP:
                val = dcp.AX.getValue() - B;
                if ( val < 0 )
                    dcp.PSW.setCarryBit();
                else
                    dcp.PSW.resetCarryBit();

                if ( val == 0 )
                    dcp.PSW.setZeroBit();
                else
                    dcp.PSW.resetZeroBit();
                break;

            case Instructions.JMP:
                dcp.PC.setValue( IR2 << 8 | IR1 );
                break;

            case Instructions.CALL:
                dcp.SP.decrement();
                mem.writeByteAtAddress( dcp.SP.getValue(), dcp.PC.getByte(2) );
                dcp.stack.push( dcp.PC.getByte(2) );
                dcp.SP.decrement();
                mem.writeByteAtAddress( dcp.SP.getValue(), dcp.PC.getByte(1) );
                dcp.stack.push( dcp.PC.getByte(1) );

                dcp.PC.setValue( IR2 << 8 | IR1 );
                break;

            case Instructions.CLI:
                dcp.PSW.resetInterruptFlag();
                break;

            case Instructions.STI:
                dcp.PSW.setInterruptFlag();
                break;

            case Instructions.LDSP:
                dcp.AX.setValue( dcp.SP );
                break;

            case Instructions.STSP:
                dcp.SP.setValue( dcp.AX );
                break;

            case Instructions.LDIMR:
                dcp.AX.setValue( dcp.IMR );
                break;

            case Instructions.STIMR:
                dcp.IMR.setValue( dcp.AX );
                break;

            case Instructions.JGT:
                if ( !dcp.PSW.getBit( PSW.Z_BIT ) && !dcp.PSW.getBit( PSW.C_BIT ) )
                {
                    dcp.PC.setValue( IR2 << 8 | IR1 );
                };
                break;

            case Instructions.JGE:
                if ( dcp.PSW.getBit( PSW.Z_BIT ) || !dcp.PSW.getBit( PSW.C_BIT ) )
                {
                    dcp.PC.setValue( IR2 << 8 | IR1 );
                };
                break;

            case Instructions.JLT:
                if ( dcp.PSW.getBit( PSW.C_BIT ) )
                {
                    dcp.PC.setValue( IR2 << 8 | IR1 );
                };
                break;

            case Instructions.JLE:
                if ( dcp.PSW.getBit( PSW.Z_BIT ) || dcp.PSW.getBit( PSW.C_BIT ) )
                {
                    dcp.PC.setValue( IR2 << 8 | IR1 );
                };
                break;

            case Instructions.JEQ:
                if ( dcp.PSW.getBit( PSW.Z_BIT ) )
                {
                    dcp.PC.setValue( IR2 << 8 | IR1 );
                };
                break;

            case Instructions.JNEQ:
                if ( !dcp.PSW.getBit( PSW.Z_BIT ) )
                {
                    dcp.PC.setValue( IR2 << 8 | IR1 );
                };
                break;

            default:
                error = true;
                break;

        }
    }

    private void handleInterrupts() {
        int addr = 0;
        boolean occur = false;
        this.interruptSem.acquireUninterruptibly();
        for (int i=3; i>=0; i--) {
          if ( PRINTR[i] && dcp.IMR.getBit((char) i) ) {
              addr = i;
              occur = true;
              break;
          };
        };

        if ( occur && dcp.IMR.getBit((char) addr) && !isIRET && !isRET && dcp.PSW.getBit(PSW.I_BIT) ) {
            int x = addr;
            addr = dcp.IVTP.getValue() + addr*2;
            addr = mem.get2BytesFromAddress(addr);
            dcp.SP.decrement();
            mem.writeByteAtAddress( dcp.SP.getValue(), PC.getByte(2) );
            dcp.stack.push( PC.getByte(2) );

            dcp.SP.decrement();
            mem.writeByteAtAddress( dcp.SP.getValue(), PC.getByte(1) );
            dcp.stack.push( PC.getByte(1) );

            dcp.SP.decrement();
            mem.writeByteAtAddress( dcp.SP.getValue(), dcp.PSW.getByte(1) );
            dcp.stack.push( dcp.PSW.getByte(1) );

            dcp.SP.decrement();
            mem.writeByteAtAddress( dcp.SP.getValue(), dcp.AX.getByte(2) );
            dcp.stack.push( dcp.AX.getByte(2) );

            dcp.SP.decrement();
            mem.writeByteAtAddress( dcp.SP.getValue(), dcp.AX.getByte(1) );
            dcp.stack.push( dcp.AX.getByte(1) );

            PC.setValue( addr );
            PRINTR[x] = false;
            dcp.PSW.resetInterruptFlag();
            updatePRINTRLabel(x, false);
        }

        this.interruptSem.release();
    }

    private void updatePRINTRLabel( int entry, boolean value ) {
        if ( PRINTR_LABELS[entry] != null )
            Utilities.runLater( () -> { PRINTR_LABELS[entry].setText( value ? "1" : "0" ); });
    }

    public void prepareForStart() {
        start();
        start.release();
    }

    public void stopExecution( Semaphore s ) {
        try {
            mutex.acquire();
            if ( !working ) {
                mutex.release();
                return;
            }
            working = false;
            isStepping = false;
            isStep = true;
            if ( s != null )
                stopSemaphoreList.add(s);
            mutex.release();
        } catch( InterruptedException e ) {}
        stop = true;
        start.release();
        super.interrupt();
        if ( s != null ) {
            try {
                s.acquire();
            } catch (InterruptedException e) {}
            stopSemaphoreList.remove(s);
        };
    }

    public void stopExecution() {
        stopExecution(null);
    }

    @Override
    public void interrupt() {
        stop = true;
        start.release();
        super.interrupt();
    }

    public void step() {
        try {
            mutex.acquire();
            isStep = true;
            mutex.release();
            breakpointWait.release();
        } catch (InterruptedException e) { interrupt(); }
    }

    public void continueExecution() {
        try {
            mutex.acquire();
            isStep = false;
            mutex.release();
            breakpointWait.release();
        } catch (InterruptedException e) { interrupt(); }
    }

    private int getLineOutOfPC()
    {
        Integer i = DebugInfo.getInstance().getInstructionPCWithLines().get(PC.getValue());
        return i == null ? -1 : i.intValue();
    }

    private boolean isBreakpointLine()
    {
        int ln = getLineOutOfPC();
        return ln == -1 ? false : Breakpoints.get().isBraekpointLine( ln );
    }

    public boolean isStepping()
    {
        boolean v = false;
        try {
            mutex.acquire();
            v = isStepping;
            mutex.release();
        } catch( InterruptedException e ) {}
        return v;
    }

    public boolean isWorking()
    {
        boolean b = false;
        try {
            mutex.acquire();
            b = working;
            mutex.release();
        } catch ( InterruptedException e ) {}
        return b;
    }

    @Override
    public void run() {

        boolean first;
        boolean isstep = false;

        try {
            while ( !stop ) {

                mutex.acquireUninterruptibly();
                working = false;
                mutex.release();
                start.acquire();
                if ( stop || interrupted() )
                    return;
                first = true;
                halted = false;
                error = false;

                try {
                    mutex.acquireUninterruptibly();
                    working = true;
                    mutex.release();
                    while ( !interrupted() ) {

                        if ( error || halted || stop )
                            break;

                        if ( isBreakpointLine() || isstep || first ) {
                            mutex.acquireUninterruptibly();
                            isStep = false;
                            isStepping = false;

                            // Highlight new line.
                            if ( !first ) {
                                Integer i = DebugInfo.getInstance().getInstructionPCWithLines().get( PC.getValue() );
                                Utilities.runLater(() -> {
                                    dcp.debugSection.ResetBtn.setDisable(false);
                                    dcp.debugSection.ContinueBtn.setDisable(false);
                                    dcp.debugSection.StepBtn.setDisable(false);
                                });
                                if ( i != null ) {
                                    Utilities.paintLine( i.intValue(), isBreakpointLine() );
                                } else
                                    Utilities.clearLinePainting();
                            };

                            mutex.release();
                            breakpointWait.acquire();
                            mutex.acquireUninterruptibly();
                            isStepping = true;
                            mutex.release();
                        };

                        // Highlight new line.
                        if ( !first ) {
                            Integer i = DebugInfo.getInstance().getInstructionPCWithLines().get( PC.getValue() );
                            if ( i != null ) {
                                Utilities.paintLine( i.intValue(), isBreakpointLine() );
                            } else
                                Utilities.clearLinePainting();
                        };

                        if ( interrupted() )
                            break;

                        // Do the work.
                        isIRET = false;
                        isRET = false;
                        skipDecode = false;
                        fetchInstruction();
                        if ( interrupted() )
                            break;
                        decodeInstruction();
                        if ( interrupted() )
                            break;
                        executeInstruction();
                        if ( interrupted() || halted )
                            break;
                        handleInterrupts();
                        if ( interrupted() || halted )
                            break;

                        sleep( 30 );

                        mutex.acquireUninterruptibly();
                        isstep = isStep;
                        mutex.release();
                        first = false;
                    }
                } catch (InterruptedException e) {}

                mutex.acquireUninterruptibly();
                if ( stopSemaphoreList.size() > 0 ) {
                    stopSemaphoreList.forEach((sem) -> {
                        sem.release();
                    });
                }
                working = false;
                mutex.release();
            }
        } catch( InterruptedException e ) {}
    }
}
