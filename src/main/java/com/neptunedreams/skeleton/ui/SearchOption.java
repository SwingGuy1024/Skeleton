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
  /**
   * Find any word in the search text.
   */
  findAny("Find Any"),
  /**
   * Find all words.
   */
  findAll("Find All"),
  /**
   * find whole string as a single word.
   */
  findWhole("Find Whole");
  private final String display;

  SearchOption(String display) {
    this.display = display;
  }

  @Override
  public String getDisplay() { return display; }
}
