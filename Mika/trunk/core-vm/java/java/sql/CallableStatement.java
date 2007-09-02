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
** $Id: CallableStatement.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

import java.util.*;
import java.math.*;

public interface CallableStatement extends PreparedStatement {

  public Array getArray(int i) throws SQLException;
  public BigDecimal getBigDecimal(int parameterIndex) throws SQLException;
  public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException;
  public Blob getBlob(int i) throws SQLException;
  public boolean getBoolean(int parameterIndex) throws SQLException;
  public byte getByte(int parameterIndex) throws SQLException;
  public byte[] getBytes(int parameterIndex) throws SQLException;
  public Clob getClob(int i) throws SQLException;
  public Date getDate(int parameterIndex) throws SQLException;
  public Date getDate(int parameterIndex, Calendar cal) throws SQLException;
  public double getDouble(int parameterIndex) throws SQLException;
  public float getFloat(int parameterIndex) throws SQLException;
  public int getInt(int parameterIndex) throws SQLException;
  public long getLong(int parameterIndex) throws SQLException;
  public Object getObject(int parameterIndex) throws SQLException;
  public Object getObject(int i, Map map) throws SQLException;
  public Ref getRef(int i) throws SQLException;
  public short getShort(int parameterIndex) throws SQLException;
  public String getString(int parameterIndex) throws SQLException;
  public Time getTime(int parameterIndex) throws SQLException;
  public Time getTime(int parameterIndex, Calendar cal) throws SQLException;
  public Timestamp getTimestamp(int parameterIndex) throws SQLException;
  public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException;
  public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException;
  public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException;
  public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException;
  public boolean wasNull() throws SQLException;

}

