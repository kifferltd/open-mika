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

