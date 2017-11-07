package com.neptunedreams.skeleton.ui;

import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 12:59 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class Resource {

  private static final String ARROW_RIGHT_PNG = "arrow_right.png";
  private static final String ARROW_LEFT_PNG = "arrow_left.png";
  private static final String MAGNIFIER_16_PNG = "magnifier16.png";
  private static final String BIN_EMPTY_PNG = "bin_empty.png";
  private static final String BULLET_ADD_PNG = "bullet_add.png";
  private static final String ARROW_FIRST_PNG = "arrow_first.png";
  private static final String ARROW_LAST_PNG = "arrow_last.png";

  private Resource() { }
  
  public static Icon getIcon(String name) {
    URL resource = Resource.class.getResource(name);
//    if (resource == null) {
//      resource = Resource.class.getResource("/com/skeleton/ui/" + name);
//    }
//    if (resource == null) {
//      resource = Resource.class.getResource("/Users/miguelmunoz/Documents/skeleton/src/main/resource/com/skeleton/ui/" + name);
//    }
//    System.out.printf("Resource: %s from %s%n", resource, name);
    return new ImageIcon(resource);
  }

  public static Icon getRightArrow() {
    return getIcon(ARROW_RIGHT_PNG);
  }

  public static Icon getLeftArrow() {
    return getIcon(ARROW_LEFT_PNG);
  }
  
  public static Icon getBin() {
    return getIcon(BIN_EMPTY_PNG);
  }
  
  public static JLabel getMagnifierLabel() {
    final Icon icon = getIcon(MAGNIFIER_16_PNG);
    return new JLabel(icon);
  }
  
  public static Icon getAdd() {
    return getIcon(BULLET_ADD_PNG);
  }
  
  public static Icon getFirst() {
    return getIcon(ARROW_FIRST_PNG);
  }
  
  public static Icon getLast() {
    return getIcon(ARROW_LAST_PNG);
  }
}
