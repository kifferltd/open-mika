###########################################################################
# Copyright (c) 2024 by Chris Gray, KIFFER Ltd.  All rights reserved.     #
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

export COMM_TOP  = $(MIKA_TOP)/vm-ext/comm

# no local include directory yet
# CFLAGS += $(COMM_TOP)/common/include

# note: see core-vm/Makefile for other possible includes we may need
CFLAGS += -I $(MIKA_TOP)/core-vm/include
CFLAGS += -I $(MIKA_TOP)/core-vm/hal/cpu/$(CPU)/include
CFLAGS += -I $(MIKA_TOP)/core-vm/hal/hostos/$(HOSTOS)/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/kernel/$(SCHEDULER)/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/kernel/common/include
CFLAGS += -I $(gendir)
ifeq ($(HOSTOS), freertos)
# to be honest these are really more about clang linker than freertos
  LDFLAGS += $(libdir)/libmika.a $(libdir)/liboswald.a 
  LDFLAGS += -fuse-ld=lld 
  LDFLAGS += -ferror-limit=0
else
  CFLAGS += -L $(libdir)
  LDFLAGS += -lmika -loswald 
endif

.PHONY : mika libs classes clean
mika : libs classes

libs :
	awk -v JNI=$(JNI) -f $(scriptdir)/uart-classes-h.awk -f $(scriptdir)/common-classes-h.awk uart-classes.in > $(gendir)/uart-classes.h
	awk -v JNI=$(JNI) -f $(scriptdir)/uart-classes-c.awk -f $(scriptdir)/common-classes-c.awk uart-classes.in > $(gendir)/uart-classes.c
	make -C common/src/native $(MAKECMDGOALS)
	make -C hal/hostos/$(HOSTOS)/src/driver $(MAKECMDGOALS)

classes :
	make -C $(JAVAX) classes

clean :
	make -C common/src/native $(MAKECMDGOALS)

