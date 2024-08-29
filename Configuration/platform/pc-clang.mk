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

export CPU_MIPS ?= 1000
export DEFAULT_HEAP_SIZE ?= 64M
export USE_NATIVE_MALLOC ?= true
export USE_LIBFFI ?= true
export BYTECODE_VERIFICATION ?= true

export AWT ?= none
export FLOATING_POINT ?= hauser
export MATH ?= native
export SHARED_OBJECTS ?= true

export HOSTOS ?= linux
export CPU ?= x86
export TOOLCHAIN ?= clang
export TOOLCHAIN_PREFIX ?= /usr/bin/
export SCHEDULER ?= o4p

export PARALLEL_GC ?= true
export JDWP ?= true
export JNI ?= true
export NETWORK ?= native
export ENABLE_THREAD_RECYCLING ?= true

BOOTCLASSDIR  ?= {}/lib/mika
BOOTCLASSFILE ?= mcl.jar

# Set this false to create uncompressed jar files (including mcl.jar)
COMPRESS_JAR_FILES ?= true

# Enable this line to enable javax.comm
# EXTENSIONS += javax_comm
export EXTENSIONS

export CFLAGS += -m32
export CFLAGS += -DSTORE_METHOD_DEBUG_INFO



