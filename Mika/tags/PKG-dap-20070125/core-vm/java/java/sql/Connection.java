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

