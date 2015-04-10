+-------------------------------------------------------------------------+
| Copyright (c) 2015 by Chris Gray, Kiffer Ltd.  All rights reserved.     |
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

Open Mika Jvm
=============

This file contains a short overview on how to run and build Open Mika. 
For more detailed information please see the 'doc' dirctory.

Build machine
=============

The build system is designed to run on x86-linux machine.
Most of the major linux flavors should enable you to compile
and run mika. 

System requirements
===================

1. ant: The main mika build system uses ant to build mika.
   use version 1.6.2 or up.
2. a java runtime environment: since ant is written in java you'll
   need a jre capable of running ant
3. jam: Ft-jam works well. 
4. Gnu awk (gawk)
5. a gcc toolchain for your target platform.
6. When using AWT on your pc, you'll also need the X11 development environment.

Note: depending on your setup, you might need to add entry for java and ant to your PATH.

Building mika
=============

Building mika is done by calling ant in the same directory as this file is (the
build.xml is next to this README.txt file). 

There are 5 tasks which can be called directly with ant

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

Running mika
============

After succesfully building mika for you platform, you'll see a platform directory in release.
Let's return to our example: we just compiled mika by calling: 'ant -DPLATFORM=pc'.
A directory pc was created in release. It contains the binary files needed to run mika.
'bin' contains the executable, while lib/mika stores java classes (wre.jar) as well as
some properties and resources. The command line of mika works very much alike the Sun
Java one. calling './release/pc/bin/mika -cp . HelloWord' causes for helloworld.class to be 
loaded from . More info on mika's commandline can be found in doc/COMMAND_LINE.txt



 
 

 
   

