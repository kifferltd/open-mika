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

