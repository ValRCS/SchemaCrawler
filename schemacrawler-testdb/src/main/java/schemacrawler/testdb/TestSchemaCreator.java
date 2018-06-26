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
package schemacrawler.testdb;


import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;

public class TestSchemaCreator
  implements Runnable
{

  public static void main(final String[] args)
    throws Exception
  {
    final String connectionUrl = args[0];
    final String user = args[1];
    final String password = args[2];
    final String scriptsResource = args[3];

    final Connection connection = DriverManager
      .getConnection(connectionUrl, user, password);
    connection.setAutoCommit(false);
    final TestSchemaCreator schemaCreator = new TestSchemaCreator(connection,
                                                                  scriptsResource);
    schemaCreator.run();
  }

  private final Connection connection;
  private final String scriptsResource;

  public TestSchemaCreator(final Connection connection,
                           final String scriptsResource)
  {
    this.connection = requireNonNull(connection,
                                     "No database connection provided");
    this.scriptsResource = requireNonNull(scriptsResource,
                                          "No script resource provided");
  }

  @Override
  public void run()
  {
    try (
        final BufferedReader scriptsReader = new BufferedReader(new InputStreamReader(TestSchemaCreator.class
          .getResourceAsStream(scriptsResource), UTF_8));)
    {
      scriptsReader.lines().forEach(scriptResourceLine -> {
        final SqlScript sqlScript = new SqlScript(scriptResourceLine,
                                                  connection);
        sqlScript.run();
      });
    }
    catch (final IOException e)
    {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
