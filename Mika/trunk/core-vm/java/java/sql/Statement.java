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
** $Id: Statement.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

public interface Statement {

  public void addBatch(String sql) throws SQLException;
  public void cancel() throws SQLException;
  public void clearBatch() throws SQLException;
  public void clearWarnings() throws SQLException;
  public void close() throws SQLException;
  public boolean execute(String sql) throws SQLException;
  public int[] executeBatch() throws SQLException;
  public ResultSet executeQuery(String sql) throws SQLException;
  public int executeUpdate(String sql) throws SQLException;
  public Connection getConnection() throws SQLException;
  public int getFetchDirection() throws SQLException;
  public int getFetchSize() throws SQLException;
  public int getMaxFieldSize() throws SQLException;
  public int getMaxRows() throws SQLException;
  public boolean getMoreResults() throws SQLException;
  public int getQueryTimeout() throws SQLException;
  public ResultSet getResultSet() throws SQLException;
  public int getResultSetConcurrency() throws SQLException;
  public int getResultSetType() throws SQLException;
  public int getUpdateCount() throws SQLException;
  public SQLWarning getWarnings() throws SQLException;
  public void setCursorName(String name) throws SQLException;
  public void setEscapeProcessing(boolean enable) throws SQLException;
  public void setFetchDirection(int direction) throws SQLException;
  public void setFetchSize(int rows) throws SQLException;
  public void setMaxFieldSize(int max) throws SQLException;
  public void setMaxRows(int max) throws SQLException;
  public void setQueryTimeout(int seconds) throws SQLException;

}

