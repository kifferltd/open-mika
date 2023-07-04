###########################################################################
# Copyright (c) 2018, 2020, 2021, 2022, 2023 by Chris Gray, KIFFER Ltd.   #
# All rights reserved.                                                    #
#                                                                         #
# Redistribution and use in source and binary forms, with or without      #
# modification, are permitted provided that the following conditions      #
# are met:                                                                #
# 1. Redistributions of source code must retain the above copyright       #
#    notice, this list of conditions and the following disclaimer.        #
# 2. Redistributions in binary form must reproduce the above copyright    #
#    notice, this list of conditions and the following disclaimer in the  #
#    documentation and/or other materials provided with the distribution. #
# 3. Neither the name of KIFFER Ltd nor the names of other contributors   #
#    may be used to endorse or promote products derived from this         #
#    software without specific prior written permission.                  #
#                                                                         #
# THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          #
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    #
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    #
# IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    #
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      #
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS #
# OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   #
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     #
# STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   #
# IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      #
# POSSIBILITY OF SUCH DAMAGE.                                             #
###########################################################################

CFLAGS = -g
CFLAGS += -Wall -Wsign-compare -Wshadow -Wpointer-arith
CFLAGS += -Wstrict-prototypes -Wformat -Winline -DDEBUG_LEVEL=7

# for clang: suppress warnings generated by mika sources
CFLAGS += \
    -Wno-assign-enum \
    -Wno-bad-function-cast \
    -Wno-cast-align \
    -Wno-cast-function-type \
    -Wno-cast-qual \
    -Wno-conditional-uninitialized \
    -Wno-constant-logical-operand \
    -Wno-date-time \
    -Wno-declaration-after-statement \
    -Wno-documentation-unknown-command \
    -Wno-enum-conversion \
    -Wno-extra-semi-stmt \
    -Wno-format \
    -Wno-format-extra-args \
    -Wno-format-invalid-specifier \
    -Wno-gnu-label-as-value \
    -Wno-implicit-fallthrough \
    -Wno-implicit-function-declaration \
    -Wno-implicit-int \
    -Wno-implicit-int-conversion \
    -Wno-incompatible-pointer-types \
    -Wno-int-conversion \
    -Wno-macro-redefined \
    -Wno-missing-field-initializers \
    -Wno-missing-noreturn \
    -Wno-missing-prototypes \
    -Wno-missing-variable-declarations \
    -Wno-padded \
    -Wno-parentheses \
    -Wno-pedantic \
    -Wno-pointer-integer-compare \
    -Wno-pointer-sign \
    -Wno-reserved-identifier \
    -Wno-reserved-macro-identifier \
    -Wno-return-type \
    -Wno-shorten-64-to-32 \
    -Wno-sign-conversion \
    -Wno-strict-prototypes \
    -Wno-unknown-escape-sequence \
    -Wno-unused-function \
    -Wno-unused-macros \
    -Wno-unused-parameter \
    -Wno-unused-variable \
    -Wno-variadic-macros \
    -Wno-visibility \
    -Wno-zero-length-array \
    -fno-inline-functions

export MIKA_TOP = $(PWD)

include ./Configuration/platform/$(PLATFORM).mk
include ./Configuration/cpu/$(CPU).mk
include ./Configuration/toolchain/$(TOOLCHAIN).mk
include ./Configuration/host/$(HOSTOS).mk
include ./Configuration/mika/default.mk

export VERSION_STRING ?= "Snapshot_$(PLATFORM)-$(shell date +%Y%m%d)-$(shell git rev-parse --short HEAD)"
export AWT_DEF ?= none
export APP_DIR ?= $(MIKA_TOP)/sample/apps/java
export CPU HOSTOS SCHEDULER
export AR
export JAVAC ?= ${JAVA6_HOME}/bin/javac

# TODO: JAVAX could be java5 (or whatever)
export JAVAX = java
export javadir = $(MIKA_TOP)/core-vm/$(JAVAX)

