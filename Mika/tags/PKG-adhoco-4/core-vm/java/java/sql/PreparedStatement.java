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
** $Id: PreparedStatement.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

import java.util.*;
import java.math.*;
import java.io.*;

public interface PreparedStatement extends Statement {

  public void addBatch() throws SQLException;
  public void clearParameters() throws SQLException;
  public boolean execute() throws SQLException;
  public ResultSet executeQuery() throws SQLException;
  public int executeUpdate() throws SQLException;
  public ResultSetMetaData getMetaData() throws SQLException;
  public void setArray(int i, Array x) throws SQLException;
  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException;
  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException;
  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException;
  public void setBlob(int i, Blob x) throws SQLException;
  public void setBoolean(int parameterIndex, boolean x) throws SQLException;
  public void setByte(int parameterIndex, byte x) throws SQLException;
  public void setBytes(int parameterIndex, byte[] x) throws SQLException;
  public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException;
  public void setClob(int i, Clob x) throws SQLException;
  public void setDate(int parameterIndex, Date x) throws SQLException;
  public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException;
  public void setDouble(int parameterIndex, double x) throws SQLException;
  public void setFloat(int parameterIndex, float x) throws SQLException;
  public void setInt(int parameterIndex, int x) throws SQLException;
  public void setLong(int parameterIndex, long x) throws SQLException;
  public void setNull(int parameterIndex, int sqlType) throws SQLException;
  public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException;
  public void setObject(int parameterIndex, Object x) throws SQLException;
  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException;
  public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException;
  public void setRef(int i, Ref x) throws SQLException;
  public void setShort(int parameterIndex, short x) throws SQLException;
  public void setString(int parameterIndex, String x) throws SQLException;
  public void setTime(int parameterIndex, Time x) throws SQLException;
  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException;
  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException;
  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException;
  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException;

}

