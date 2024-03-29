package com.neptunedreams.skeleton.data.sqlite;

import com.neptunedreams.framework.data.AbstractDaoFactory;
import com.neptunedreams.framework.data.ConnectionSource;
import com.neptunedreams.skeleton.gen.tables.records.SiteRecord;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/12/17
 * <p>Time: 2:08 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class SqLiteDaoFactory extends AbstractDaoFactory {
  @SuppressWarnings("JavaDoc")
  SqLiteDaoFactory(ConnectionSource connectionSource) {
    super();
    //noinspection UnnecessaryLocalVariable
    ConnectionSource source = connectionSource;
    addDao(SiteRecord.class, SQLiteRecordDao.create(source));
  }
}
