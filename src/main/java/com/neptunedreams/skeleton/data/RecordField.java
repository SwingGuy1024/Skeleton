package com.neptunedreams.skeleton.data;

import org.checkerframework.checker.nullness.qual.KeyFor;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/22/17
 * <p>Time: 5:18 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum RecordField {
  @KeyFor("com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.fieldMap") ID,
  @KeyFor("com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.fieldMap") SOURCE,
  @KeyFor("com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.fieldMap") USERNAME,
  @KeyFor("com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.fieldMap") PASSWORD,
  @KeyFor("com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.fieldMap") NOTES
}
