The skeleton.db file in this directory is the SqLite3 database that jOOQ uses to generate the java code used to access the table. But it's not the database that the application uses. This way, we can delete the actual database as we make changes, but the master, from which the code is generated, will remain untouched.

To modify the structure of the database, you must change the create statement in the java code in `com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao.java`

If you change the create statement, it's important to apply that revised create statement to the table in this database. It has a few records that were once used for testing, but they're not necessary.

There is also a create statement in src/main/sql/skeleton.sql. This should match the one in SQLiteRecordDao.java. You may use it to create the database. Utltimately, I should change the Dao class to read this file instead of having two copies.
