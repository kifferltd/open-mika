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
** $Id: DatabaseMetaData.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

public interface DatabaseMetaData {

  public static final int bestRowTemporary = 0;
  public static final int bestRowUnknown = 0;
  public static final int bestRowNotPseudo = 1;
  public static final int bestRowTransaction = 1;
  public static final int bestRowPseudo = 2;
  public static final int bestRowSession = 2;
  
  public static final int columnNoNulls = 0;
  public static final int columnNullable = 1;
  public static final int columnNullableUnknown = 2;

  public static final int importedKeyCascade = 0;
  public static final int importedKeyRestrict = 1;
  public static final int importedKeySetNull = 2;
  public static final int importedKeyNoAction = 3;
  public static final int importedKeySetDefault = 4;
  public static final int importedKeyInitiallyDeferred = 5;
  public static final int importedKeyInitiallyImmediate = 6;
  public static final int importedKeyNotDeferrable = 7;
  
  public static final int procedureColumnUnknown = 0;
  public static final int procedureColumnIn = 1;
  public static final int procedureColumnInOut = 2;
  public static final int procedureColumnResult = 3;
  public static final int procedureColumnOut = 4;
  public static final int procedureColumnReturn = 5;
  
  public static final int procedureNoNulls = 0;
  public static final int procedureNullable = 1;
  public static final int procedureNullableUnknown = 2;
  
  public static final int procedureResultUnknown = 0;
  public static final int procedureNoResult = 1;
  public static final int procedureReturnsResult = 2;
  
  public static final short tableIndexStatistic = 0;
  public static final short tableIndexClustered = 1;
  public static final short tableIndexHashed = 2;
  public static final short tableIndexOther = 3;
  
  public static final int typeNoNulls = 0;
  public static final int typeNullable = 1;
  public static final int typeNullableUnknown = 2;
  
  public static final int typePredNone = 0;
  public static final int typePredChar = 1;
  public static final int typePredBasic = 2;
  public static final int typeSearchable = 3;
  
  public static final int versionColumnUnknown = 0;
  public static final int versionColumnNotPseudo = 1;
  public static final int versionColumnPseudo = 2;

