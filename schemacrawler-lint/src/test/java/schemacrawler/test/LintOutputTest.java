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

package schemacrawler.test;


import static org.junit.Assert.fail;
import static schemacrawler.test.utility.TestUtility.clean;
import static schemacrawler.test.utility.TestUtility.compareOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;

import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.RegularExpressionInclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.test.utility.BaseDatabaseTest;
import schemacrawler.test.utility.TestWriter;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.options.InfoLevel;
import schemacrawler.tools.options.OutputFormat;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;
import schemacrawler.tools.options.TextOutputFormat;
import schemacrawler.tools.text.operation.Operation;
import schemacrawler.tools.text.schema.SchemaTextDetailType;
import schemacrawler.tools.text.schema.SchemaTextOptionsBuilder;
import sf.util.IOUtility;

public class LintOutputTest
  extends BaseDatabaseTest
{

  private static final String TEXT_OUTPUT = "lint_text_output/";
  private static final String COMPOSITE_OUTPUT = "lint_composite_output/";
  private static final String JSON_OUTPUT = "lint_json_output/";

  @Test
  public void compareCompositeOutput()
    throws Exception
  {
    clean(COMPOSITE_OUTPUT);

    final String queryCommand1 = "dump_top5";
    final Config queriesConfig = new Config();
    queriesConfig
      .put(queryCommand1,
           "SELECT TOP 5 ${orderbycolumns} FROM ${table} ORDER BY ${orderbycolumns}");

    final String[] commands = new String[] {
                                             SchemaTextDetailType.brief
                                             + "," + Operation.count + ","
                                             + "lint",
                                             queryCommand1 + ","
                                                       + SchemaTextDetailType.brief
                                                       + "," + "lint", };

    final List<String> failures = new ArrayList<>();
    for (final OutputFormat outputFormat: EnumSet
      .complementOf(EnumSet.of(TextOutputFormat.tsv)))
    {
      for (final String command: commands)
      {
        final String referenceFile = command + "." + outputFormat.getFormat();

        final Path testOutputFile = IOUtility
          .createTempFilePath(referenceFile, outputFormat.getFormat());

        final OutputOptions outputOptions = OutputOptionsBuilder
          .newOutputOptions(outputFormat, testOutputFile);

        final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
          .includeSchemas(new RegularExpressionInclusionRule(".*FOR_LINT"))
          .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
        final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
          .toOptions();

        final SchemaTextOptionsBuilder schemaTextOptionsBuilder = new SchemaTextOptionsBuilder();
        schemaTextOptionsBuilder.withInfo();
        queriesConfig.putAll(schemaTextOptionsBuilder.toConfig());

        final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(command);
        executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
        executable.setOutputOptions(outputOptions);
        executable.setAdditionalConfiguration(queriesConfig);
        executable.setConnection(getConnection());
        executable.execute();

        failures.addAll(compareOutput(COMPOSITE_OUTPUT + referenceFile,
                                      testOutputFile,
                                      outputFormat.getFormat()));
      }
    }
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

  @Test
  public void compareJsonOutput()
    throws Exception
  {
    clean(JSON_OUTPUT);

    final InfoLevel infoLevel = InfoLevel.standard;
    try (final TestWriter out = new TestWriter(TextOutputFormat.json
      .getFormat());)
    {
      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(TextOutputFormat.json, out);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .includeSchemas(new RegularExpressionInclusionRule(".*FOR_LINT"))
        .withSchemaInfoLevel(infoLevel.buildSchemaInfoLevel());
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable("lint");
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable.setConnection(getConnection());
      executable.execute();

      out.assertEquals(JSON_OUTPUT + "lints.json");
    }
  }

  @Test
  public void compareTextOutput()
    throws Exception
  {
    clean(TEXT_OUTPUT);

    final InfoLevel infoLevel = InfoLevel.standard;
    try (final TestWriter out = new TestWriter(TextOutputFormat.text
      .getFormat());)
    {
      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(TextOutputFormat.text, out);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .includeSchemas(new RegularExpressionInclusionRule(".*FOR_LINT"))
        .withSchemaInfoLevel(infoLevel.buildSchemaInfoLevel());
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable("lint");
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable.setConnection(getConnection());
      executable.execute();

      out.assertEquals(TEXT_OUTPUT + "lint.txt");
    }
  }

}
