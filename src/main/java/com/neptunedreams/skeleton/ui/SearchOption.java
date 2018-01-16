package com.neptunedreams.skeleton.ui;

import com.neptunedreams.framework.ui.DisplayEnum;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/29/17
 * <p>Time: 12:31 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("HardCodedStringLiteral")
public enum SearchOption implements DisplayEnum {
  findAny("Find Any"),
  findAll("Find All"),
  findExact("Find Exact");
  private final String display;

  SearchOption(String display) {
    this.display = display;
  }

  @Override
  public String getDisplay() { return display; }
}
