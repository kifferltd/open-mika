###########################################################################
# Copyright (c) 2019, 2020, 2021 by Chris Gray, KIFFER Ltd.               #
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
export USE_LIBFFI = true

export AWT = none
export FLOATING_POINT = hauser
export SHARED_OBJECTS = false

export HOSTOS = freertos
export CPU = im4000
export TOOLCHAIN = clang
export TOOLCHAIN_PREFIX = /home/chris/imsys-phabricator/tool-llvm/build/bin/
export SCHEDULER = o4f

CFLAGS += --target=imsys -mcpu=im4000   -D__imsysisal__ -misal-internals -misac -misab   -fPIC -misal-phase1 -misal-phase1only   -D__imsysisal_phase1__
CFLAGS += -nostdlib -I/home/chris/imsys-phabricator/sw-newlib/newlib/libc/include/ -DSTORE_METHOD_DEBUG_INFO
LDFLAGS += -L/home/chris/libffi-armel-linux-eabi/libffi-master/arm-unknown-linux-gnueabi/.libs/
export JDWP = true




