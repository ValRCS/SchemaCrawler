/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2018, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.integration.test;


import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Arrays;

import javax.sql.DataSource;

import org.junit.Test;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.test.utility.BaseSqliteTest;
import schemacrawler.testdb.SqlScript;
import schemacrawler.testdb.TestSchemaCreator;
import schemacrawler.utility.SchemaCrawlerUtility;
import sf.util.IOUtility;

public class TempTablesTest
  extends BaseSqliteTest
{

  @Test
  public void tempTables()
    throws Exception
  {
    final Path sqliteDbFile = IOUtility.createTempFilePath("sc", ".db")
      .normalize().toAbsolutePath();

    TestSchemaCreator.main(new String[] {
                                          "jdbc:sqlite:" + sqliteDbFile,
                                          null,
                                          null,
                                          "/sqlite.scripts.txt" });
    final Connection connection = executeSqlInTestDatabase(sqliteDbFile,
                                                           "/db/books/05_temp_tables_01_B.sql");

    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
      .withSchemaInfoLevel(SchemaInfoLevelBuilder.minimum())
      .tableTypes(Arrays.asList("GLOBAL TEMPORARY"));
    final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
      .toOptions();

    final Catalog catalog = SchemaCrawlerUtility
      .getCatalog(connection, schemaCrawlerOptions);
    final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
    assertEquals("Schema count does not match", 1, schemas.length);
    final Table[] tables = catalog.getTables(schemas[0]).toArray(new Table[0]);
    assertEquals("Table count does not match", 1, tables.length);
    final Table table = tables[0];
    assertEquals("Table name does not match",
                 "TEMP_AUTHOR_LIST",
                 table.getFullName());
  }

  protected Connection executeSqlInTestDatabase(final Path sqliteDbFile,
                                                final String databaseSqlResource)
    throws Exception
  {
    final DataSource dataSource = createDataSource(sqliteDbFile);

    final Connection connection = dataSource.getConnection();
    connection.setAutoCommit(false);

    final SqlScript sqlScript = new SqlScript(databaseSqlResource, connection);
    sqlScript.run();

    return connection;
  }

}
