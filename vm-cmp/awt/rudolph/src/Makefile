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
CFLAGS += -I $(MIKA_TOP)/vm-cmp/kernel/common/include
#CFLAGS += -I $(MIKA_TOP)/core-vm/hal/hostos/linux/include
CFLAGS += -I $(AWT_TOP)/hal/hostos/linux/include

objects = $(awtobjdir)/awt-classes.o

.PHONY : mika
mika : $(AWT_LIB) 

$(objects) :
	awk -f $(scriptdir)/awt-classes-h.awk -f $(scriptdir)/common-classes-h.awk $(AWT_TOP)/src/awt-classes.in > $(gendir)/awt-classes.h
ifeq ($(JAVA5_SUPPORT), true)
	awk -f $(scriptdir)/awt-classes-c.awk -f $(scriptdir)/common-classes-c.awk $(AWT_TOP)/src/awt-classes5.in > $(gendir)/awt-classes.c
else
	awk -f $(scriptdir)/awt-classes-c.awk -f $(scriptdir)/common-classes-c.awk $(AWT_TOP)/src/awt-classes.in > $(gendir)/awt-classes.c
endif
	$(CC) -c -o $(awtobjdir)/awt-classes.o $(CFLAGS) $(gendir)/awt-classes.c
	make -C $(AWT_TOP)/src/native
	make -C $(AWT_TOP)/src/png
    ifeq "$(AWT_JPEG_SUPPORT)" "true"
	make -C $(AWT_TOP)/src/jpeg
    endif

$(AWT_LIB) : $(objects)
	@echo "Archiving object files from directory $(awtobjdir)"
	$(AR) $@ $(awtobjdir)/*.o
	ranlib $@

clean:



