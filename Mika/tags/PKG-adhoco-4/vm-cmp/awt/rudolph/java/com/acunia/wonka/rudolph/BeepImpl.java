/**************************************************************************
* Copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/


package com.acunia.wonka.rudolph;

/**
 ** Skeleton for a Toolkit.beep() implementation.
 ** The API allows the duration, volume, and frequency of the beep to be set.
 ** On some implementations, some or all of these parameters may have no effect.
 */
abstract class BeepImpl implements Beep {
  /**
   ** The beep duration, in milliseconds.
   */
  protected int duration;

  /**
   ** The beep volume, on a scale from 0 (silence) to 100 (max).
   */
  protected int volume;

  /**
   ** The beep frequency, in Hertz.
   */
  protected int frequency;

  /**
   ** Default constructor used by Toolkit initialization.
   */
  protected BeepImpl() {}

  /**
   ** Set the beep duration in milliseconds.
   */
  protected void setDuration(int d) {
    duration = d;
  }

  /**
   ** Set the beep volume, on a scale from 0 (silence) to 100 (max).
   */
  protected void setVolume(int v) {
    volume = v;
  }

  /**
   ** Set the beep frequency in Hertz.
   */
  protected void setFrequency(int f) {
    frequency = f;
  }

  /**
   ** Sound the beep.
   */
  public abstract void beep();
}

