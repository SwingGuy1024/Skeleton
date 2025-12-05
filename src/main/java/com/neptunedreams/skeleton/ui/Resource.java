package com.neptunedreams.skeleton.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.neptunedreams.framework.ui.TangoUtils;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 12:59 PM
 *
 * @author Miguel Muñoz
 */
@SuppressWarnings("CloneableClassWithoutClone")
enum Resource {
  ARROW_RIGHT_PNG("arrow_right.png", "Next Page"),
  ARROW_LEFT_PNG("arrow_left.png", "Previous Page"),
  MAGNIFIER_16_PNG("magnifier16.png", "Search"),
  BIN_EMPTY_PNG("bin_empty.png", "Delete Page"),
  BULLET_ADD_PNG("bullet_add.png", "Add New Page"),
  ARROW_FIRST_PNG("arrow_first.png", "First Page"),
  ARROW_LAST_PNG("arrow_last.png", "Last Page"),
  EDIT_PNG("bullet_edit.png", "Edit Page"),
  TEXT_SIZE_PNG("text_smallcaps.png", "Text Size"),
  ;
  
  private final String fileName;
  private final String text;
  Resource(String name, String text) {
    this.fileName = name;
    this.text = text;
  }
  
  public Action createAction(final @Nullable ActionListener actionListener) {
    final ActionListener actualActionListener = (actionListener == null) ? e -> {} : actionListener;
    return new AbstractAction(text, getIcon()) {
      @Override
      public void actionPerformed(ActionEvent e) {
        actualActionListener.actionPerformed(e);
      }
    };
  }

  public JButton createButton(@Nullable ActionListener actionListener) {
    Action action = createAction(actionListener);
    JButton button = new JButton(action);
    button.setToolTipText(text);
    button.setText(null); // argument.type.incompatible here
    return button;
  }
  
  public JLabel createLabel() {
    return new JLabel(getIcon());
  }
  
  public String getText() { return text; }

  private static final Set<Resource> colorShiftImages = EnumSet.of(
      ARROW_FIRST_PNG,
      ARROW_LAST_PNG,
      ARROW_LEFT_PNG,
      ARROW_RIGHT_PNG,
      BULLET_ADD_PNG
  );
  
  private static final int COLOR_SHIFT = 93;
  
  public Icon getIcon() { return getIcon(this); }

  private static Icon getIcon(Resource item) {
    URL resource = Resource.class.getResource(item.fileName);
    if (resource == null) {
      throw new IllegalStateException(String.format("Resource '%s' Not Found", item.fileName));
    }
    if (colorShiftImages.contains(item)) {
      return TangoUtils.shiftHue(new ImageIcon(resource), COLOR_SHIFT);
    }
    return new ImageIcon(resource);
  }


}
