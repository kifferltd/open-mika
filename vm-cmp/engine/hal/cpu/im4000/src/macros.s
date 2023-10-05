;===========================================================

.macro em.isal.alloc.nlsf.pop.es_4
    pop.es.w i#3
    em.isal.alloc.nlsf.pop.es_3
.endmacro

.macro em.isal.alloc.nlsf.pop.es_3
    pop.es.w i#2
    em.isal.alloc.nlsf.pop.es_2
.endmacro

.macro em.isal.alloc.nlsf.pop.es_2
    pop.es.w i#1
    em.isal.alloc.nlsf.pop.es_1
.endmacro

.macro em.isal.alloc.nlsf.pop.es_1
    pop.es.w i#0
    em.isal.alloc.nlsf.pop.es_0
.endmacro

.macro em.isal.alloc.nlsf.pop.es_0
.endmacro

; Allocate a complete ISAL frame on top of whatever is in the stacks.
; Also popping values from the evaluation stack into i#0... registers.
; NOTE: The topmost value is moved to the register with the highest index
; and the last popped value to i#0.
.macro em.isal.alloc.nlsf narg
        irs.off          ; disable interrupts
        c.ldi.b 0       ; push 0 to the evaluation stack as number of 8-byte slots
; copied from m.alloc.nlih.i4
        c.ld.lmp        ; push old lmp at evaluation stack
        c.swap          ; get number of 8-byte slots to top
        c.ldi.b 1       ; push 1 to the evaluation stack
        c.shl           ; get number of locals (4-byte slots)
        c.addi 1        ; add 1 reserved slot
        c.aml           ; push offset to old lmp,
                        ; allocate a reserved slot and new locals storage at execution stack,
                        ; push offset to old msp,
                        ; and save pointer in lmp
        c.push.es       ; push old lmp at execution stack
                        ; Save 0 where RAR is usually saved so the debugger and unwind code knows that this is the end of the stack
        c.ldi.b 0       ; push 0 to the evaluation stack
        c.push.es       ; push 0 from the evaluation stack to the execution stack
        push.rar        ; push rar at execution stack
        push.ear        ; push ear at execution stack
        push.iasp       ; push iasp at execution stack
        ld.oasp ; load oasp
        st.iasp ; store it in iasp
        adrs.i8 16      ; allocate 16 int regs

; move parameter(s) to i#0... registers from evaluation stack
        em.isal.alloc.nlsf.pop.es_\narg

        c.ld.erar       ; Save ERAR to evaluation stack
        push.es.lrcb ; Hide away evaluation stack

        check.lrcb      ; check precondition for adls.i8
        adls.i8 4       ; allocate 4 long regs
        irs.on          ; re-enable interrupts
.endmacro

;------------------------------------------------------------

.macro em.isal.dealloc.nlsf.push.es_2
    push.es.w i#1
    em.isal.dealloc.nlsf.push.es_1
.endmacro

.macro em.isal.dealloc.nlsf.push.es_1
    push.es.w i#0
    em.isal.dealloc.nlsf.push.es_0
.endmacro

.macro em.isal.dealloc.nlsf.push.es_0
.endmacro

; Deallocate a complete ISAL frame and reveal the stacks beneath.
; Also pushing values from i#0... registers onto the evaluation stack.
; NOTE: The value from the register with the highest index is pushed first
; and the value from i#0 is going to be at the top of the evaluation stack.
.macro em.isal.dealloc.nlsf narg
        irs.off          ; disable interrupts
; copied from m.dealloc.nlih
        c.ld.lmp        ; push lmp at evaluation stack
        c.addi -(6*4) ; offset lmp with 6 int-slots
                                                                ; this ignores the reserved stack slot
        c.st.msp        ; restore original msp
        check.lrcb      ; check precondition for adls.i8
        adls.i8 -4      ; deallocate 4 long regs

        pop.es.lrcb   ; Reveal evaluation stack
        c.st.erar        ; Restore ERAR

; move parameter(s) from i#0... registers to evaluation stack
        em.isal.dealloc.nlsf.push.es_\narg

        adrs.i8 -16     ; deallocate 16 int regs
        ld.iasp ; load iasp
        st.oasp ; store it in oasp
        pop.iasp        ; pop iasp from execution stack
        pop.ear ; pop ear from execution stack
        pop.rar ; pop rar from execution stack
        c.pop.es        ; pop debugger end of stack symbol from execution stack
        c.pop.es        ; pop old lmp from execution stack
        c.drop2         ; ignore old lmp and debugger end of stack symbol
        c.dml           ; revert c.aml

        irs.on          ; re-enable interrupts
.endmacro

;===========================================================

.macro em.push.es_2
    irs.off
    c.push.es
    c.push.es
    irs.on
.endmacro

.macro em.push.es_1
    c.ldi.b 0
    em.push.es_2
.endmacro

; Push a number of words to the memory stack from the ISAC evaluation stack.
; Since MSP needs to be 8-aligned, words are pushed in a critical section.
; In case of an odd number of words, a leading padding word is pushed to memory.
.macro em.push.es nword
    em.push.es_\nword
.endmacro

;===========================================================

.macro em.pop.es_2
    irs.off
    c.pop.es
    c.pop.es
    irs.on
.endmacro

.macro em.pop.es_1
    em.pop.es_2
    c.drop
.endmacro

; Pop a number of words from the memory stack and push them to the ISAC evaluation stack.
; Since MSP needs to be 8-aligned, words are popped in a critical section.
; In case of an odd number of words, a trailing padding word is popped from memory
; (also dropped from the evaluation stack).
.macro em.pop.es nword
    em.pop.es_\nword
.endmacro
