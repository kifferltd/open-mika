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

package java.awt;

import java.awt.image.*;
import java.util.*;

public class MediaTracker implements java.io.Serializable {

  public static final int LOADING = 1;
  public static final int ABORTED = 2;
  public static final int ERRORED = 4;
  public static final int COMPLETE = 8;

  private Component comp;
  private MediaObserver observer;
  private Vector list;

  private class Wrapper {
    public Image image;
    public int id;
    public int state;
    public Wrapper(int id, Image image) {
      this.id =  id;
      this.image = image;
    }
  }

  private class MediaObserver implements ImageObserver {
    public boolean imageUpdate(Image img, int infofloat, int x, int y, int width, int height) {
      return false;
    }
  }

  /**
   * @status  implemented
   * @remark  implemented
   */ 
  public MediaTracker(Component comp) {
    this.comp = comp;
    observer = new MediaObserver();
    list = new Vector();
  }

  /**
   * @status  implemented
   * @remark  implemented
   */ 
  public void addImage(Image image, int id) {
    addImage(image, id, -1, -1);
  }

  /**
   * @status  implemented
   * @remark  no support for scaled images yet
   */
  public void addImage(Image image, int id, int w, int h) {
    if(w != -1 || h != -1) {
      // System.out.println("[java.awt.MediaTracker.addImage()] Scaled images are not yet supported (" + w + ", " + h + ")");
    }
    list.add(new Wrapper(id, image));
  }
  
  /**
   * @status  implemented
   * @remark  implemented
   */
  public boolean checkAll() {
    return checkAll(false);
  }

  /**
   * @status  implemented
   * @remark  implemented
   */
  public boolean checkAll(boolean load) {
    return true;
  }

  /**
   * @status  implemented
   * @remark  implemented
   */
  public synchronized boolean isErrorAny() {
    // Should check some flags in the images...
    // -> what if we tried to load .gif instead of a .png
    return false;
  }

  /**
   * @status  implemented
   * @remark  implemented
   */
  public synchronized Object[] getErrorsAny() {
    // Should check some flags in the images... 
    // -> what if we tried to load .gif instead of a .png
    return null;
  }

  /**
   * @status  implemented
   * @remark  implemented
   */
  public void waitForAll() throws InterruptedException {
    return;
  }

  /**
   * @status  implemented
   * @remark  implemented
   */
  public synchronized boolean waitForAll(long ms) throws InterruptedException  {
    return true;
  }

  /**
   * @status  implemented
   * @remark  implemented
   */
  public int statusAll(boolean load) {
    return COMPLETE;
  }

  /**
   * @status  implemented
   * @remark  implemented
   */  
  public boolean checkID(int id) {
    return checkID(id, false);
  }

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public boolean checkID(int id, boolean load) {
    return true;
  }

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public synchronized boolean isErrorID(int id) {
    return false;
  }

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public synchronized Object[] getErrorsID(int id) {
    return null;
  }

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public void waitForID(int id) throws InterruptedException {
    return;
  }

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public boolean waitForID(int id, long ms) throws InterruptedException {
    return true;
  }

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public int statusID(int id, boolean load) {
    return COMPLETE;
  }

  /**
   * @status  implemented
   * @remark  implemented
   */
  public void removeImage(Image image) {
    Iterator iter = list.iterator();
    Vector newlist = new Vector();
    while(iter.hasNext()) {
      Wrapper wrap = (Wrapper)iter.next();
      if(wrap.image != image) {
        newlist.add(wrap);
      }
    }
    list = newlist;
  }

  /**
   * @status  implemented
   * @remark  implemented
   */
  public void removeImage(Image image, int id) {
    Iterator iter = list.iterator();
    Vector newlist = new Vector();
    while(iter.hasNext()) {
      Wrapper wrap = (Wrapper)iter.next();
      if(wrap.image != image || wrap.id != id) {
        newlist.add(wrap);
      }
    }
    list = newlist;
  }

  /**
   * @status  implemented
   * @remark  scaling not yet supported
   */
  public void removeImage(Image image, int id, int w, int h) {
    if(w != -1 || h != -1) {
      // System.out.println("[java.awt.MediaTracker.addImage()] Scaled images are not yet supported");
    }
    Iterator iter = list.iterator();
    Vector newlist = new Vector();
    while(iter.hasNext()) {
      Wrapper wrap = (Wrapper)iter.next();
      if(wrap.image != image || wrap.id != id) {
        newlist.add(wrap);
      }
    }
    list = newlist;
  }
  
}
