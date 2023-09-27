;===========================================================
; Offsets in the w_Field struct (See fields.h)
; Automatically generated
;
FIELD_DECLARING_CLAZZ = 0
FIELD_NAME = 8
FIELD_VALUE_CLAZZ = 12
FIELD_FLAGS = 16
FIELD_SLOT = 20
FIELD_DESC = 24
FIELD_INITVAL = 28

; Flags within FIELD_FLAGS word
FIELD_IS_LONG      = 0x10000
FIELD_IS_REFERENCE = 0x20000
FIELD_IS_ARRAY     = 0x40000
FIELD_IS_WOTSIT    = 0x80000


;===========================================================
; Offsets in the w_Method struct (See methods.h)
; Automatically generated
;
METHOD_SPEC = 0
METHOD_SPEC_DECLARING_CLAZZ = 0
METHOD_SPEC_NAME = 4
METHOD_SPEC_DESC = 8
METHOD_SPEC_ARG_TYPES = 12
METHOD_SPEC_RETURN_TYPE = 16
METHOD_SLOT = 20
METHOD_FLAGS = 24
METHOD_PARENT = 28
METHOD_NUM_THROWS = 34
METHOD_THROWS = 36
METHOD_EXEC = 40
METHOD_EXEC_DISPATCHER = 40
METHOD_EXEC_ARG_I = 44
METHOD_EXEC_FUNCTION = 48
METHOD_EXEC_RETURN_I = 52
METHOD_EXEC_LOCAL_I = 54
METHOD_EXEC_STACK_I = 56
METHOD_EXEC_NARGS = 58
METHOD_EXEC_CODE_LENGTH = 60
METHOD_EXEC_CODE = 64
METHOD_EXEC_NUM_EXCEPTIONS = 68
METHOD_EXEC_EXCEPTIONS = 72



;----------------------------------------------------------
; Offsets in the w_Clazz struct
;


;===========================================================
; Offsets in the Java stack frame on the memory stack.
; Note: accessed via FMP register, offset is in words.
; For now the C emulation only uses FRAME_METHOD.

FRAME_CONSTANTPOOL = 0
FRAME_METHOD       = 1
FRAME_SYNCOBJECT   = 2
FRAME_THROWPC      = 3
FRAME_LSP          = 4
FRAME_ESP          = 5
FRAME_LMP          = 6

;
; The following offsets order is used by pop/push sequences in the program
;
FRAME_LS_COUNT      = 7
FRAME_RETADDRESS    = 8
FRAME_FMP           = 9

SIZEOF_FRAME = 10

;===========================================================
