/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: DataOutput.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public interface DataOutput {

  public void write(int b)
    throws IOException;
  
  public void write(byte[] b)
    throws IOException;

  public void write(byte[] b, int off, int len)
    throws IOException;

  public void writeBoolean(boolean v)
    throws IOException;

  public void writeByte(int v)
    throws IOException;

  public void writeShort(int v)
    throws IOException;

  public void writeChar(int v)
    throws IOException;

  public void writeInt(int v)
    throws IOException;

  public void writeLong(long v)
    throws IOException;

  public void writeFloat(float v)
    throws IOException;

  public void writeDouble(double v)
    throws IOException;

  public void writeBytes(String s)
    throws IOException;

  public void writeChars(String s)
    throws IOException;

  public void writeUTF(String s)
    throws IOException;

}
