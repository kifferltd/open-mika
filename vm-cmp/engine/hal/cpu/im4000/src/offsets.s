;===========================================================
; Offsets in the w_Method struct (See methods.h)
;
SIZEOF_METHOD_SPEC          = 20     ; size of struct w_MethodSpec

METHOD_METHOD_SPEC          = 0      
METHOD_SLOT                 = SIZEOF_METHOD_SPEC      
METHOD_FLAGS                = METHOD_SLOT + 4
METHOD_METHOD               = METHOD_FLAGS + 4
METHOD_PARENT               = METHOD_METHOD + 4
METHOD_DUMMY                = METHOD_PARENT + 4
METHOD_NUM_THROWS           = METHOD_DUMMY + 2
METHOD_THROWS               = METHOD_NUM_THROWS + 2
; Here starts w_MethodExec
METHOD_DISPATCHER           = METHOD_THROWS + 4
METHOD_ARG_I                = METHOD_DISPATCHER + 4
METHOD_FUNCTION             = METHOD_ARG_I + 4        
METHOD_RETURN_I             = METHOD_FUNCTION + 4
METHOD_LOCAL_I              = METHOD_RETURN_I + 2
METHOD_STACK_I              = METHOD_LOCAL_I + 2
METHOD_NARGS                = METHOD_STACK_I + 2
METHOD_CODE_LENGTH          = METHOD_NARGS + 2
METHOD_CODE                 = METHOD_CODE_LENGTH + 4
METHOD_NUM_EXCEPTIONS       = METHOD_CODE + 4
METHOD_EXCEPTIONS           = METHOD_NUM_EXCEPTIONS + 2
METHOD_DEBUG_INFO           = METHOD_EXCEPTIONS + 4
METHOD_STATUS_ARRAY         = METHOD_DEBUG_INFO + 4
METHOD_BASIC_BLOCKS         = METHOD_STATUS_ARRAY + 4

;----------------------------------------------------------
; Offsets in the w_Clazz struct
;


;===========================================================
; Offsets in the Java stack frame on the memory stack
;
FRAME_CONSTANTPOOL	  = 0
FRAME_METHOD		  = 1
FRAME_SYNCOBJECT	  = 2
FRAME_THROWPC		  = 3
FRAME_LSP		      = 4
FRAME_ESP		      = 5
FRAME_LMP		      = 6
;
; The following offsets order is used by pop/push sequences in the program
;
FRAME_LS_COUNT		  = 7
FRAME_RETADDRESS	  = 8
FRAME_FMP		      = 9
FRAME_RAR		      = 10

FRAME_SIZEOF          = 11	; Size in cells of a frame 
;===========================================================
