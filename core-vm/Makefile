###########################################################################
# Copyright (c) 2018, 2020, 2022, 2023, 2024 by Chris Gray, KIFFER Ltd.   #
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

CFLAGS += -I $(MIKA_TOP)/core-vm/include
CFLAGS += -I $(MIKA_TOP)/core-vm/hal/cpu/$(CPU)/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/kernel/$(SCHEDULER)/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/kernel/common/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/fs/native/common/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/fs/native/hal/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/fs/native/hal/hostos/$(HOSTOS)/include
CFLAGS += -I $(MIKA_TOP)/vm-ext/jpda/jdwp/include

ifeq ($(HOSTOS), linux)
  CFLAGS += -I $(MIKA_TOP)/core-vm/hal/hostos/posix/include
  CFLAGS += -pthread
  LDFLAGS += -pthread
endif
ifeq ($(HOSTOS), android)
  CFLAGS += -I $(MIKA_TOP)/core-vm/hal/hostos/posix/include
  CFLAGS += -I $(TOOLCHAIN_HEADERS)
endif
ifeq ($(HOSTOS), netbsd)
  CFLAGS += -I $(MIKA_TOP)/core-vm/hal/hostos/posix/include
endif
ifeq ($(HOSTOS), freertos)
  CFLAGS += -I $(MIKA_TOP)/core-vm/hal/hostos/freertos/include
  CFLAGS += -nostdlib
  CFLAGS += -DFREERTOS
endif

CFLAGS += -I $(gendir)
ifeq ($(TOOLCHAIN), clang)
  LDFLAGS += $(libdir)/libmika.a $(libdir)/liboswald.a $(libdir)/libawt.a
  LDFLAGS += -fuse-ld=lld 
  LDFLAGS += -ferror-limit=0
else
# assuming gcc
  CFLAGS += -L $(libdir)
  LDFLAGS += -lmika -loswald -lawt
endif

# 
# Add the dl library if we want to be able to load shared libraries.
# Also define SHARED_OBJECTS
#

ifeq ($(SHARED_OBJECTS), true)
  CFLAGS_shared_objects_linux = -DSHARED_OBJECTS 
  LDFLAGS_shared_objects_linux = -Wl,-export-dynamic
  CFLAGS_shared_objects_netbsd = -DSHARED_OBJECTS
  LDFLAGS_shared_objects_netbsd = -Wl,-export-dynamic

  CFLAGS += $(CFLAGS_shared_objects_$(HOSTOS))
  LDFLAGS += $(LDFLAGS_shared_objects_$(HOSTOS))

  ifeq ($(FFI), libffi)
    ifdef LIBFFI_A_LOCATION
      LDFLAGS += -L$(LIBFFI_A_LOCATION)
    endif
    LDFLAGS += -lffi
  endif
  ifeq ($(SCHEDULER), oswald)
    ifeq ($(SHARED_HEAP), true)
      CFLAGS += -DSHARED_HEAP
    endif
  endif
  LDFLAGS += -ldl
endif

ifeq "$(AWT)" "rudolph"
  ifeq "$(AWT_DEVICE)" "xsim"
    LDFLAGS += -lX11
  endif
endif

ifeq ($(MAPFILE), true)
  LDFLAGS += -Wl,--cref,-Map,$(OROOT)/map
endif

ifeq ($(SCHEDULER), "o4e")
  CFLAGS += -L $(MIKA_TOP)/vm-cmp/fs/vfs/src/image.a
endif

ifneq ($(filter $(EXTENSIONS),bytecodetest),)
  CFLAGS += -DINIT_CLASS=\"wonka.bytecodetest.Main\"
endif

ifneq ($(filter $(EXTENSIONS),javax_comm),)
  CFLAGS += -lcomm
endif

.PHONY : mika awt engine filesystem network fp jpda math security core-vm dispatchers generated jarfile mcldir libs javaclasses

mika : main.o libs
	@echo "LDFLAGS = $(LDFLAGS)"
	$(CC) -o mika main.o $(LDFLAGS)

# TODO do we really need OSWALD_LIB and AWT_LIB here?
libs: $(MIKA_LIB) $(OSWALD_LIB) $(AWT_LIB)

main.o : main.c

