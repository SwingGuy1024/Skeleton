-- I didn't use this file to create the database. I got this from the schema after the database had been created. I don't remember how I created it, but I probably did so manually instead of from a file like this.
-- Jooq generates its java source files from the actual database, not from the schema.

CREATE TABLE "site" (
  id         INTEGER      NOT NULL PRIMARY KEY,
  source     VARCHAR(256) NOT NULL collate noCase,
  username   VARCHAR(256) NOT NULL collate noCase,
  password   VARCHAR(256) NOT NULL collate noCase,
  notes      LONG VARCHAR NOT NULL collate noCase
);
