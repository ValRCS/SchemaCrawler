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
package schemacrawler.tools.integration.velocity;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.executable.CommandProvider;
import schemacrawler.tools.executable.SchemaCrawlerCommand;
import schemacrawler.tools.iosource.ClasspathInputResource;
import schemacrawler.tools.iosource.EmptyInputResource;
import schemacrawler.tools.iosource.InputResource;
import schemacrawler.tools.options.OutputOptions;
import sf.util.SchemaCrawlerLogger;

public class VelocityCommandProvider
  implements CommandProvider
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(VelocityCommandProvider.class.getName());

  @Override
  public InputResource getHelp()
  {
    final String helpResource = "/help/VelocityRenderer.txt";
    try
    {
      return new ClasspathInputResource(helpResource);
    }
    catch (final IOException e)
    {
      LOGGER.log(Level.WARNING,
                 String.format("Could not load help resource <%s>",
                               helpResource),
                 e);
      return new EmptyInputResource();
    }
  }

  @Override
  public Collection<String> getSupportedCommands()
  {
    return Arrays.asList(VelocityRenderer.COMMAND);
  }

  @Override
  public SchemaCrawlerCommand newSchemaCrawlerCommand(final String command)
  {
    final VelocityRenderer scCommand = new VelocityRenderer();
    return scCommand;
  }

  @Override
  public boolean supportsSchemaCrawlerCommand(final String command,
                                              final SchemaCrawlerOptions schemaCrawlerOptions,
                                              final OutputOptions outputOptions)
  {
    return VelocityRenderer.COMMAND.equals(command);
  }
}