$(MIKA_LIB) : awt engine filesystem network fp jpda math security core-vm dispatchers generated 
	@echo "Archiving object files from directories $(wildcard $(objdir)/core-vm/*) $(wildcard $(objdir)/vm-cmp/*) $(wildcard $(objdir)/vm-ext/*) $(objdir)/generated"
	$(AR) $@ $(foreach dir, $(wildcard $(objdir)/core-vm/*), $(wildcard $(dir)/*.o)) $(foreach dir, $(wildcard $(objdir)/vm-cmp/*), $(wildcard $(dir)/*.o)) $(foreach dir, $(wildcard $(objdir)/vm-ext/*), $(wildcard $(dir)/*.o)) $(wildcard $(objdir)/generated/*.o)
	$(RANLIB) $@

engine :
	make -C ../vm-cmp/engine/common $(MAKECMDGOALS)
	make -C ../vm-cmp/engine/$(ENGINE) $(MAKECMDGOALS)

filesystem :
	make -C ../vm-cmp/fs/$(FILESYSTEM) $(MAKECMDGOALS)

network :
	make -C ../vm-cmp/network/$(NETWORK) $(MAKECMDGOALS)

awt : core-vm $(gendir)/core-classes.h
	make -C ../vm-cmp/awt/$(AWT) $(MAKECMDGOALS)

tool :
	make -C tool $(MAKECMDGOALS)

jpda : core-vm $(gendir)/core-classes.h
	make -C ../vm-ext/jpda $(MAKECMDGOALS)

fp:
	make -C ../vm-cmp/fp/$(FLOATING_POINT) $(MAKECMDGOALS)

security :
ifneq ($(SECURITY), "none")
	make -C ../vm-cmp/security/provider/any/src/native/wonka/security $(MAKECMDGOALS)
endif

math :
ifeq ($(MATH), native)
	make -C ../vm-cmp/math/native $(MAKECMDGOALS)
endif

dispatchers :
	awk -f $(scriptdir)/dispatchers.awk > $(gendir)/dispatchers.c

generated : core-vm dispatchers
	$(CC) -c -o $(objdir)/generated/core-classes.o $(CFLAGS) $(gendir)/core-classes.c
	$(CC) -c -o $(objdir)/generated/dispatchers.o $(CFLAGS) $(gendir)/dispatchers.c
	$(CC) -c -o $(objdir)/generated/unicode.o $(CFLAGS) $(gendir)/unicode.c
	$(CC) -c -o $(objdir)/generated/extensions.o $(CFLAGS) $(gendir)/extensions.c
ifeq ($(JNI), true)
	$(CC) -c -o $(objdir)/generated/jni-classes.o $(CFLAGS) $(gendir)/jni-classes.c
endif
ifeq ($(MATH), native)
	$(CC) -c -o $(objdir)/generated/math-classes.o $(CFLAGS) $(gendir)/math-classes.c
endif
ifeq ($(USE_ROMFS),true)
	$(CC) -c -o $(objdir)/generated/romfs.o $(CFLAGS) $(gendir)/romfs.c
endif

# FIXME: select right security dir(s)
jarfile : 
	@echo "Building ${mikadeploydir}/mcl.jar from core-vm/resource/mcl.mf and classes in ${classdir}"
	${JAVA6_HOME}/bin/jar cmf$(JAR_CMD_COMPRESSION_LEVEL) core-vm/resource/mcl.mf ${mikadeploydir}/mcl.jar -C ${classdir} .

# FIXME: select right security dir(s)
mcldir :
	@echo "Building ${mikadeploydir}/$(BOOTCLASSSUBDIR)/ from classes in ${classdir}"
	rm -rf ${mikadeploydir}/$(BOOTCLASSSUBDIR)/
	mkdir ${mikadeploydir}/$(BOOTCLASSSUBDIR)/
	cp -rv ${classdir}/* ${mikadeploydir}/$(BOOTCLASSSUBDIR)/

# FIXME: select right security dir(s)
javaclasses:
	# make -C ${secanyprovdir} classes
	make -C ${secprovdir} classes
	make -C ${securitydir} classes
	make -C ${javajardir} classes
	make -C $(JAVAX) classes

# TODO figure out how to handle all of these with a foreach, something like
# 	$(foreach ext, $(EXTENSIONS), $(eval make -C ../vm-ext/$(ext)))

bytecodetest :
	@echo Building extension bytecodetest
	make -C ../vm-ext/bytecodetest

# TODO we probably don't need the JNI=$(JNI) bit
core-vm : javaclasses 
	@echo Enabled extensions : $(EXTENSIONS)
	awk -v JNI=$(JNI) -f $(scriptdir)/unicode.awk src/vm/UnicodeData-Latest.txt > $(gendir)/unicode.c
	awk -v EXTENSIONS="$(EXTENSIONS)" -f $(scriptdir)/extensions.awk > $(gendir)/extensions.c
	awk -v JNI=$(JNI) -f $(scriptdir)/core-classes-h.awk -f $(scriptdir)/common-classes-h.awk src/vm/core-classes.in > $(gendir)/core-classes.h
ifeq ($(JAVA5_SUPPORT), true)
	awk -v JNI=$(JNI) -f $(scriptdir)/core-classes-c.awk -f $(scriptdir)/common-classes-c.awk src/vm/core-classes5.in > $(gendir)/core-classes.c
else
	awk -v JNI=$(JNI) -f $(scriptdir)/core-classes-c.awk -f $(scriptdir)/common-classes-c.awk src/vm/core-classes.in > $(gendir)/core-classes.c
endif
ifeq ($(JNI), true)
	awk -v JNI=$(JNI) -f $(scriptdir)/jni-classes-h.awk -f $(scriptdir)/common-classes-h.awk src/vm/jni-classes.in > $(gendir)/jni-classes.h
	awk -v JNI=$(JNI) -f $(scriptdir)/jni-classes-c.awk -f $(scriptdir)/common-classes-c.awk src/vm/jni-classes.in > $(gendir)/jni-classes.c
endif
ifeq ($(USE_ROMFS), true)
	@echo "Building ROMFS from classes in ${classdir}"
	(cd $(classdir); find -type f -print -exec od -An -tu1 {} \;) | awk -f $(scriptdir)/romify.awk > ${gendir}/romfs.c
endif
	make -C src 
	make -C hal 

clean:
	rm main.o
	make -C ../vm-cmp/awt/$(AWT) $(MAKECMDGOALS)
	make -C ../vm-cmp/engine/common $(MAKECMDGOALS)
	make -C ../vm-cmp/engine/$(ENGINE) $(MAKECMDGOALS)
	make -C ../vm-cmp/fp/$(FLOATING_POINT) $(MAKECMDGOALS)
	make -C ../vm-cmp/fs/$(FILESYSTEM) $(MAKECMDGOALS)
	make -C ../vm-cmp/network/$(NETWORK) $(MAKECMDGOALS)
# TODO: we probably only need this if SECURITY_PROVIDER is not 'none'
	make -C ../vm-cmp/security/provider/any/src/native/wonka/security $(MAKECMDGOALS)
	make -C ../vm-ext/jpda $(MAKECMDGOALS)
	make -C tool $(MAKECMDGOALS)

