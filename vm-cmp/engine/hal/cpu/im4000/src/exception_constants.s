
;===========================================================
; Microcoded Exception codes
;
EC_NULLPOINTEREXCEPTION = 0x11
EC_ARRAYINDEXOUTOFBOUNDSEXCEPTION = 0x12
EC_ARITHMETICEXCEPTION = 0X13
EC_STACKOVERFLOWERROR = 0xFF

;===========================================================
; Hardcoded class name
;
; TODO: Could refer to corresponding entries in classmap[].
_exception_ArithmeticException: .asciz "java/lang/ArithmeticException"
_exception_NullPointerException: .asciz "java/lang/NullPointerException"
_exception_ArrayIndexOutOfBoundsException: .asciz "java/lang/ArrayIndexOutOfBoundsException"
_exception_StackOverflowError: .asciz "java/lang/StackOverflowError"
