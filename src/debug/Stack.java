package debug;

import controller.StackScreenController;
import debug.registers.SP;

import java.util.LinkedList;

public class Stack {

    private SP stackPointer;
    private LinkedList<Integer> stack;

    public Stack( SP sp ) {
        this.stackPointer = sp;
        reset();
    }

    public int pop() {
        if ( stack.size() == 0 )
            return 0;
        Integer i = stack.get(0);
        stack.removeFirst();
        if (StackScreenController.instance != null) {
            StackScreenController.Stack.DrawStack();
        };
        return i.intValue();
    }

    public void push( int b ) {
        stack.addFirst(b);
        if (StackScreenController.instance != null) {
            StackScreenController.Stack.DrawStack();
        };
    }

    public int elements() {
        return stack.size();
    }

    public void reset() {
        this.stack = new LinkedList<>();
    }

    public int getStackPointer() {
        return stackPointer.getValue();
    }
}
