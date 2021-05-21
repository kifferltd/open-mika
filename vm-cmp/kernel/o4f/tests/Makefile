###########################################################################
# Copyright (c) 2020 by Chris Gray, KIFFER Ltd.  All rights reserved.     #
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

builddir = $(MIKA_TOP)/build/$(PLATFORM)
objdir = $(builddir)/obj

CFLAGS += -I $(MIKA_TOP)/core-vm/include
CFLAGS += -I $(MIKA_TOP)/core-vm/hal/cpu/$(CPU)/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/kernel/o4f/include

LDFLAGS += /home/chris/github/imsys-mika/build/im4000/lib/liboswald.a
LDFLAGS += /home/chris/imsys-phabricator/sw-isal-system/build/lib/libisal-system.a
#LDFLAGS += -loswald -lcommon
LDFLAGS += -L $(libdir)

sources  = main_test.c monitor_test.c

objects=$(patsubst %.c, $(objdir)/kernel/o4f/%.o, $(sources))

outputfile = $(OROOT)/kernel/o4f/tests/oswaldtests

test : $(objects)
	$(CC) -o test $(objects) $(CFLAGS) $(LDFLAGS)

$(objects) : $(objdir)/kernel/o4f/%.o : %.c
	@echo "objectdir =" $(objdir)/kernel/o4f
	@echo "sources =" $(sources)
	@echo "objects =" $(objects)
	@echo "outputfile =" $(outputfile)
	$(CC) $(CFLAGS) -c $< -o $@

clean:


