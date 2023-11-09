
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
    ; ERAR points to indexbyte. Adjust ERAR and load indexbyte.
    em.load_byte_from_erar
    ; es: ..., indexbyte

    em.isal.alloc.nlsf 1
    ; i#0 = indexbyte
    copy.w  i#1 i#0     ; index
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#2 emul_ldc
    call        i#2

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

    em.isal.alloc.nlsf 1

    copy.w  i#1 i#0
    c.ld.fmp
    pop.es.w    i#0     ; frame

    move.i.i32  i#2  emul_ldc_w
    call        i#2

    em.isal.dealloc.nlsf 2
    ret.eh
    ; errorpoint       Not implemented

    ; same as e_lds but reads 2 bytes from code stream instead of 1

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

    em.isal.alloc.nlsf 1

    copy.w  i#1 i#0
    c.ld.fmp
    pop.es.w    i#0     ; frame

    move.i.i32  i#2  emul_ldc2_w
    call        i#2

    em.isal.dealloc.nlsf 2
    ret.eh

    ; errorpoint       Not implemented

    ; needs to call emul_ldc2_w(frame, offset)

;===========================================================
; em_iload
;
; Emulates wide iload (0xC4,0x15)
;          wide fload (0xC4,0x17)
;          wide aload (0xC4,0x19)
;
; Stack: ... -> ..., value
;				  
;===========================================================
e_iload:
    em.load_short_from_erar
    ; Stack: ..., index
    c.addi    -8
    c.br.nc   e_iload_10       ; variable is in scratchpad?

    errorpoint                  ; Case idx <=8 not yet implemented

 e_iload_10:                   ; not in scratchpad
    c.addi      8               ; restore index

    em.isal.alloc.nlsf 1
    copy.w      i#1 i#0
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#2 emul_iload
    call        i#2
    em.isal.dealloc.nlsf 1
    ret.eh

;===========================================================
; em_lload
;
; Emulates wide lload (0xC4,0x16)
;          wide dload (0xC4,0x18)
;
; Stack: ... -> ..., value_ms, value_ls
;				  
;===========================================================
e_lload:
    em.load_short_from_erar
    ; Stack: ..., index
    c.addi    -8
    c.br.nc   e_lload_10       ; variable is in scratchpad?

    errorpoint                 ; Case idx <=8 not yet implemented

 e_lload_10:                   ; not in scratchpad
    c.addi      8              ; restore index

    em.isal.alloc.nlsf 1
    copy.w      i#2 i#1
    copy.w      i#1 i#0
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#3 emul_lload
    call        i#3

    em.decomp_l0_to_i1_i0
    em.isal.dealloc.nlsf 2
 
    ret.eh


;===========================================================
; em_istore
;
; Emulates wide istore (0xC4,0x36)
;          wide fstore (0xC4,0x38)
;          wide astore (0xC4,0x3A)
;
; Stack: ..., value -> ...
;				  
;===========================================================
e_istore:
    em.load_short_from_erar     
    ; Stack: ..., value, index
    c.addi    -8
    c.br.nc   e_istore_10       ; variable is in scratchpad?

    errorpoint                  ; Case idx <=8 not yet implemented

 e_istore_10:                   ; not in scratchpad
    c.addi      8               ; restore index

    em.isal.alloc.nlsf 2
    copy.w      i#2 i#1
    copy.w      i#1 i#0
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#3 emul_istore
    call        i#3
    em.isal.dealloc.nlsf 0
    ret.eh


;===========================================================
; em_lstore
;
; Emulates wide lstore (0xC4,0x37)
;          wide dstore (0xC4,0x39)
;
; Stack: ..., value_ms, value_ls -> ...
;				  
;===========================================================
e_lstore:
    em.load_short_from_erar
    ; Stack: ..., value_ms, value_ls, index
    c.addi    -8
    c.br.nc   e_lstore_10       ; variable is in scratchpad?

    errorpoint                  ; Case idx <=8 not yet implemented

 e_lstore_10:                   ; not in scratchpad
    c.addi      8               ; restore index

    em.isal.alloc.nlsf 3
    copy.w      i#3 i#2
    copy.w      i#2 i#1
    copy.w      i#1 i#0
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#4 emul_lstore
    call        i#4
    em.isal.dealloc.nlsf 0
    ret.eh

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
    ; David: Need to juggle with ERAR, perhaps it better be kept in assembler

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
    ; David: Offset not pushed by microcode, ERAR points to branchbyte1


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
    ; David: ERAR points to index or indexbyte1 if wide. Also check whether it is wide.

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
    em.isal.alloc.nlsf 1

    copy.w  i#1 i#0     ; index
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_SPEC_DECLARING_CLAZZ
    c.ld.i      ; es: ..., calling_clazz
    pop.es.w    i#0     ; clazz
    
