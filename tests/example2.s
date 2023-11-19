@
@ Example 2
@
@ Sum of N element array of integers given in .data section. 
@ Numbers are 10,20,30,40,50.
@
@ Result of this calculation is printed on screen peripheral.
@
@ ======================================================================================
@
@ To start program click first on 'Assemble' and than start debugging by
@ clicking 'Debug' and entering start line to be 23. After that, don't forget to open
@ Peripheral window.
@ Click on continue(F7).
@

.set sp 0xff00
.set ax 0x1000

.section .text
.skip 256

    CALL initScreenDecimal

    LD #niz
    ST r0               @ r0 -> array address
    LD N
    ST r1               @ r1 -> number of array elements
                        @ r2 -> number of processed elements

while:
    LD [r0]
    ADD r5
    ST r5
    LD ++[r0]             @ Increment r0 by 2;
    LD r2
    INC
    ST r2
    CMP r1
    JEQ terminate
    JNEQ while

terminate:
    LD r5                    @ r5 -> sum of numbers
    PUSH
    CALL printToScreen
    POP
    HALT

defaultHandler:
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
    RET

.section .data
niz:
.word 10,20,30,40,50
N:
.word 5			@ N numbers

@ Interrupt vector table
@ ===================================================
.section .iv_table
@ ===================================================
.rept 4
    .word defaultHandler
.endr























