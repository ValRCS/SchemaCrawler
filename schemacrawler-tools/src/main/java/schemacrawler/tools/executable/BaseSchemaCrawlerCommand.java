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

package schemacrawler.tools.executable;


import static java.util.Objects.requireNonNull;
import static sf.util.Utility.isBlank;

import java.sql.Connection;

import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;
import schemacrawler.utility.Identifiers;
import sf.util.ObjectToString;

/**
 * A SchemaCrawler tools executable unit.
 *
 * @author Sualeh Fatehi
 */
public abstract class BaseSchemaCrawlerCommand
  implements SchemaCrawlerCommand
{

  protected final String command;

  protected SchemaCrawlerOptions schemaCrawlerOptions;
  protected OutputOptions outputOptions;
  protected Config additionalConfiguration;
  protected Identifiers identifiers;
  protected Connection connection;
  protected Catalog catalog;

  protected BaseSchemaCrawlerCommand(final String command)
  {
    if (isBlank(command))
    {
      throw new IllegalArgumentException("No command specified");
    }
    this.command = command;

    schemaCrawlerOptions = SchemaCrawlerOptionsBuilder
      .newSchemaCrawlerOptions();
    outputOptions = OutputOptionsBuilder.newOutputOptions();
    additionalConfiguration = new Config();
  }

  @Override
  public void checkAvailibility()
    throws Exception
  {
    // Assume that the command is available
  }

  protected void checkOptions()
  {
    requireNonNull(schemaCrawlerOptions, "No SchemaCrawler options provided");
    requireNonNull(additionalConfiguration,
                   "No additional configuration provided");
    requireNonNull(outputOptions, "No output options provided");
    requireNonNull(identifiers, "No database identifiers provided");
  }

  protected void checkCatalog()
  {
    requireNonNull(catalog, "No database catalog provided");
    requireNonNull(connection, "No database connection provided");
  }

  @Override
  public final Config getAdditionalConfiguration()
  {
    return additionalConfiguration;
  }

  @Override
  public Catalog getCatalog()
  {
    return catalog;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getCommand()
  {
    return command;
  }

  @Override
  public Connection getConnection()
  {
    return connection;
  }

  @Override
  public Identifiers getIdentifiers()
  {
    return identifiers;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final OutputOptions getOutputOptions()
  {
    return outputOptions;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final SchemaCrawlerOptions getSchemaCrawlerOptions()
  {
    return schemaCrawlerOptions;
  }

  @Override
  public final void setAdditionalConfiguration(final Config additionalConfiguration)
  {
    this.additionalConfiguration = requireNonNull(additionalConfiguration,
                                                  "No additional configuration provided");
  }

  @Override
  public void setCatalog(final Catalog catalog)
  {
    this.catalog = catalog;
  }

  @Override
  public void setConnection(final Connection connection)
  {
    this.connection = connection;
  }

  @Override
  public void setIdentifiers(final Identifiers identifiers)
  {
    this.identifiers = identifiers;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setOutputOptions(final OutputOptions outputOptions)
  {
    if (outputOptions != null)
    {
      this.outputOptions = outputOptions;
    }
    else
    {
      this.outputOptions = OutputOptionsBuilder.newOutputOptions();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setSchemaCrawlerOptions(final SchemaCrawlerOptions schemaCrawlerOptions)
  {
    if (schemaCrawlerOptions != null)
    {
      this.schemaCrawlerOptions = schemaCrawlerOptions;
    }
    else
    {
      this.schemaCrawlerOptions = SchemaCrawlerOptionsBuilder
        .newSchemaCrawlerOptions();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String toString()
  {
    return ObjectToString.toString(this);
  }

}
