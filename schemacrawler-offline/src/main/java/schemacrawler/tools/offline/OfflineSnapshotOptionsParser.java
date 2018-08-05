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

package schemacrawler.tools.offline;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.SchemaCrawlerCommandLineException;
import schemacrawler.tools.commandline.BaseOptionsParser;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;

/**
 * Parses the command-line.
 *
 * @author Sualeh Fatehi
 */
public final class OfflineSnapshotOptionsParser
  extends BaseOptionsParser<OutputOptions>
{

  private static final String DATABASE = "database";

  final OutputOptionsBuilder outputOptionsBuilder;

  public OfflineSnapshotOptionsParser(final Config config)
  {
    super(config);
    outputOptionsBuilder = OutputOptionsBuilder.builder().fromConfig(config);
  }

  @Override
  public OutputOptions getOptions()
  {
    final String inputSource = config.getStringValue(DATABASE, null);
    consumeOption(DATABASE);
    try
    {
      final Path databaseFile = Paths.get(inputSource);
      outputOptionsBuilder.withCompressedInputFile(databaseFile);
    }
    catch (final IOException e)
    {
      throw new SchemaCrawlerCommandLineException(String
        .format("Unable to read database snapshot from %s", inputSource), e);
    }

    return outputOptionsBuilder.toOptions();
  }

}
