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
export SCHEDULER = o4p

export CFLAGS += -m32
export CFLAGS += -DSTORE_METHOD_DEBUG_INFO
export JDWP = true
export ENABLE_THREAD_RECYCLING = true

# if AWT=rudolph then AWT_DEF must also be specified
export AWT=rudolph
export AWT_DEF=xsim
# when AWT = rudolph, AWT_DEVICE can have values 'fdev' for frame buffer
# or 'xsim' for simulation under X; AWT_PIXELFORMAT can take the following
# values:      c565 : 16-bit color
#              c332 : 8-bit color
#                g4 : 4-bit grayscale;
# and AWT_INVERSENIBBLES and AWT_INVERSEDISPLAY handle various display driver
# characteristics, see the source for more details.
#
export AWT_DEVICE = xsim ;
export AWT_PIXELFORMAT = c555 ;
export AWT_INVERSENIBBLES default = false ;
export AWT_INVERSEDISPLAY default = false ;
export AWT_GIF_SUPPORT = true ;
export AWT_JPEG_SUPPORT = true ;
export CCFLAGS += -DRUDOLPH_DEFAULT_FRAME_TITLE='\"Mika\"' ;
#
# Defines the screen with to be default 720 by 420
#
export CCFLAGS += -DRUDOLPH_DEFAULT_FRAME_WIDTH=720 ;
export CCFLAGS += -DRUDOLPH_DEFAULT_FRAME_HEIGHT=420 ;

TOOLCHAIN_PREFIX = /usr/bin/

