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


import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.DatabaseSpecificOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.options.OutputOptions;
import sf.util.ObjectToString;
import sf.util.SchemaCrawlerLogger;

import static sf.util.Utility.isBlank;

/**
 * A SchemaCrawler tools executable unit.
 *
 * @author Sualeh Fatehi
 */
public abstract class BaseStagedExecutable
    implements StagedExecutable
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
      .getLogger(BaseStagedExecutable.class.getName());

  protected final String command;
  protected SchemaCrawlerOptions schemaCrawlerOptions;
  protected OutputOptions outputOptions;
  protected Config additionalConfiguration;
  protected DatabaseSpecificOptions databaseSpecificOptions;

  protected BaseStagedExecutable(final String command)
  {
    if (isBlank(command))
    {
      throw new IllegalArgumentException("No command specified");
    }
    this.command = command;

    schemaCrawlerOptions = new SchemaCrawlerOptions();
    outputOptions = new OutputOptions();
  }

  @Override
  public DatabaseSpecificOptions getDatabaseSpecificOptions()
  {
    return databaseSpecificOptions;
  }


  @Override
  public void setDatabaseSpecificOptions(final DatabaseSpecificOptions databaseSpecificOptions)
  {
    if (databaseSpecificOptions != null)
    {
      this.databaseSpecificOptions = databaseSpecificOptions;
    }
  }

  @Override
  public final Config getAdditionalConfiguration()
  {
    return additionalConfiguration;
  }

  @Override
  public final void setAdditionalConfiguration(final Config additionalConfiguration)
  {
    if (additionalConfiguration == null)
    {
      this.additionalConfiguration = new Config();
    }
    else
    {
      this.additionalConfiguration = additionalConfiguration;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getCommand()
  {
    return command;
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
  public final void setOutputOptions(final OutputOptions outputOptions)
  {
    if (outputOptions != null)
    {
      this.outputOptions = outputOptions;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final SchemaCrawlerOptions getSchemaCrawlerOptions()
  {
    return schemaCrawlerOptions;
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
