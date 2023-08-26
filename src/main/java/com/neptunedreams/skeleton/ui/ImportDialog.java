package com.neptunedreams.skeleton.ui;

import com.neptunedreams.framework.ui.RecordController;
import com.neptunedreams.skeleton.data.SiteField;
import com.neptunedreams.skeleton.gen.tables.records.SiteRecord;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLayer;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/3/17
 * <p>Time: 9:51 PM
 *
 * @author Miguel Muñoz
 */
@SuppressWarnings("StringConcatenation")
public final class ImportDialog extends JDialog {
  private final RecordController<SiteRecord, Integer, @NonNull SiteField> controller;

  private @Nullable JTextComponent sourceField = null;
  private @Nullable JTextComponent usernameField = null;
  private @Nullable JTextComponent passwordField = null;
  private @Nullable JTextComponent notesField = null;

  private ImportDialog(
      Window parent,
      RecordController<SiteRecord, Integer, @NonNull SiteField> controller,
      RecordUI<@NonNull SiteField> recordUi
    ) {
    super(parent, ModalityType.DOCUMENT_MODAL);
    this.controller = controller;
    findRecordView(recordUi);
  }

  private void findRecordView(RecordUI<?> recordUI) {
    Map<Object, Consumer<JTextComponent>> cMap = new HashMap<>();
    cMap.put(RecordView.SOURCE_LABEL, c-> sourceField = c);
    cMap.put(RecordView.USER_NAME_LABEL, c-> usernameField = c);
    cMap.put(RecordView.PASSWORD_LABEL, c-> passwordField = c);
    cMap.put(RecordView.NOTES_LABEL, c-> notesField = c);
    for (Component c: recordUI.getComponents()) {
      if (c instanceof JLayer<?>) {
        @SuppressWarnings("unchecked")
        JLayer<RecordView<?>> layer = (JLayer<RecordView<?>>) c;
        JComponent rView = layer.getView();
        findTextComponents(rView, cMap);
      }
    }
  }

  @SuppressWarnings("argument") // this one makes no sense.
  private void findTextComponents(JComponent parent, Map<Object, Consumer<JTextComponent>> cMap) {
    for (Component c : parent.getComponents()) {
      if (c instanceof JComponent) {
        JComponent jc = (JComponent) c;
        if (jc instanceof JTextComponent) {
          JTextComponent jtc = (JTextComponent) jc;
          Object prop = jc.getClientProperty(RecordView.FIELD_NAME);
          if (cMap.containsKey(prop)) { // Suppressed warning: [argument] incompatible argument for parameter arg0 of Map.containsKey.
            cMap.get(prop).accept(jtc);
          }
        } else {
          if (!cMap.isEmpty()) {
            findTextComponents(jc, cMap);
          }
        }
        if (cMap.isEmpty()) {
          break;
        }
      }
    }
  } 
  
  static ImportDialog build(
      Window parent,
      RecordController<SiteRecord, Integer, @NonNull SiteField> controller,
      RecordUI<@NonNull SiteField> recordUI
  ) {
    ImportDialog importDialog = new ImportDialog(parent, controller, recordUI);
    importDialog.build();
    return importDialog;
  }

  private void build(ImportDialog this) {
    //noinspection MagicNumber
    JTextArea importArea = new JTextArea(40, 60);
    JScrollPane scrollPane = new JScrollPane(importArea, 
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(scrollPane, BorderLayout.CENTER);
    //noinspection HardCodedStringLiteral
    JButton load = new JButton("Load");
    getContentPane().add(load, BorderLayout.PAGE_END);
    pack();
    importArea.setLineWrap(true);
    importArea.setWrapStyleWord(true);
    
    load.addActionListener((e -> doLoad(importArea.getText())));
  }
  
  private void doLoad(final String rawText) {
    String text = rawText + "\n\n"; // Need to have a blank last line, or we lose the last entry.
    try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
      String line = "";
      ImportRecord importRecord = null;
      while (line != null) {
        line = line.trim();
        if (line.isEmpty()) {
          if (importRecord != null) {
            controller.addBlankRecord();
            importRecord.updateRecord();
            importRecord = null;
          }
        } else {
          if (importRecord == null) {
            importRecord = new ImportRecord();
          }
          importRecord.addString(line);
        }
        line = reader.readLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    dispose();
  }
  
  private class ImportRecord {
    public static final char SPACE = ' ';
    private final List<String> stringList = new LinkedList<>();
    
    public void addString(String s) {
      stringList.add(s);
    }
    
    public void updateRecord() {
      String pw;
      String userName;
      String source;
      String notes;
      final String firstLine = stringList.get(0).trim();

      // If there is a word with an @, that's the username. Everything after it is the password, and
      // everything before it is the source.
      //noinspection MagicCharacter
      int atSpot = firstLine.indexOf('@');

      if (atSpot >= 0) {
        int userNameEnd = firstLine.indexOf(SPACE, atSpot);
        if (userNameEnd < 0) {
          userNameEnd = firstLine.length() - 1;
        }
        int userNameStart = firstLine.substring(0, atSpot).lastIndexOf(SPACE)+1;
        userName = firstLine.substring(userNameStart, userNameEnd).trim();
        pw = firstLine.substring(userNameEnd).trim();
        source = firstLine.substring(0, userNameStart).trim();
      } else {
        
        // No @ character found, so the password is the last word, the username is the previous word,
        // and the source is everything before that.
        int pwStart = firstLine.lastIndexOf(SPACE) + 1;
        pw = firstLine.substring(pwStart);
        String remainder = firstLine.substring(0, pwStart).trim();
        int unStart = remainder.lastIndexOf(SPACE) + 1;
        userName = remainder.substring(unStart);
        source = remainder.substring(0, unStart).trim();
        if (source.isEmpty()) {
          source = userName;
          userName = "";
        }
      }

      StringBuilder buffer = new StringBuilder();
      for (int i = 1; i < stringList.size(); ++i) {
        //noinspection MagicCharacter
        buffer
            .append(stringList.get(i).trim())
            .append('\n');
      }
      notes = buffer.toString().trim();

      assert (notesField != null);
      assert (passwordField != null);
      assert (usernameField != null);
      assert (sourceField != null);

      if (notesField == null) {
        throw new IllegalStateException("Notes field");
      }
      notesField.setText(notes);

      if (passwordField == null) {
        throw new IllegalStateException("Password field");
      }
      passwordField.setText(pw);

      if (usernameField == null) {
        throw new IllegalStateException("UserName field");
      }
      usernameField.setText(userName);

      if (sourceField == null) {
        throw new IllegalStateException("Source field");
      }
      sourceField.setText(source);
    }
  }
}
