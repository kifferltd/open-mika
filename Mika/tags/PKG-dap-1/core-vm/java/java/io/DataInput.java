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
** $Id: DataInput.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public interface DataInput {

  public void readFully(byte[] b)
    throws IOException;
  
  public void readFully(byte[] b, int off, int len)
    throws IOException;
  
  public int skipBytes(int n)
    throws IOException;
  
  public boolean readBoolean()
    throws IOException;

  public byte readByte()
    throws IOException;
  
  public int readUnsignedByte()
    throws IOException;

  public short readShort()
    throws IOException;

  public int readUnsignedShort()
    throws IOException;

  public char readChar()
    throws IOException;

  public int readInt()
    throws IOException;

  public long readLong()
    throws IOException;

  public float readFloat()
    throws IOException;

  public double readDouble()
    throws IOException;

  public String readLine()
    throws IOException;

  public String readUTF()
    throws IOException;

}
