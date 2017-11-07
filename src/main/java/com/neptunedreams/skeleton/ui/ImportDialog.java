package com.neptunedreams.skeleton.ui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.Record;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/3/17
 * <p>Time: 9:51 PM
 *
 * @author Miguel Mu\u00f1oz
 */
class ImportDialog extends JDialog {
  private final Dao<Record> recordDao;
  ImportDialog(Window parent, Dao<Record> dao) {
    super(parent, ModalityType.DOCUMENT_MODAL);
    recordDao = dao;

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
    
    load.addActionListener((e -> doLoad(importArea.getText())));
  }
  
  private void doLoad(String text) {
    try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
      int fieldNumber=0;
      Record record = null;
      String line = "";
      while (line != null) {
        if (line.trim().isEmpty()) {
          fieldNumber = 0;
          if (record != null) {
            recordDao.save(record);
          }
          record = new Record();
        } else {
          if (record != null) {
            putLine(line.trim(), fieldNumber++, record);
          }
        }
        line = reader.readLine();
      }
      if (record != null) {
        recordDao.save(record);
      }
    } catch (IOException | SQLException e) {
      e.printStackTrace();
    }
  }
  
  private void putLine(String text, int fieldNumber, Record record) {
    switch (fieldNumber) {
      case 0:
        record.setSource(text);
        break;
      case 1:
        record.setUserName(text);
        break;
      case 2:
        record.setPassword(text);
        break;
      default:
        //noinspection StringConcatenation,MagicCharacter,HardcodedLineSeparator
        String notes = record.getNotes() + '\n' + text;
        //noinspection HardcodedLineSeparator
        while (notes.startsWith("\n")) {
          notes = notes.substring(1);
        }
        record.setNotes(notes);
    }
  }
}
