
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
    em.cfi_start
    em.isac.cfi_return_rar

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
    em.cfi_end

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
    em.cfi_start
; Called in ISAL context, can use registers.
    em.isal.cfi_init
    move.i.i32 i#11 0
    alloc.nlsf i#11
    em.isal.cfi_alloc_done

; i#12 contains parameter 'method'
; i#13 contains parameter 'narg', i.e. number of argument words
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

; Close the ISAL unwind entry for activate_frame() here
; because the following instructions might be executed in ISAC context.
    em.cfi_end

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
    em.cfi_start
    em.isac.cfi

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
    c.ldi.b SIZEOF_FRAME - 2    ; Even number maintains MSP alignment
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

; THROWPC is initialized 0 here and shall be updated whenever
; entering bytecode emulation.

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
; es: ..., [arg0, [arg1...]], narg, method, return_address

    ; FIXME: This part of the code assumes native method is invoked
    ; via bytecode emulation. Should it be able to handle going from
    ; activate_frame()?

    ; Store original MSP and top two values in local variables,
    ; keep nargs as counter in the stack
    c.als.3     ; Alloate temporary local variables
    c.ld.msp
    c.st.v0
    c.st.v1
    c.st.v2

    ; Move arg words to memory
    ; FIXME: Is narg==0 is a possible case? If no, just delete the following 3 lines.
    ; First, skip over this if no args at all.
    c.addi  0
    c.br.z  activate_frame_native_args_moved

    ; Need to add an extra slot for alignment if narg is odd
    c.dis   ; Protect critical section while MSP might be not 8-aligned
    c.dup
    c.andi  1
    c.if.z  activate_frame_native_move_args ; consume the value from es
    c.ld.0      ; Load the alignment value
    c.push.es   ; Push it to the memory stack

activate_frame_native_move_args:
    c.addi  0

activate_frame_native_move_next_arg:
    c.br.z  activate_frame_native_args_moved
    c.swap
    c.push.es
    c.addi  -1
    c.jumps activate_frame_native_move_next_arg

activate_frame_native_args_moved:
    c.enb   ; End of critical section -- this is fine to do even if jumped over c.dis above
    c.drop  ; Drop counter that is 0

    ; Push values to the evaluation stack
    c.ld.v0     ; original_msp
    c.ld.v1     ; return_address
    c.ld.v2     ; method
    c.dls.3     ; Deallocate local variables
    ; es: ..., original_msp, return_address, method
    c.dup
    c.addi  METHOD_EXEC_RETURN_I
    c.ld.s      ; Number of return words; possible values: 0, 1, 2
    c.swap
    ; es: ..., original_msp, return_address, return_i, method
    c.ld.fmp    ; Caller frame pointer
    c.swap
    c.ld.msp    ; Base address of args
    c.ld.2
    c.ams       ; Allocate two more entries for potential return value -- MSP alignment maintained
    c.ld.msp    ; Address for return buffer
; es: ..., original_msp, return_address, return_i, frame, method, args, return_buf

    ; Initialize ISAL frame, take only parameters for emul_invoke_native
    em.isal.alloc.nlsf  4
    em.isal.cfi_alloc_done
    ; i#0 = frame
    ; i#1 = method
    ; i#2 = args
    ; i#3 = return_buf

    ; Perform invocation
    move.i.i32  i#4 emul_invoke_native
    call        i#4
    ; Exception, if any, thrown from C
    ; Here on normal return

    ; Deallocate ISAL frame
    em.isal.dealloc.nlsf    0
    em.isal.cfi_dealloc_done

_activate_frame_native_return:
; es: ..., original_msp, return_address, return_i
; Return buffer at MSP

    ; Store away original_msp, return_address
    c.nrot
    c.als.2
    c.st.v0 ; return_address
    c.st.v1 ; original_msp
    ; es: ..., return_i

    ; Push return words to the evaluation stack. Possible values: 0, 1, 2
    ; Do not pop out words from the memory stack because that would constitute a critical section for MSP alignment
    c.dup
    c.if.z  activate_frame_native_return_nothing    ; consume value from es
    c.addi  -1
    c.if.z  activate_frame_native_return_single_word    ; consume value from es
    ; fall through to the double-word case

_activate_frame_native_return_double_word:
    c.ld.i.msp  0
    c.ld.i.msp  1
    c.jumps activate_frame_native_return_pushed

activate_frame_native_return_single_word:
    c.ld.i.msp  0
    c.jumps activate_frame_native_return_pushed

activate_frame_native_return_nothing:
    c.drop  ; Drop counter that is 0
    ; fall through...

activate_frame_native_return_pushed:
    ; es: ..., [retval1,[ retval2,]]

    ; Restore original_msp, return_address
    c.ld.v0 ; return_address
    c.ld.v1 ; original_msp
    c.dls.2
    ; es: ..., [retval1,[ retval2,]] return_address, original_msp

    ; Restore original MSP, that is deallocate return_buf and args
    c.st.msp

    ; Return to caller
    c.st.erar
    ; es: ..., [retval1,[ retval2,]]
    c.rete
    em.cfi_end
