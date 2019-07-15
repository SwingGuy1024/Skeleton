package com.neptunedreams.framework.ui;

import java.awt.CardLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Allows you to show or hide any JComponent, without changing the layout. The hideable component takes up
 * the same amount of space regardless of whether it's visible. The setContentVisible() method is used to show or 
 * hide the content. (Be sure not to mistakenly call setVisible().)
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/28/17
 * <p>Time: 7:10 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class HidingPanel extends JPanel {
  private boolean visible = false;
  private final @NonNull CardLayout layout;

  private HidingPanel() {
    super();
    layout = new CardLayout();
  }

  /**
   * I'm using a factory method because I can't call the add() methods from a constructor. That's very annoying. 
   * I'm not sure how I feel about this. It prevents me from writing subclasses. Is this a good thing or a bad thing?
   * @param content The content to wrap
   * @return a HidingPanel with the specified content, which is initially hidden.
   */
  public static HidingPanel create(JComponent content) {
    HidingPanel panel = new HidingPanel();
    panel.setLayout(panel.layout);
    // I'm assuming here that the first item added will be the default card, so visible defaults to false;
    panel.add(String.valueOf(false), new JPanel()); // blank panel
    panel.add(String.valueOf(true), content);
    return panel;
  }

  /**
   * Create a HidingPanel with the specified content and initial visibility.
   * @param content The content
   * @param visible The initial visibility
   * @return the properly configured content panel
   */
  public static HidingPanel create(JComponent content, boolean visible) {
    HidingPanel panel = create(content);
    panel.visible = visible;
    panel.setContentVisible(visible);
    return panel;
  }
  
  public void setContentVisible(boolean vis) {
    layout.show(this, String.valueOf(vis));
    visible = vis;
  }
  
  public boolean isContentVisible() {
    return visible;
  }
}
