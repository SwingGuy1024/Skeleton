package com.neptunedreams.skeleton;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.function.Function;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/3/17
 * <p>Time: 12:55 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"ConstantConditions", "ReturnOfNull", "ZeroLengthArrayAllocation", "MagicConstant", "MagicCharacter", "UseOfSystemOutOrSystemErr"})
public enum DataUtil {
  ;

  /*
  public static <T> void printRecord(Collection<T> records, Function<T, String>... getter) throws SQLException {
    ResultSet resultSet = wrapCollection(records, getter);
    printResultSet(resultSet);
  }
  */

  public static void printResultSet(final ResultSet resultSet) throws SQLException {
    ResultSetMetaData metaData = resultSet.getMetaData();
    int columnCount = metaData.getColumnCount();
    List<List<String>> results = new LinkedList<>();

    // maxWidth_1 is 1-based
    int[] maxWidth_1 = new int[columnCount + 1];
    Arrays.fill(maxWidth_1, 0);

    // Gather all the data and calculate the maximum column widths.
    boolean hasNext = resultSet.next();
    while (hasNext) {
      List<String> row = new ArrayList<>();
      for (int col = 1; col <= columnCount; ++col) {
        String value = String.valueOf(resultSet.getString(col)); // I use String.valueOf() to turn null into "null"
        // value is definitely not null here.
        row.add(value);
        int width = value.length();
        if (maxWidth_1[col] < width) {
          maxWidth_1[col] = width;
        }
      }
      results.add(row);
      hasNext = resultSet.next();
    }

    // Incorporate the header widths into the column widths.
    for (int ii = 1; ii <= columnCount; ++ii) {
      String cName = metaData.getColumnName(ii);
      int width = cName.length();
      if (maxWidth_1[ii] < width) {
        maxWidth_1[ii] = width;
      }
    }

    // Find the widest column
    int max = max(maxWidth_1);
    char[] maxArray = new char[max + 1]; // + 1 for space between columns
    //noinspection MagicCharacter
    Arrays.fill(maxArray, ' '); // Fill the array with spaces.
    String spaces = new String(maxArray); // This is the maximum number of spaces for any column in any row.

    // Pack data into an empty line from the column headers.
    StringBuilder line = new StringBuilder();
    for (int ii = 1; ii <= columnCount; ++ii) {
      pack(line, metaData.getColumnName(ii), maxWidth_1[ii], spaces);
    }
    System.out.println(line);

    // Print out the underscores
    line = new StringBuilder();
    for (int ii = 1; ii <= columnCount; ++ii) {
      final int width = maxWidth_1[ii];
      char[] dashes = new char[width + 1];
      Arrays.fill(dashes, '-');
      dashes[width] = ' '; // column-separator space
      pack(line, new String(dashes), width, spaces);
    }
    System.out.println(line);

    // For each record, pack data into an empty line.
    for (List<String> resultData : results) {
      line = new StringBuilder();
      for (int col = 0; col < columnCount; ++col) {
        pack(line, resultData.get(col), maxWidth_1[col + 1], spaces);
      }
      System.out.println(line);
    }
    System.out.println("---");
  }

  private static int max(int[] array) {
    int max = Integer.MIN_VALUE;
    for (final int i : array) {
      if (i > max) {
        max = i;
      }
    }
    return max;
  }

  private static void pack(StringBuilder line, final String value, final int width, String spaces) {
    line.append(value);
    // Pad spaces at the end of the value, including a column-separator space.
    line.append(spaces.substring(0, (width - value.length()) + 1));
//    line.append(spaces.substring(value.length(), width+1)); // alternate way, works too.
  }