;===========================================================

;===========================================================
; Method returns here when invoked via activate_frame()
; Continue in ISAL context and take care of return value in
; the ISAC evaluation stack if any.
activate_frame_return:
    ; Start a new unwind entry for this part of activate_frame()
    em.cfi_start
    em.isal.cfi_init        ; Set initial info to apply after dealloc
    em.isal.cfi_alloc_done  ; Update info immediately as already in ISAL frame

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
    em.isal.cfi_dealloc_done
    ret
    em.cfi_end
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
    em.cfi_start
    em.isac.cfi

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
    ; Here when stack corruption is detected in the current Java frame.
    ; There is not much to do for recovery.
    ; NOTE: do NOT throw an exception here because that is to be handled
    ; in the current (corrupted) Java frame, which will induce an infinite
    ; loop going through here.

    ; Terminating the system with an errorpoint now.
    errorpoint
    ; FIXME: Instead of stopping the system, it might be better to terminate the
    ; current FreeRTOS task. Note, however, that blindly terminating a task and
    ; so the current Java thread might cause other problems in the system (e.g., deadlock).
    ; A well-designed application should detect and handle such a situation.

    em.cfi_end

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
    em.cfi_start
    em.isac.cfi

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
    em.cfi_end
;===========================================================

;===========================================================
;
; C syntax: void throw(w_instance objectref) __attribute__((noreturn))
;
;===========================================================
.global throw
throw:
    em.cfi_start
    em.isal.cfi_init

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
    ; Do not change unwind info, keep seeing a caller if any left.

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
    em.isac.cfi                 ; No more ISAL frame, switch to ISAC context

    ; Go to athrow to handle if objectref is null
    c.jumpw e_athrow
    em.cfi_end

;===========================================================
;
; Throw the object from the top of the evaluation stack.
;
; stack:    ..., objectref => [empty], objectref
;
;===========================================================
.global _emul_throw
_emul_throw:
    em.cfi_start
    em.isac.cfi

    ; Get current frame
    c.ld.fmp
    ; es: ..., objectref, frame

    ; If no Java frame (FMP==0), manage uncaught exception
    c.addi  0
    c.brs.z  _throw_uncaught
    ; es: ..., objectref, frame

    ; Get bytecode pc
    c.ld.i.fmp  FRAME_THROWPC
    ; es: ..., objectref, frame, pc

    ; Get objectref to the top and keep the original for potential future use
    c.ldi.b 2
    c.pick      ; es: ..., objectref, frame, pc, objectref

    ; Check handler in current frame
    ; Calling: int32_t throwExceptionAt(im4000_frame frame, int32_t pc, w_instance objectref)
    em.isal.alloc.nlsf 3
    move.i.i32  i#3 throwExceptionAt
    call        i#3
    em.isal.dealloc.nlsf 1
    ; es: ..., objectref, handler-pc

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; If no handler, need to remove top frame and check again
    c.addi  0
    c.br.z  _throw_unwind

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ; Matching handler found, execute it.
    ; es: ..., objectref, handler-pc

    ; Get base PC for current method
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_EXEC_CODE
    c.ld.i
    ; es: ..., objectref, handler-pc, method-pc

    ; Set ERAR to the absolute PC for handler in the method
    c.add
    c.st.erar
    ; es: ..., objectref

    ; Discard remaining operands in the current frame
    ; es: ..., objectref
    ; Store away objectref in the memory stack
    ; NOTE: The called check would break if the value is left in the evaluation stack
    ; as well as if the value is stored in a temporary local.
    em.push.es  1
    c.callw _emul_check_frame_stacks
    em.pop.es  1
    ; es: [empty], objectref

    c.rete

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
_throw_unwind:
; es: ..., objectref, 0 (handler-pc)
    c.drop

    ; When unwinding, ISAJ frames are continous in the execution stack.
    ; That is because all emulation routines deallocate their ISAL frame
    ; before continuing with ISAJ execution.

    ; Remove top frame including remaining operands and check the new top
    ; es: ..., objectref
    ; Store away objectref in the memory stack
    ; NOTE: The called check would break if the value is left in the evaluation stack
    ; as well as if the value is stored in a temporary local.
    em.push.es  1
    c.callw _emul_check_frame_stacks
    em.pop.es   1
    ; es: [empty], objectref
    c.callw _emul_deallocate_frame
    ; es: ..., objectref
    c.jumpw _emul_throw

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
_throw_uncaught:
; es: objectref, 0 (frame)
    c.drop

    ; Here no Java frame in the execution stack, only objectref in the evaluation stack.
    ; We returned to activate_frame().

    ; Drop exception from the evaluation stack, C code has it in the current thread.
    c.drop
    ; es: <empty>

    ; Let activate_frame return
    c.jumpw activate_frame_return_done

    em.cfi_end

;===========================================================