; Call field resolution -  class in i#0, index in i#1
    move.i.i32  i#2 getFieldConstant_unsafe
    call        i#2
    copy.w      i#1 i#0

    add.i.i8        i#0 i#0 FIELD_FLAGS
    load.w          i#0 i#0
    move.i.i32      i#2 FIELD_IS_LONG
    and.upd.i       i#0 i#2
    br.cmp.i.i8.eq  _e_getstatic_single i#0 0

; 2-word field

    ; Call function
    copy.w  i#0 i#1
    copy.w  i#1 i#6
    move.i.i32  i#2  emul_getstatic_double
    call        i#2

    em.decomp_l0_to_i1_i0

    ; Deallocate execution frame including the dynamically allocated area
    em.isal.dealloc.nlsf 2
    ; es: ..., high-word, low-word
    ret.eh

_e_getstatic_single:
    copy.w  i#0 i#1
    move.i.i32  i#2  emul_getstatic_single
    call        i#2
      
    em.isal.dealloc.nlsf 1
    ret.eh

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
    copy.w      i#1 i#0
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_SPEC_DECLARING_CLAZZ
    c.ld.i      ; es: ..., calling_clazz
    pop.es.w    i#0     ; clazz
    move.i.i32  i#2 getFieldConstant_unsafe
    call        i#2
    copy.w      i#1 i#0
    
    add.i.i8        i#1 i#1 FIELD_FLAGS
    load.w          i#1 i#1
    move.i.i32      i#2 FIELD_IS_LONG
    and.upd.i       i#1 i#2
    cmp.i.i8.eq     b#8 i#1 0
    br.c.i16        b#8 _e_putstatic_single

    em.isal.dealloc.nlsf 1
    em.isal.alloc.nlsf 3
    move.i.i32      i#3 emul_putstatic_double
    call            i#3
    em.isal.dealloc.nlsf 0
    ret.eh

_e_putstatic_single:
    em.isal.dealloc.nlsf 1
    em.isal.alloc.nlsf 2
    move.i.i32      i#2 emul_putstatic_single
    call            i#2
      
    em.isal.dealloc.nlsf 0
    ret.eh


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
; Check if objectref is null
    c.over
    c.addi  0
    c.brs.z _exception_nullpointer
    c.drop

    em.isal.alloc.nlsf 2

    copy.w  i#4 i#0
    copy.w  i#5 i#1
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_SPEC_DECLARING_CLAZZ
    c.ld.i      ; es: ..., calling_clazz
    pop.es.w    i#0     ; clazz
    
; Call field resolution -  class in i#0, index in i#1
; TODO we know the contents is resolved, so we just want clazz->values[index]
    move.i.i32  i#2 getFieldConstant_unsafe
    call        i#2
    copy.w      i#1 i#0 ; field

    add.i.i8        i#0 i#0 FIELD_FLAGS
    load.w          i#0 i#0
    move.i.i32      i#2 FIELD_IS_LONG
    and.upd.i       i#0 i#2
    br.cmp.i.i8.eq  _e_getfield_single i#0 0

; 2-word field
    ; Call function
    copy.w  i#0 i#1 ; field
    copy.w  i#1 i#4 ; objectref
    move.i.i32  i#3  emul_getfield_double
    call        i#3

    em.decomp_l0_to_i1_i0

    ; Deallocate execution frame including the dynamically allocated area
    em.isal.dealloc.nlsf 2
    ; es: ..., high-word, low-word
    ret.eh

