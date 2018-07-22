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

#
# Where will all generated files end up.
#

#
# What are the source files.
#

OSWALD_TESTS = main_test.c atomic_test.c block_test.c map_test.c \
                memory_test.c monitor_test.c mutex_test.c queue_test.c \
                sem_test.c signals_test.c exception_test.c \
                thread_test.c timer_test.c module_test.c join_test.c

#
# Create the object file as C code for the module tests. We create the tool to
# make this file if it doesn't exist yet. We use the specific tool compiler
# for this tool to create.
#

# CC on <vm-cmp!kernel!oswald!tests>create_modules.o = $(TOOL_CC) ;
# LINK on create_modules = $(TOOL_LINK) ;
# Main create_modules : create_modules.c ;

#
# Now generate the modules_data.c file that contains the object
# code in a C source form.
#

# GenFile modules_data.c : create_modules module_1.c module_2.c : $(CPU) $(HOSTOS) $(CC) ;

#
# Generate the sample tymbol table.
#

# GenFile sampletab.c : crestab : --output=$(OROOT)/kernel/oswald/tests/sampletab.c --relocatable=$(OROOT)/kernel/oswald/tests/module_test.o --function=sample --table=$(OSWALD_TOP)/tests/table --verbose --extra=1 ;

#
# Create the test program for Oswald.
#

# Main $(OSWALD_TEST) : $(OSWALD_TESTS) sampletab.c modules_data.c ;

# LinkLibraries $(OSWALD_TEST) : $(OSWALD_LIB) $(COMMON_LIB) ;

all:

