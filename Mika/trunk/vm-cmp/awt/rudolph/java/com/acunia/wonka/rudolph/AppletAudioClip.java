/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
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
    if(DSPManager.USE_DSP_MANAGER){
      DSPManager.setMute(true);
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

    if(DSPManager.USE_DSP_MANAGER){
      DSPManager.setMute(false);
    }
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

