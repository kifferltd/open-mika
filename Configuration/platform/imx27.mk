export CPU_MIPS = 200
export DEFAULT_HEAP_SIZE = 64M
export USE_NATIVE_MALLOC = true

export FLOATING_POINT = hauser
export SHARED_OBJECTS = true

export HOSTOS = linux
export CPU = arm
export TOOLCHAIN_PREFIX = arm-linux-
export SCHEDULER = o4p

export CCFLAGS += -march=armv5te -mtune=arm926ej-s -O3 -DSTORE_METHOD_DEBUG_INFO

export JDWP = true


