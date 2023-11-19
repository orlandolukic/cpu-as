package debug.registers;

import debug.DebugInfo;

public class RegisterGroup {

    @FunctionalInterface
    public interface RegisterGroupEach
    {
        void each(Register r);
    }

    protected Register[] registers;

    public RegisterGroup( Register[] registers )
    {
        this.registers = registers;
    }

    public Register[] getRegisters()
    {
        return registers;
    }

    public Register getRegister( String name ) {
        for (int i=0; i<registers.length; i++) {
          if ( registers[i].getName().equals(name) )
              return registers[i];
        };
        return null;
    }

    public void forEachRegister( RegisterGroupEach interf )
    {
        for (int i=0; i<registers.length; i++)
            interf.each(registers[i]);
    }

    public void resetAllRegisters()
    {
        for (int i=0; i<registers.length; i++)
            registers[i].reset();
    }

    public void updateLabels()
    {
        for (int i=0; i<registers.length; i++)
            registers[i].updateLabels();
    }

    public void initialize() {
        for (int i=0; i<registers.length; i++) {
            registers[i].initialize();
            registers[i].updateLabels();
        }
    }



}