  public boolean allProceduresAreCallable() throws SQLException;
  public boolean allTablesAreSelectable() throws SQLException;
  public boolean dataDefinitionCausesTransactionCommit() throws SQLException;
  public boolean dataDefinitionIgnoredInTransactions() throws SQLException;
  public boolean deletesAreDetected(int type) throws SQLException;
  public boolean doesMaxRowSizeIncludeBlobs() throws SQLException;
  public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException;
  public ResultSet getCatalogs() throws SQLException;
  public String getCatalogSeparator() throws SQLException;
  public String getCatalogTerm() throws SQLException;
  public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException;
  public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException;
  public Connection getConnection() throws SQLException;
  public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException;
  public String getDatabaseProductName() throws SQLException;
  public String getDatabaseProductVersion() throws SQLException;
  public int getDefaultTransactionIsolation() throws SQLException;
  public int getDriverMajorVersion();
  public int getDriverMinorVersion();
  public String getDriverName() throws SQLException;
  public String getDriverVersion() throws SQLException;
  public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException;
  public String getExtraNameCharacters() throws SQLException;
  public String getIdentifierQuoteString() throws SQLException;
  public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException;
  public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException;
  public int getMaxBinaryLiteralLength() throws SQLException;
  public int getMaxCatalogNameLength() throws SQLException;
  public int getMaxCharLiteralLength() throws SQLException;
  public int getMaxColumnNameLength() throws SQLException;
  public int getMaxColumnsInGroupBy() throws SQLException;
  public int getMaxColumnsInIndex() throws SQLException;
  public int getMaxColumnsInOrderBy() throws SQLException;
  public int getMaxColumnsInSelect() throws SQLException;
  public int getMaxColumnsInTable() throws SQLException;
  public int getMaxConnections() throws SQLException;
  public int getMaxCursorNameLength() throws SQLException;
  public int getMaxIndexLength() throws SQLException;
  public int getMaxProcedureNameLength() throws SQLException;
  public int getMaxRowSize() throws SQLException;
  public int getMaxSchemaNameLength() throws SQLException;
  public int getMaxStatementLength() throws SQLException;
  public int getMaxStatements() throws SQLException;
  public int getMaxTableNameLength() throws SQLException;
  public int getMaxTablesInSelect() throws SQLException;
  public int getMaxUserNameLength() throws SQLException;
  public String getNumericFunctions() throws SQLException;
  public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException;
  public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException;
  public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException;
  public String getProcedureTerm() throws SQLException;
  public ResultSet getSchemas() throws SQLException;
  public String getSchemaTerm() throws SQLException;
  public String getSearchStringEscape() throws SQLException;
  public String getSQLKeywords() throws SQLException;
  public String getStringFunctions() throws SQLException;
  public String getSystemFunctions() throws SQLException;
  public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException;
  public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException;
  public ResultSet getTableTypes() throws SQLException;
  public String getTimeDateFunctions() throws SQLException;
  public ResultSet getTypeInfo() throws SQLException;
  public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException;
  public String getURL() throws SQLException;
  public String getUserName() throws SQLException;
  public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException;
  public boolean insertsAreDetected(int type) throws SQLException;
  public boolean isCatalogAtStart() throws SQLException;
  public boolean isReadOnly() throws SQLException;
  public boolean nullPlusNonNullIsNull() throws SQLException;
  public boolean nullsAreSortedAtEnd() throws SQLException;
  public boolean nullsAreSortedAtStart() throws SQLException;
  public boolean nullsAreSortedHigh() throws SQLException;
  public boolean nullsAreSortedLow() throws SQLException;
  public boolean othersDeletesAreVisible(int type) throws SQLException;
  public boolean othersInsertsAreVisible(int type) throws SQLException;
  public boolean othersUpdatesAreVisible(int type) throws SQLException;
  public boolean ownDeletesAreVisible(int type) throws SQLException;
  public boolean ownInsertsAreVisible(int type) throws SQLException;
  public boolean ownUpdatesAreVisible(int type) throws SQLException;
  public boolean storesLowerCaseIdentifiers() throws SQLException;
  public boolean storesLowerCaseQuotedIdentifiers() throws SQLException;
  public boolean storesMixedCaseIdentifiers() throws SQLException;
  public boolean storesMixedCaseQuotedIdentifiers() throws SQLException;
  public boolean storesUpperCaseIdentifiers() throws SQLException;
  public boolean storesUpperCaseQuotedIdentifiers() throws SQLException;
  public boolean supportsAlterTableWithAddColumn() throws SQLException;
  public boolean supportsAlterTableWithDropColumn() throws SQLException;
  public boolean supportsANSI92EntryLevelSQL() throws SQLException;
  public boolean supportsANSI92FullSQL() throws SQLException;
  public boolean supportsANSI92IntermediateSQL() throws SQLException;
  public boolean supportsBatchUpdates() throws SQLException;
  public boolean supportsCatalogsInDataManipulation() throws SQLException;
  public boolean supportsCatalogsInIndexDefinitions() throws SQLException;
  public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException;
  public boolean supportsCatalogsInProcedureCalls() throws SQLException;
  public boolean supportsCatalogsInTableDefinitions() throws SQLException;
  public boolean supportsColumnAliasing() throws SQLException;
  public boolean supportsConvert() throws SQLException;
  public boolean supportsConvert(int fromType, int toType) throws SQLException;
  public boolean supportsCoreSQLGrammar() throws SQLException;
  public boolean supportsCorrelatedSubqueries() throws SQLException;
  public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException;
  public boolean supportsDataManipulationTransactionsOnly() throws SQLException;
  public boolean supportsDifferentTableCorrelationNames() throws SQLException;
  public boolean supportsExpressionsInOrderBy() throws SQLException;
  public boolean supportsExtendedSQLGrammar() throws SQLException;
  public boolean supportsFullOuterJoins() throws SQLException;
  public boolean supportsGroupBy() throws SQLException;
  public boolean supportsGroupByBeyondSelect() throws SQLException;
  public boolean supportsGroupByUnrelated() throws SQLException;
  public boolean supportsIntegrityEnhancementFacility() throws SQLException;
  public boolean supportsLikeEscapeClause() throws SQLException;
  public boolean supportsLimitedOuterJoins() throws SQLException;
  public boolean supportsMinimumSQLGrammar() throws SQLException;
  public boolean supportsMixedCaseIdentifiers() throws SQLException;
  public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException;
  public boolean supportsMultipleResultSets() throws SQLException;
  public boolean supportsMultipleTransactions() throws SQLException;
  public boolean supportsNonNullableColumns() throws SQLException;
  public boolean supportsOpenCursorsAcrossCommit() throws SQLException;
  public boolean supportsOpenCursorsAcrossRollback() throws SQLException;
  public boolean supportsOpenStatementsAcrossCommit() throws SQLException;
  public boolean supportsOpenStatementsAcrossRollback() throws SQLException;
  public boolean supportsOrderByUnrelated() throws SQLException;
  public boolean supportsOuterJoins() throws SQLException;
  public boolean supportsPositionedDelete() throws SQLException;
  public boolean supportsPositionedUpdate() throws SQLException;
  public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException;
  public boolean supportsResultSetType(int type) throws SQLException;
  public boolean supportsSchemasInDataManipulation() throws SQLException;
  public boolean supportsSchemasInIndexDefinitions() throws SQLException;
  public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException;
  public boolean supportsSchemasInProcedureCalls() throws SQLException;
  public boolean supportsSchemasInTableDefinitions() throws SQLException;
  public boolean supportsSelectForUpdate() throws SQLException;
  public boolean supportsStoredProcedures() throws SQLException;
  public boolean supportsSubqueriesInComparisons() throws SQLException;
  public boolean supportsSubqueriesInExists() throws SQLException;
  public boolean supportsSubqueriesInIns() throws SQLException;
  public boolean supportsSubqueriesInQuantifieds() throws SQLException;
  public boolean supportsTableCorrelationNames() throws SQLException;
  public boolean supportsTransactionIsolationLevel(int level) throws SQLException;
  public boolean supportsTransactions() throws SQLException;
  public boolean supportsUnion() throws SQLException;
  public boolean supportsUnionAll() throws SQLException;
  public boolean updatesAreDetected(int type) throws SQLException;
  public boolean usesLocalFilePerTable() throws SQLException;
  public boolean usesLocalFiles() throws SQLException;

}

