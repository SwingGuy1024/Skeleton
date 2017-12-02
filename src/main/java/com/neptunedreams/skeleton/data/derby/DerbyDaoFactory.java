package com.neptunedreams.skeleton.data.derby;

import com.neptunedreams.skeleton.data.AbstractDaoFactory;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.Record;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/12/17
 * <p>Time: 12:06 PM
 *
 * @author Miguel Mu\u00f1oz
 */
class DerbyDaoFactory extends AbstractDaoFactory {
  DerbyDaoFactory(ConnectionSource source) {
    super();
    //noinspection UnnecessaryLocalVariable
    final ConnectionSource connectionSource = source;
    addDao(Record.class, new DerbyRecordDao(connectionSource));
  }
  
  
}
