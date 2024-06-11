export AWT=none
export CPU_MIPS = 1000
ifndef DEFAULT_HEAP_SIZE
  export DEFAULT_HEAP_SIZE = 64M
endif
export USE_NATIVE_MALLOC = true
export USE_LIBFFI = true
export BYTECODE_VERIFICATION = true

export FLOATING_POINT = hauser
export MATH = native
export SHARED_OBJECTS = true

export HOSTOS = linux
export CPU = x86
export TOOLCHAIN = gcc
export SCHEDULER = o4p

export CFLAGS += -m32
export CFLAGS += -DSTORE_METHOD_DEBUG_INFO
export JDWP = true
export ENABLE_THREAD_RECYCLING = true

export TOOLCHAIN_PREFIX = /usr/bin/


