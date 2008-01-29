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

public abstract class Image {

  public static final int SCALE_DEFAULT = 1;
  public static final int SCALE_FAST = 2;
  public static final int SCALE_SMOOTH = 4;
  public static final int SCALE_REPLICATE = 8;
  public static final int SCALE_AREA_AVERAGING = 16;

  /**
   * @status implemented
   * @remark compliant with specifications
   */
  public static final Object UndefinedProperty = new Object();

  /**
   * @status not implemented
   * @remark not implemented
   */
  public abstract void flush();

  /**
   * @status implemented
   * @remark not compliant with specification; instead of throwing a 'ClassCastException' a null pointer 
   * is returned if the image is not an off-screen image.
   */
  public abstract Graphics getGraphics();
  
  /**
   * @status implemented
   * @remark compliant with specifications
   */
  public abstract int getHeight(ImageObserver observer);

  /**
   * @status implemented
   * @remark compliant with specifications
   */
  public abstract ImageProducer getSource();

  /**
   * @status implemented
   * @remark specifications are obscure...
   */
  public abstract Object getProperty(String name, ImageObserver observer);

  /**
   * @status implemented
   * @remark compliant with specifications
   */
  public Image getScaledInstance(int w, int h, int hints) {
    if(w < 0 && h >= 0) w = (getWidth(null) * h) / getHeight(null); 
    else if(h < 0 && w >= 0) h = (getHeight(null) * w) / getWidth(null); 
    else if(w < 0 && h < 0) {
      w = getWidth(null);
      h = getHeight(null);
    }

    // return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(this.getSource(), new ReplicateScaleFilter(w, h)));

    Image img = new Canvas().createImage(w, h);
    Graphics g = img.getGraphics();
    g.drawImage(this, 0, 0, w, h, null);
    return img;
  }

  /**
   * @status implemented
   * @remark compliant with the specifications
   */
  public abstract int getWidth(ImageObserver observer);

}
