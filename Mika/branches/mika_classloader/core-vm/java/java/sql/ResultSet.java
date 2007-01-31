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
** $Id: ResultSet.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

import java.util.*;
import java.io.*;
import java.math.*;

public interface ResultSet {
  
  public static final int FETCH_FORWARD = 1000;
  public static final int FETCH_REVERSE = 1001;
  public static final int FETCH_UNKNOWN = 1002;
  public static final int TYPE_FORWARD_ONLY = 1003;
  public static final int TYPE_SCROLL_INSENSITIVE = 1004;
  public static final int TYPE_SCROLL_SENSITIVE = 1005;
  public static final int CONCUR_READ_ONLY = 1007;
  public static final int CONCUR_UPDATABLE = 1008;

  public boolean absolute(int row) throws SQLException;
  public void afterLast() throws SQLException;
  public void beforeFirst() throws SQLException;
  public void cancelRowUpdates() throws SQLException;
  public void clearWarnings() throws SQLException;
  public void close() throws SQLException;
  public void deleteRow() throws SQLException;
  public int findColumn(String columnName) throws SQLException;
  public boolean first() throws SQLException;
  public Array getArray(int i) throws SQLException;
  public Array getArray(String colName) throws SQLException;
  public InputStream getAsciiStream(int columnIndex) throws SQLException;
  public InputStream getAsciiStream(String columnName) throws SQLException;
  public BigDecimal getBigDecimal(int columnIndex) throws SQLException;
  public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException; 
  public BigDecimal getBigDecimal(String columnName) throws SQLException;
  public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException;
  public InputStream getBinaryStream(int columnIndex) throws SQLException;
  public InputStream getBinaryStream(String columnName) throws SQLException;
  public Blob getBlob(int i) throws SQLException;
  public Blob getBlob(String colName) throws SQLException;
  public boolean getBoolean(int columnIndex) throws SQLException;
  public boolean getBoolean(String columnName) throws SQLException;
  public byte getByte(int columnIndex) throws SQLException;
  public byte getByte(String columnName) throws SQLException;
  public byte[] getBytes(int columnIndex) throws SQLException;
  public byte[] getBytes(String columnName) throws SQLException;
  public Reader getCharacterStream(int columnIndex) throws SQLException;
  public Reader getCharacterStream(String columnName) throws SQLException;
  public Clob getClob(int i) throws SQLException;
  public Clob getClob(String colName) throws SQLException;
  public int getConcurrency() throws SQLException;
  public String getCursorName() throws SQLException;
  public Date getDate(int columnIndex) throws SQLException;
  public Date getDate(int columnIndex, Calendar cal) throws SQLException;
  public Date getDate(String columnName) throws SQLException;
  public Date getDate(String columnName, Calendar cal) throws SQLException;
  public double getDouble(int columnIndex) throws SQLException;
  public double getDouble(String columnName) throws SQLException;
  public int getFetchDirection() throws SQLException;
  public int getFetchSize() throws SQLException;
  public float getFloat(int columnIndex) throws SQLException;
  public float getFloat(String columnName) throws SQLException;
  public int getInt(int columnIndex) throws SQLException;
  public int getInt(String columnName) throws SQLException;
  public long getLong(int columnIndex) throws SQLException;
  public long getLong(String columnName) throws SQLException;
  public ResultSetMetaData getMetaData() throws SQLException;
  public Object getObject(int columnIndex) throws SQLException;
  public Object getObject(int i, Map map) throws SQLException;
  public Object getObject(String columnName) throws SQLException;
  public Object getObject(String colName, Map map) throws SQLException;
  public Ref getRef(int i) throws SQLException;
  public Ref getRef(String colName) throws SQLException;
  public int getRow() throws SQLException;
  public short getShort(int columnIndex) throws SQLException;
  public short getShort(String columnName) throws SQLException;
  public Statement getStatement() throws SQLException;
  public String getString(int columnIndex) throws SQLException;
  public String getString(String columnName) throws SQLException;
  public Time getTime(int columnIndex) throws SQLException;
  public Time getTime(int columnIndex, Calendar cal) throws SQLException;
  public Time getTime(String columnName) throws SQLException;
  public Time getTime(String columnName, Calendar cal) throws SQLException;
  public Timestamp getTimestamp(int columnIndex) throws SQLException;
  public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException;
  public Timestamp getTimestamp(String columnName) throws SQLException;
  public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException;
  public int getType() throws SQLException;
  public InputStream getUnicodeStream(int columnIndex) throws SQLException;
  public InputStream getUnicodeStream(String columnName) throws SQLException;
  public SQLWarning getWarnings() throws SQLException;
  public void insertRow() throws SQLException;
  public boolean isAfterLast() throws SQLException;
  public boolean isBeforeFirst() throws SQLException;
  public boolean isFirst() throws SQLException;
  public boolean isLast() throws SQLException;
  public boolean last() throws SQLException;
  public void moveToCurrentRow() throws SQLException;
  public void moveToInsertRow() throws SQLException;
  public boolean next() throws SQLException;
  public boolean previous() throws SQLException;
  public void refreshRow() throws SQLException;
  public boolean relative(int rows) throws SQLException;
  public boolean rowDeleted() throws SQLException;
  public boolean rowInserted() throws SQLException;
  public boolean rowUpdated() throws SQLException;
  public void setFetchDirection(int direction) throws SQLException;
  public void setFetchSize(int rows) throws SQLException;
  public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException;
  public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException;
  public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException;
  public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException;
  public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException;
  public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException;
  public void updateBoolean(int columnIndex, boolean x) throws SQLException;
  public void updateBoolean(String columnName, boolean x) throws SQLException;
  public void updateByte(int columnIndex, byte x) throws SQLException;
  public void updateByte(String columnName, byte x) throws SQLException;
  public void updateBytes(int columnIndex, byte[] x) throws SQLException;
  public void updateBytes(String columnName, byte[] x) throws SQLException;
  public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException;
  public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException;
  public void updateDate(int columnIndex, Date x) throws SQLException;
  public void updateDate(String columnName, Date x) throws SQLException;
  public void updateDouble(int columnIndex, double x) throws SQLException;
  public void updateDouble(String columnName, double x) throws SQLException;
  public void updateFloat(int columnIndex, float x) throws SQLException;
  public void updateFloat(String columnName, float x) throws SQLException;
  public void updateInt(int columnIndex, int x) throws SQLException;
  public void updateInt(String columnName, int x) throws SQLException;
  public void updateLong(int columnIndex, long x) throws SQLException;
  public void updateLong(String columnName, long x) throws SQLException;
  public void updateNull(int columnIndex) throws SQLException;
  public void updateNull(String columnName) throws SQLException;
  public void updateObject(int columnIndex, Object x) throws SQLException;
  public void updateObject(int columnIndex, Object x, int scale) throws SQLException;
  public void updateObject(String columnName, Object x) throws SQLException;
  public void updateObject(String columnName, Object x, int scale) throws SQLException;
  public void updateRow() throws SQLException;
  public void updateShort(int columnIndex, short x) throws SQLException;
  public void updateShort(String columnName, short x) throws SQLException;
  public void updateString(int columnIndex, String x) throws SQLException;
  public void updateString(String columnName, String x) throws SQLException;
  public void updateTime(int columnIndex, Time x) throws SQLException;
  public void updateTime(String columnName, Time x) throws SQLException;
  public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException;
  public void updateTimestamp(String columnName, Timestamp x) throws SQLException;
  public boolean wasNull() throws SQLException;

}

