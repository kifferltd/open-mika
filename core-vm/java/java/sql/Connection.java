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
** $Id: Connection.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

import java.util.*;

public interface Connection {

  public static final int TRANSACTION_NONE = 0;
  public static final int TRANSACTION_READ_UNCOMMITTED = 1;
  public static final int TRANSACTION_READ_COMMITTED = 2;
  public static final int TRANSACTION_REPEATABLE_READ = 4;
  public static final int TRANSACTION_SERIALIZABLE = 8;

  public void clearWarnings() throws SQLException;
  public void close() throws SQLException;
  public void commit() throws SQLException;
  public Statement createStatement() throws SQLException;
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException;
  public boolean getAutoCommit() throws SQLException;
  public String getCatalog() throws SQLException;
  public DatabaseMetaData getMetaData() throws SQLException;
  public int getTransactionIsolation() throws SQLException;
  public Map getTypeMap() throws SQLException;
  public SQLWarning getWarnings() throws SQLException;
  public boolean isClosed() throws SQLException;
  public boolean isReadOnly() throws SQLException;
  public String nativeSQL(String sql) throws SQLException;
  public CallableStatement prepareCall(String sql) throws SQLException;
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException;
  public PreparedStatement prepareStatement(String sql) throws SQLException;
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException;
  public void rollback() throws SQLException;
  public void setAutoCommit(boolean autoCommit) throws SQLException; 
  public void setCatalog(String catalog) throws SQLException; 
  public void setReadOnly(boolean readOnly) throws SQLException;
  public void setTransactionIsolation(int level) throws SQLException;
  public void setTypeMap(Map map) throws SQLException;

}

