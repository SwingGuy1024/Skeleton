package com.neptunedreams.skeleton.data;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/28/17
 * <p>Time: 11:22 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class ConnectionWrapper implements Connection {
  private final Connection w;

  @SuppressWarnings("JavaDoc")
  public ConnectionWrapper(Connection wrapped) {
    w = wrapped;
  }
  @Override
  public Statement createStatement() throws SQLException {
    return w.createStatement();
  }

  @Override
  public PreparedStatement prepareStatement(final String sql) throws SQLException {
    return w.prepareStatement(sql);
  }

  @Override
  public CallableStatement prepareCall(final String sql) throws SQLException {
    return w.prepareCall(sql);
  }

  @Override
  public String nativeSQL(final String sql) throws SQLException {
    return w.nativeSQL(sql);
  }

  @Override
  public void setAutoCommit(final boolean autoCommit) throws SQLException {
    w.setAutoCommit(autoCommit);
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    return w.getAutoCommit();
  }

  @Override
  public void commit() throws SQLException {
    w.commit();
  }

  @Override
  public void rollback() throws SQLException {
    w.rollback();
  }

  @Override
  public void close() throws SQLException {
    w.close();
    Thread.dumpStack();
  }

  @Override
  public boolean isClosed() throws SQLException {
    return w.isClosed();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return w.getMetaData();
  }

  @Override
  public void setReadOnly(final boolean readOnly) throws SQLException {
    w.setReadOnly(readOnly);
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return w.isReadOnly();
  }

  @Override
  public void setCatalog(final String catalog) throws SQLException {
    w.setCatalog(catalog);
  }

  @Override
  public String getCatalog() throws SQLException {
    return w.getCatalog();
  }

  @Override
  public void setTransactionIsolation(final int level) throws SQLException {
    w.setTransactionIsolation(level);
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return w.getTransactionIsolation();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return w.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    w.clearWarnings();
  }

  @Override
  public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
    return w.createStatement(resultSetType, resultSetConcurrency);
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
    return w.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
    return w.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return w.getTypeMap();
  }

  @Override
  public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
    w.setTypeMap(map);
  }

  @Override
  public void setHoldability(final int holdability) throws SQLException {
    w.setHoldability(holdability);
  }

  @Override
  public int getHoldability() throws SQLException {
    return w.getHoldability();
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    return w.setSavepoint();
  }

  @Override
  public Savepoint setSavepoint(final String name) throws SQLException {
    return w.setSavepoint(name);
  }

  @Override
  public void rollback(final Savepoint savepoint) throws SQLException {
    w.rollback(savepoint);
  }

  @Override
  public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
    w.releaseSavepoint(savepoint);
  }

  @Override
  public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
    return w.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
    return w.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
    return w.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
    return w.prepareStatement(sql, autoGeneratedKeys);
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
    return w.prepareStatement(sql, columnIndexes);
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
    return w.prepareStatement(sql, columnNames);
  }

  @Override
  public Clob createClob() throws SQLException {
    return w.createClob();
  }

  @Override
  public Blob createBlob() throws SQLException {
    return w.createBlob();
  }

  @Override
  public NClob createNClob() throws SQLException {
    return w.createNClob();
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    return w.createSQLXML();
  }

  @Override
  public boolean isValid(final int timeout) throws SQLException {
    return w.isValid(timeout);
  }

  @Override
  public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
    w.setClientInfo(name, value);
  }

  @Override
  public void setClientInfo(final Properties properties) throws SQLClientInfoException {
    w.setClientInfo(properties);
  }

  @Override
  public String getClientInfo(final String name) throws SQLException {
    return w.getClientInfo(name);
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    return w.getClientInfo();
  }

  @Override
  public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
    return w.createArrayOf(typeName, elements);
  }

  @Override
  public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
    return w.createStruct(typeName, attributes);
  }

  @Override
  public void setSchema(final String schema) throws SQLException {
    w.setSchema(schema);
  }

  @Override
  public String getSchema() throws SQLException {
    return w.getSchema();
  }

  @Override
  public void abort(final Executor executor) throws SQLException {
    w.abort(executor);
  }

  @Override
  public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
    w.setNetworkTimeout(executor, milliseconds);
  }

  @Override
  public int getNetworkTimeout() throws SQLException {
    return w.getNetworkTimeout();
  }

  @Override
  public <T> T unwrap(final Class<T> iFace) throws SQLException {
    return w.unwrap(iFace);
  }

  @Override
  public boolean isWrapperFor(final Class<?> iFace) throws SQLException {
    return w.isWrapperFor(iFace);
  }
}
