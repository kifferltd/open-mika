
.include "offsets.s"
.include "macros.s"

;===========================================================
; e_ldc
;
; Emulates ldc (0x12)
;
; Format: ldc, indexbyte
;
; Stack: ... -> ..., value
;				  
;===========================================================
e_ldc:

    em.isal.alloc.nlsf 1

; Get index from evaluation stack
    pop.es.w    i#0     ; index
    move.i.i32  i#1 emul_ldc
    call        i#1

    em.isal.dealloc.nlsf 1
    ret.eh

;===========================================================
; e_ldc_w
;
; Emulates ldc_w (0x13)
;
; Format: ldc_w, indexbyte1, indexbyte2
;
; Stack: ..., cpIndex --> ..., value
;				  
;===========================================================
e_ldc_w:		

    errorpoint      ; Not implemented

    ; needs to call emul_ldc(tricky, offset)

;===========================================================
; em_ldc2_w
;
; Emulates ldc2_w (0x14)
;
; Push long or double from runtime constant pool
;
; Format: ldc2_w, indexbyte1, indexbyte2
;
; Stack: ..., cpIndex -> ..., value
;				  
;===========================================================
e_ldc2_w:		

    errorpoint      ; Not implemented

    ; needs to call emul_ldc2_w(frame, offset)

;===========================================================
; e_jsr
;
; Emulates jsr (0xA8)
;
; Format: jsr, branchbyte1, branchbyte2
;
; Stack: ..., offset -> ..., address
;
;===========================================================
e_jsr:		

    errorpoint      ; Not implemented

    ; needs to call emul_jsr(frame, offset) - or do we do everything in assembler?

;===========================================================
; e_jsr_w
;
; Emulates jsr_w (0xC9)
;
; Format: jsr_w, branchbyte1, branchbyte2, branchbyte3, branchbyte4
;
; Stack: ... -> address
;
;===========================================================
e_jsr_w:

    errorpoint      ; Not implemented

    ; needs to call emul_jsr_w(frame, offset) - or do we do everything in assembler?
    ; [CG 20230818] shouldn't the firmware push the branch offset onto the
    ; stack, i.e.
    ; ..., offset --> ..., address?


;===========================================================
; e_ret
;
; Emulates ret (0xA9)
;
; Format: ret, index
;
; Stack: no change
;
;===========================================================
e_ret:

    errorpoint      ; Not implemented

    ; needs to call emul_ret(frame, offset) - or do we do everything in assembler?

;===========================================================
; e_getstatic
;
; Emulates getstatic (0xB2)
;
; Format: getstatic, indexbyte1, indexbyte2
;
; Get static field in class
;
; Stack: ..., cpIndex -> ..., value
;
;===========================================================
e_getstatic:		

    errorpoint      ; Not implemented

    ; needs to call emul_getstatic(frame, index)

;===========================================================
; e_putstatic
;
; Emulates putstatic (0xB3)
;
; Format: putstatic, indexbyte1, indexbyte2
;
; Set static field in class
;
; Stack: ..., value, cpIndex -> ...
;
;===========================================================
e_putstatic:

    em.isal.alloc.nlsf 1

; Get index

    copy.w  i#1 i#0     ; index
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#2  emul_putstatic
    call        i#2
      
    em.isal.dealloc.nlsf 1
    ret
    ;errorpoint       Not implemented

    ; needs to call emul_putstatic(clazz, index, value)

;===========================================================
; e_getfield
;
; Emulates getfield (0xB4)
;
; Format: getfield, indexbyte1, indexbyte2
;
; Stack: ..., objectref, cpIndex -> ...,value
;					        
;===========================================================
e_getfield:

    errorpoint      ; Not implemented

    ; needs to call emul_getfield(frame, index, objectref)

;===========================================================
; e_putfield
;
; Emulates putfield (0xB5)
;
; Format: putfield, indexbyte1, indexbyte2
;
; Stack: ..., objectref, value, cpIndex -> ...
;
;===========================================================
e_putfield:

    errorpoint      ; Not implemented

    ; needs to call putfield(frame, index, value, objectref)

;===========================================================
; e_new
;
; Emulates new (0xBB)
;
; Format: new, indexbyte1, indexbyte2
;
; Stack: ..., index -> ..., objectref
;
;===========================================================
e_new:	
    em.isal.alloc.nlsf 1

; Get index
    copy.w  i#1 i#0     ; index
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#2 emul_new
    call        i#2
      
    em.isal.dealloc.nlsf 1
    ret.eh    

;===========================================================
; e_newarray
;
; Emulates newarray (0xBC)
;
; Format: newarray, arraytype
;
; Stack: ..., count -> ..., objectref
;
;===========================================================
e_newarray:

    errorpoint      ; Not implemented

    ; needs to call emul_newarray(atype, count)

    ; [CG 20230818] shouldn't the firmware push the array type onto the
    ; stack, i.e.
    ; ..., count, atype --> ..., objectref?

