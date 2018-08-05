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


import static org.junit.Assert.fail;
import static schemacrawler.test.utility.TestUtility.clean;
import static schemacrawler.test.utility.TestUtility.compareOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.ExcludeAll;
import schemacrawler.schemacrawler.RegularExpressionExclusionRule;
import schemacrawler.schemacrawler.RegularExpressionInclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.test.utility.BaseDatabaseTest;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.integration.graph.GraphOutputFormat;
import schemacrawler.tools.options.InfoLevel;
import schemacrawler.tools.options.OutputFormat;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;
import schemacrawler.tools.options.TextOutputFormat;
import schemacrawler.tools.text.operation.Operation;
import schemacrawler.tools.text.schema.SchemaTextDetailType;
import schemacrawler.tools.text.schema.SchemaTextOptions;
import schemacrawler.tools.text.schema.SchemaTextOptionsBuilder;
import schemacrawler.utility.IdentifierQuotingStrategy;
import sf.util.IOUtility;

public class SchemaCrawlerOutputTest
  extends BaseDatabaseTest
{

  private static final String COMPOSITE_OUTPUT = "composite_output/";
  private static final String ORDINAL_OUTPUT = "ordinal_output/";
  private static final String TABLE_ROW_COUNT_OUTPUT = "table_row_count_output/";
  private static final String SHOW_WEAK_ASSOCIATIONS_OUTPUT = "show_weak_associations_output/";
  private static final String JSON_OUTPUT = "json_output/";
  private static final String HIDE_CONSTRAINT_NAMES_OUTPUT = "hide_constraint_names_output/";
  private static final String UNQUALIFIED_NAMES_OUTPUT = "unqualified_names_output/";
  private static final String ROUTINES_OUTPUT = "routines_output/";
  private static final String NO_REMARKS_OUTPUT = "no_remarks_output/";
  private static final String NO_SCHEMA_COLORS_OUTPUT = "no_schema_colors_output/";
  private static final String IDENTIFIER_QUOTING_OUTPUT = "identifier_quoting_output/";

  @Test
  public void compareCompositeOutput()
    throws Exception
  {
    clean(COMPOSITE_OUTPUT);

    final String queryCommand1 = "all_tables";
    final Config queriesConfig = new Config();
    queriesConfig
      .put(queryCommand1,
           "SELECT * FROM INFORMATION_SCHEMA.SYSTEM_TABLES ORDER BY TABLE_SCHEM, TABLE_NAME");
    final String queryCommand2 = "dump_tables";
    queriesConfig
      .put(queryCommand2,
           "SELECT ${orderbycolumns} FROM ${table} ORDER BY ${orderbycolumns}");

    final String[] commands = new String[] {
                                             SchemaTextDetailType.details + ","
                                             + Operation.count + ","
                                             + Operation.dump,
                                             SchemaTextDetailType.brief + ","
                                                               + Operation.count,
                                             queryCommand1 + "," + queryCommand2 + ","
                                                                                  + Operation.count
                                                                                  + ","
                                                                                  + SchemaTextDetailType.brief, };

    final SchemaTextOptionsBuilder textOptionsBuilder = new SchemaTextOptionsBuilder();
    textOptionsBuilder.noSchemaCrawlerInfo(false).showDatabaseInfo()
      .showJdbcDriverInfo();
    final SchemaTextOptions textOptions = (SchemaTextOptions) textOptionsBuilder
      .toOptions();

    final List<String> failures = new ArrayList<>();
    for (final OutputFormat outputFormat: getOutputFormats())
    {
      for (final String command: commands)
      {
        final String referenceFile = command + "." + outputFormat.getFormat();

        final Path testOutputFile = IOUtility
          .createTempFilePath(referenceFile, outputFormat.getFormat());

        final OutputOptions outputOptions = OutputOptionsBuilder
          .newOutputOptions(outputFormat, testOutputFile);

        final Config config = loadHsqldbConfig();

        final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder = new SchemaRetrievalOptionsBuilder()
          .fromConfig(config);

        final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
          .includeSchemas(new RegularExpressionExclusionRule(".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
          .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
          .includeAllSequences().includeAllRoutines();
        final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
          .toOptions();

        queriesConfig
          .putAll(new SchemaTextOptionsBuilder(textOptions).toConfig());

        final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(command);
        executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
        executable.setOutputOptions(outputOptions);
        executable.setAdditionalConfiguration(queriesConfig);
        executable.setConnection(getConnection());
        executable
          .setSchemaRetrievalOptions(schemaRetrievalOptionsBuilder.toOptions());
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
  public void compareHideConstraintNamesOutput()
    throws Exception
  {
    clean(HIDE_CONSTRAINT_NAMES_OUTPUT);

    final List<String> failures = new ArrayList<>();

    final SchemaTextOptionsBuilder textOptionsBuilder = new SchemaTextOptionsBuilder();
    textOptionsBuilder.noHeader(false).noFooter(false)
      .noSchemaCrawlerInfo(false).showDatabaseInfo(true)
      .showJdbcDriverInfo(true).noPrimaryKeyNames().noForeignKeyNames()
      .noIndexNames().noConstraintNames();
    textOptionsBuilder.noConstraintNames();
    final SchemaTextOptions textOptions = (SchemaTextOptions) textOptionsBuilder
      .toOptions();

    for (final OutputFormat outputFormat: getOutputFormats())
    {
      final String referenceFile = "details_maximum."
                                   + outputFormat.getFormat();

      final Path testOutputFile = IOUtility
        .createTempFilePath(referenceFile, outputFormat.getFormat());

      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(outputFormat, testOutputFile);

      final Config config = loadHsqldbConfig();

      final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder = new SchemaRetrievalOptionsBuilder()
        .fromConfig(config);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
        .includeSchemas(new RegularExpressionExclusionRule(".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
        .includeAllSequences().includeAllRoutines();
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaTextOptionsBuilder schemaTextOptionsBuilder = new SchemaTextOptionsBuilder(textOptions);
      schemaTextOptionsBuilder.sortTables(true);

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(SchemaTextDetailType.details
                                                                             + ","
                                                                             + Operation.count
                                                                             + ","
                                                                             + Operation.dump);
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable
        .setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());
      executable.setConnection(getConnection());
      executable
        .setSchemaRetrievalOptions(schemaRetrievalOptionsBuilder.toOptions());
      executable.execute();

      failures
        .addAll(compareOutput(HIDE_CONSTRAINT_NAMES_OUTPUT + referenceFile,
                              testOutputFile,
                              outputFormat.getFormat()));
    }
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

  @Test
  public void compareIdentifierQuotingOutput()
    throws Exception
  {
    clean(IDENTIFIER_QUOTING_OUTPUT);

    final List<String> failures = new ArrayList<>();

    final SchemaTextOptionsBuilder textOptionsBuilder = new SchemaTextOptionsBuilder();
    textOptionsBuilder.noRemarks().noSchemaCrawlerInfo().showDatabaseInfo(false)
      .showJdbcDriverInfo(false);

    for (final IdentifierQuotingStrategy identifierQuotingStrategy: IdentifierQuotingStrategy
      .values())
    {
      final OutputFormat outputFormat = TextOutputFormat.text;
      textOptionsBuilder
        .withIdentifierQuotingStrategy(identifierQuotingStrategy);
      final SchemaTextOptions textOptions = (SchemaTextOptions) textOptionsBuilder
        .toOptions();

      final String referenceFile = "schema_" + identifierQuotingStrategy.name()
                                   + "." + outputFormat.getFormat();

      final Path testOutputFile = IOUtility
        .createTempFilePath(referenceFile, outputFormat.getFormat());

      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(outputFormat, testOutputFile);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .withSchemaInfoLevel(SchemaInfoLevelBuilder.standard())
        .includeSchemas(new RegularExpressionInclusionRule(".*\\.BOOKS"))
        .includeAllRoutines();
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(SchemaTextDetailType.schema
        .name());
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable
        .setAdditionalConfiguration(new SchemaTextOptionsBuilder(textOptions)
          .toConfig());
      executable.setConnection(getConnection());
      executable.execute();

      failures.addAll(compareOutput(IDENTIFIER_QUOTING_OUTPUT + referenceFile,
                                    testOutputFile,
                                    outputFormat.getFormat()));
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

    final SchemaTextOptionsBuilder textOptionsBuilder = new SchemaTextOptionsBuilder();
    textOptionsBuilder.noSchemaCrawlerInfo(false).showDatabaseInfo()
      .showJdbcDriverInfo();
    final SchemaTextOptions textOptions = (SchemaTextOptions) textOptionsBuilder
      .toOptions();

    final List<String> failures = new ArrayList<>();
    final InfoLevel infoLevel = InfoLevel.maximum;
    for (final SchemaTextDetailType schemaTextDetailType: SchemaTextDetailType
      .values())
    {
      final String referenceFile = schemaTextDetailType + "_" + infoLevel
                                   + ".json";

      final Path testOutputFile = IOUtility
        .createTempFilePath(referenceFile, TextOutputFormat.json.getFormat());

      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(TextOutputFormat.json, testOutputFile);

      final Config config = loadHsqldbConfig();

      final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder = new SchemaRetrievalOptionsBuilder()
        .fromConfig(config);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .withSchemaInfoLevel(infoLevel.buildSchemaInfoLevel())
        .includeSchemas(new RegularExpressionExclusionRule(".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
        .includeAllSequences().includeAllRoutines();
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaTextOptionsBuilder schemaTextOptionsBuilder = new SchemaTextOptionsBuilder(textOptions);

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(schemaTextDetailType
        .name());
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable
        .setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());
      executable.setConnection(getConnection());
      executable
        .setSchemaRetrievalOptions(schemaRetrievalOptionsBuilder.toOptions());
      executable.execute();

      failures.addAll(compareOutput(JSON_OUTPUT + referenceFile,
                                    testOutputFile,
                                    outputOptions.getOutputFormatValue()));
    }
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

  @Test
  public void compareNoRemarksOutput()
    throws Exception
  {
    clean(NO_REMARKS_OUTPUT);

    final List<String> failures = new ArrayList<>();

    final SchemaTextOptionsBuilder textOptionsBuilder = new SchemaTextOptionsBuilder();
    textOptionsBuilder.noRemarks().noSchemaCrawlerInfo().showDatabaseInfo(false)
      .showJdbcDriverInfo(false);
    final SchemaTextOptions textOptions = (SchemaTextOptions) textOptionsBuilder
      .toOptions();

    for (final OutputFormat outputFormat: getOutputFormats())
    {
      final String referenceFile = "schema_detailed."
                                   + outputFormat.getFormat();

      final Path testOutputFile = IOUtility
        .createTempFilePath(referenceFile, outputFormat.getFormat());

      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(outputFormat, testOutputFile);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .withSchemaInfoLevel(SchemaInfoLevelBuilder.detailed())
        .includeSchemas(new RegularExpressionInclusionRule(".*\\.BOOKS"))
        .includeAllRoutines();
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(SchemaTextDetailType.schema
        .name());
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable
        .setAdditionalConfiguration(new SchemaTextOptionsBuilder(textOptions)
          .toConfig());
      executable.setConnection(getConnection());
      executable.execute();

      failures.addAll(compareOutput(NO_REMARKS_OUTPUT + referenceFile,
                                    testOutputFile,
                                    outputFormat.getFormat()));
    }
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

  @Test
  public void compareNoSchemaColorsOutput()
    throws Exception
  {
    clean(NO_SCHEMA_COLORS_OUTPUT);

    final List<String> failures = new ArrayList<>();

    final SchemaTextOptionsBuilder textOptionsBuilder = new SchemaTextOptionsBuilder();
    textOptionsBuilder.noRemarks().noSchemaCrawlerInfo().showDatabaseInfo(false)
      .showJdbcDriverInfo(false).noSchemaColors();
    final SchemaTextOptions textOptions = (SchemaTextOptions) textOptionsBuilder
      .toOptions();

    for (final OutputFormat outputFormat: getOutputFormats())
    {
      final String referenceFile = "schema_detailed."
                                   + outputFormat.getFormat();

      final Path testOutputFile = IOUtility
        .createTempFilePath(referenceFile, outputFormat.getFormat());

      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(outputFormat, testOutputFile);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .withSchemaInfoLevel(SchemaInfoLevelBuilder.standard())
        .includeSchemas(new RegularExpressionInclusionRule(".*\\.BOOKS"))
        .includeAllRoutines();
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(SchemaTextDetailType.schema
        .name());
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable
        .setAdditionalConfiguration(new SchemaTextOptionsBuilder(textOptions)
          .toConfig());
      executable.setConnection(getConnection());
      executable.execute();

      failures.addAll(compareOutput(NO_SCHEMA_COLORS_OUTPUT + referenceFile,
                                    testOutputFile,
                                    outputFormat.getFormat()));
    }
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

  @Test
  public void compareOrdinalOutput()
    throws Exception
  {
    clean(ORDINAL_OUTPUT);

    final List<String> failures = new ArrayList<>();

    final SchemaTextOptionsBuilder textOptionsBuilder = new SchemaTextOptionsBuilder();
    textOptionsBuilder.noSchemaCrawlerInfo(false).showDatabaseInfo()
      .showJdbcDriverInfo();
    textOptionsBuilder.showOrdinalNumbers();
    final SchemaTextOptions textOptions = (SchemaTextOptions) textOptionsBuilder
      .toOptions();

    for (final OutputFormat outputFormat: getOutputFormats())
    {
      final String referenceFile = "details_maximum."
                                   + outputFormat.getFormat();

      final Path testOutputFile = IOUtility
        .createTempFilePath(referenceFile, outputFormat.getFormat());

      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(outputFormat, testOutputFile);

      final Config config = loadHsqldbConfig();

      final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder = new SchemaRetrievalOptionsBuilder()
        .fromConfig(config);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
        .includeSchemas(new RegularExpressionExclusionRule(".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
        .includeAllSequences().includeAllRoutines();
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaTextOptionsBuilder schemaTextOptionsBuilder = new SchemaTextOptionsBuilder(textOptions);
      schemaTextOptionsBuilder.sortTables(true);

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(SchemaTextDetailType.details
                                                                             + ","
                                                                             + Operation.count
                                                                             + ","
                                                                             + Operation.dump);
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable
        .setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());
      executable.setConnection(getConnection());
      executable
        .setSchemaRetrievalOptions(schemaRetrievalOptionsBuilder.toOptions());
      executable.execute();

      failures.addAll(compareOutput(ORDINAL_OUTPUT + referenceFile,
                                    testOutputFile,
                                    outputFormat.getFormat()));
    }
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

  @Test
  public void compareRoutinesOutput()
    throws Exception
  {
    clean(ROUTINES_OUTPUT);

    final List<String> failures = new ArrayList<>();

    final SchemaTextOptionsBuilder textOptionsBuilder = new SchemaTextOptionsBuilder();
    textOptionsBuilder.noSchemaCrawlerInfo(false).showDatabaseInfo()
      .showJdbcDriverInfo().showUnqualifiedNames();
    final SchemaTextOptions textOptions = (SchemaTextOptions) textOptionsBuilder
      .toOptions();

    for (final OutputFormat outputFormat: getOutputFormats())
    {
      final String referenceFile = "routines." + outputFormat.getFormat();

      final Path testOutputFile = IOUtility
        .createTempFilePath(referenceFile, outputFormat.getFormat());

      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(outputFormat, testOutputFile);

      final Config config = loadHsqldbConfig();

      final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder = new SchemaRetrievalOptionsBuilder()
        .fromConfig(config);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .includeSchemas(new RegularExpressionExclusionRule(".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
        .includeTables(new ExcludeAll()).includeAllRoutines()
        .includeSequences(new ExcludeAll()).includeSynonyms(new ExcludeAll())
        .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaTextOptionsBuilder schemaTextOptionsBuilder = new SchemaTextOptionsBuilder(textOptions);
      schemaTextOptionsBuilder.sortTables(true);

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(SchemaTextDetailType.details
        .name());
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable
        .setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());
      executable.setConnection(getConnection());
      executable
        .setSchemaRetrievalOptions(schemaRetrievalOptionsBuilder.toOptions());
      executable.execute();

      failures.addAll(compareOutput(ROUTINES_OUTPUT + referenceFile,
                                    testOutputFile,
                                    outputFormat.getFormat()));
    }
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

  @Test
  public void compareShowWeakAssociationsOutput()
    throws Exception
  {
    clean(SHOW_WEAK_ASSOCIATIONS_OUTPUT);

    final List<String> failures = new ArrayList<>();

    final SchemaTextOptionsBuilder textOptionsBuilder = new SchemaTextOptionsBuilder();
    textOptionsBuilder.noSchemaCrawlerInfo(false).showDatabaseInfo()
      .showJdbcDriverInfo();
    textOptionsBuilder.weakAssociations();
    final SchemaTextOptions textOptions = (SchemaTextOptions) textOptionsBuilder
      .toOptions();

    for (final OutputFormat outputFormat: getOutputFormats())
    {
      final String referenceFile = "schema_standard."
                                   + outputFormat.getFormat();

      final Path testOutputFile = IOUtility
        .createTempFilePath(referenceFile, outputFormat.getFormat());

      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(outputFormat, testOutputFile);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .withSchemaInfoLevel(SchemaInfoLevelBuilder.standard())
        .includeSchemas(new RegularExpressionExclusionRule(".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
        .includeAllRoutines();
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaTextOptionsBuilder schemaTextOptionsBuilder = new SchemaTextOptionsBuilder(textOptions);
      schemaTextOptionsBuilder.sortTables(true);

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(SchemaTextDetailType.schema
        .name());
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable
        .setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());
      executable.setConnection(getConnection());
      executable.execute();

      failures
        .addAll(compareOutput(SHOW_WEAK_ASSOCIATIONS_OUTPUT + referenceFile,
                              testOutputFile,
                              outputFormat.getFormat()));
    }
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

  @Test
  public void compareTableRowCountOutput()
    throws Exception
  {
    clean(TABLE_ROW_COUNT_OUTPUT);

    final List<String> failures = new ArrayList<>();

    final SchemaTextOptionsBuilder textOptionsBuilder = new SchemaTextOptionsBuilder();
    textOptionsBuilder.noSchemaCrawlerInfo(false).showDatabaseInfo()
      .showJdbcDriverInfo();
    textOptionsBuilder.showRowCounts();
    final SchemaTextOptions textOptions = (SchemaTextOptions) textOptionsBuilder
      .toOptions();

    for (final OutputFormat outputFormat: getOutputFormats())
    {
      final String referenceFile = "details_maximum."
                                   + outputFormat.getFormat();

      final Path testOutputFile = IOUtility
        .createTempFilePath(referenceFile, outputFormat.getFormat());

      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(outputFormat, testOutputFile);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
        .includeSchemas(new RegularExpressionExclusionRule(".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
        .includeAllRoutines();
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaTextOptionsBuilder schemaTextOptionsBuilder = new SchemaTextOptionsBuilder(textOptions);
      schemaTextOptionsBuilder.sortTables(true);

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(SchemaTextDetailType.details
        .name());
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable
        .setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());
      executable.setConnection(getConnection());
      executable.execute();

      failures.addAll(compareOutput(TABLE_ROW_COUNT_OUTPUT + referenceFile,
                                    testOutputFile,
                                    outputFormat.getFormat()));
    }
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

  @Test
  public void compareUnqualifiedNamesOutput()
    throws Exception
  {
    clean(UNQUALIFIED_NAMES_OUTPUT);

    final List<String> failures = new ArrayList<>();

    final SchemaTextOptionsBuilder textOptionsBuilder = new SchemaTextOptionsBuilder();
    textOptionsBuilder.noSchemaCrawlerInfo(false).showDatabaseInfo()
      .showJdbcDriverInfo().showUnqualifiedNames();
    final SchemaTextOptions textOptions = (SchemaTextOptions) textOptionsBuilder
      .toOptions();

    for (final OutputFormat outputFormat: getOutputFormats())
    {
      final String referenceFile = "details_maximum."
                                   + outputFormat.getFormat();

      final Path testOutputFile = IOUtility
        .createTempFilePath(referenceFile, outputFormat.getFormat());

      final OutputOptions outputOptions = OutputOptionsBuilder
        .newOutputOptions(outputFormat, testOutputFile);

      final Config config = loadHsqldbConfig();

      final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder = new SchemaRetrievalOptionsBuilder()
        .fromConfig(config);

      final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
        .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
        .includeSchemas(new RegularExpressionExclusionRule(".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
        .includeAllSequences().includeAllRoutines();
      final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
        .toOptions();

      final SchemaTextOptionsBuilder schemaTextOptionsBuilder = new SchemaTextOptionsBuilder(textOptions);
      schemaTextOptionsBuilder.sortTables(true);

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(SchemaTextDetailType.details
                                                                             + ","
                                                                             + Operation.count
                                                                             + ","
                                                                             + Operation.dump);
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable
        .setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());
      executable.setConnection(getConnection());
      executable
        .setSchemaRetrievalOptions(schemaRetrievalOptionsBuilder.toOptions());
      executable.execute();

      failures.addAll(compareOutput(UNQUALIFIED_NAMES_OUTPUT + referenceFile,
                                    testOutputFile,
                                    outputFormat.getFormat()));
    }
    if (failures.size() > 0)
    {
      fail(failures.toString());
    }
  }

  private Set<OutputFormat> getOutputFormats()
  {
    final Set<OutputFormat> outputFormats = new HashSet<>();
    outputFormats
      .addAll(EnumSet.complementOf(EnumSet.of(TextOutputFormat.tsv)));
    outputFormats.add(GraphOutputFormat.scdot);

    return outputFormats;
  }

}
