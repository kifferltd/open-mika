
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

;	Check if only embedded locals needed
	c.dup
    c.addi	-8
    c.br.c	allocate_loc_10		    ;also memory locals ?

	c.addi	8			            ;adjust number
	c.ldi.b	0
    c.aml				            ;allocate empty memory stack
	c.jumps	allocate_loc_20

; Allocate memory locals
allocate_loc_10:
    c.aml				            ;allocate memory stack
	c.ldi.b	8

; Allocate embedded locals
allocate_loc_20:
    c.dup
    c.als				            ;allocate embedded stack
    c.st.i.fmp	FRAME_LS_COUNT

	c.mstvh                         ; Move the arguments to locals

; Save current esp, lsp and lmp
	c.flld.esp
    c.st.i.fmp	 FRAME_ESP

	c.flld.lsp
    c.st.i.fmp    FRAME_LSP

	c.ld.lmp	
    c.st.i.fmp	FRAME_LMP

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
	move.i.i32 i#11 0x2
    alloc.nlsf i#11

; i#12 contains parameter 'method'
; i#13 contains parameter 'narg'
; i#14 contains parameter 'args'

    ; Temp for debug. Can be removed
    stack.save i#1          ; i#1 = MSP

; Start setup a Java stack frame
    c.push.rar              
    c.push.fmp

; Stack address to activate_frame_return as ERAR
    c.ldi.i     activate_frame_return
    c.push.es               

; Get and push number of locals
    copy.w      i#1 i#12
    add.i.i8    i#1 i#1 METHOD_EXEC_LOCAL_I
    load.h      s#2 i#1
    push.es.h   s#2         ; es: ..., locals_i
    c.push.es               ; push FRAME_LS_COUNT

    c.ldi.b SIZEOF_FRAME-4  ; Allocate rest of the frame
    c.ams

; Copy the arguments to the evaluation stack
    copy.w      i#1 i#13    ; i#1 = narg
    br.cmp.i.i8.eq   activate_frame_10 i#1 0         ; Any arguments?

activate_frame_5:
    load.w      i#2 i#14            ; Next arg to i#2
    push.es.w   i#2

    add.upd.i.i4 i#14 0x4
    sub.upd.i.i4 i#1 0x1
    br.cmp.i.i8.ne activate_frame_5 i#1 0         ; More arguments?

; Push narg onto evaluation stack
activate_frame_10:
    push.es.w   i#13

; Set FMP to point to the frame
    stack.save  i#1                 ; i#4 = MSP
    set.fmp     i#1                 ; FMP = MSP

; store method pointer in the frame
    push.es.w   i#12
    c.st.i.fmp  FRAME_METHOD

;; Push locals count onto the evaluation stack
    c.ld.i.fmp  FRAME_LS_COUNT

    c.callw     allocate_locals

; Jump to java_method1 - for testing only
;    c.ldi.i     java_method1
;    .short      0xf9c2      ; c.jump.java

; jump to method code
    copy.w      i#3 i#12
    add.i.i8    i#3 i#3 METHOD_EXEC_CODE
    load.w      i#4 i#3
    push.es.w   i#4         ; es: ..., method_code
    .short      0xf9c2      ; c.jump.java

activate_frame_return:
    dealloc.nlsf
    ret
;===========================================================