;===========================================================
; e_anewarray
;
; Emulates anewarray (0xBD)
;
; Format: anewarray, indexbyte1, indexbyte2
;
; Stack: ..., count, cpIndex -> ..., arrayref
;
;===========================================================
e_anewarray:

    errorpoint      ; Not implemented

    ; needs to emul_anewarray(clazz, index, count)

;===========================================================
; e_arraylength
;
; Emulates arraylength (0xBE)
;
; Format: arraylength
;
; Stack: ..., arrayref -> ..., length
;
;===========================================================
e_arraylength:

    errorpoint      ; Not implemented

    ; see partial implementation in branch bc-optimize-create-and-invoke,
    ; needs error handling

;===========================================================
; e_athrow
;
; Emulates athrow (0xBF)
;
; Throw exception or error
;
; Format: athrow
;
; Stack: ..., objectref -> [empty], objectref
;
;===========================================================
e_athrow:

; Check if objectref is null
    c.addi  0
    c.brs.z _exception_nullpointer

    c.jumpw _emul_throw

;===========================================================
; e_checkcast
;
; Emulates checkcast (0xC0)
;
; Check whether object is of given type
;
; Format: checkcast, indexbyte1, indexbyte2
;
; Stack: ..., objectref, cpIndex -> ..., objectref
;
;===========================================================
e_checkcast:		

    errorpoint      ; Not implemented

    ; needs to call emul_checkcast(frame, index, objectref)

;===========================================================
; e_instanceof
;
; Emulates instanceof (0xC1)
;
; Determine if object is of given type
;
; Format: instanceof, indexbyte1, indexbyte2
;
; Stack: ..., objectref, cpIndex -> ..., result
;
;===========================================================
;
e_instanceof:

    errorpoint      ; Not implemented

    ; needs to call emul_instanceof(frame, index, objectref)

;===========================================================
; e_monitorenter
;
; Emulates monitorenter (0xC2)
;
; Format: monitorenter
;
; Stack: ..., objectref -> ...
;
;===========================================================
e_monitorenter:

    errorpoint      ; Not implemented

    ; needs to call emul_monitorenter(objectref)

;===========================================================
; e_monitorexit
;
; Emulates monitorexit (0xC3)
;
; Format: monitorexit
;
; Stack: ..., objectref -> ...
;
;===========================================================
;
e_monitorexit:

    errorpoint      ; Not implemented

    ; needs to call emul_monitorexit(objectref)

;===========================================================
; e_multianewarray
;
; Emulates multianewarray (0xC5)
;
; Format: multinewarray, indexbyte1, indexbyte2, dimensions
;
; Stack: ..., count1, [count2, ...], cpIndex -> arrayref
;
; Local stack usage:	
;	0:	dimensions
;	1:	counter
;
;===========================================================
e_multianewarray:

    errorpoint      ; Not implemented

    ; needs to call emul_anewarray(frame, index, dimension, count...)
;===========================================================
; e_invokevirtual
;
; Emulates invokevirtual (0xB6)
;
; Format: invokevirtual, indexbyte1, indexbyte2
;
; Stack: ..., objectref, [arg1, [arg2...]], cpIndex -> ...
;
;===========================================================
e_invokevirtual:

    errorpoint          ; Not implemented

    ; needs to call emul_invokevirtual(frame, index, objectref, args ...)

;===========================================================
; e_invokespecial
;
; Emulates invokespecial (0xB7)
;
; Format: invokespecial, indexbyte1, indexbyte2
;
; Stack: ..., objectref, [arg1, [arg2...]], cpIndex -> ...
;
;===========================================================
e_invokespecial:
    em.isal.alloc.nlsf 1

; Get index
    copy.w  i#1 i#0     ; index
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_SPEC_DECLARING_CLAZZ
    c.ld.i      ; es: ..., calling_clazz
    pop.es.w    i#0     ; clazz

; Call method resolution -  class in i#0, index in i#1
    move.i.i32  i#2 getMethodConstant
    call        i#2

; Reveal evaluation stack and prepare new frame.
    em.isal.dealloc.nlsf 1
; Stack: ..., objectref, [arg1, [arg2...]], method
    c.dup
    c.addi  METHOD_EXEC_ARG_I
    c.ld.i
    c.swap
; Stack: ..., objectref, [arg1, [arg2...]], narg, method
    c.ld.erar
; Stack: ..., objectref, [arg1, [arg2...]], narg, method, return_address
    c.jumpw _emul_allocate_frame


;===========================================================
; e_invokestatic
;
; Emulates invokestatic (0xB8)
;
; Format: invokestatic, indexbyte1, indexbyte2
;
; Stack: ..., [arg1, [arg2...]], cpIndex -> ...
;
;===========================================================
e_invokestatic:

    errorpoint          ; Not implemented

    ; needs to call emul_invokesstatic(frame, index, args ...)

;===========================================================
; e_invokeinterface
;
; Emulates invokeinterface (0xB9)
;
; Format: invokespecial, indexbyte1, indexbyte2, count, 0
;
; Stack: ..., objectref, [arg1, [arg2...]], cpIndex -> ...
;
;===========================================================
;
e_invokeinterface:

    errorpoint          ; Not implemented

    ; needs to call emul_invokeinterface(frame, index, objectref, args ...)

