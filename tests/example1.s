@
@ Example 1
@
@ Enter N on keyboard and press ENTER.
@ After that, enter N numbers and after each and every number also press ENTER.
@
@ On screen it will be displayed sum of entered elements.
@
@ ======================================================================================
@
@ To start program click first on 'Assemble' and than start debugging by
@ clicking 'Debug' and entering start line to be 21. After that, don't forget to open
@ Peripheral window.
@ Click on continue(F7).
@

.set sp 0xff00

.section .text

    CALL initKeyboard
    CALL initScreenDecimal
    STI

check:
    LD r2
    CMP #1
    JEQ terminate
    JMP check

terminate:
    LD r5                    				@ r10 -> sum of numbers
    PUSH
    CALL printToScreen
    POP
    HALT

keyboardHandler:
    LD 0xFF14
    ST r0                      @ r0 -> read number from keyboard

    LD r1
    CMP #0
    JEQ initTransfer
    JNEQ calculate

initTransfer:
    LD #1
    ST r1                       @ r1 -> is repetition number set?
    LD #0
    ST r3                       @ r3 -> number of entered numbers
    LD r0
    ST r4                       @ r4 -> repetition number
    CMP #50
    JGT beforeTerminate
    JMP defaultHandler

@ ==========================================
@ Calculations
@ ==========================================
calculate:
    LD r0
    PUSH
    CALL printToScreen
    CALL printEnterToScreen
    POP
    ADD r5
    ST r5                       @ sum = sum + number;

    LD r3
    INC
    ST r3                       @ numbers++;

cmpr4:
    CMP r4                      @ if ( numbers >= repetition )
    JGE beforeTerminate         @    end_register = 1;
    JLT defaultHandler

beforeTerminate:
    LD #1                       @ r2 -> end register
    ST r2

defaultHandler:
    IRET

timerInterrupt:
	LD r7
	INC
	ST r7
	IRET


@ =========================================================
@   Screen
@ =========================================================
initScreenChar:
    LD #0b1100
    ST 0xFF02
    RET

@ function
initScreenDecimal:
    LD #0b0100_1100
    ST 0xFF02
    RET

@ function
printSumMask:
    LD #sumMask
    ST r0

whilePrintSumMask:
    LD [r0]
    CMP #0
    JEQ retFromPrintSumMask
    PUSH
    CALL printToScreen
    POP
    LD r0
    INC
    ST r0
    JMP whilePrintSumMask
retFromPrintSumMask:
    RET

@ =========================================================
@   Keyboard
@ =========================================================
initKeyboard:
    LD #0b1011_0110
    ST 0xFF12
    RET

printToScreen:
    PUSH
    LD r0
    PUSH

    LDSP
    ADD #6
    ST r0

    LD [r0]
    ST 0xFF04
    LD #0b1_0000
    OR 0xFF02
    ST 0xFF02

    POP
    ST r0
    POP
    RET

printEnterToScreen:
wait:
    LD 0xFF00
    AND #0b1_0000
    CMP #0
    JNEQ wait
    LD 0xFF02
    OR #0b1000_0000
    ST 0xFF02
wait1:
	 LD 0xFF00
    AND #0b1_0000
    CMP #0
    JNEQ wait1
    RET

.section .data
sumMask:
.asciz "Sum is:\n"

@ Interrupt vector table
@ ===================================================
.section .iv_table
@ ===================================================
.word keyboardHandler
.word timerInterrupt
.rept 3
    .word defaultHandler
.endr






