  /*
  private static <T> ResultSet wrapCollection(final Collection<T> recordCollection, final Getter<?>... getter) {
    final List<T> records = ((recordCollection instanceof RandomAccess) && (recordCollection instanceof List)) ?
        (List<T>) recordCollection : new ArrayList<>(recordCollection);
    return new ResultSet() {
      int rowIndex = 0;
      boolean wasNull = false;
      @Override
      public boolean next() throws SQLException {
        rowIndex++;
        return rowIndex < records.size();
      }

      @Override
      public void close() throws SQLException { }

      @Override
      public boolean wasNull() throws SQLException {
        return wasNull;
      }

      @Override
      public String getString(final int columnIndex) throws SQLException {
        return String.valueOf(getter[columnIndex].apply(records.get(rowIndex-1)));
      }

      @Override
      public boolean getBoolean(final int columnIndex) throws SQLException {
        return Boolean.valueOf(getter[columnIndex].apply(records.get(rowIndex-1)));
      }

      @Override
      public byte getByte(final int columnIndex) throws SQLException {
        return Byte.valueOf(getter[columnIndex].apply(records.get(rowIndex-1)));
      }

      @Override
      public short getShort(final int columnIndex) throws SQLException {
        return Short.valueOf(getter[columnIndex].apply(records.get(rowIndex-1)));
      }

      @Override
      public int getInt(final int columnIndex) throws SQLException {
        return Integer.valueOf(getter[columnIndex].apply(records.get(rowIndex-1)));
      }

      @Override
      public long getLong(final int columnIndex) throws SQLException {
        return Long.valueOf(getter[columnIndex].apply(records.get(rowIndex-1)));
      }

      @Override
      public float getFloat(final int columnIndex) throws SQLException {
        return Float.valueOf(getter[columnIndex].apply(records.get(rowIndex-1)));
      }

      @Override
      public double getDouble(final int columnIndex) throws SQLException {
        return Double.valueOf(getter[columnIndex].apply(records.get(rowIndex-1)));
      }

      @Override
      public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return new BigDecimal(String.valueOf(getter[columnIndex].apply(records.get(rowIndex-1))));
      }

      @Override
      public byte[] getBytes(final int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Can't get Bytes[] yet.");
      }

      @Override
      public Date getDate(final int columnIndex) throws SQLException {
        return Date.valueOf(String.valueOf(getter[columnIndex].apply(records.get(rowIndex-1))));
      }

      @Override
      public Time getTime(final int columnIndex) throws SQLException {
        return Time.valueOf(String.valueOf(getter[columnIndex].apply(records.get(rowIndex-1))));
      }

      @Override
      public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return Timestamp.valueOf(String.valueOf(getter[columnIndex].apply(records.get(rowIndex-1))));
      }

      @Override
      public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public String getString(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public boolean getBoolean(final String columnLabel) throws SQLException {
        return false;
      }

      @Override
      public byte getByte(final String columnLabel) throws SQLException {
        return 0;
      }

      @Override
      public short getShort(final String columnLabel) throws SQLException {
        return 0;
      }

      @Override
      public int getInt(final String columnLabel) throws SQLException {
        return 0;
      }

      @Override
      public long getLong(final String columnLabel) throws SQLException {
        return 0;
      }

      @Override
      public float getFloat(final String columnLabel) throws SQLException {
        return 0;
      }

      @Override
      public double getDouble(final String columnLabel) throws SQLException {
        return 0;
      }

      @Override
      public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return null;
      }

      @Override
      public byte[] getBytes(final String columnLabel) throws SQLException {
        return new byte[0];
      }

      @Override
      public Date getDate(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public Time getTime(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public SQLWarning getWarnings() throws SQLException {
        return null;
      }

      @Override
      public void clearWarnings() throws SQLException {

      }

      @Override
      public String getCursorName() throws SQLException {
        return null;
      }

      @Override
      public ResultSetMetaData getMetaData() throws SQLException {
        // We only call one method of ResultSetMetaData, so that's all we implement.
        return new ResultSetMetaData() {
          @Override
          public int getColumnCount() throws SQLException {
            return getter.length;
          }

          @Override
          public boolean isAutoIncrement(final int column) throws SQLException {
            return false;
          }

          @Override
          public boolean isCaseSensitive(final int column) throws SQLException {
            return false;
          }

          @Override
          public boolean isSearchable(final int column) throws SQLException {
            return false;
          }

          @Override
          public boolean isCurrency(final int column) throws SQLException {
            return false;
          }

          @Override
          public int isNullable(final int column) throws SQLException {
            return 0;
          }

          @Override
          public boolean isSigned(final int column) throws SQLException {
            return false;
          }

          @Override
          public int getColumnDisplaySize(final int column) throws SQLException {
            return 0;
          }

          @Override
          public String getColumnLabel(final int column) throws SQLException {
            return null;
          }

          @Override
          public String getColumnName(final int column) throws SQLException {
            return getter[column].toString();
          }

          @Override
          public String getSchemaName(final int column) throws SQLException {
            return null;
          }

          @Override
          public int getPrecision(final int column) throws SQLException {
            return 0;
          }

          @Override
          public int getScale(final int column) throws SQLException {
            return 0;
          }

          @Override
          public String getTableName(final int column) throws SQLException {
            return null;
          }

          @Override
          public String getCatalogName(final int column) throws SQLException {
            return null;
          }

          @Override
          public int getColumnType(final int column) throws SQLException {
            return 0;
          }

          @Override
          public String getColumnTypeName(final int column) throws SQLException {
            return null;
          }

          @Override
          public boolean isReadOnly(final int column) throws SQLException {
            return false;
          }

          @Override
          public boolean isWritable(final int column) throws SQLException {
            return false;
          }

          @Override
          public boolean isDefinitelyWritable(final int column) throws SQLException {
            return false;
          }

          @Override
          public String getColumnClassName(final int column) throws SQLException {
            return null;
          }

          @Override
          public <X> X unwrap(final Class<X> iFace) throws SQLException {
            return null;
          }

          @Override
          public boolean isWrapperFor(final Class<?> iFace) throws SQLException {
            return false;
          }
        };
      }

      @Override
      public Object getObject(final int columnIndex) throws SQLException {
        return getter[columnIndex].apply(records.get(rowIndex));
      }

      @Override
      public Object getObject(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public int findColumn(final String columnLabel) throws SQLException {
        return 0;
      }

      @Override
      public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public Reader getCharacterStream(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public boolean isBeforeFirst() throws SQLException {
        return rowIndex == 0;
      }

      @Override
      public boolean isAfterLast() throws SQLException {
        return rowIndex >= records.size();
      }

      @Override
      public boolean isFirst() throws SQLException {
        return rowIndex == 1;
      }

      @Override
      public boolean isLast() throws SQLException {
        return rowIndex == (records.size() - 1);
      }

      @Override
      public void beforeFirst() throws SQLException {
        rowIndex = 0;
      }

      @Override
      public void afterLast() throws SQLException {
        rowIndex = records.size();
      }

      @Override
      public boolean first() throws SQLException {
        rowIndex = 1;
        return !records.isEmpty();
      }

      @Override
      public boolean last() throws SQLException {
        rowIndex = records.size();
        return !records.isEmpty();
      }

      @Override
      public int getRow() throws SQLException {
        return rowIndex;
      }

      @Override
      public boolean absolute(final int row) throws SQLException {
        rowIndex = row;
        return (rowIndex > 0) && (rowIndex <= records.size());
      }

      @Override
      public boolean relative(final int rows) throws SQLException {
        rowIndex += rows;
        return (rowIndex > 0) && (rowIndex <= records.size());
      }

      @Override
      public boolean previous() throws SQLException {
        rowIndex--;
        return (rowIndex > 0) && (rowIndex <= records.size());
      }

      @Override
      public void setFetchDirection(final int direction) throws SQLException {

      }

      @Override
      public int getFetchDirection() throws SQLException {
        return 0;
      }

      @Override
      public void setFetchSize(final int rows) throws SQLException {

      }

      @Override
      public int getFetchSize() throws SQLException {
        return 0;
      }

      @Override
      public int getType() throws SQLException {
        return 0;
      }

      @Override
      public int getConcurrency() throws SQLException {
        return 0;
      }

      @Override
      public boolean rowUpdated() throws SQLException {
        return false;
      }

      @Override
      public boolean rowInserted() throws SQLException {
        return false;
      }

      @Override
      public boolean rowDeleted() throws SQLException {
        return false;
      }

      @Override
      public void updateNull(final int columnIndex) throws SQLException {

      }

      @Override
      public void updateBoolean(final int columnIndex, final boolean x) throws SQLException {

      }

      @Override
      public void updateByte(final int columnIndex, final byte x) throws SQLException {

      }

      @Override
      public void updateShort(final int columnIndex, final short x) throws SQLException {

      }

      @Override
      public void updateInt(final int columnIndex, final int x) throws SQLException {

      }

      @Override
      public void updateLong(final int columnIndex, final long x) throws SQLException {

      }

      @Override
      public void updateFloat(final int columnIndex, final float x) throws SQLException {

      }

      @Override
      public void updateDouble(final int columnIndex, final double x) throws SQLException {

      }

      @Override
      public void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException {

      }

      @Override
      public void updateString(final int columnIndex, final String x) throws SQLException {

      }

      @Override
      public void updateBytes(final int columnIndex, final byte[] x) throws SQLException {

      }

      @Override
      public void updateDate(final int columnIndex, final Date x) throws SQLException {

      }

      @Override
      public void updateTime(final int columnIndex, final Time x) throws SQLException {

      }

      @Override
      public void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException {

      }

      @Override
      public void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException {

      }

      @Override
      public void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException {

      }

      @Override
      public void updateCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {

      }

      @Override
      public void updateObject(final int columnIndex, final Object x, final int scaleOrLength) throws SQLException {

      }

      @Override
      public void updateObject(final int columnIndex, final Object x) throws SQLException {

      }

      @Override
      public void updateNull(final String columnLabel) throws SQLException {

      }

      @Override
      public void updateBoolean(final String columnLabel, final boolean x) throws SQLException {

      }

      @Override
      public void updateByte(final String columnLabel, final byte x) throws SQLException {

      }

      @Override
      public void updateShort(final String columnLabel, final short x) throws SQLException {

      }

      @Override
      public void updateInt(final String columnLabel, final int x) throws SQLException {

      }

      @Override
      public void updateLong(final String columnLabel, final long x) throws SQLException {

      }

      @Override
      public void updateFloat(final String columnLabel, final float x) throws SQLException {

      }

      @Override
      public void updateDouble(final String columnLabel, final double x) throws SQLException {

      }

      @Override
      public void updateBigDecimal(final String columnLabel, final BigDecimal x) throws SQLException {

      }

      @Override
      public void updateString(final String columnLabel, final String x) throws SQLException {

      }

      @Override
      public void updateBytes(final String columnLabel, final byte[] x) throws SQLException {

      }

      @Override
      public void updateDate(final String columnLabel, final Date x) throws SQLException {

      }

      @Override
      public void updateTime(final String columnLabel, final Time x) throws SQLException {

      }

      @Override
      public void updateTimestamp(final String columnLabel, final Timestamp x) throws SQLException {

      }

      @Override
      public void updateAsciiStream(final String columnLabel, final InputStream x, final int length) throws SQLException {

      }

      @Override
      public void updateBinaryStream(final String columnLabel, final InputStream x, final int length) throws SQLException {

      }

      @Override
      public void updateCharacterStream(final String columnLabel, final Reader reader, final int length) throws SQLException {

      }

      @Override
      public void updateObject(final String columnLabel, final Object x, final int scaleOrLength) throws SQLException {

      }

      @Override
      public void updateObject(final String columnLabel, final Object x) throws SQLException {

      }

      @Override
      public void insertRow() throws SQLException {

      }

      @Override
      public void updateRow() throws SQLException {

      }

      @Override
      public void deleteRow() throws SQLException {

      }

      @Override
      public void refreshRow() throws SQLException {

      }

      @Override
      public void cancelRowUpdates() throws SQLException {

      }

      @Override
      public void moveToInsertRow() throws SQLException {

      }

      @Override
      public void moveToCurrentRow() throws SQLException {

      }

      @Override
      public Statement getStatement() throws SQLException {
        return null;
      }

      @Override
      public Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException {
        return null;
      }

      @Override
      public Ref getRef(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public Blob getBlob(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public Clob getClob(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public Array getArray(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public Object getObject(final String columnLabel, final Map<String, Class<?>> map) throws SQLException {
        return null;
      }

      @Override
      public Ref getRef(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public Blob getBlob(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public Clob getClob(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public Array getArray(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
      }

      @Override
      public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
      }

      @Override
      public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
      }

      @Override
      public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
      }

      @Override
      public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
      }

      @Override
      public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
      }

      @Override
      public URL getURL(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public URL getURL(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public void updateRef(final int columnIndex, final Ref x) throws SQLException {

      }

      @Override
      public void updateRef(final String columnLabel, final Ref x) throws SQLException {

      }

      @Override
      public void updateBlob(final int columnIndex, final Blob x) throws SQLException {

      }

      @Override
      public void updateBlob(final String columnLabel, final Blob x) throws SQLException {

      }

      @Override
      public void updateClob(final int columnIndex, final Clob x) throws SQLException {

      }

      @Override
      public void updateClob(final String columnLabel, final Clob x) throws SQLException {

      }

      @Override
      public void updateArray(final int columnIndex, final Array x) throws SQLException {

      }

      @Override
      public void updateArray(final String columnLabel, final Array x) throws SQLException {

      }

      @Override
      public RowId getRowId(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public RowId getRowId(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public void updateRowId(final int columnIndex, final RowId x) throws SQLException {

      }

      @Override
      public void updateRowId(final String columnLabel, final RowId x) throws SQLException {

      }

      @Override
      public int getHoldability() throws SQLException {
        return 0;
      }

      @Override
      public boolean isClosed() throws SQLException {
        return false;
      }

      @Override
      public void updateNString(final int columnIndex, final String nString) throws SQLException {

      }

      @Override
      public void updateNString(final String columnLabel, final String nString) throws SQLException {

      }

      @Override
      public void updateNClob(final int columnIndex, final NClob nClob) throws SQLException {

      }

      @Override
      public void updateNClob(final String columnLabel, final NClob nClob) throws SQLException {

      }

      @Override
      public NClob getNClob(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public NClob getNClob(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException {

      }

      @Override
      public void updateSQLXML(final String columnLabel, final SQLXML xmlObject) throws SQLException {

      }

      @Override
      public String getNString(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public String getNString(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public Reader getNCharacterStream(final int columnIndex) throws SQLException {
        return null;
      }

      @Override
      public Reader getNCharacterStream(final String columnLabel) throws SQLException {
        return null;
      }

      @Override
      public void updateNCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {

      }

      @Override
      public void updateNCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {

      }

      @Override
      public void updateAsciiStream(final int columnIndex, final InputStream x, final long length) throws SQLException {

      }

      @Override
      public void updateBinaryStream(final int columnIndex, final InputStream x, final long length) throws SQLException {

      }

      @Override
      public void updateCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {

      }

      @Override
      public void updateAsciiStream(final String columnLabel, final InputStream x, final long length) throws SQLException {

      }

      @Override
      public void updateBinaryStream(final String columnLabel, final InputStream x, final long length) throws SQLException {

      }

      @Override
      public void updateCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {

      }

      @Override
      public void updateBlob(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {

      }

      @Override
      public void updateBlob(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {

      }

      @Override
      public void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException {

      }

      @Override
      public void updateClob(final String columnLabel, final Reader reader, final long length) throws SQLException {

      }

      @Override
      public void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException {

      }

      @Override
      public void updateNClob(final String columnLabel, final Reader reader, final long length) throws SQLException {

      }

      @Override
      public void updateNCharacterStream(final int columnIndex, final Reader x) throws SQLException {

      }

      @Override
      public void updateNCharacterStream(final String columnLabel, final Reader reader) throws SQLException {

      }

      @Override
      public void updateAsciiStream(final int columnIndex, final InputStream x) throws SQLException {

      }

      @Override
      public void updateBinaryStream(final int columnIndex, final InputStream x) throws SQLException {

      }

      @Override
      public void updateCharacterStream(final int columnIndex, final Reader x) throws SQLException {

      }

      @Override
      public void updateAsciiStream(final String columnLabel, final InputStream x) throws SQLException {

      }

      @Override
      public void updateBinaryStream(final String columnLabel, final InputStream x) throws SQLException {

      }

      @Override
      public void updateCharacterStream(final String columnLabel, final Reader reader) throws SQLException {

      }

      @Override
      public void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException {

      }

      @Override
      public void updateBlob(final String columnLabel, final InputStream inputStream) throws SQLException {

      }

      @Override
      public void updateClob(final int columnIndex, final Reader reader) throws SQLException {

      }

      @Override
      public void updateClob(final String columnLabel, final Reader reader) throws SQLException {

      }

      @Override
      public void updateNClob(final int columnIndex, final Reader reader) throws SQLException {

      }

      @Override
      public void updateNClob(final String columnLabel, final Reader reader) throws SQLException {

      }

      @Override
      public <X> X getObject(final int columnIndex, final Class<X> type) throws SQLException {
        return null;
      }

      @Override
      public <X> X getObject(final String columnLabel, final Class<X> type) throws SQLException {
        return null;
      }

      @Override
      public <X> X unwrap(final Class<X> iFace) throws SQLException {
        return null;
      }

      @Override
      public boolean isWrapperFor(final Class<?> iFace) throws SQLException {
        return false;
      }
    };
  }
  */
  @FunctionalInterface
  public interface Getter<T> {
    T getValue();
  }
}
