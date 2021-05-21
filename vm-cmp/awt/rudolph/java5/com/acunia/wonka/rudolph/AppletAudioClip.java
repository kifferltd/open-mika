/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package com.acunia.wonka.rudolph;

import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class AppletAudioClip implements java.applet.AudioClip, Runnable {

  private Thread thread;
  private boolean restart;
  private boolean play;
  private boolean loop;
  private boolean broken;
  private byte[] sound_data;
  private URL url;

  public AppletAudioClip(byte[] data) {
    sound_data = decode(data);
    broken = sound_data == null;
  }

  public AppletAudioClip(URL url) {
    this.url = url;
  }

  public synchronized void loop() {
    if(thread == null) {
      if(broken) {
        return;
      }
      thread = new Thread(this, this+" Thread");
      thread.start();
    }
    loop = true;
    restart = false;
  }

  public synchronized void play() {
    if(thread == null) {
      if(broken) {
        return;
      }
      thread = new Thread(this, this+" Thread");
      thread.start();
    }
    restart = true;
    loop = false;
  }

  public synchronized void stop() {
    loop = false;
    restart = false;
    play = false;
  }

  public void run() {
    if (sound_data == null) {
      try {
        InputStream in = url.openStream();
        int length = 2048;
        byte[] bytes = new byte[length];
        ByteArrayOutputStream out = new ByteArrayOutputStream(length);
        int rd = in.read(bytes, 0, length);
        while(rd != -1) {
          out.write(bytes,0, rd);
          rd = in.read(bytes, 0, length);
        }
        sound_data = decode(out.toByteArray());
        if(sound_data == null) {
          badData();
          return;
        }
      }
      catch (Exception e) {
        badData();
        return;
      }
    }

    do {
      synchronized(this) {
        if (restart) {
          restart = false;
        }
        else if(!loop) {
          thread = null;
          break;
        }
        play = true;
      }
      play(sound_data);
    } while(true);
  }

  private synchronized void badData() {
    restart = false;
    loop = false;
    broken = true;
    thread = null;
  }

  private native void play(byte[] sound_data);
  private native byte[] decode(byte[] data);
}

