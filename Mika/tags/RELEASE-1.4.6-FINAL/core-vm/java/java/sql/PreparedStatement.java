/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
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

