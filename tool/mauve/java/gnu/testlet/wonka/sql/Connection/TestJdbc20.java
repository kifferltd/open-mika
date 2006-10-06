/*
 * Based on Kaffe's Connection
 *
 * Copyright (c) 1998
 *      Transvirtual Technologies, Inc.  All rights reserved.
 * Modifications Copyright (c) 1999 Aaron M. Renn (arenn@urbanophile.com) 
 *
 * See the file "COPYING" for information on usage and redistribution
 * of this file.
 */

// Tags: JDK1.2 JDBC2.0

package gnu.testlet.wonka.sql.Connection;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.sql.*;
import java.util.Map;

public class TestJdbc20 implements Connection, Testlet {

public void
test(TestHarness harness)
{
  harness.check(TRANSACTION_NONE, 0, "TRANSACTION_NONE");
  harness.check(TRANSACTION_READ_UNCOMMITTED, 1, 
                "TRANSACTION_READ_UNCOMMITTED");
  harness.check(TRANSACTION_READ_COMMITTED, 2, "TRANSACTION_READ_COMMITTED");
  harness.check(TRANSACTION_REPEATABLE_READ, 4, "TRANSACTION_REPEATABLE_READ");
  harness.check(TRANSACTION_SERIALIZABLE, 8, "TRANSACTION_SERIALIZABLE");
}

public Statement createStatement() throws SQLException
{
  return(null);
}

public Statement createStatement(int resultSetType, int resultSetConcurrency)
  throws SQLException
{
  return(null);
}

public PreparedStatement prepareStatement(String sql) throws SQLException
{
  return(null);
}

public PreparedStatement prepareStatement(String sql, int resultSetType,
  int resultSetConcurrency) throws SQLException
{
  return(null);
}

public CallableStatement prepareCall(String sql) throws SQLException
{
  return(null);
}

public CallableStatement prepareCall(String sql, int resultSetType,
  int resultSetConcurrency) throws SQLException
{
  return(null);
}

public String nativeSQL(String sql) throws SQLException
{
  return(null);
}

public void setAutoCommit(boolean autoCommit) throws SQLException
{
  return;
}

public boolean getAutoCommit() throws SQLException
{
  return(false);
}

public void commit() throws SQLException
{
  return;
}

public void rollback() throws SQLException
{
  return;
}

public void close() throws SQLException
{
  return;
}

public boolean isClosed() throws SQLException
{
  return(false);
}

public DatabaseMetaData getMetaData() throws SQLException
{
  return(null);
}

public void setReadOnly(boolean readOnly) throws SQLException
{
  return;
}

public boolean isReadOnly() throws SQLException
{
  return(false);
}

public void setCatalog(String catalog) throws SQLException
{
  return;
}

public String getCatalog() throws SQLException
{
  return(null);
}

public void setTransactionIsolation(int level) throws SQLException
{
  return;
}

public int getTransactionIsolation() throws SQLException
{
  return(0);
}

public SQLWarning getWarnings() throws SQLException
{
  return(null);
}

public void clearWarnings() throws SQLException
{
  return;
}

public Map getTypeMap() throws SQLException
{
  return(null);
}

public void setTypeMap(Map map) throws SQLException
{
  return;
}

}

