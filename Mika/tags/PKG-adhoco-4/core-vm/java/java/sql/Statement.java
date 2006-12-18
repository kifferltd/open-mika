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

