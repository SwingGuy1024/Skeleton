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
    List<String> row = new ArrayList<>();
    while (hasNext) {
      row.clear();
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
      //noinspection ObjectAllocationInLoop
      char[] dashes = new char[width + 1];
      Arrays.fill(dashes, '-');
      dashes[width] = ' '; // column-separator space
      //noinspection ObjectAllocationInLoop
      pack(line, new String(dashes), width, spaces);
    }
    System.out.println(line);

    // For each record, pack data into an empty line.
    for (List<String> resultData : results) {
      //noinspection ObjectAllocationInLoop
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
}
