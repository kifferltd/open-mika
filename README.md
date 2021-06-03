[![Build Status](https://travis-ci.org/kifferltd/open-mika.svg?branch=master)](https://travis-ci.org/kifferltd/open-mika)

```
+-------------------------------------------------------------------------+
| Copyright (c) 2015, 2020, 2021 by Chris Gray, Kiffer Ltd.               |
| All rights reserved.                                                    |
|                                                                         |
| Redistribution and use in source and binary forms, with or without      |
| modification, are permitted provided that the following conditions      |
| are met:                                                                |
| 1. Redistributions of source code must retain the above copyright       |
|    notice, this list of conditions and the following disclaimer.        |
| 2. Redistributions in binary form must reproduce the above copyright    |
|    notice, this list of conditions and the following disclaimer in the  |
|    documentation and/or other materials provided with the distribution. |
| 3. Neither the name of KIFFER Ltd nor the names of other contributors   |
|    may be used to endorse or promote products derived from this         |
|    software without specific prior written permission.                  |
|                                                                         |
| THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          |
| WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    |
| MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    |
| IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    |
| DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      |
| DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS |
| OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   |
| HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     |
| STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   |
| IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      |
| POSSIBILITY OF SUCH DAMAGE.                                             |
+-------------------------------------------------------------------------+
```

# HOW TO RUN THE OSWALD TESTS

First compile o4f (Oswald For FreeRTOS) as follows:
```
sw-open-mika/build$ cmake -DIMSYS_CLANG_DIR=`realpath ../../tool-llvm/build` -DIMSYS_NEWLIB_INCLUDE_DIR=`realpath ../../sw-newlib/newlib/libc/include` -DIMSYS_ISAL_SYSTEM_INCLUDE_DIR=`realpath ../../sw-isal-system/include` -DIMSYS_FREERTOS_KERNEL_INCLUDE_DIR=`realpath ../../sw-freertos/FreeRTOS/Source/include` -DIMSYS_FREERTOS_PORT_INCLUDE_DIR=`realpath ../../sw-freertos/FreeRTOS/Source/portable/Imsys/ISAL` -DIMSYS_FREERTOS_APP_INCLUDE_DIR=`realpath ../../sw-freertos/FreeRTOS/Demo/Imsys_Mika/` -DCMAKE_BUILD_TYPE=Debug ..
sw-open-mika/build$ cmake --build .
```

Then build the Imsys_Mika Demo:
```sw-freertos/FreeRTOS/Demo/Imsys_Mika$ ./build.sh```

The test executables are created in the ```build/``` directory:
```sw-freertos/FreeRTOS/Demo/Imsys_Mika$ ls build
CMakeCache.txt  CMakeFiles  cmake_install.cmake  demo.elf  demo.gpx  Makefile```

# Open Mika Jvm

This file contains a short overview on how to run and build Open Mika. 
For more detailed information please see the 'doc' dirctory.

## Build machine

The build system is designed to run on x86-linux machine,
but it can also be run self-hosted if your target machine
has enough memory etc. to perform the compilation.
Most of the major linux flavors should enable you to compile
and run mika. 

## Build system requirements

1. ant: The main mika build system uses ant to build mika.  
   Use version 1.6.2 or up.
2. a java runtime environment: since ant is written in java you'll
   need a jre capable of running ant
3. jam: Ft-jam works well, _or_ GNU make (see below). 
4. Gnu awk (gawk)
5. a gcc cross-compiling toolchain for your target platform.
6. When using AWT on your pc, you'll also need the X11 development environment.

Note: depending on your setup, you might need to add entries for java and ant to your PATH.

## Building mika

Building mika is done by calling ant in the same directory as this file is in
(the build.xml is next to this README.txt file). 

There are 5 tasks which can be called directly with ant :

1. main: this is the main build task. This is also the default task run by ant.
   However just typing 'ant' will fail since the build needs to know for what
   platform you intend to build, and this is specified by the PLATFORM property.
   To compile for the 'pc' platform the command line would be: 
      'ant -DPLATFORM=pc' 
   or 'ant -DPLATFORM=pc main'.
   Ant will now compile and package mika. You can build mika for another
   platform by calling 'ant -DPLATFORM=otherplatform'.

2. clean: clean will clean all generated files from a specific build. Example:
   'ant -DPLATFORM=pc' clean will delete all files generated specific for the
    pc build.

3. cleanall: as it says, cleanall will delete all generated files (no need to
   specify a PLATFORM).

4. sample: this target will compile and package all the samples.

5. tests: the 'tests' target  will build the VisualTestEngine, the AppletViewer 
   and the mauve-test. More details on these test can be found in doc/TESTS.txt.

More details on building with various options mika can be found in doc/BUILD.txt

### Compiling with make or jam

Traditionally the native part of Mika has been built using Jam, but we are
in the process of developing a parallel system using GNU Make. This does not
yet work for all build options, but the intention is that soon the Makefiles
will be able to do everything which is now done by Jam and that GNU Make will
replace Jam (and probably also Ant) as the primary build tool.

To build for a PC Linux target using the Makefiles, use the command
```ant -DPLATFORM=pc -DBUILD_TOOL=make```

## Running mika

After succesfully building mika for you platform, you'll see a directory 
under the 'release' directory with the same name as your platform, e.g.
'release/pc'.  This contains the binary files needed to run mika;
'bin' contains the executable, while 'lib/mika' stores java classes
(e.g. mcl.jar) plus some properties and resources. The command line of mika works
pretty much like the Sun/Oracle one, for example calling 
```./release/pc/bin/mika -cp . HelloWord```
causes HelloWorld.class to be executed . More info on mika's commandline can be found in doc/COMMAND_LINE.txt
