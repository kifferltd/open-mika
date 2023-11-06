###########################################################################
# Copyright (c) 2019, 2020, 2021, 2022, 2023 by Chris Gray, KIFFER Ltd.   #
#  All rights reserved.                                                   #
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

export CPU_MIPS = 200
export DEFAULT_HEAP_SIZE default = 16M
export USE_NATIVE_MALLOC = true
export USE_LIBFFI = false

export AWT = none
export FLOATING_POINT = native
export MATH = java
export SHARED_OBJECTS = false

export HOSTOS = freertos
export CPU = im4000
export TOOLCHAIN = clang
export TOOLCHAIN_PREFIX = $(IMSYS_TOOLDIR)/
export SCHEDULER = o4f

ifeq ($(ISALFEATURE),0)
  CFLAGS += -misal
else ifeq ($(ISALFEATURE),1)
  CFLAGS += -misal-phase1 -misal-phase1only -D__imsysisal_phase1__
else
  $(error UNKNOWN ISALFEATURE)
endif
CFLAGS += -nostdlib 
# Use -isystem instead of -I to suppress warnings generated by those header files
CFLAGS += -isystem$(NEWLIB_INCLUDE_DIR) 
CFLAGS += -isystem$(ISAL_SYSTEM_INCLUDE_DIR) 
CFLAGS += -isystem$(FREERTOS_KERNEL_INCLUDE_DIR) 
CFLAGS += -isystem$(IMSYS_IM4000_INCLUDE_DIR) 
CFLAGS += -isystem$(FREERTOS_PORT_INCLUDE_DIR) 
CFLAGS += -isystem$(FREERTOS_FAT_INCLUDE_DIR) 
CFLAGS += -isystem$(FREERTOS_TCP_INCLUDE_DIR) 
CFLAGS += -isystem$(FREERTOS_TCP_COMPILER_DIR) 
# CFLAGS += -isystem$(FREERTOS_CLI_INCLUDE_DIR)
CFLAGS += -isystem/home/chris/Imsys/env-isal/sw-imsys-freertos/libraries/freertos_plus/standard/freertos_plus_cli/include
CFLAGS += -isystem$(FREERTOS_APP_INCLUDE_DIR) 
CFLAGS += -isystem$(FREERTOS_IO_INCLUDE_DIR) 
CFLAGS += -DSTORE_METHOD_DEBUG_INFO

export JDWP = true
export JNI = false
export NETWORK = native
export TESTS = true

# Root directory for Mika is root of FreeRTOS filesystem
export FSROOT = /

#
# If Mika is run with no -Xbootclasspath parameter, the bootstrap class 
# loader will search the zip/jarfile $(BOOTCLASSFILE) or the directory
# $(BOOTCLASSSUBDIR), located in $(BOOTCLASSDIR).
# These variables also specify where the zipfile of bootstrap classes will
# be created at build time, and what name it will be given. 
#

# export USE_ROMFS = true
BOOTCLASSDIR  = /lib/mika
BOOTCLASSFILE = mcl.jar
# BOOTCLASSSUBDIR = mcl

# Set this false to create uncompressed jar files (including mcl.jar)
COMPRESS_JAR_FILES = true

# FreeRTOS-specific stuff which is defined here because choices may depend on the platform-specific
# configuration of FreeRTOS.

# TODO should this be settable at cmake level?
# CFLAGS += -DFREERTOS_CLI

# If JAVA_THREAD_YIELD_IS_FREERTOS_DELAY is defined in CFLAGS, the Thread.yield() method will be
# implmemted as vTaskDelay(1) rather than as taskYIELD().

CFLAGS += -DJAVA_THREAD_YIELD_IS_FREERTOS_DELAY -DUSE_OBJECT_HASHTABLE 

# Enable this line if you want to run bytecode tests instead of an application
# export BYTECODETEST = true

