###########################################################################
# Copyright (c) 2018 KIFFER Ltd. All rights reserved.                     #
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
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       #
# GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           #
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    #
# IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         #
# OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN     #
# IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           #
###########################################################################

# AWT can have values 'rudolph' or 'none'. If it's a rudolph version of Mika only
# please configure following properties. When AWT is none these all are irelevant.
# when AWT_DEVICE = rudolph, AWT_DEVICE can have values 'fdev' for frame buffer
# or 'xsim' for simulation under X; AWT_PIXELFORMAT can take the following
# values:      c565 : 16-bit color
#              c332 : 8-bit color
#                g4 : 4-bit grayscale;
# and AWT_INVERSENIBBLES and AWT_INVERSEDISPLAY handle various display driver
# characteristics, see the source for more details.
#
export AWT_DEVICE = xsim
export AWT_PIXELFORMAT = c555
export AWT_INVERSENIBBLES default = false
export AWT_INVERSEDISPLAY default = false

#
# GIF support is disabled by default.
#

export AWT_GIF_SUPPORT = true

#
# JPEG support is disabled by default.
#

export AWT_JPEG_SUPPORT = true

#
# Sets the main window title.
#
export CCFLAGS += -DRUDOLPH_DEFAULT_FRAME_TITLE='\"Mika\"'

#
# Defines the screen with to be default 720 by 420
# These settings are usefull for the nk770
#
export CCFLAGS += -DRUDOLPH_DEFAULT_FRAME_WIDTH=720
export CCFLAGS += -DRUDOLPH_DEFAULT_FRAME_HEIGHT=420