_e_getfield_single:
    ; field is already in i#1
    copy.w  i#0 i#4 ; objectref
    move.i.i32  i#2  emul_getfield_single
    call        i#2
    em.isal.dealloc.nlsf 1
    ret.eh

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
; Check if objectref is null
    c.ldi.i 2
    c.pick
    c.addi  0
    c.brs.z _exception_nullpointer
    c.drop

    em.isal.alloc.nlsf 1
    copy.w      i#1 i#0
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_SPEC_DECLARING_CLAZZ
    c.ld.i      ; es: ..., calling_clazz
    pop.es.w    i#0     ; clazz
    move.i.i32  i#2 getFieldConstant_unsafe
    call        i#2
    copy.w      i#1 i#0
    
    add.i.i8        i#1 i#1 FIELD_FLAGS
    load.w          i#1 i#1
    move.i.i32      i#2 FIELD_IS_LONG
    and.upd.i       i#1 i#2
    cmp.i.i8.eq     b#8 i#1 0
    br.c.i16        b#8 _e_putfield_single

    em.isal.dealloc.nlsf 1
    em.isal.alloc.nlsf 4
    move.i.i32      i#4 emul_putfield_double
    call            i#4
    em.isal.dealloc.nlsf 0
    ret.eh

_e_putfield_single:
    em.isal.dealloc.nlsf 1
    em.isal.alloc.nlsf 3
    move.i.i32      i#3 emul_putfield_single
    call            i#3
    em.isal.dealloc.nlsf 0
    ret.eh

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
; Stack: ..., count, -> ..., objectref
; N.B. need to fetch atype from bytecode qnd step over it!
;
;===========================================================
e_newarray:
; Need to read atype from bytecode stream here and skip over the byte by incrementing erar
    c.ld.erar
    c.dup
    c.addi  1
    c.st.erar
    c.ld.b
    c.andi  0xff
; es: ..., count, atype

    em.isal.alloc.nlsf 2
    ; atype is in #0, count in #1

    move.i.i32  i#3 emul_newarray
    call        i#3
      
    em.isal.dealloc.nlsf 1
    ret.eh 

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
    em.isal.alloc.nlsf 2
    ; i#0 = count
    ; i#1 = cpIndex

; Get count in i#2
    copy.w  i#2 i#0     ; count
; Leave index in i#1
; Get frame in i#0
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#3 emul_anewarray
    call        i#3
      
    em.isal.dealloc.nlsf 1
    ret.eh

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
; Check if arrayref is null
    c.dup
    c.addi  0
    c.brs.z _exception_nullpointer
    c.drop

    ; next instruction not needed so long as ARRAY_LENGTH is 0
    ; c.addi  ARRAY_LENGTH
    c.ld.i      ; ..., arraylength
    ret.eh

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
.global e_athrow    ; Used to throw in invoke.s
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
    em.isal.alloc.nlsf 2

; TODO currently we are passing objectref to C and getting the
; same objectref back - maybe better to dup objectref and let
; the C code return void?

; Get objectref in i#2
    copy.w  i#2 i#1     ; objectref
; Get index in i#1
    copy.w  i#1 i#0     ; index
; Get frame in i#0
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#3 emul_checkcast
    call        i#3
      
    em.isal.dealloc.nlsf 1
    ret.eh 

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

    em.isal.alloc.nlsf 2
    copy.w  i#2 i#1
    copy.w  i#1 i#0
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#3 emul_instanceof
    call        i#3

    ; needs to call emul_instanceof(frame, objectref, index)

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
; Check if objectref is null
    c.dup
    c.addi  0
    c.brs.z _exception_nullpointer
    c.drop

    em.isal.alloc.nlsf 1

    move.i.i32  i#1 emul_monitorenter
    call        i#1

    em.isal.dealloc.nlsf 0
    ret.eh 


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
; Check if objectref is null
    c.dup
    c.addi  0
    c.brs.z _exception_nullpointer
    c.drop

    em.isal.alloc.nlsf 1

    move.i.i32  i#1 emul_monitorexit
    call        i#1

    em.isal.dealloc.nlsf 0

;===========================================================
; e_multianewarray
;
; Emulates multianewarray (0xC5)
;
; Format: multinewarray, indexbyte1, indexbyte2, dimensions
;
; Stack: ..., count1, [count2, ...], cpIndex -> arrayref
;
;===========================================================
e_multianewarray:

    em.load_byte_from_erar
    c.als.3
    c.dup
    c.rot
    c.st.v0    ;cpIndex
    c.st.v1    ;nbrDimension
    
    c.ld.msp
    c.st.v2
    c.addi 0
_e_multianewarray_0:
    c.if.z _e_multianewarray_10
    c.swap
    c.push.es
    c.addi -1
    c.jumps _e_multianewarray_0

_e_multianewarray_10:
    c.drop
    c.ld.v2
    c.ld.fmp
    c.ld.v0
    c.ld.v1
    c.dls.3
    c.ld.msp
    em.isal.alloc.nlsf 4
    ;i#3 : dimensions
    ;i#2 : nbrDimensions
    ;i#1 : cpIndex
    ;i#0 : frame
    move.i.i32  i#4 emul_multianewarray
    call        i#4
    em.isal.dealloc.nlsf 1
    c.swap
    c.st.msp
    c.rete

    ; needs to call emul_anewarray(frame, index, dimension, count...)
    ; David: It seems ERAR points to dimensions

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
    em.isal.alloc.nlsf 1

; Get index
    copy.w  i#1 i#0     ; index
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_SPEC_DECLARING_CLAZZ
    c.ld.i      ; es: ..., calling_clazz
    pop.es.w    i#0     ; clazz

; Call method resolution -  class in i#0, index in i#1
    move.i.i32  i#2 getMethodConstant_unsafe
    call        i#2
    ; called method is in i#0

    em.isal.dealloc.nlsf 1

    ;      Stack: ..., objectref, [arg1, [arg2...]], cld_method
    c.dup       ; ..., objectref, [arg1, [arg2...]], cld_method, cld_method
    c.addi  METHOD_EXEC_ARG_I
    c.ld.i      ; ..., objectref, [arg1, [arg2...]], cld_method, arg_i
    c.pick      ; ..., objectref, [arg1, [arg2...]], cld_method, objectref
; Check if objectref is null
    c.dup
    c.addi  0
    c.brs.z _exception_nullpointer
    c.drop

    em.isal.alloc.nlsf 2
; Now we have the called method in i#0 and the objectref in i#1
; Call emul_virtual_target to get the real target method
    move.i.i32  i#2 emul_virtual_target
    call        i#2
; target method is now in i#0
    c.jumps     _invoke_common

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
    move.i.i32  i#2 getMethodConstant_unsafe
    call        i#2

; Now we have the called method in i#0 - but we need to find the true target
; TODO we should probably check for null objectref here
    copy.w  i#1 i#0     ; called_method
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#2 emul_special_target
    call        i#2
; target method is now in i#0
    c.jumps     _invoke_common

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
    em.isal.alloc.nlsf 1

; Get index
    copy.w  i#1 i#0     ; index
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_SPEC_DECLARING_CLAZZ
    c.ld.i      ; es: ..., calling_clazz
    pop.es.w    i#0     ; clazz

; Call method resolution -  class in i#0, index in i#1
; TODO we know the constant is resolved, so we just want clazz->values[index]
    move.i.i32  i#2 getMethodConstant_unsafe
    call        i#2

; Now we have the called method in i#0 - but we need to check the target
    copy.w  i#1 i#0     ; called_method
    c.ld.fmp
    pop.es.w    i#0     ; frame
    move.i.i32  i#2 emul_static_target
    call        i#2
    c.jumps     _invoke_common

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
    em.isal.alloc.nlsf 1

; Get index
    copy.w  i#1 i#0     ; index
    c.ld.i.fmp  FRAME_METHOD
    c.addi      METHOD_SPEC_DECLARING_CLAZZ
    c.ld.i      ; es: ..., calling_clazz
    pop.es.w    i#0     ; clazz

; Call method resolution -  class in i#0, index in i#1
; TODO we know the contents is resolved, so we just want clazz->values[index]
    move.i.i32  i#2 getIMethodConstant_unsafe
    call        i#2     ; leaves cld_method in i#0

    em.isal.dealloc.nlsf 1

    ;      Stack: ..., objectref, [arg1, [arg2...]], cld_method
    c.dup       ; ..., objectref, [arg1, [arg2...]], cld_method, cld_method
    c.addi  METHOD_EXEC_ARG_I
    c.ld.i      ; ..., objectref, [arg1, [arg2...]], cld_method, arg_i
    c.pick      ; ..., objectref, [arg1, [arg2...]], cld_method, objectref
; Check if objectref is null
    c.dup
    c.addi  0
    c.brs.z _exception_nullpointer
    c.drop

    em.isal.alloc.nlsf 2
