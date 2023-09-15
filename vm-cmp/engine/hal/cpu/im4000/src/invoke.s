
.include "offsets.s"

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
; C syntax:     void activate_frame(w_method method, int narg, void* args)
;
;===========================================================
.global activate_frame
activate_frame:
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Start setup a Java stack frame in the memory stack

; Disable interrupts while pushing values to the memory
; That is to ensure 8-byte alignment of MSP
    irs.off

; Current (caller in Java) FMP. Presumably 0 when invoking a Java method the first time.
    c.push.fmp

; ERAR (for java return) set to activate_frame_return for cleanup
    c.ldi.i    activate_frame_return
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
    push.es.w   i#12
    c.st.i.fmp  FRAME_METHOD

; NOTE: CONSTANTPOOL and SYNCOBJECT remain zero in FRAME
; but was handled here in legacy code.

; Push locals count onto the evaluation stack
    copy.w      i#1 i#12
    add.i.i8    i#1 i#1 METHOD_EXEC_LOCAL_I
    load.h      s#2 i#1
    push.es.h   s#2         ; es: ..., locals_i

; Allocate locals
    c.callw     allocate_locals

;; Jump to java_method1 - for testing only
;    c.ldi.i     java_method1
;    .short      0xf9c2      ; c.jump.java

; Jump to method code
    copy.w      i#3 i#12
    add.i.i8    i#3 i#3 METHOD_EXEC_CODE
    load.w      i#4 i#3
    push.es.w   i#4         ; es: ..., method_code
    .short      0xf9c2      ; c.jump.java

; Java code returns here
activate_frame_return:
    ; Evaluation stack should be empty here
    check.lrcb

    ; Cleanup ISAL frame and return
    dealloc.nlsf
    ret
;===========================================================
