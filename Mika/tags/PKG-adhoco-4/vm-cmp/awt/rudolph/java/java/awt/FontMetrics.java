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

public class FontMetrics implements java.io.Serializable {

  protected Font font;
  
  /**
   * @status  dummy implementation
   * @remark  not compliant with specs: temporarily this method is provided as a public method instead
   * of a protected method; actually, the class should be abstract, which isn't the case.
   */
  public FontMetrics(Font font) {
    this.font = font;
  }
  
  /**
   * @status  imlemented & compliant
   * @remark  Although it seems a waste of resources to map the chars into a String and then call stringWidth, it is the most efficient way
   */
  public int bytesWidth(byte[] data, int offset, int count) {
    return stringWidth(new String(data,offset,count));
  }


  /**
   * @status  imlemented & compliant
   * @remark  Although it seems a waste of resources to map the chars into a String and then call stringWidth, it is the most efficient way
   */
  public int charsWidth(char[] data, int offset, int count) {
    return stringWidth(new String(data,offset,count));
  }
  
  /**
   * @status  imlemented & compliant
   * @remark  imlemented & compliant
   */
  public int charWidth(char ch) {
    return charWidth((int)ch);
  }
  
  /**
   * @status  imlemented & compliant
   * @remark  imlemented & compliant
   */
  native public int charWidth(int i);

  /**
   * @status  imlemented & compliant
   * @remark  imlemented & compliant
   */
  public Font getFont() {
    return font;
  }

  /**
   * @status  imlemented & compliant
   * @remark  imlemented & compliant
   */
  native public int getAscent();

  /**
   * @status  imlemented & compliant
   * @remark  imlemented & compliant
   */
  native public int getDescent();

  /**
   * @status  imlemented & compliant
   * @remark  imlemented & compliant
   */
  native public int getLeading();

  /**
   * @status  imlemented & compliant
   * @remark  imlemented & compliant
   */
  native public int getHeight();

  /**
   * @status  imlemented & compliant
   * @remark  imlemented & compliant
   */
  native public int getMaxAscent();

  /**
   * @status  imlemented & compliant
   * @remark  imlemented & compliant
   */
  native public int getMaxDescent();

  /**
   * @status  imlemented & compliant
   * @remark  imlemented & compliant
   */
  native public int getMaxAdvance();

  /**
   * @status  imlemented & compliant
   * @remark  imlemented & compliant
   */
  public int[] getWidths()
  {
    int[] widths = new int[0x100];
    for (int i=0; i<0x100;i++)
    {
      widths[i]=charWidth(i);
    }
    return widths;
  }
  /**
   * @status  seems to be working ogood enough
   */
  native public int stringWidth(String string);

  public String toString() {
    return getClass().getName() +" - font: "+ getFont() +", height: "+ getHeight();
  }
}