; Now we have the called method in i#0 and the objectref in i#1
; Call emul_interface_target to get the real target method
    move.i.i32  i#2 emul_interface_target
    call        i#2
; target method is now in i#0

; fall through to e_invoke_common

_invoke_common:
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
; e_ireturn
;
; Emulates ireturn (0xAC)
;
; Stack: ..., value -> ..., value
;
;===========================================================
e_ireturn:
    em.push.es  1
    c.callw     _emul_check_frame_stacks
    em.pop.es   1
    c.callw     _emul_deallocate_frame
    c.rete

;===========================================================
; e_freturn
;
; Emulates freturn (0xAE)
;
; Stack: ..., value -> ..., value
;
;===========================================================
e_freturn:
    em.push.es  1
    c.callw     _emul_check_frame_stacks
    em.pop.es   1
    c.callw     _emul_deallocate_frame
    c.rete

;===========================================================
; e_areturn
;
; Emulates areturn (0xB0)
;
; Stack: ..., value -> ..., value
;
;===========================================================
e_areturn:
    em.push.es  1
    c.callw     _emul_check_frame_stacks
    em.pop.es   1
    c.callw     _emul_deallocate_frame
    c.rete

;===========================================================
; e_lreturn
;
; Emulates lreturn (0xAD)
;
; Stack: ..., value1, value2 -> ..., value1, value2
;
;===========================================================
e_lreturn:
    em.push.es  2
    c.callw     _emul_check_frame_stacks
    em.pop.es   2
    c.callw     _emul_deallocate_frame
    c.rete

;===========================================================
; e_dreturn
;
; Emulates lreturn (0xAF)
;
; Stack: ..., value1, value2 -> ..., value1, value2
;
;===========================================================
e_dreturn:
    em.push.es  2
    c.callw     _emul_check_frame_stacks
    em.pop.es   2
    c.callw     _emul_deallocate_frame
    c.rete

;===========================================================
; e_return
;
; Emulates return (0xB1)
;
; Stack: ... -> ...
;
;===========================================================
e_return:
    c.callw _emul_check_frame_stacks
    c.callw _emul_deallocate_frame
    c.rete

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

    ; Copied from c0start.s:
    ; ----------------------
    ; Exception code is at the top of the ISAC evaluation stack,
    ; that is register ESTR.
    ; The exception was caused by the assembly instruction right before
    ; where register ERAR points to. Beware branches and returns, for which
    ; ERAR might point to somewhere else than after the problematic instruction.
    ;
    ; Some possible exception codes:
    ; 0x13 -- Division with zero up to 4-byte values.
    ; 0x14 -- Division with zero of 8-byte values.
    ; 0x20 -- Execution Stack (ISAC Memory Stack) overflow;
    ;         try to increase the stack size of software task.
    ; 0x30 -- Invalid service request (check usage of reqfs).
    ;
    ; See document IMX-SYS6003 for a complete list and description.
    errorpoint
    c.rete

; Throw an ArithmeticException
_exception_arthmetic:
    c.drop
    c.ldi.i clazzArithmeticException
    c.jumpw _exception_raise

; Throw a NullPointerException
_exception_nullpointer:
    c.drop
    c.ldi.i clazzNullPointerException
    c.jumpw _exception_raise

; Throw an ArrayIndexOutOfBoundsException
_exception_arrayindex:
    c.drop
    c.ldi.i clazzArrayIndexOutOfBoundsException
    c.jumpw _exception_raise

; Throw a StackOverflowError
_exception_stackoverflow:
    c.drop
    c.ldi.i clazzStackOverflowError
    c.jumpw _exception_raise

; Instatiate and throw exception
; es: ..., exception_class
.global _exception_raise    ; Used to raise exception in invoke.s
_exception_raise:

; Instantiate class
    ; Calling: w_instance createRuntimeException(w_clazz excClazz)
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
    set_bc_emulation iload 21
    set_bc_emulation lload 22
    set_bc_emulation iload 23
    set_bc_emulation lload 24
    set_bc_emulation iload 25
    set_bc_emulation istore 54
    set_bc_emulation lstore 55
    set_bc_emulation istore 56
    set_bc_emulation lstore 57
    set_bc_emulation istore 58
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