;===========================================================
; e_ireturn
;
; Emulates ireturn (0xAC)
;
; Stack: ..., value -> ..., value
;
;===========================================================
e_ireturn:

    errorpoint          ; Not implemented

    ; should be similar to return (v.i.), but leaves 1 word on stack

;===========================================================
; e_freturn
;
; Emulates freturn (0xAE)
;
; Stack: ..., value -> ..., value
;
;===========================================================
e_freturn:

    errorpoint          ; Not implemented

    ; should be similar to return (v.i.), but leaves 1 word on stack

;===========================================================
; e_areturn
;
; Emulates areturn (0xB0)
;
; Stack: ..., value -> ..., value
;
;===========================================================
e_areturn:

    errorpoint          ; Not implemented

    ; should be similar to return (v.i.), but leaves 1 word on stack

;===========================================================
; e_lreturn
;
; Emulates lreturn (0xAD)
;
; Stack: ..., value1, value2 -> ..., value1, value2
;
;===========================================================
e_lreturn:

    errorpoint          ; Not implemented

    ; should be similar to return (v.i.), but leaves 2 words on stack

;===========================================================
; e_dreturn
;
; Emulates lreturn (0xAF)
;
; Stack: ..., value1, value2 -> ..., value1, value2
;
;===========================================================
e_dreturn:

    errorpoint          ; Not implemented

    ; should be similar to return (v.i.), but leaves 2 words on stack

;===========================================================
; e_return
;
; Emulates return (0xB1)
;
; Stack: ... -> ...
;
;===========================================================
e_return:
    c.jumpw _emul_return

;===========================================================
; e_exception
;
; This function handles exceptions thrown in microcoded
; bytecodes.
;
; Stack: ..., exception code
;
; Register ERAR contains the program counters value when
; the exception was thrown plus 1.
;
;===========================================================
.include "exception_constants.s"

e_exception:

; Decode the exception code
    c.dup
    c.xori  EC_ARITHMETICEXCEPTION
    c.drop
    c.br.z  _exception_arthmetic

    c.dup
    c.xori  EC_NULLPOINTEREXCEPTION
    c.drop
    c.br.z  _exception_nullpointer

    c.dup
    c.xori  EC_ARRAYINDEXOUTOFBOUNDSEXCEPTION
    c.drop
    c.br.z  _exception_arrayindex

    c.dup
    c.xori  EC_STACKOVERFLOWERROR
    c.drop
    c.br.z  _exception_stackoverflow

; Here if unknown exception code
    errorpoint

; Throw an ArithmeticException
_exception_arthmetic:
    c.drop
    c.ldi.i clazzArithmeticException
    c.jumps _exception_raise

; Throw a NullPointerException
_exception_nullpointer:
    c.drop
    c.ldi.i clazzNullPointerException
    c.jumps _exception_raise

; Throw an ArrayIndexOutOfBoundsException
_exception_arrayindex:
    c.drop
    c.ldi.i clazzArrayIndexOutOfBoundsException
    c.jumps _exception_raise

; Throw a StackOverflowError
_exception_stackoverflow:
    c.drop
    c.ldi.i clazzStackOverflowError
    c.jumps _exception_raise

; Instatiate and throw exception
; es: ..., exception_class
_exception_raise:

; Instantiate class
    em.isal.alloc.nlsf 1
    move.i.i32  i#1 createRuntimeException
    call        i#1
    em.isal.dealloc.nlsf 1

; Throw the object
    c.jumpw _emul_throw

;=====================================================================
;
;	Register ISAJ bytecode emulation routines
;
;=====================================================================

.macro set_bc_emulation bcname, opcode
    ld.i32  _sys_xEmulTab
    ld.i32  \opcode
    c.shli 2
    c.add 
 	ld.i32 e_\bcname
	c.st.i
.endm


.global setupBcEmulation
setupBcEmulation:

    set_bc_emulation ldc 18
    set_bc_emulation ldc_w 19
    set_bc_emulation ldc2_w 20
    set_bc_emulation jsr 168
    set_bc_emulation jsr_w 201
    set_bc_emulation ret 169
    set_bc_emulation getstatic 178
    set_bc_emulation putstatic 179
    set_bc_emulation getfield 180
    set_bc_emulation putfield 181
    set_bc_emulation new 187
    set_bc_emulation newarray 188
    set_bc_emulation anewarray 189
    set_bc_emulation arraylength 190
    set_bc_emulation athrow 191
    set_bc_emulation checkcast 192
    set_bc_emulation instanceof 193
    set_bc_emulation monitorenter 194
    set_bc_emulation monitorexit 195
    set_bc_emulation multianewarray 197
    set_bc_emulation invokevirtual 182
    set_bc_emulation invokespecial 183
    set_bc_emulation invokestatic 184
    set_bc_emulation invokeinterface 185
    set_bc_emulation ireturn 172
    set_bc_emulation freturn 174
    set_bc_emulation areturn 176
    set_bc_emulation lreturn 173
    set_bc_emulation dreturn 175
    set_bc_emulation return 177
    set_bc_emulation exception 255

    ret
;=====================================================================
