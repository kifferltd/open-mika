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
