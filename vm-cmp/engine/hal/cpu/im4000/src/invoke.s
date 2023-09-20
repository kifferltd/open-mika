
.include "offsets.s"
.include "macros.s"

; !!! Use ISAC only by default as parts of this file are called from
; emulation code without an ISAL frame. The only exception is
; activate_frame, which is called in ISAL context as a C function.

;===========================================================
;
; Allocate locals and move args from evaluation stack to locals
;
; stack:  ..., arg0, [arg1, ... argn-1,] narg, maxLocals
;    ==>  ...,
;
;===========================================================
allocate_locals:
; Check if only embedded locals needed
    c.dup
    c.addi    -8
    c.br.c    allocate_loc_10    ; also memory locals ?

    c.addi    8                       ; adjust number
    c.ldi.b 0
    c.aml                           ; allocate empty memory stack
    c.jumps allocate_loc_20

; Allocate memory locals
allocate_loc_10:
    ; Allocate even number of slots to ensure 8-byte alignment of MSP
    c.addi 1
    c.andi 0xFE
    c.aml                           ; allocate memory stack
    c.ldi.b 8

; Allocate embedded locals
allocate_loc_20:
    c.dup
    c.als                           ; allocate embedded stack
    c.st.i.fmp  FRAME_LS_COUNT

; Move the arguments to locals
    c.mstvh

; Save current esp, lsp and lmp
    c.flld.esp
    c.st.i.fmp  FRAME_ESP

    c.flld.lsp
    c.st.i.fmp  FRAME_LSP

    c.ld.lmp
    c.st.i.fmp  FRAME_LMP

    c.ret

;===========================================================
java_method1:

    .byte   0xbb            ; new
    .byte   0               ; indexbyte1
    .byte   47              ; indexbyte2

    .byte   0xb1            ; return

;===========================================================
; activate_frame
;
; C syntax: void activate_frame(w_method method, int narg, void* args)
;
;===========================================================
.global activate_frame
activate_frame:
; Called in ISAL context, can use registers.
    move.i.i32 i#11 0
    alloc.nlsf i#11

; i#12 contains parameter 'method'
; i#13 contains parameter 'narg'
; i#14 contains parameter 'args'

    ; Evaluation stack should be empty here
    check.lrcb

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Copy the arguments to the evaluation stack
    copy.w      i#1 i#13    ; i#1 = narg
    br.cmp.i.i8.eq   activate_frame_10 i#1 0         ; Any arguments?

activate_frame_5:
    load.w      i#2 i#14            ; Next arg to i#2
    push.es.w   i#2

    add.upd.i.i4 i#14 0x4
    sub.upd.i.i4 i#1 0x1
    br.cmp.i.i8.ne activate_frame_5 i#1 0         ; More arguments?

; Push narg onto the evaluation stack
activate_frame_10:
    push.es.w   i#13

; Push method onto the evaluation stack
    push.es.w   i#12

; Push return address onto the evaluation stack
    c.ldi.i     activate_frame_return

; Make sure FMP is 0 when invoking Java method from ISAL
    c.ldi.b     0
    c.st.fmp

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; activate_frame() falls through to _emul_allocate_frame.
; !!! Continue with ISAC as can be called from emulation routines.
;===========================================================
;
; Set up a Java stack frame in the memory stack and continue with
; Java execution.
;
; stack:    ..., [arg0, [arg1...]], narg, method, return_address
;        => ...,
;
;===========================================================
.global _emul_allocate_frame
_emul_allocate_frame:

; Disable interrupts while pushing values to the memory
; That is to ensure 8-byte alignment of MSP
    irs.off

; Current (caller) FMP.
    c.push.fmp

; ERAR (for java return) set to return_address
    c.push.es

; Re-enable interrupts, should be always safe to do it here
    irs.on

; Allocate the rest of FRAME
    c.ldi.b SIZEOF_FRAME - 2
    c.ams

; Zero the allocated area
    c.ld.msp
    c.ldi.b (SIZEOF_FRAME - 2) * 4
    c.ldi.b 0
    c.fill

