;===========================================================
; Unwind info for different situations

; Directive to create section holding function unwind information
.cfi_sections .debug_frame

; Use at the very beginning of a function, right after a label, to emit
; an entry in .debug_frame.
; Function here means any consecutive part of the code where unwind info
; can be reasonably defined.
; Do not forget to close the function with em.cfi_end.
.macro em.cfi_start
    .cfi_startproc
.endmacro

; Use at the very end of a function to close the entry in .debug_frame.
.macro em.cfi_end
    .cfi_endproc
.endmacro

; Use at the beginning of C functions before
; allocating the ISAL stack frame
.macro em.isal.cfi_init
    .cfi_def_cfa    2, 0    ; Dummy CFA, MSP+0
    .cfi_register   1, 5    ; Caller PC in RAR
    .cfi_same_value 2       ; Caller MSP is in place
    .cfi_same_value 8       ; Caller LMP is in place
.endmacro

; Use after allocating the ISAL stack frame
.macro em.isal.cfi_alloc_done
    .cfi_def_cfa    8, 0        ; CFA, LMP+0
    .cfi_offset     1, -3 * 4   ; Caller PC at CFA + (-3 * ptrSize)
    .cfi_offset     8, -2 * 4   ; Caller LMP at CFA + (-2 * ptrSize)
    .cfi_undefined  2           ; Caller MSP undefined
.endmacro

; Use after deallocating the ISAL stack frame
.macro em.isal.cfi_dealloc_done
    .cfi_def_cfa        2, 0    ; Dummy CFA, MSP+0
    .cfi_restore        1       ; Caller PC restored
    .cfi_restore        2       ; Caller MSP restored
    .cfi_restore        8       ; Caller LMP restored
    .cfi_return_column  5       ; Return address in RAR
.endmacro

; Use in ISAC context, where cannot unwind
.macro em.isac.cfi
    .cfi_def_cfa    2, 0    ; Dummy CFA, MSP+0
    .cfi_undefined  1       ; Caller PC undefined
    .cfi_undefined  2       ; Caller MSP undefined
    .cfi_undefined  8       ; Caller LMP undefined
.endmacro

; Use in ISAC context, where return address is in RAR
.macro em.isac.cfi_return_rar
    .cfi_def_cfa        2, 0    ; Dummy CFA, MSP+0
    .cfi_register       1, 5    ; Caller PC in RAR
    .cfi_undefined      2       ; Caller MSP undefined
    .cfi_undefined      8       ; Caller LMP undefined
    .cfi_return_column  5       ; Return address in RAR
.endmacro

; Use in emulation routines when return address is in ERAR
; NOTE: The microcoded uses the first bit in ERAR as a flag
; indicating Java execution mode. The ISAL debugger sees a
; high address in ERAR during bytecode emulation. The debugger
; needs to recognize the flag bit and simply unwind the Java frame.
; When a non-Java frame, for which the flag in ERAR is not set,
; is reached, CFI should be available and used from the ELF
; executable.
.macro em.emul.cfi
    .cfi_def_cfa        2, 0    ; Dummy CFA, MSP+0
    .cfi_register       1, 7    ; Caller PC in ERAR
    .cfi_undefined      2       ; Caller MSP in undefined
    .cfi_undefined      8       ; Caller LMP is undefined
    .cfi_return_column  7       ; Return address in ERAR
.endmacro

;===========================================================

; Save the current bytecode pc at (fmp + FRAME_THROWPC).
; Use this macro at the beginning of each emulation routine
; after reading in all immediate arguments from the bytecode
; stream. Meaning that ERAR points to the beginning of the
; subsequent bytecode. This macro will store bytecode pc for the
; last byte position of the current emulated bytecode.
.macro em.update.pc
    ;es: ...

    ; Get absolute throw-PC.
    ; The highest bit in ERAR is used to indicate Java mode for the microcode.
    ; It is to be reset for having the actual PC value.
    ; Also decrement the addres to point to the last byte of the throwing bytecode
    ; because the actual return address may be the beginning of a new try statement.
    c.ld.erar
    c.ldi.i 0x7fffffff
    c.and
    c.addi  -1
    ; es: ..., throw-pc

    ; Get base PC for current method
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_EXEC_CODE
    c.ld.i
    ; es: ..., throw-pc, method-pc

    ; Calculate bytecode pc as (throw-pc - method-pc)
    c.sub
    ; es: ..., pc

    c.st.i.fmp  FRAME_THROWPC
    ; es: ...
.endmacro


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
; NOTE: Standard ISAL unwinding stops at this frame as the allocation sets
; the normal return address slot to 0 in the frame.
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

;===========================================================

; Decomposes the double word value from l#0 into i#1 and i#0.
; i#1 will hold the high-significant half and i#0 the low one.
.macro em.decomp_l0_to_i1_i0
    trunc.l.i i#0 l#0
    move.b.i8 b#4 32
    lshr.l l#0 l#0 b#4
    trunc.l.i i#1 l#0
.endmacro

;===========================================================

; Composes a double word value from i#1 and i#0 into l#0.
; i#0 should have the high-signifcant hald and i#1 the low one.
; NOTE: The operation clobbers l#1.
; NOTE: Asymmetry with em.decomp_l0_to_i1_i0 is intentional.
.macro em.comp_i0_i1_to_l0_xl1
    zext.i.l    l#0 i#0
    zext.i.l    l#1 i#1
    move.b.i8   b#4 32
    shl.l       l#0 l#0 b#4
    add.l       l#0 l#0 l#1
.endmacro

;=============================================================
; Loads one byte from memory into the evaluation stack via
; ERAR and also steps ERAR 
.macro em.load_byte_from_erar
    c.ld.erar
    c.dup
    c.addi  1
    c.st.erar
    c.ld.b          ; Loads sign-extended byte into the top word
    c.andi    0xff  ; Clear potential sign extension
.endmacro

;=============================================================
; Loads two bytecode from memory into the evaluation stack via
; ERAR and also steps ERAR by 2
.macro em.load_short_from_erar
    c.ld.erar
    c.dup
    c.addi  2
    c.st.erar
    c.ld.s          ; Loads sign-extended short into the top word
    c.i2c           ; Clear potential sign extension
.endmacro
