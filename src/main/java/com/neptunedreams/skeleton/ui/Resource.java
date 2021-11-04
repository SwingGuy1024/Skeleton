package com.neptunedreams.skeleton.ui;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import com.neptunedreams.framework.ui.TangoUtils;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 12:59 PM
 *
 * @author Miguel Mu\u00f1oz
 */
enum Resource {
  ;

  private static final String ARROW_RIGHT_PNG = "arrow_right.png";
  private static final String ARROW_LEFT_PNG = "arrow_left.png";
  private static final String MAGNIFIER_16_PNG = "magnifier16.png";
  private static final String BIN_EMPTY_PNG = "bin_empty.png";
  private static final String BULLET_ADD_PNG = "bullet_add.png";
  private static final String ARROW_FIRST_PNG = "arrow_first.png";
  private static final String ARROW_LAST_PNG = "arrow_last.png";
  private static final String EDIT_PNG = "bullet_edit.png";
  
  private static final Set<String> colorShiftImages = new HashSet<>(Arrays.asList(
      ARROW_FIRST_PNG,
      ARROW_LAST_PNG,
      ARROW_LEFT_PNG,
      ARROW_RIGHT_PNG,
      BULLET_ADD_PNG
  ));
  
  private static final int COLOR_SHIFT = 93;

  private static Icon getIcon(String name) {
    URL resource = Objects.requireNonNull(Resource.class.getResource(name));
    if (colorShiftImages.contains(name)) {
      return TangoUtils.shiftHue(new ImageIcon(resource), COLOR_SHIFT);
    }
    return new ImageIcon(resource);
  }

  static Icon getRightArrow() {
    return getIcon(ARROW_RIGHT_PNG);
  }

  static Icon getLeftArrow() {
    return getIcon(ARROW_LEFT_PNG);
  }
  
  static Icon getBin() {
    return getIcon(BIN_EMPTY_PNG);
  }
  
  static JLabel getMagnifierLabel() {
    final Icon icon = getIcon(MAGNIFIER_16_PNG);
    return new JLabel(icon);
  }
  
  static Icon getAdd() {
    return getIcon(BULLET_ADD_PNG);
  }
  
  static Icon getFirst() {
    return getIcon(ARROW_FIRST_PNG);
  }
  
  static Icon getLast() {
    return getIcon(ARROW_LAST_PNG);
  }

  static Icon getEdit() {
    return getIcon(EDIT_PNG);
  }
}