; Set FMP to point to the frame
    c.ld.msp
    c.st.fmp

; Store method pointer in the frame
    c.dup
    c.st.i.fmp  FRAME_METHOD

; NOTE: CONSTANTPOOL and SYNCOBJECT remain zero in FRAME
; but was handled here in legacy code.

; Push locals count onto the evaluation stack
; es: ..., [arg0, [arg1...]], narg, method
    c.addi METHOD_EXEC_LOCAL_I
    c.ld.s
; es: ..., [arg0, [arg1...]], narg, locals_i

; Allocate locals
    c.callw     allocate_locals

;; Jump to java_method1 - for testing only
;    c.ldi.i     java_method1
;    .short      0xf9c2      ; c.jump.java

; Jump to method code
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_EXEC_CODE
    c.ld.i      ; es: ..., method_code
    .short      0xf9c2      ; c.jump.java

;===========================================================
; Java code returns here when invoked via activate_frame()
; Continue in ISAL context.
activate_frame_return:
    ; Evaluation stack should be empty here
    check.lrcb

    ; Cleanup ISAL frame and return
    dealloc.nlsf
    ret
;===========================================================

;===========================================================
;
; Remove top Java stack frame from the memory stack and restore
; registers. Do not touch the evaluation stack where return
; values should be already prepared as needed.
;
; stack:    ... => ...
;
;===========================================================
.global _emul_return
_emul_return:
; NOTE: Legacy code checked eval stack level, memory stack level, and unlocked synchronized objects here.

; Deallocate locals on the memory stack
    c.dml

; Disable interrupts while popping out values from the memory
; That is to ensure 8-byte alignment of MSP
    irs.off

; Deallocate not needed frame entries
    c.ldi.b	    SIZEOF_FRAME - 3
    c.dms

; Deallocate locals on the locals stack
    c.pop.es        ; stack: ..., FRAME_LS_COUNT
    c.dls

; Recover pointers from the rest of the frame and return
    c.pop.es        ; stack: ..., FRAME_RETADDRESS
    c.st.erar
    c.pop.fmp

; Re-enable interrupts, should be always safe to do it here
    irs.on

    c.rete
;===========================================================

;===========================================================
;
; Throw the object from the top of the evaluation stack.
;
; stack:    ..., objectref => [empty], objectref
;
;===========================================================
.global _emul_throw
_emul_throw:
    ; Get current frame
    c.ld.fmp

    ; If no Java frame (FMP==0), manage uncaught exception
    c.addi  0
    c.br.z  _throw_uncaught
    ; es: ..., objectref, frame

    ; Keep a copy of objectref for potential future use
    c.over  ; es: ..., objectref, frame, objectref

    ; Check handler in current frame
    em.isal.alloc.nlsf 2
    ; FIXME: Prepare parameters and call Mika function to find handler
    errorpoint
    ;move.i.i32  i#2 _findHandler_
    ;call        i#2
    em.isal.dealloc.nlsf 1
    ; es: ..., objectref, handler

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; If no handler, need to remove top frame and check again
    c.addi  0
    c.br.z  _throw_unwind

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; Matching handler found, execute it.
    ; FIXME
    errorpoint

    c.rete

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
_throw_unwind:
; es: ..., objectref, 0 (handler)
    c.drop

    ; FIXME...
    errorpoint

    ; Remove top frame

    ; When unwinding, there are only ISAJ frames
    ; in the execution stack and the evaluation stack is continous.
    ; That is because emulation routines
    ; (including those for invoke*) deallocate their ISAL frame
    ; before continuing with ISAJ execution.

    ; The evaluation stack is supposed to be empty anyway?
    ; The compiler does not keep any data in it between operations...
    ; Might add check.lrcb at the appropriate place to check that.

    ; Check frame...
    ; es: ..., objectref
    c.jumpw _emul_throw

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
_throw_uncaught:
; es: ..., objectref, 0 (frame)
    c.drop

    ; FIXME: Uncaught exception
    errorpoint

;===========================================================
