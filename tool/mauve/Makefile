###########################################################################
# Copyright (c) 2022, 2023 by Chris Gray, KIFFER Ltd.                     #
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

export mauvedeploydir = ${tooldeploydir}/mauve

.PHONY : test clean dirs classes resources

test : jarfile

# Note to self: this is how to get the output of a comand into a make variable ...
#    $(eval GIT_BRANCH=$(shell git rev-parse --abbrev-ref HEAD))
#    echo Git branch is $(GIT_BRANCH)

classes : 
	make -C java classes

resources :
	truncate -s 0 resource/wonkatest.properties
	cat resource/fragment/wonkatest.properties-prelude > resource/wonkatest.properties
ifneq ($(AWT),none)
	@echo "Prepending AWT tests to wonkatest.properties"
	cat resource/fragment/wonkatest.properties-awt >> resource/wonkatest.properties
endif
	@echo "Adding main tests to wonkatest.properties"
	cat resource/fragment/wonkatest.properties-main >> resource/wonkatest.properties
ifneq ($(JNI),false)
	@echo "Appending JNI tests to wonkatest.properties"
	cat resource/fragment/wonkatest.properties-jni >> resource/wonkatest.properties
endif

jarfile : classes resources
	mkdir -p $(mauvedeploydir)
	cd $(toolclassdir); find gnu -name \*\.class | xargs ${JAVA6_HOME}/bin/jar cmf$(JAR_CMD_COMPRESSION_LEVEL) ${MIKA_TOP}/tool/mauve/mauve.mf ${mauvedeploydir}/mauve-suite.jar 
	cd $(toolclassdir); find JUnitTests -name \*\.class | xargs ${JAVA6_HOME}/bin/jar uf ${mauvedeploydir}/mauve-suite.jar 
	find resource -type f | grep -v "^resource/fragment" | xargs ${JAVA6_HOME}/bin/jar uf ${mauvedeploydir}/mauve-suite.jar

ifdef JNI 
# TODO compile jnitest_natives ;
endif

clean :
	make -C java clean
	rm ${mauvedeploydir}/mauve-suite.jar
# TODO
