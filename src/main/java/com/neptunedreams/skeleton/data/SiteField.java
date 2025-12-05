package com.neptunedreams.skeleton.data;

import com.neptunedreams.framework.data.DBField;
import com.neptunedreams.framework.ui.DisplayEnum;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/22/17
 * <p>Time: 5:18 PM
 *
 * @author Miguel Muñoz
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
  public @NotNull String getDisplay() {
    return toString();
  }
}
