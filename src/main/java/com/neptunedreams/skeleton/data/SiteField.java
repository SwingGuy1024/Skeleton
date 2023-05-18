package com.neptunedreams.skeleton.data;

import com.neptunedreams.framework.data.DBField;
import com.neptunedreams.framework.ui.DisplayEnum;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/22/17
 * <p>Time: 5:18 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum SiteField implements DisplayEnum, DBField {
  ID,
  Source,
  Username,
  Password,
  Notes,
  All(false);

  private final boolean isField;

  SiteField() {
    isField = true;
  }

  SiteField(boolean field) {
    isField = field;
  }

  @Override
  public boolean isField() {
    return isField;
  }

  @Override
  public @NonNull String getDisplay() {
    return toString();
  }
}
