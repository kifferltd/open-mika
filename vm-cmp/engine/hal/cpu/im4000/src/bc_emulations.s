
.include "offsets.s"

;===========================================================
.macro em.iasl.alloc.lsf

; Save ERAR
    c.push.erar

; Allocate 8 local registers
    c.ldi.b     8
    c.aml 

    c.ld.msp
    c.dup
    st.iasp             ; IASP = MSP

    c.ldi.b      8
    c.sub
    c.dup
    c.st.msp            ; MSP = MSP - 8
    c.dup
    st.oasp             ; OASP = MSP

    c.ldi.b      4
    c.sub
    c.dup
    c.st.msp            ; MSP = MSP - 4
    st.rvp              ; RVP = MSP
.endmacro

;------------------------------------------------------------

.macro em.isal.dealloc.lsf
    ld.iasp     
    c.addi      16
    c.st.msp

    c.dml                   ; Revert c.aml

; Restore ERAR
    c.pop.erar

.endmacro
;===========================================================


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

    errorpoint      ; Not implemented

    ; needs to call emul_ldc(clazz, offset)

    ; [CG 20230818] shouldn't the firmware push the constant pool
    ; index onto the stack, i.e.
    ; ..., index --> ..., value?

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

    ; needs to call emul_ldc(clazz, offset)

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

    ; needs to call emul_ldc2_w(clazz, offset)

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

    ; needs to call emul_getstatic(clazz, index)

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

    errorpoint      ; Not implemented

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

    ; needs to call emul_getfield(clazz, index, objectref)

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

    ; needs to call putfield(clazz, index, value, objectref)

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
    em.iasl.alloc.lsf

; Get index from evaluation stack
    pop.es.w    i#0     ; index

    ; [CG 20230818] needs to also push the pointer to the calling clazz
    ; i.e. call emul_new(clazz, offset)

    move.i.i32  i#1 new_emul
    call        i#1
      
; Push object reference onto the evaluation stack
    push.es.w    i#0

    em.isal.dealloc.lsf  
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
; Stack: ..., objectref -> objectref
;
; Local variables:      +0:	program counter
;		        +1:	program counter relative method start
;		        +2:	method.java.handlers.length
;		        +3:	method.java.handlers.handlers[]
;		        +4:     method.java.code
;
;===========================================================
e_athrow:		

    errorpoint      ; Not implemented

    ; this is a tricky, one, because the microcide is passing
    ; information taken from kvm-specific data structures
    ; -> can we change the microcode?
    ; Note that this instruction may or may not cause execution
    ; to leave the current frame 

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

    ; needs to call emul_checkcast(clazz, index, objectref)

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

    ; needs to call emul_instanceof(clazz, index, objectref)

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

    ; needs to call emul_anewarray(clazz, index, dimension, count...)
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

    ; needs to call emul_invokevirtual(clazz, index, objectref, args ...)

;===========================================================
; e_invokespecial
;
; Emulates invokespecial (0xB7)
;
; Format: invokespecial, indexbyte1, indexbyte2
;
; Stack: ..., objectref, [arg1, [arg2...]] -> ...
;
;===========================================================
e_invokespecial:

    errorpoint          ; Not implemented

    ; needs to call emul_invokespecial(clazz, index, objectref, args ...)

;===========================================================
; e_invokestatic
;
; Emulates invokestatic (0xB8)
;
; Format: invokestatic, indexbyte1, indexbyte2
;
; Stack: ..., [arg1, [arg2...]] -> ...
;
;===========================================================
e_invokestatic:

    errorpoint          ; Not implemented

    ; needs to call emul_invokesstatic(clazz, index, args ...)

;===========================================================
; e_invokeinterface
;
; Emulates invokeinterface (0xB9)
;
; Format: invokespecial, indexbyte1, indexbyte2, count, 0
;
; Stack: ..., objectref, [arg1, [arg2...]] -> ...
;
;===========================================================
;
e_invokeinterface:

    errorpoint          ; Not implemented

    ; needs to call emul_invokeinterface(clazz, index, objectref, args ...)

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

; Deallocate locals on the memory stack
    c.dml

; Deallocate not needed frame entries
	c.ldi.b	    FRAME_SIZEOF-4
	c.dms

; Deallocate locals on the locals stack
	c.pop.es		;stack: ..., FRAME_LS_COUNT
	c.dls

; Recover pointers from the rest of the frame and return
	c.pop.es		;stack: ..., FRAME_RETADDRESS
	c.st.erar
	c.pop.fmp
	c.pop.rar

	c.rete	

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

    ret
;=====================================================================
