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
** $Id: ResultSetMetaData.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

public interface ResultSetMetaData {

  public static final int columnNoNulls = 0;
  public static final int columnNullable = 1;
  public static final int columnNullableUnknown = 2;

  public String getCatalogName(int column) throws SQLException;
  public String getColumnClassName(int column) throws SQLException;
  public int getColumnCount() throws SQLException;
  public int getColumnDisplaySize(int column) throws SQLException;
  public String getColumnLabel(int column) throws SQLException;
  public String getColumnName(int column) throws SQLException;
  public int getColumnType(int column) throws SQLException;
  public String getColumnTypeName(int column) throws SQLException;
  public int getPrecision(int column) throws SQLException;
  public int getScale(int column) throws SQLException;
  public String getSchemaName(int column) throws SQLException;
  public String getTableName(int column) throws SQLException;
  public boolean isAutoIncrement(int column) throws SQLException;
  public boolean isCaseSensitive(int column) throws SQLException;
  public boolean isCurrency(int column) throws SQLException;
  public boolean isDefinitelyWritable(int column) throws SQLException;
  public int isNullable(int column) throws SQLException;
  public boolean isReadOnly(int column) throws SQLException;
  public boolean isSearchable(int column) throws SQLException;
  public boolean isSigned(int column) throws SQLException;
  public boolean isWritable(int column) throws SQLException;

}

