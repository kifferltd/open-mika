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
** $Id: SQLOutput.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

import java.io.*;
import java.math.*;

public interface SQLOutput {

  public void writeArray(Array x) throws SQLException;
  public void writeAsciiStream(InputStream x) throws SQLException;
  public void writeBigDecimal(BigDecimal x) throws SQLException;
  public void writeBinaryStream(InputStream x) throws SQLException;
  public void writeBlob(Blob x) throws SQLException;
  public void writeBoolean(boolean x) throws SQLException;
  public void writeByte(byte x) throws SQLException;
  public void writeBytes(byte[] x) throws SQLException;
  public void writeCharacterStream(Reader x) throws SQLException;
  public void writeClob(Clob x) throws SQLException;
  public void writeDate(Date x) throws SQLException;
  public void writeDouble(double x) throws SQLException;
  public void writeFloat(float x) throws SQLException;
  public void writeInt(int x) throws SQLException;
  public void writeLong(long x) throws SQLException;
  public void writeObject(SQLData x) throws SQLException;
  public void writeRef(Ref x) throws SQLException;
  public void writeShort(short x) throws SQLException;
  public void writeString(String x) throws SQLException;
  public void writeStruct(Struct x) throws SQLException;
  public void writeTime(Time x) throws SQLException;
  public void writeTimestamp(Timestamp x) throws SQLException;

}

