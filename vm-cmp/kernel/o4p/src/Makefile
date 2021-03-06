###########################################################################
# Copyright (c) 2018 by Chris Gray, KIFFER Ltd.  All rights reserved.     #
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

CFLAGS += -I $(MIKA_TOP)/core-vm/include
CFLAGS += -I $(MIKA_TOP)/core-vm/hal/cpu/$(CPU)/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/kernel/$(SCHEDULER)/include

sources = o4p.c debug.c dump.c monitor.c mutex.c queue.c thread.c time.c

ifeq ($(HOSTOS), android)
  sources += mem.c
endif
ifeq ($(HOSTOS), netbsd)
  sources += mem.c
endif
ifeq ($(HOSTOS), linux)
  ifeq ($(USE_NATIVE_MALLOC), true)
    sources += mem.c
  else
    sources += memnommu.c
  endif
endif

objects=$(patsubst %.c, $(kernobjdir)/%.o, $(sources))

CFLAGS += -I ../include -I ../hal/cpu/$(CPU)/include
CFLAGS += -I ../hal/host/$(HOSTOS)/include
CFLAGS += -I ../../common/include
#CFLAGS += -loswald 

.PHONY : mika
mika : $(kernobjdir) $(objects)

$(kernobjdir) :
	@echo Creating $(kernobjdir)
	@mkdir -p $(kernobjdir)

$(objects) : $(kernobjdir)/%.o : %.c
	$(CC) $(CFLAGS) -c $< -o $@

clean:

