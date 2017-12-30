package com.neptunedreams.skeleton.data;

import com.neptunedreams.framework.ui.DisplayEnum;
import org.checkerframework.checker.nullness.qual.KeyFor;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/22/17
 * <p>Time: 5:18 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum RecordField implements DisplayEnum {
  @KeyFor("com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.fieldMap") ID,
  @KeyFor("com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.fieldMap") Source,
  @KeyFor("com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.fieldMap") Username,
  @KeyFor("com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.fieldMap") Password,
  @KeyFor("com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.fieldMap") Notes,
  @KeyFor("com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.fieldMap") All(false);

  private final boolean isField;

  RecordField() {
    isField = true;
  }

  RecordField(boolean field) {
    isField = field;
  }

  public boolean isField() {
    return isField;
  }

  @Override
  public String getDisplay() {
    return toString();
  }
}
