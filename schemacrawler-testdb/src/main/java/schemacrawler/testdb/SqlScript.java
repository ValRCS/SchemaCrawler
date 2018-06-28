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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SqlScript
  implements Runnable
{

  private static final Logger LOGGER = Logger
    .getLogger(SqlScript.class.getName());

  private static final boolean debug = Boolean.valueOf(System
    .getProperty("schemacrawler.testdb.SqlScript.debug", "false"));

  private final String scriptResource;
  private final String delimiter;
  private final Connection connection;

  public SqlScript(final String scriptResourceLine, final Connection connection)
  {
    requireNonNull(scriptResourceLine, "No script resource line provided");
    final String[] split = scriptResourceLine.split(",");
    if (split.length == 1)
    {
      scriptResource = scriptResourceLine.trim();
      if (scriptResource == null || scriptResource.isEmpty())
      {
        delimiter = "#";
      }
      else
      {
        delimiter = ";";
      }
    }
    else if (split.length == 2)
    {
      delimiter = split[0].trim();
      scriptResource = split[1].trim();
    }
    else
    {
      throw new RuntimeException("Too many fields in " + scriptResourceLine);
    }

    this.connection = requireNonNull(connection,
                                     "No database connection provided");
  }

  @Override
  public void run()
  {

    final boolean skip = delimiter.equals("#");

    if (debug)
    {
      LOGGER.log(Level.INFO,
                 String.format("%s -- delimiter %s -- %s",
                               scriptResource,
                               delimiter,
                               skip? "skip": "execute"));
    }

    if (skip)
    {
      return;
    }

    try (
        final BufferedReader lineReader = new BufferedReader(new InputStreamReader(this
          .getClass().getResourceAsStream(scriptResource), UTF_8));
    // NOTE: Do not close connection, since we did not open it
    )
    {
      final List<String> sqlList = readSql(lineReader);
      for (final String sql: sqlList)
      {
        try (final Statement statement = connection.createStatement();)
        {
          if (Pattern.matches("\\s+", sql))
          {
            continue;
          }
          if (debug)
          {
            LOGGER.log(Level.INFO, "\n" + sql);
          }
          statement.execute(sql);
          connection.commit();
        }
      }
    }
    catch (final Exception e)
    {
      final Throwable throwable = getCause(e);
      final String message = String
        .format("Script: %s -- %s", scriptResource, throwable.getMessage());
      System.err.println(message);
      LOGGER.log(Level.WARNING, message, throwable);
      throw new RuntimeException(e);
    }

  }

  private Throwable getCause(final Throwable e)
  {
    Throwable cause = null;
    Throwable result = e;

    while (null != (cause = result.getCause()) && result != cause)
    {
      result = cause;
    }
    return result;
  }

  private List<String> readSql(final BufferedReader lineReader)
    throws IOException
  {
    final List<String> list = new ArrayList<>();
    String line;
    StringBuilder sql = new StringBuilder();
    while ((line = lineReader.readLine()) != null)
    {
      final String trimmedLine = line.trim();
      final boolean isComment = trimmedLine.startsWith("--")
                                || trimmedLine.startsWith("//");
      if (!isComment && trimmedLine.endsWith(delimiter))
      {
        sql.append(line.substring(0, line.lastIndexOf(delimiter)));
        list.add(sql.toString());
        sql = new StringBuilder();
      }
      else
      {
        sql.append(line);
        sql.append("\n");
      }
    }
    // Check if the last line is not delimited
    if (sql.length() > 0)
    {
      list.add(sql.toString());
    }

    return list;
  }

}
