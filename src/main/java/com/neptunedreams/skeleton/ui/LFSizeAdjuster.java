package com.neptunedreams.skeleton.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sun.swing.SwingLazyValue;

/**
 * <p>Tool to adjust the size of the fonts used in the file. I created this because on MS Windows, the text was a bit too small to read.
 * This can create a JComboBox that lets the user choose a larger font size.</p> 
 * <p>Created by IntelliJ IDEA.</p>
 * <p>Date: 3/2/23</p>
 * <p>Time: 6:48 PM</p>
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("Singleton")
public enum LFSizeAdjuster {
  instance;
  
  private int delta = 0;
  private static final Set<Object> fontKeys = extractFontKeys();
  private static final Map<Object, Integer> defaultFontSizes = extractFontSizeMap(fontKeys);

  private final int defaultFontSize = UIManager.getLookAndFeelDefaults().getFont("TextField.font").getSize();
  
  private @Nullable Consumer<Integer> relaunch;
  
  public void setDelta(final int delta) {
    this.delta = delta;
  }
  
  public void setRelaunch(@Nullable Consumer<Integer> relaunch) {
    this.relaunch = relaunch;
  }

  public void adjustLookAndFeel() {
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();
    for (Object key: fontKeys) {
      Font font = (Font) defaults.get(key);
      int defaultSize = Objects.requireNonNull(defaultFontSizes.get(key));
      float newSize = defaultSize + delta; // has to be a float to mean size when we call deriveFont()
      Font revisedFont = font.deriveFont(newSize);
      defaults.put(key, revisedFont);
    }
  }

  // Only gets created once.
  private static Set<Object> extractFontKeys() {
    Set<Object> fontKeys = new HashSet<>();
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();

    // Do Not convert to EntrySet iteration. That strangely returns values of the wrong type.
    //noinspection KeySetIterationMayUseEntrySet
    for (Object key: defaults.keySet()) {
      Object value = defaults.get(key);
      if (value instanceof Font) {
        fontKeys.add(key);
      }
    }
    return fontKeys;
  }

  // Only gets created once.
  private static Map<Object, Integer> extractFontSizeMap(Set<Object> fontKeys) {
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();
    Map<Object, @NonNull Integer> fontMap = new HashMap<>();
    for (Object key: fontKeys) {
      Object value = defaults.get(key);
      if (value instanceof Font) {
        final Font theFont = (Font) value;
        fontMap.put(key, theFont.getSize());
//        System.out.printf("From %s, found %s %d point%n", key, theFont.getFamily(), theFont.getSize()); // NON-NLS
      }
    }
    return fontMap;
  }

  public JComponent createComboBox() {
    JPanel comboPanel = new JPanel();
    final BoxLayout layout = new BoxLayout(comboPanel, BoxLayout.LINE_AXIS);
    comboPanel.setLayout(layout);
    comboPanel.add(Box.createHorizontalGlue());
    comboPanel.add(new JLabel("Font Sizes: "));
    comboPanel.add(makeComboBox());
    comboPanel.add(Box.createHorizontalGlue());
    return comboPanel;
  }
  
  public JList<Object> createFontSizeList() {
    JList<Object> jList = new JList<>(getFontSizesArray());
    jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jList.setCellRenderer(getFontSizeRenderer());
    jList.setSelectedIndex(delta);
    return jList;
  }
  
  private JComboBox<String> makeComboBox() {
    String[] sizes = getFontSizesArray();
    ListCellRenderer<Object> renderer = getFontSizeRenderer();
    JComboBox<String> comboBox = new JComboBox<>(sizes);
    comboBox.setRenderer(renderer);
    comboBox.setSelectedIndex(delta);
    comboBox.setMaximumRowCount(sizes.length);
    comboBox.addItemListener(this::processItem);
    return comboBox;
  }

  @NonNull
  private ListCellRenderer<Object> getFontSizeRenderer() {
    @SuppressWarnings("unchecked")
    ListCellRenderer<Object> renderer = (ListCellRenderer<Object>) new BasicComboBoxRenderer() {
      @Override
      public Component getListCellRendererComponent(
          final JList list,
          final Object value,
          final int index,
          final boolean isSelected
          , final boolean cellHasFocus) {
        // TODO: Write .getListCellRendererComponent()
        final Component listCellRendererComponent
            = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        JLabel label = (JLabel) listCellRendererComponent;
        float size = firstWordToNumber(value.toString());
        label.setFont(label.getFont().deriveFont(size));
        return listCellRendererComponent;
      }
    };
    return renderer;
  }

  @NonNull
  private String[] getFontSizesArray() {
    final int max = 11;
    List<String> itemList = new ArrayList<>(max);
    for (int i=0; i<max; ++i) {
      final int size = i + defaultFontSize;
      String s = String.format("%d Point Font", size);
      itemList.add(s);
    }
    @SuppressWarnings("ZeroLengthArrayAllocation")
    String[] sizes = itemList.toArray(new String[0]);
    return sizes;
  }

  private void processItem(final ItemEvent itemEvent) {
    // Ignore the DESELECTED event.
    if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
      String text = itemEvent.getItem().toString();
      changeFontSize(text);
    }
  }

  public void changeFontSize(final String text) {
    int size = firstWordToNumber(text);
    delta = size - defaultFontSize;
    Objects.requireNonNull(relaunch).accept(delta);
  }

  private int firstWordToNumber(final String s) {
    StringTokenizer tokenizer = new StringTokenizer(s);
    return  Integer.parseInt(tokenizer.nextToken());
  }
}
