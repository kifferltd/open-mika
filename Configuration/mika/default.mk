###########################################################################
# Copyright (c) 2018, 2022 by Chris Gray, KIFFER Ltd. All rights reserved.#
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
# If you are building Mika for your target, don't change the settings here:
# create your own Configuration/platform/foo file and do it there instead.
#

#
# Debug is set off per default.
#

ifndef DEBUG
  export DEBUG = false
endif

#
# If DETECT_FLYING_PIGS is true, a check will be performed for objects which
# appear to be unreachable in one GC pass and reachable in the next. This of
# course "cannot happen", hence the name ...
# Normally the VM recovers well from these "glitches", so this check should
# be disabled for production code.
#

ifndef DETECT_FLYING_PIGS
  export DETECT_FLYING_PIGS = false
endif

#
# SCHEDULER must be 'oswald' for uClinux.
# For Linux and NetBSD, scheduler must be 'o4p', except on arm and x86,
# where either 'oswald' or 'o4p' are allowed.
#

ifndef SCHEDULER
  export SCHEDULER = oswald
endif

#
# Java Native Interface support is set on per default.
#

ifndef JNI
  export JNI = true
endif

#
# The Garbage Collector is set on per default.
#

ifndef GC
  export GC = true
endif

#
# By default GC runs in parallel with bytecode excution so long as the bytecode does not
# mutate the reachability graph; functions enterSafeRegion() and enterUnsafeRegion() are
# used to ensure this. However on platforms which implement bytecode execution in hardware
# this may not be possible: for these platforms PARALLEL_GC should be set false, and GC
# will then execute in "stop the world" mode.
#

ifndef PARALLEL_GC
  export PARALLEL_GC = true
endif

#
# FILESYSTEM can take the values `native' (meaning use the filesystem of
# the host os) or `vfs' (meaning use Mika's own built-in implementation
# of e2fs).
# The default is `native', i.e. use the hostos filesystem.
#

ifndef FILESYSTEM
  export FILESYSTEM = native
endif

#
# If the hostos filesystem is used then when Mika is run it will translate
# the prefix '{}/' attached to any path into $(FSROOT); e.g. the path
# will be translated to $(FSROOT)/system/mcl.jar.
# FSROOT may be an absolute or a relative path; if relative it is interpreted 
# relative to the location of the Mika binary (args[0]), not the current 
# directory of the shell from which Mika was launched.
#

ifndef FSROOT
  export FSROOT = ../
endif

#
# OROOT will be set via the commandline (by ant). This directory should be used
# by ant to store all of it's output 
#

#
# If Mika is run with no -Xbootclasspath parameter, the bootstrap class 
# loader will search the file $(BOOTCLASSFILE), located in $(BOOTCLASSDIR).
# These variables also specify where the zipfile of bootstrap classes will
# be created, and what name it will be given. 
#

ifndef BOOTCLASSDIR
  export BOOTCLASSDIR  = "{}/lib/mika"
endif
ifndef BOOTCLASSFILE
  export BOOTCLASSFILE = "mcl.jar"
endif

ifndef TESTDIR
  export TESTDIR = "test"
endif
ifndef INSTALLTESTDIR
  export INSTALLTESTDIR  = $(OROOT)/$(TESTDIR)
endif 

#
# network defaults to the hostos native network.
#

ifndef NETWORK
  export NETWORK = native
endif

#
# Modules are default switched off.
#

ifndef MODULES
  export MODULES = false
endif

#
# Setting SHARED_OBJECTS to true makes it possible to load lib.so files.
#

ifndef SHARED_OBJECTS
  export SHARED_OBJECTS = true
endif

#
# Static linking is default. 
#

ifndef STATIC
  export STATIC = false
endif

#
# When SHARED_HEAP is enabled, Oswald will export malloc(), calloc(), realloc()
# and free. When a loaded lib.so file wants some memory, they'll get some
# from the same heap as Mika.
#

ifndef SHARED_HEAP
  export SHARED_HEAP = false
endif

#
# Support for Java5 language features is disabled by default.
#

ifndef JAVA5_SUPPORT
  export JAVA5_SUPPORT = false
endif

#
# The Java Debug Wire Protocol is disabled by default.
#

ifndef JDWP
  export JDWP = false
endif

#
# When METHOD_DEBUG is enabled, line-number information included in class
# files will be used by the VM when printing a stack trace. Note that this
# substantially increases the amount of memory used to store classes.
#

ifndef METHOD_DEBUG
  export METHOD_DEBUG = false
endif

# By default code will be generated in unicode.c to cover only ISO 8859-1. 
# Specify a colon-separated list of subset numbers to extend Unicode coverage
# to one or more subsets of Unicode: see tool/script/unicode.awk for details.
# Specify 999 to get complete Unicode coverage.  Characters from non-included 
# subsets will be treated by Mika as undefined.
#

ifndef UNICODE_SUBSETS
  export UNICODE_SUBSETS = 0
endif

#
# The mauve and JNI tests are not enabled. (The code to generate them is
# not included in this package).
#

ifndef TESTS
  export TESTS = false
endif

#
# If no -Xmx=<bytes> flag is specified on the command line, the default heap 
# size is used. The usual suffices 'k', 'm' can be used.
#

ifndef DEFAULT_HEAP_SIZE
  export DEFAULT_HEAP_SIZE = 8M
endif

#
# If no -Xss=<bytes> flag is specified on the command line, the default Java 
# stack size is used. The usual suffices 'k', 'm' can be used.
# One slot = 16 bytes, so the default is 2048 slots.
#

ifndef DEFAULT_STACK_SIZE
  export DEFAULT_STACK_SIZE = 16384
endif

#
# CPU_MIPS is used to calculate a sensible duration for the OSwald 'tick'.
# Choose a "round" number rather than a pedantically accurate one.
#

ifndef CPU_MIPS
  export CPU_MIPS = 40
endif

#
# Timers. The timer granularity used internally will never be finer than
# HOST_TIMER_GRANULARITY microseconds (for SCHEDULER=oswald it may be a
# multiple of this, if CPU_MIPS is low). If USE_NANOSLEEP is true, O4P
# will use the nanosleep(2) function to control the timers, otherwise
# usleep(3) is used.
# Note: we say 20 msec when the truth is 10 msec. That's how slow nanosleep runs. 

ifndef HOST_TIMER_GRANULARITY
  export HOST_TIMER_GRANULARITY = 20000
endif
ifndef USE_NANOSLEEP
  export USE_NANOSLEEP = true
endif

#
# allows you to specify a different tool chain set then normally used
# for the chosen CPU. 
#
ifndef TOOL_CONFIG
  export TOOL_CONFIG = none
endif

#
# Enables or disable the java bytecode verifier
#
ifndef BYTECODE_VERIFIER
  export BYTECODE_VERIFIER = false ; 
endif

#
# If true use libffi, otherwise use our own hackery.
#
ifndef USE_LIBFFI
  export USE_LIBFFI = false
endif

#
# If USE_LIBFFI is true, this location holds the libffi.a file used for linking
#
ifndef LIBFFI_A_LOCATION
  export LIBFFI_A_LOCATION = $(MIKA_TOP)/core-vm/hal/cpu/$(CPU)/lib
endif

#
# Enable or disable re-use of a native thread when the Java thread terminates
#
ifndef ENABLE_THREAD_RECYCLING
  export ENABLE_THREAD_RECYCLING = false
endif