# if COMPRESS_JAR_FILES is set to false, we will add the -0 flag to the jar command.
# otherwise we will not add this flag and the jar command will default to compression level 9.
ifeq ($(COMPRESS_JAR_FILES),false)
  export JAR_CMD_COMPRESSION_LEVEL=0
endif

export ENGINE ?= hal/cpu/im4000
export enginedir = $(MIKA_TOP)/vm-cmp/engine/$(ENGINE)
export enginecomdir = $(MIKA_TOP)/vm-cmp/engine/common

export SECURITY ?= none
export securitydir = $(MIKA_TOP)/vm-cmp/security/$(SECURITY)/$(JAVAX)

# TODO: SECURITY_PROVIDER can take different values e.g. harmony or none
export SECURITY_PROVIDER ?= none
export secprovdir = $(MIKA_TOP)/vm-cmp/security/provider/$(SECURITY_PROVIDER)/$(JAVAX)
# FIXME only if SECURITY_PROVIDER is not 'none'
ifneq ($(SECURITY), none)
  export secanyprovdir = $(MIKA_TOP)/vm-cmp/security/provider/any/$(JAVAX)
endif

export javamathdir = $(MIKA_TOP)/vm-cmp/math/${MATH}/$(JAVAX)

# TODO JAR really means JAR_VERIFIER
# TODO JAR can be java or none
export JAR = none
export javajardir = $(MIKA_TOP)/vm-cmp/jar/$(JAR)

export classpath = $(javadir):$(javamathdir):$(securitydir):$(secprovdir):$(javajardir)

export tooldir = $(MIKA_TOP)/tool
export scriptdir = $(tooldir)/script
export builddir = $(MIKA_TOP)/build/$(PLATFORM)
export libdir = $(builddir)/lib
export objdir = $(builddir)/obj
export classdir = $(builddir)/class
export toolclassdir = $(MIKA_TOP)/tool/class
export emptydir = $(builddir)/java/empty

export awtobjdir = $(objdir)/awt/$(AWT)
export engineobjdir = $(objdir)/vm-cmp/engine
export filesystemobjdir = $(objdir)/filesystem/$(FILESYSTEM)
export fpobjdir = $(objdir)/fp/$(FLOATING_POINT)
export mathobjdir = $(objdir)/math/$(MATH)
export networkobjdir = $(objdir)/network/$(NETWORK)
export schedulerobjdir = $(objdir)/kernel/$(SCHEDULER)

export imagedir = $(MIKA_TOP)/image/$(PLATFORM)
export deploydir = $(MIKA_TOP)/deploy/$(PLATFORM)
export mikadeploydir = $(deploydir)/lib/mika
export appdeploydir = $(deploydir)/app
export testdeploydir = $(deploydir)/test
export tooldeploydir = $(MIKA_TOP)/deploy/tool

CFLAGS += -I $(MIKA_TOP)/vm-cmp/fp/$(FLOATING_POINT)/include

ifeq "$(AWT)" "rudolph"
  include $(MIKA_TOP)/Configuration/awt/$(AWT_DEF).mk
else ifneq "$(AWT)" "none"
  $(error AWT must be rudolph or none, not $(AWT))
endif

ifdef UNIX
  LIBPREFIX ?= lib
else
  LIBPREFIX ?= ""
endif

export CCLASSPATH LIBPREFIX

ifeq ($(CPU),x86)
  LDFLAGS = -m32
endif

ifeq ($(HOSTOS), linux)
  CFLAGS += -DHAVE_TIMEDWAIT
endif

ifeq ($(HOSTOS), android)
  CFLAGS += -DHAVE_TIMEDWAIT
  CFLAGS += --sysroot $(TOOLCHAIN_SYSROOT)
  LDFLAGS += --sysroot $(TOOLCHAIN_SYSROOT)
endif

ifeq ($(HOSTOS), netbsd)
  CFLAGS += -DHAVE_TIMEDWAIT
  LDFLAGS += -m32
endif

#
# Set the compiler for the tools we need. We use the default compiler
# on this compiling host.
#

TOOL_CC   = $(CC)
TOOL_LINK = $(LD)

SCRIPT_DIR = $(MIKA_TOP)/tool/script

#
# The libraries
#

UNICODE_LIB = libunicode
FS_LIB    = libfs
export OSWALD_LIB = $(libdir)/liboswald.a
export AWT_LIB = $(libdir)/libawt.a
export MIKA_LIB = $(libdir)/libmika.a

ifdef UNIX
  BUILD_HOST = `uname`
else
  BUILD_HOST = <unknown>
endif


CFLAGS += -DBOOTCLASSDIR=\"$(BOOTCLASSDIR)\"
CFLAGS += -DBOOTCLASSFILE=\"$(BOOTCLASSFILE)\"
CFLAGS += -DEXTCLASSDIR=\"$(BOOTCLASSDIR)/ext\"
CFLAGS += -DDEFAULT_HEAP_SIZE=\"$(DEFAULT_HEAP_SIZE)\"
CFLAGS += -DDEFAULT_STACK_SIZE=\"$(DEFAULT_STACK_SIZE)\"
CFLAGS += -DVERSION_STRING=\"$(VERSION_STRING)\"

ifdef USE_ROMFS
  CFLAGS += -DUSE_ROMFS
  mcltarget = romfs
else ifdef BOOTCLASSSUBDIR 
  CFLAGS += -DBOOTCLASSSUBDIR=\"$(BOOTCLASSSUBDIR)\"
  mcltarget = mcldir
else
  mcltarget = jarfile
endif

ifeq ($(JAVA5_SUPPORT), true)
  CFLAGS += -DJAVA5
endif

ifeq ($(JNI), true)
  CFLAGS += -DJNI
endif

ifeq ($(JDWP), true)
  CFLAGS += -DJDWP
endif

ifeq ($(JAVA_PROFILE), true)
  CFLAGS += -DJAVA_PROFILE
endif

ifeq ($(TRACE_MEM_ALLOC), true)
  CFLAGS += -DTRACE_MEM_ALLOC
endif

ifeq ($(METHOD_DEBUG), true)
  CFLAGS += -DMETHOD_DEBUG
endif

ifdef UNICODE_SUBSETS
  CFLAGS += -DUNICODE_SUBSETS=\"$(UNICODE_SUBSETS)\"
endif

ifeq ($(USE_NATIVE_MALLOC), true)
  CFLAGS += -DUSE_NATIVE_MALLOC
endif

# For now we support both USE_LIBFFI (boolean) and FFI ("libffi" or "bruteforce")
# USE_LIBFFI should be phased out
ifdef $FFI
  ifeq ($(FFI), libffi)
    CFLAGS += -DUSE_LIBFFI
    FFI = libffi
  else
    FFI = bruteforce
  endif
else
  ifeq ($(USE_LIBFFI), true)
    CFLAGS += -DUSE_LIBFFI
    FFI = libffi
  else
    FFI = bruteforce
  endif
endif

ifdef CPU_MIPS
  CFLAGS += -DCPU_MIPS=$(CPU_MIPS)
endif

ifeq ($(USE_NANOSLEEP), true)
  CFLAGS += -DUSE_NANOSLEEP
endif

ifdef HOST_TIMER_GRANULARITY
  CFLAGS += -DHOST_TIMER_GRANULARITY=$(HOST_TIMER_GRANULARITY)
endif

ifeq ($(DETECT_FLYING_PIGS), true)
  CFLAGS += -DDETECT_FLYING_PIGS
endif

ifeq ($(ENABLE_THREAD_RECYCLING), true)
  CFLAGS += -DENABLE_THREAD_RECYCLING
endif

ifeq ($(JAVAX_COMM), true)
  CFLAGS += -DJAVAX_COMM
endif

ifeq ($(MIKA_MAX), true)
  CFLAGS += -DMIKA_MAX
  CFLAGS += -DRESMON
endif

# Make USE_APP_DIR = true the default for now
USE_APP_DIR ?= true

ifeq ($(USE_APP_DIR), true)
  CCLASSPATH := /app:$(CCLASSPATH)
endif

ifdef CCLASSPATH
  CFLAGS += -DCLASSPATH=\"$(CCLASSPATH)\"
endif

#
# Add the -static flag to the linker if a static binary is required.
#

ifeq ($(STATIC), true)
  LDFLAGS += -static
endif


ifeq ($(BYTECODE_VERIFIER), true)
  CFLAGS += -DUSE_BYTECODE_VERIFIER 
endif

#
# NOTE: The module loader doesn't like gdb symbols.
#
ifndef GDB_SYMBOLS
  GDB_SYMBOLS = false
endif

ifeq ($(DEBUG), true)
  JFLAGS += -g
  
  CFLAGS += -DDEBUG -DRUNTIME_CHECKS
  
  ifeq ($(HOSTOS), winnt)
    ifeq ($(OPTIM), "-O")
      OPTIM = -O1
    else
      OPTIM = $(OPTIM) -O1
    endif
          #CG optimise for size
    ifeq ($(OPTIM), "-O")
      OPTIM = -Os
    else
      OPTIM = $(OPTIM) -Os
    endif
  else
    OPTIM = -O0
  endif

  GDB_SYMBOLS = true
  
else  # No debug -> Full optimalisation 
          #CG optimise for size
          #CG 20220221 leave this out for now

#  ifeq ($(OPTIM), "-O")
#    OPTIM = -Os
#  else
#    OPTIM = $(OPTIM) -Os
#  endif

          #CG 20220221 for lldb
  CFLAGS += -ggdb
endif

CFLAGS += $(OPTIM)

ifeq ($(GDB_SYMBOLS), true)
    CFLAGS += -ggdb  
endif

#
# Only add -fomit-frame-pointer when debugging and profiling are turned off.
# Debugging with the flag on, gives a lot less information. Profiling with the
# flag on is not possible.
#
#
ifeq ($(DEBUG), false)
  ifeq ($(SCHEDULER), oswald)
  CFLAGS += -fomit-frame-pointer
  endif
endif

ifndef UPTIME_LIMIT
  UPTIME_LIMIT = none
endif

ifndef ACADEMIC_LICENCE
  ACADEMIC_LICENCE = false
endif

ifneq ($(UPTIME_LIMIT), none)
  CFLAGS += -DUPTIME_LIMIT=$(UPTIME_LIMIT)
endif 

ifeq ($(ACADEMIC_LICENCE), true)
  CFLAGS += -DACADEMIC_LICENCE
endif 

ifeq ($(AWT_SWAPDISPLAY), true)
  CFLAGS += -DAWT_SWAPDISPLAY
endif
  
CFLAGS_scheduler_oswald = -DOSWALD -D_REENTRANT
CFLAGS_scheduler_o4e = -DO4E -pthread
CFLAGS_scheduler_o4p = -DO4P -D_REENTRANT # -pthread
CFLAGS_scheduler_o4f = -DO4F -D_REENTRANT

CFLAGS += $(CFLAGS_scheduler_$(SCHEDULER))
export kernobjdir = $(objdir)/kernel/$(SCHEDULER)

#
# Print out the configuration settings.
#

ifeq "$(AWT)" "rudolph"
  ifeq "$(AWT_DEVICE)" "none"
    CFLAGS += -DAWT_NONE
  else ifeq "$(AWT_DEVICE)" "fdev"
# TODO check that AWT_PIXELFORMAT is one of c332 c555 c565 g4 pp888
# TODO check that AWT_MOUSE is one of touchscreen ps2mouse none
    CFLAGS += -DAWT_FDEV -DAWT_PIXELFORMAT_$(AWT_PIXELFORMAT)
  else ifeq "$(AWT_DEVICE)" "xsim"
    CFLAGS += -DAWT_XSIM
    LDFLAGS += -lX11
  else
    $(error AWT_DEVICE must be fdev, xsim, or none)
  endif
  CFLAGS += -DRUDOLPH 
endif

ifeq "$(FILESYSTEM)" "vfs"
  CFLAGS += -DFSENABLE
else ifeq "$(FILESYSTEM)" "native"
  CFLAGS += -DFSROOT=\"$(FSROOT)\"
else
  $(error FILESYSTEM must be "vfs" or "native")
endif

ifeq "$(FLOATING_POINT)" "native"
  CFLAGS += -DNATIVE_FP
  LDFLAGS += -lm
else ifeq "$(FLOATING_POINT)" "hauser"
  CFLAGS += -DHAUSER_FP
else
  $(error FLOATING_POINT must be "native" or "hauser")
endif

ifeq "$(MATH)" "native"
  CFLAGS += -DNATIVE_MATH
else ifneq "$(MATH)" "java"
  $(error MATH must be "native" or "java")
endif

ifeq "$(NETWORK)" "native"
  export networkinc = $(MIKA_TOP)/vm-cmp/network/native/hal/hostos/$(HOSTOS)/include
else ifeq "$(NETWORK)" "none"
  export networkinc = $(MIKA_TOP)/vm-cmp/network/none/include
else
  $(error NETWORK must be "native" or "none")
endif

ifndef TESTS
  TESTS = false
endif

ifndef JAVAX_COMM
  JAVAX_COMM = false
endif

#
# Compiling for WinNT and Cygwin on an x86 gives errors with leading underscores.
#

ifeq ($(HOSTOS), winnt)
  CFLAGS += -fno-leading-underscore
endif

export ENGINE FILESYSTEM NETWORK
export JAVA5_SUPPORT JDWP JAVAX_COMM BYTECODE_VERIFIER
export FLOATING_POINT MATH UNICODE_SUBSETS
export ENABLE_THREAD_RECYCLING
export FFI
export UPTIME_LIMIT
export SCHEDULER USE_NANOSLEEP HAVE_TIMEDWAIT USE_NATIVE_MALLOC HOST_TIMER_GRANULARITY CPU_MIPS SHARED_HEAP

include WonkaInfo.mk
include KernelInfo.mk

export fsinc = $(MIKA_TOP)/vm-cmp/fs/native/hal/hostos/$(HOSTOS)/include

ifeq ($(SECURITY), fine)
#    ECHO "Warning: SECURITY=fine is deprecated, using SECURITY=java2"
    SECURITY = java2
endif
ifeq ($(SECURITY), coarse)
#    ECHO "Warning: SECURITY=coarse is deprecated, using SECURITY=java2";
    SECURITY = java2
endif

ifeq ($(TESTS), true)
  TEST_INFO = generating tests for Mauve and for the VisualTestEngine.
endif
ifeq ($(TESTS), false)
  TEST_INFO = not generating tests for Mauve and for the VisualTestEngine.
endif

ifeq "$(AWT)" "rudolph"
    AWT_INFO = Rudolph AWT\;

    ifeq "$(AWT_DEVICE)" "none"
        AWT_INFO += no visual display\;
    endif
    ifeq "$(AWT_DEVICE)" "fdev"
        AWT_INFO += frame buffer display\;
    endif
    ifeq "$(AWT_DEVICE)" "xsim"
        AWT_INFO += display is X window\;
    endif

    ifeq "$(AWT_GIF_SUPPORT)" "true"
      AWT_INFO += GIF support enabled\;
    endif

    ifeq "$(AWT_JPEG_SUPPORT)" "true"
      AWT_INFO += JPEG support enabled\;
    endif
endif

ifeq ($(AWT), none)
    AWT_INFO = no AWT\;
endif

CFLAGS += -DWONKA_INFO=\"" $(WONKA_INFO) "\"
CFLAGS += -DTEST_INFO=\"" $(TEST_INFO) "\"
CFLAGS += -DAWT_INFO=\"" $(AWT_INFO) "\"
ifeq ($(SCHEDULER), o4p)
  CFLAGS += -DO4P_INFO=\"" $(O4P_INFO) "\"
endif

ifeq ($(SCHEDULER), o4f)
  CFLAGS += -DO4F_INFO=\"" $(O4F_INFO) "\"
endif

ifeq ($(SCHEDULER), oswald)
  CFLAGS += -DOSWALD_INFO=\"" $(OSWALD_INFO) "\"
endif

export gendir = $(builddir)/generated

CFLAGS += -DBUILD_HOST=\"" $(BUILD_HOST) "\"

# TODO objdirlist not used anywhere?
# objdirlist += $(objdir)/awt/$(AWT)
# objdirlist += $(objdir)/filesystem/$(FILESYSTEM)
# objdirlist += $(objdir)/fp/$(FLOATING_POINT)
# objdirlist += $(objdir)/math/$(MATH)
# objdirlist += $(objdir)/network/$(NETWORK)

export CFLAGS
export JFLAGS
export LDFLAGS
export JNI

.PHONY : mika core-vm echo builddir install clean test common-test scheduler-test deployable binary jarfile mcldir romfs resource app image

mika : echo builddir deployable kernel core-vm test

# TODO : only depends on $(mcltarget) when this is romfs
$(MIKA_LIB) : $(mcltarget)
	@echo "mcltarget =" $(mcltarget)
	@echo "CFLAGS =" $(CFLAGS)
	make -C core-vm libs

libs: kernel $(MIKA_LIB)

kecho :
	@echo "Building $(SCHEDULER) kernel for platform '$(PLATFORM)'"
	@echo "CPU =" $(CPU)
	@echo "TOOLCHAIN =" $(TOOLCHAIN)
	@echo "HOSTOS =" $(HOSTOS)
	@echo "LDFLAGS =" $(LDFLAGS)

echo : kecho
	@echo "Building Mika for platform '$(PLATFORM)'"
	@echo "SCHEDULER =" $(SCHEDULER)
	@echo "AWT =" $(AWT)
	@echo "LDFLAGS =" $(LDFLAGS)
	@echo "SHARED_OBJECTS =" $(SHARED_OBJECTS)
	@echo "JNI =" $(JNI)
	@echo "TESTS =" $(TESTS)
	@echo "JDWP =" $(JDWP)
	@echo "MIKA_LIB = " $(MIKA_LIB)
	@echo "OSWALD_LIB = " $(OSWALD_LIB)
	@echo "AWT_LIB = " $(AWT_LIB)
	@echo "networkinc = " $(networkinc)
	@echo "fsinc = " $(fsinc)
ifdef USE_ROMFS
	@echo "using ROMFS"
endif
ifeq ($(JAR_CMD_COMPRESSION_LEVEL),0)
	@echo "Creating uncompressed jar files."
endif

builddir :
	@echo "Creating " $(imagedir)
	@mkdir -p $(imagedir)
	@echo "Creating " $(deploydir)
	@mkdir -p $(deploydir)
	@echo "Creating " $(mikadeploydir)
	@mkdir -p $(mikadeploydir)
	@echo "Creating " $(appdeploydir)
	@mkdir -p $(appdeploydir)
	@echo "Creating " $(testdeploydir)
	@mkdir -p $(testdeploydir)
	@echo "Creating " $(objdir)
	@mkdir -p $(objdir)
	@echo "Creating " $(testdeploydir)
	@mkdir -p $(testdeploydir)
	@echo "Creating " $(awtobjdir)
	@mkdir -p $(awtobjdir)
	@echo "Creating " $(filesystemobjdir)
	@mkdir -p $(filesystemobjdir)
	@echo "Creating " $(fpobjdir)
	@mkdir -p $(fpobjdir)
	@echo "Creating " $(mathobjdir)
	@mkdir -p $(mathobjdir)
	@echo "Creating " $(networkobjdir)
	@mkdir -p $(networkobjdir)
	@echo "Creating " $(schedulerobjdir)
	@mkdir -p $(schedulerobjdir)
	@echo "Creating " $(objdir)/generated
	@mkdir -p $(objdir)/generated
	@echo "Creating " $(libdir)
	@mkdir -p $(libdir)
	@echo "Creating " $(gendir)
	@mkdir -p $(gendir)
	@echo "Creating " $(classdir)
	@mkdir -p $(classdir)
	@echo "Creating " $(emptydir)
	@mkdir -p $(emptydir)

kernel : kecho builddir kcommon 
	make -C vm-cmp/kernel/$(SCHEDULER) 

kcommon : echo builddir
	make -C vm-cmp/kernel/common 

comm :
ifeq ($(JAVAX_COMM), true)
	make -C vm-ext/comm 
endif

max :
ifeq ($(MIKA_MAX), true)
	make -C max/src/native/mika/max 
endif

core-vm : builddir comm max 
	make -C core-vm 

binary : 
	@echo "TODO: here we would copy the open-mika binary to ${mikadeploydir}"

# FIXME: select right security dir(s)
jarfile : 
	# make -C ${secanyprovdir} classes
	make -C ${secprovdir} classes
	make -C ${securitydir} classes
	make -C ${javajardir} classes
	make -C core-vm/$(JAVAX) classes
	@echo "Building ${mikadeploydir}/mcl.jar from core-vm/resource/mcl.mf and classes in ${classdir}"
	${JAVA6_HOME}/bin/jar cmf$(JAR_CMD_COMPRESSION_LEVEL) core-vm/resource/mcl.mf ${mikadeploydir}/mcl.jar -C ${classdir} .

resource :
	@echo "Copying resources to ${mikadeploydir}"
	cp -r core-vm/resource/system/* ${mikadeploydir}	

app :
ifneq ($(APP_DIR),"none")
	make -C $(APP_DIR) classes
endif

deployable : binary $(mcltarget) resource app test

# note: image target was here
install : mika
	@echo "Installing mika binary in ${INSTALL_DIR}"
	cp core-vm/mika ${INSTALL_DIR}

clean :
	@rm -rf $(gendir)
	@rm -rf $(imagedir)
	@rm -rf $(deploydir)
	@rm -rf $(objdir)
	@rm -rf $(libdir)
	@rm -rf $(classdir)
	-make -C vm-cmp/kernel/$(SCHEDULER) clean
	-make -C vm-cmp/kernel/common clean
	-make -C vm-cmp/engine/$(ENGINE) clean
	-make -C vm-cmp/fs/$(FILESYSTEM) clean
	-make -C vm-cmp/network/$(NETWORK) clean
	-make -C vm-cmp/awt/$(AWT) clean
	-make -C tool clean
	-make -C vm-ext/jpda clean
	-make -C vm-cmp/fp/$(FLOATING_POINT) clean
	-make -C vm-cmp/security/provider/any/src/native/wonka/security clean
	-make -C vm-cmp/math/$(MATH) clean
	-make -C vm-ext/comm clean
	-make -C core-vm clean
	-make -C max/src/native/mika/max clean

test : echo builddir kernel $(mcltarget) scheduler-test
	@echo "Creating tools"
	make -C tool test

scheduler-test : common-test
	make -C vm-cmp/kernel/$(SCHEDULER) test

common-test :
	make -C vm-cmp/kernel/common test

#	make -C vm-cmp/fs/$(ENGINE) test
#	make -C vm-cmp/fs/$(FILESYSTEM) test
#	make -C vm-cmp/network/$(NETWORK) test
#	make -C vm-cmp/awt/$(AWT) test
#	make -C tool test
#	make -C vm-ext/jpda test
#	make -C vm-cmp/fp/$(FLOATING_POINT) test
#	make -C vm-cmp/security/provider/any/src/native/wonka/security test
#	make -C vm-cmp/math/$(MATH) test
#	make -C vm-ext/comm test
#	make -C max/src/native/mika/max test

include $(wildcard ./Configuration/platform/$(PLATFORM).mk2)
