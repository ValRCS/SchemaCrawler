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


import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static schemacrawler.test.utility.TestUtility.clean;
import static schemacrawler.test.utility.TestUtility.validateDiagram;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import schemacrawler.schemacrawler.IncludeAll;
import schemacrawler.schemacrawler.RegularExpressionExclusionRule;
import schemacrawler.schemacrawler.RegularExpressionInclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.test.utility.BaseExecutableTest;
import schemacrawler.test.utility.TestName;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.integration.graph.GraphOptions;
import schemacrawler.tools.integration.graph.GraphOptionsBuilder;
import schemacrawler.tools.integration.graph.GraphOutputFormat;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;
import schemacrawler.tools.text.schema.SchemaTextDetailType;
import sf.util.IOUtility;

public class GraphRendererOptionsTest
  extends BaseExecutableTest
{

  private static final String GRAPH_OPTIONS_OUTPUT = "graph_options_output/";

  private static Path directory;

  @BeforeClass
  public static void removeOutputDir()
    throws Exception
  {
    clean(GRAPH_OPTIONS_OUTPUT);
  }

  @BeforeClass
  public static void setupDirectory()
    throws Exception
  {
    final Path codePath = Paths.get(GraphRendererOptionsTest.class
      .getProtectionDomain().getCodeSource().getLocation().toURI()).normalize()
      .toAbsolutePath();
    directory = codePath
      .resolve("../../../schemacrawler-docs/graphs/"
               + GraphRendererOptionsTest.class.getSimpleName())
      .normalize().toAbsolutePath();
    FileUtils.deleteDirectory(directory.toFile());
    createDirectories(directory);
  }

  @Rule
  public TestName testName = new TestName();

  @Test
  public void executableForGraph_00()
    throws Exception
  {
    final SchemaCrawlerOptions schemaCrawlerOptions = SchemaCrawlerOptionsBuilder
      .withMaximumSchemaInfoLevel();
    final GraphOptions graphOptions = new GraphOptions();

    executableGraph(SchemaTextDetailType.schema.name(),
                    schemaCrawlerOptions,
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_01()
    throws Exception
  {
    final GraphOptions graphOptions = new GraphOptions();
    graphOptions.setAlphabeticalSortForTableColumns(true);
    graphOptions.setShowOrdinalNumbers(true);

    executableGraph(SchemaTextDetailType.schema.name(),
                    SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions(),
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_02()
    throws Exception
  {
    final GraphOptions graphOptions = new GraphOptions();
    graphOptions.setHideForeignKeyNames(true);

    executableGraph(SchemaTextDetailType.schema.name(),
                    SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions(),
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_03()
    throws Exception
  {
    final GraphOptions graphOptions = new GraphOptions();
    graphOptions.setNoSchemaCrawlerInfo(true);
    graphOptions.setShowDatabaseInfo(false);
    graphOptions.setShowJdbcDriverInfo(false);

    executableGraph(SchemaTextDetailType.schema.name(),
                    SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions(),
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_04()
    throws Exception
  {
    final GraphOptions graphOptions = new GraphOptions();
    graphOptions.setShowUnqualifiedNames(true);

    executableGraph(SchemaTextDetailType.schema.name(),
                    SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions(),
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_05()
    throws Exception
  {
    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
      .includeTables(new RegularExpressionInclusionRule(".*BOOKS"));
    final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
      .toOptions();

    final GraphOptions graphOptions = new GraphOptions();

    executableGraph(SchemaTextDetailType.schema.name(),
                    schemaCrawlerOptions,
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_06()
    throws Exception
  {
    final SchemaCrawlerOptions schemaCrawlerOptions = SchemaCrawlerOptionsBuilder
      .withMaximumSchemaInfoLevel();
    final GraphOptions graphOptions = new GraphOptions();

    executableGraph(SchemaTextDetailType.brief.name(),
                    schemaCrawlerOptions,
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_07()
    throws Exception
  {
    final SchemaCrawlerOptions schemaCrawlerOptions = SchemaCrawlerOptionsBuilder
      .withMaximumSchemaInfoLevel();
    final GraphOptions graphOptions = new GraphOptions();

    executableGraph(SchemaTextDetailType.schema.name(),
                    schemaCrawlerOptions,
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_08()
    throws Exception
  {
    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
      .includeTables(new RegularExpressionInclusionRule(".*BOOKS"));
    final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
      .toOptions();

    final GraphOptions graphOptions = new GraphOptions();
    graphOptions.setShowUnqualifiedNames(true);
    graphOptions.setHideForeignKeyNames(true);

    executableGraph(SchemaTextDetailType.schema.name(),
                    schemaCrawlerOptions,
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_09()
    throws Exception
  {
    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
      .includeTables(new RegularExpressionInclusionRule(".*BOOKS"))
      .grepOnlyMatching(true);
    final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
      .toOptions();

    final GraphOptions graphOptions = new GraphOptions();
    graphOptions.setShowUnqualifiedNames(true);
    graphOptions.setHideForeignKeyNames(true);

    executableGraph(SchemaTextDetailType.schema.name(),
                    schemaCrawlerOptions,
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_10()
    throws Exception
  {
    final SchemaCrawlerOptions schemaCrawlerOptions = new SchemaCrawlerOptionsBuilder()
      .includeGreppedColumns(new RegularExpressionInclusionRule(".*\\.REGIONS\\..*"))
      .toOptions();

    final GraphOptions graphOptions = new GraphOptions();

    executableGraph(SchemaTextDetailType.schema.name(),
                    schemaCrawlerOptions,
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_11()
    throws Exception
  {
    final SchemaCrawlerOptions schemaCrawlerOptions = new SchemaCrawlerOptionsBuilder()
      .includeGreppedColumns(new RegularExpressionInclusionRule(".*\\.REGIONS\\..*"))
      .grepOnlyMatching(true).toOptions();

    final GraphOptions graphOptions = new GraphOptions();

    executableGraph(SchemaTextDetailType.schema.name(),
                    schemaCrawlerOptions,
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_12()
    throws Exception
  {
    final GraphOptions graphOptions = new GraphOptions();
    graphOptions.setShowRowCounts(true);

    final SchemaCrawlerOptions schemaCrawlerOptions = SchemaCrawlerOptionsBuilder
      .withMaximumSchemaInfoLevel();

    executableGraph(SchemaTextDetailType.schema.name(),
                    schemaCrawlerOptions,
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_13()
    throws Exception
  {
    final Map<String, String> graphvizAttributes = new HashMap<>();

    final String GRAPH = "graph.";
    graphvizAttributes.put(GRAPH + "splines", "ortho");

    final String NODE = "node.";
    graphvizAttributes.put(NODE + "shape", "none");

    final GraphOptions graphOptions = new GraphOptions();
    graphOptions.setGraphvizAttributes(graphvizAttributes);

    final SchemaCrawlerOptions schemaCrawlerOptions = SchemaCrawlerOptionsBuilder
      .withMaximumSchemaInfoLevel();

    executableGraph(SchemaTextDetailType.schema.name(),
                    schemaCrawlerOptions,
                    graphOptions,
                    testName.currentMethodName());
  }

  @Test
  public void executableForGraph_lintschema()
    throws Exception
  {
    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = new SchemaCrawlerOptionsBuilder()
      .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
      .includeSchemas(new RegularExpressionInclusionRule(".*\\.FOR_LINT"));
    final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
      .toOptions();

    final GraphOptions graphOptions = new GraphOptions();

    executableGraph(SchemaTextDetailType.schema.name(),
                    schemaCrawlerOptions,
                    graphOptions,
                    testName.currentMethodName());
  }

  private void executableGraph(final String command,
                               final SchemaCrawlerOptions options,
                               final GraphOptions graphOptions,
                               final String testMethodName)
    throws Exception
  {
    SchemaCrawlerOptions schemaCrawlerOptions = options;
    if (options.getSchemaInclusionRule().equals(new IncludeAll()))
    {
      schemaCrawlerOptions = new SchemaCrawlerOptionsBuilder(options)
        .includeSchemas(new RegularExpressionExclusionRule(".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
        .toOptions();
    }

    final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(command);
    executable.setSchemaCrawlerOptions(schemaCrawlerOptions);

    final GraphOptionsBuilder graphOptionsBuilder = new GraphOptionsBuilder(graphOptions);
    graphOptionsBuilder.sortTables(true);
    if (!graphOptions.isNoInfo())
    {
      graphOptionsBuilder.withInfo();
    }
    if (!"maximum".equals(options.getSchemaInfoLevel().getTag()))
    {
      graphOptionsBuilder.weakAssociations(true);
    }
    executable.setAdditionalConfiguration(graphOptionsBuilder.toConfig());

    // Generate diagram, so that we have something to look at, even if
    // the DOT file comparison fails
    final Path testDiagramFile = executeGraphExecutable(executable);
    copy(testDiagramFile,
         directory.resolve(testMethodName + ".png"),
         REPLACE_EXISTING);

    // Check DOT file
    final String referenceFileName = testMethodName;
    executeExecutable(executable,
                      GraphOutputFormat.scdot.getFormat(),
                      GRAPH_OPTIONS_OUTPUT + referenceFileName + ".dot");
  }

  private Path executeGraphExecutable(final SchemaCrawlerExecutable executable)
    throws Exception
  {
    final String outputFormatValue = GraphOutputFormat.png.getFormat();

    final Path testOutputFile = IOUtility.createTempFilePath("sc",
                                                             outputFormatValue);

    final OutputOptions outputOptions = OutputOptionsBuilder
      .newOutputOptions(outputFormatValue, testOutputFile);

    executable.setOutputOptions(outputOptions);
    executable.setConnection(getConnection());
    executable.execute();

    validateDiagram(testOutputFile);

    return testOutputFile;
  }

}
