+-------------------------------------------------------------------------+
| Copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.          |
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
| 3. Neither the name of /k/ Embedded Java Solutions nor the names of     |
|    other contributors may be used to endorse or promote products        |
|    derived from this software without specific prior written permission.|
|                                                                         |
| THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          |
| WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    |
| MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    |
| IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  |
| LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     |
| CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    |
| SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         |
| BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   |
| WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    |
| OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  |
| IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           |
+-------------------------------------------------------------------------+

Mika Jvm
========

Thank you for downloading mika. This file contains a short overview
on how to run and build mika. For more detailed information please read
the corresponding section in the 'doc' dirctory.

Build machine
=============

The build system is designed to run on x86-linux machine.
Most of the major linux flavors should enable you to compile
and run mika. Although this should not stop you from trying it on 
a different machine.

System requirements
===================

1. ant: The main mika build system uses ant to build mika.
   use version 1.6.2 or up.
2. a java runtime environment: since ant is written in java you'll
   need a jre capable of running ant
3. jam: Internally we're using acunia-jam (a bit of legacy), but ft-jam
   also works. You can download acunia-jam:
   https://opensource.luminis.net/confluence/display/WONKA/Wonka+Downloads
   Make sure the binary file is name jam and is somewhere in your PATH.
4. awk
5. a gcc toolchain for the platform your building for.
6. When using AWT on your pc, you'll also need the X11 development environment.

Note: depending on your setup, you might need to add entry for java and ant to your PATH.

Building mika
=============

Building mika is done by calling ant in the same directory as this file is (you'll
find a nice build.xml next to this README.txt file). If you're unfamiliar to ant, I
can recommend looking at 'http://ant.apache.org'. You'll find all the nice details on
ant overthere. 

There are 5 tasks which can be called directly with ant

1. main: this is the main build task. This is also the default task run by ant.
   Typing ant in this directory is actually the same as ant main. However just typing 
   ant will fail since the build wants to know what platform you intend to build for.
   To specify the platform, you must define a parameter (property) called PLATFORM.
   Passing properties to ant goes with -D option. So, let's keep it easy and build for pc. 
   You're commandline would look like: 'ant -DPLATFORM=pc' or 'ant -DPLATFORM=pc main'.
   Ant will now compile and package mika. You can build mika for another platform by
   calling 'ant -DPLATFORM=otherplatform' or for multiple platforms in one call:
   'ant -DPLATFORM=platform1,platform2,...'

2. clean: clean will clean all generated files from a specific build. Example:
   'ant -DPLATFORM=pc' will delete all files generated specific for the pc build.

3. cleanall: as it says, cleanall will delete all generated files (no need to specify
   a PLATFORM here). 'ant cleanall' will do.

4. sample: this target will compile and package all the samples.

5. tests: the 'tests' target  will build the VisualTestEngine, the ApletViewer 
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


More Docmuntation can be found in the doc directory ...

 
 

 
   

