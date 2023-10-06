
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
; C syntax: void activate_frame(w_method method, int narg, void* args, void* ret)
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
; i#15 contains parameter 'ret'

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

; Disable interrupts while pushing values to the memory
; That is to ensure 8-byte alignment of MSP
    irs.off

; Save current FMP, which is valid in the case of nested activation.
    c.push.fmp

; Push dummy word for alignment
    c.ldi.b     0
    c.push.es

; Re-enable interrupts, should be always safe to do it here
    irs.on

; Make sure FMP is 0 when invoking Java method from ISAL
    c.ldi.b     0
    c.st.fmp

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; activate_frame() falls through to _emul_allocate_frame.
; !!! Continue with ISAC as can be called from emulation routines.
;===========================================================
;
; Prepare a stack frame and execute method.
;
; stack:    ..., [arg0, [arg1...]], narg, method, return_address
;        => ...,
;
;===========================================================
.global _emul_allocate_frame
_emul_allocate_frame:

; Check whether native method
    c.over
    c.addi  METHOD_FLAGS
    c.ld.i
    ; TODO: Take flag value from a proper definition
    c.ldi.i 0x00000100  ; ACC_NATIVE
    c.and
    c.drop
    c.br.nz activate_frame_native

; Non-native methods fall through
;===========================================================
; Set up a Java stack frame in the memory stack and continue with
; Java execution.

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
; Call a native method
activate_frame_native:
    ; FIXME
    errorpoint

    ; Adapt native dispatchers to handle arguments from the evaluation stack, maybe also take im4000_frame instead of w_Frame
    ; Allocate ISAL frame
    ; Prepare whatever is needed for the dispatcher
    ; Call native method via dispatcher: method->exec.dispatcher(caller, method);
    ; Native method returns
    ;  - Check if exception is left in the thread; go to throw if so
    ;  - Do something with the return value and clean up
    ; Deallocate ISAL frame
    ; Return somewhere appropriate...

;===========================================================
; Method returns here when invoked via activate_frame()
; Continue in ISAL context and take care of return value in
; the ISAC evaluation stack if any.
activate_frame_return:
    ; Parameters of activate_frame() are still in place:
    ; i#12 = method
    ; i#15 = ret

    move.i.i8       i#11 METHOD_EXEC_RETURN_I
    add.upd.i       i#12 i#11
    load.h          s#0 i#12
    br.cmp.s.i8.eq  activate_frame_return_done s#0 0

    move.s.i8       s#1 -1
    add.upd.s       s#0 s#1
    br.cmp.s.i8.eq  activate_frame_return_single s#0 0

    ; A double-word value is returned
    pop.es.w    i#1 ; low-significant half
    pop.es.w    i#0 ; high-significant half

    ; Ignore the value if no ret pointer is provided
    br.cmp.i.i8.eq  activate_frame_return_done i#15 0

    em.comp_i0_i1_to_l0_xl1
    store.d     i#15 l#0

    br.i8   activate_frame_return_done

activate_frame_return_single:
    ; A single-word value is returned
    pop.es.w    i#0

    ; Ignore the value if no ret pointer is provided
    br.cmp.i.i8.eq  activate_frame_return_done i#15 0

    ; Store the value
    store.w     i#15 i#0
    ; Falling through to epilogue...

activate_frame_return_done:
    ; Evaluation stack should be empty here
    check.lrcb

    ; Disable interrupts while popping out values from the memory
    ; That is to ensure 8-byte alignment of MSP
    irs.off

    ; Remove dummy word
    c.pop.es        ; es: 0
    c.drop

    ; Restore original FMP
    c.pop.fmp

    ; Re-enable interrupts, should be always safe to do it here
    irs.on

    ; Cleanup ISAL frame and return
    dealloc.nlsf
    ret
;===========================================================

;===========================================================
;
; Make sure the operend stack of the current frame is empty;
; remove values if needed. Also check no elements have been
; removed from the evaluation stack and the locals stack
; have not been changed with respect to the initial pointers
; of the current frame.
;
; stack:    ... => [empty]
;
;===========================================================
.global _emul_check_frame_stacks
_emul_check_frame_stacks:

; Check evaluation stack level
    c.flld.esp
    c.ld.i.fmp FRAME_ESP
    c.sub
    c.drop  ; Flags unchanged
    c.br.z  _emul_check_frame_stacks_operand_stack_empty
    c.br.c  _emul_check_frame_error    ; Fewer values than incoming

    ; Unused operand values for the current frame.
    ; Remove one element from the evaluation stack and check again
    c.drop
    c.jumpw    _emul_check_frame_stacks

_emul_check_frame_stacks_operand_stack_empty:
    c.flld.lsp
    c.ld.i.fmp  FRAME_LSP
    c.sub
    c.drop  ; Flags unchanged
    c.br.nz _emul_check_frame_error
    ; LMP intact, good to return

    c.ret

;===========================================================
_emul_check_frame_error:
    ; TODO: What if we end up here while throwing an exception?
    ; That starts an infinite loop going through here.
    c.ldi.i clazzError
    c.jumpw _exception_raise
    ; Not expecting to come back here
    ; NOTE: Should we be able to return here and restore
    ; the register that caused the error?
    ill

;===========================================================
;
; Remove top Java stack frame from the memory stack and restore
; registers. Do not touch the evaluation stack where return
; values should be already prepared as needed.
;
; stack:    ... => ...
;
;===========================================================
.global _emul_deallocate_frame
_emul_deallocate_frame:

; Check memory stack level
    c.ld.msp
    c.ld.lmp
    c.addi  -4
    c.sub
    c.drop  ; Flags unchanged
    c.br.nz _emul_check_frame_error
    ; MSP intact, good to proceed with deallocation

; NOTE: Legacy code unlocked synchronized objects here.

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

    c.ret
;===========================================================

;===========================================================
;
; C syntax: void throw(w_instance objectref) __attribute__((noreturn))
;
;===========================================================
.global throw
throw:
; Do not allocate any new ISAL frame here
; i#0 = objectref

; First unwind all ISAL frames except for the bottom one
; created in an emulation routine.
; There is at least one ISAL frame from which throw() was called,
; so it is safe to start with one deallocation.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
throw_unwind_isal_frame:
    copy.w i#12 i#0
    dealloc.nlsf    ; NOTE: activation frame identical for both leaf and non-leaf functions.
    ; i#0 = objectref

    ; LMP + (-3*4) is non-zero RAR in normal ISAL frames
    ; but 0 at the bottom of the callchain.
    c.ld.lmp
    c.addi -12
    c.ld.i  ; load value and set zero flag
    c.drop  ; zero flag unchanged
    c.br.nz throw_unwind_isal_frame
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

    ; Deallocate final ISAL frame and push objectref
    em.isal.dealloc.nlsf    1

    ; Go to athrow to handle if objectref is null
    c.jumpw e_athrow

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
    ; es: ..., objectref, frame

    ; FIXME: Can we just return to the ISAL caller in case of
    ; no more Java frames? Anyhow, make sure original FMP is
    ; restored from the memory stack
    ; If no Java frame (FMP==0), manage uncaught exception
    c.addi  0
    c.brs.z  _throw_uncaught
    ; es: ..., objectref, frame

    ; Get base PC for current method and also save a copy for later use
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_EXEC_CODE
    c.ld.i
    c.tuck
    ; es: ..., objectref, method-pc, frame, method-pc

    ; Get absolute throw-PC.
    ; The highest bit in ERAR is used to indicate Java mode for the microcode.
    ; It is to be reset for having the actual PC value.
    ; Also decrement the addres to point to the last byte of the throwing bytecode
    ; because the actual return address may be the beginning of a new try statement.
    c.ld.erar
    c.ldi.i 0x7fffffff
    c.and
    c.addi  -1
    ; es: ..., objectref, method-pc, frame, method-pc, throw-pc

    ; Calculate bytecode pc
    c.sub
    ; es: ..., objectref, method-pc, frame, pc

    ; Get objectref to the top and keep the original for potential future use
    c.ldi.b 3
    c.pick      ; es: ..., objectref, method-pc, frame, pc, objectref

    ; Check handler in current frame
    ; Calling: int32_t throwExceptionAt(im4000_frame frame, int32_t pc, w_instance objectref)
    em.isal.alloc.nlsf 3
    move.i.i32  i#3 throwExceptionAt
    call        i#3
    em.isal.dealloc.nlsf 1
    ; es: ..., objectref, method-pc, handler

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; If no handler, need to remove top frame and check again
    c.addi  0
    c.br.z  _throw_unwind

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; Matching handler found, execute it.
    ; es: ..., objectref, method-pc, handler (bytecode pc)
    c.add
    c.st.erar
    ; es: ..., objectref

    ; Discard remaining operands in the current frame
    ; es: ..., objectref
    c.push.es
    c.callw _emul_check_frame_stacks
    c.pop.es
    ; es: [empty], objectref

    c.rete

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
_throw_unwind:
; es: ..., objectref, method-pc, 0 (handler)
    c.drop2

    ; When unwinding, ISAJ frames are continous in the execution stack.
    ; That is because all emulation routines deallocate their ISAL frame
    ; before continuing with ISAJ execution.

    ; Remove top frame including remaining operands and check the new top
    ; es: ..., objectref
    c.push.es
    c.callw _emul_check_frame_stacks
    c.pop.es
    ; es: [empty], objectref
    c.callw _emul_deallocate_frame
    ; es: ..., objectref
    c.jumpw _emul_throw

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
_throw_uncaught:
; es: ..., objectref, 0 (frame)
    c.drop

    ; Here no Java frame in the execution stack, only objectref in the evaluation stack.
    ; We returned to activate_frame().

    ; Put objectref into i#12 here, so it becomes i#0 after deallocating the ISAL frame.
    pop.es.w    i#12

    ; Deallocate ISAL frame for activate_frame(), also restores RAR
    check.lrcb  ; evaluation stack should be empty now
    dealloc.nlsf

    ; Go to handler by returning from emulation.
    ; The handler replaces activate_frame() in the ISAL callchain
    ; and will get the uncaught object as first argument.
    c.ldi.i emul_unhandled_exception
    c.st.erar
    c.rete

;===========================================================
