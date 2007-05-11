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
** $Id: SQLInput.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

import java.io.*;
import java.math.*;

public interface SQLInput {

  public Array readArray() throws SQLException;
  public InputStream readAsciiStream() throws SQLException;
  public BigDecimal readBigDecimal() throws SQLException;
  public InputStream readBinaryStream() throws SQLException;
  public Blob readBlob() throws SQLException;
  public boolean readBoolean() throws SQLException;
  public byte readByte() throws SQLException;
  public byte[] readBytes() throws SQLException;
  public Reader readCharacterStream() throws SQLException;
  public Clob readClob() throws SQLException;
  public Date readDate() throws SQLException;
  public double readDouble() throws SQLException;
  public float readFloat() throws SQLException;
  public int readInt() throws SQLException;
  public long readLong() throws SQLException;
  public Object readObject() throws SQLException;
  public Ref readRef() throws SQLException;
  public short readShort() throws SQLException;
  public String readString() throws SQLException;
  public Time readTime() throws SQLException;
  public Timestamp readTimestamp() throws SQLException;
  public boolean wasNull() throws SQLException;
  
}

