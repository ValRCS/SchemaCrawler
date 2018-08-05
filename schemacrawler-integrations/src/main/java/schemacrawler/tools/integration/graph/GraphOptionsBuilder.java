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
package schemacrawler.tools.integration.graph;


import static sf.util.Utility.isBlank;
import static sf.util.Utility.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import schemacrawler.schemacrawler.Config;
import schemacrawler.tools.text.schema.BaseSchemaTextOptionsBuilder;
import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;

public final class GraphOptionsBuilder
  extends BaseSchemaTextOptionsBuilder<GraphOptionsBuilder, GraphOptions>
{

  private static final String GRAPH_SHOW_PRIMARY_KEY_CARDINALITY = "schemacrawler.graph.show.primarykey.cardinality";
  private static final String GRAPH_SHOW_FOREIGN_KEY_CARDINALITY = "schemacrawler.graph.show.foreignkey.cardinality";
  private static final String GRAPH_GRAPHVIZ_OPTS = "schemacrawler.graph.graphviz_opts";
  private static final String SC_GRAPHVIZ_OPTS = "SC_GRAPHVIZ_OPTS";
  private static final String GRAPH_GRAPHVIZ_ATTRIBUTES = "schemacrawler.graph.graphviz";

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(GraphOptions.class.getName());

  public static GraphOptionsBuilder builder()
  {
    return new GraphOptionsBuilder();
  }

  public static GraphOptionsBuilder builder(final GraphOptions options)
  {
    return new GraphOptionsBuilder().fromOptions(options);
  }

  public static GraphOptions newGraphOptions()
  {
    return new GraphOptionsBuilder().toOptions();
  }

  public static GraphOptions newGraphOptions(final Config config)
  {
    return new GraphOptionsBuilder().fromConfig(config).toOptions();
  }

  private static Map<String, String> makeDefaultGraphvizAttributes()
  {
    final Map<String, String> graphvizAttributes = new HashMap<>();

    final String GRAPH = "graph.";
    graphvizAttributes.put(GRAPH + "rankdir", "RL");
    graphvizAttributes.put(GRAPH + "labeljust", "r");
    graphvizAttributes.put(GRAPH + "fontname", "Helvetica");

    final String NODE = "node.";
    graphvizAttributes.put(NODE + "shape", "none");
    graphvizAttributes.put(NODE + "fontname", "Helvetica");

    final String EDGE = "edge.";
    graphvizAttributes.put(EDGE + "fontname", "Helvetica");

    return graphvizAttributes;
  }

  protected List<String> graphvizOpts;
  protected Map<String, String> graphvizAttributes;
  protected boolean isShowForeignKeyCardinality;
  protected boolean isShowPrimaryKeyCardinality;

  private GraphOptionsBuilder()
  {
    // Default values
    graphvizOpts = new ArrayList<>();
    graphvizAttributes = makeDefaultGraphvizAttributes();
    isShowForeignKeyCardinality = true;
    isShowPrimaryKeyCardinality = true;
  }

  @Override
  public GraphOptionsBuilder fromConfig(final Config config)
  {
    if (config == null)
    {
      return this;
    }
    super.fromConfig(config);

    isShowPrimaryKeyCardinality = config
      .getBooleanValue(GRAPH_SHOW_PRIMARY_KEY_CARDINALITY, true);
    isShowForeignKeyCardinality = config
      .getBooleanValue(GRAPH_SHOW_FOREIGN_KEY_CARDINALITY, true);

    graphvizOpts = listGraphvizOpts(readGraphvizOpts(config));

    final Map<String, String> graphvizAttributes = readGraphvizAttributes(config);
    if (graphvizAttributes != null)
    {
      this.graphvizAttributes = graphvizAttributes;
    }

    return this;
  }

  @Override
  public GraphOptionsBuilder fromOptions(final GraphOptions options)
  {
    if (options == null)
    {
      return this;
    }
    super.fromOptions(options);

    isShowPrimaryKeyCardinality = options.isShowPrimaryKeyCardinality();
    isShowForeignKeyCardinality = options.isShowForeignKeyCardinality();

    graphvizOpts = options.getGraphvizOpts();
    graphvizAttributes = options.getGraphvizAttributes();

    return this;
  }

  public GraphOptionsBuilder showForeignKeyCardinality()
  {
    return showForeignKeyCardinality(true);
  }

  public GraphOptionsBuilder showForeignKeyCardinality(final boolean value)
  {
    isShowForeignKeyCardinality = value;
    return this;
  }

  public GraphOptionsBuilder showPrimaryKeyCardinality()
  {
    return showPrimaryKeyCardinality(true);
  }

  public GraphOptionsBuilder showPrimaryKeyCardinality(final boolean value)
  {
    isShowPrimaryKeyCardinality = value;
    return this;
  }

  @Override
  public Config toConfig()
  {
    final Config config = super.toConfig();

    config.setBooleanValue(GRAPH_SHOW_PRIMARY_KEY_CARDINALITY,
                           isShowPrimaryKeyCardinality);
    config.setBooleanValue(GRAPH_SHOW_FOREIGN_KEY_CARDINALITY,
                           isShowForeignKeyCardinality);

    config.setStringValue(GRAPH_GRAPHVIZ_OPTS, join(graphvizOpts, " "));

    graphvizAttributesToConfig(graphvizAttributes, config);

    return config;
  }

  @Override
  public GraphOptions toOptions()
  {
    return new GraphOptions(this);
  }

  public GraphOptionsBuilder withGraphvizAttributes(final Map<String, String> graphvizAttributes)
  {
    if (graphvizAttributes == null)
    {
      this.graphvizAttributes = makeDefaultGraphvizAttributes();
    }
    else
    {
      this.graphvizAttributes = graphvizAttributes;
    }
    return this;
  }

  public GraphOptionsBuilder withGraphvizOpts(final List<String> graphvizOpts)
  {
    if (graphvizOpts == null)
    {
      this.graphvizOpts = new ArrayList<>();
    }
    else
    {
      this.graphvizOpts = graphvizOpts;
    }
    return this;
  }

  private void graphvizAttributesToConfig(final Map<String, String> graphvizAttributes,
                                          final Config config)
  {
    for (final Entry<String, String> graphvizAttribute: graphvizAttributes
      .entrySet())
    {
      final String fullKey = String
        .format("%s.%s", GRAPH_GRAPHVIZ_ATTRIBUTES, graphvizAttribute.getKey());
      final String value = graphvizAttribute.getValue();
      config.put(fullKey, value);
    }
  }

  private List<String> listGraphvizOpts(final String graphVizOptions)
  {
    final List<String> graphVizOptionsList = Arrays
      .asList(graphVizOptions.split("\\s+"));
    return graphVizOptionsList;
  }

  private Map<String, String> readGraphvizAttributes(final Config config)
  {
    if (config == null)
    {
      return null;
    }

    final Map<String, String> graphvizAttributes = new HashMap<>();
    for (final Entry<String, String> configEntry: config.entrySet())
    {
      final String fullKey = configEntry.getKey();
      if (fullKey == null || !fullKey.startsWith(GRAPH_GRAPHVIZ_ATTRIBUTES))
      {
        continue;
      }

      final String key = fullKey
        .substring(GRAPH_GRAPHVIZ_ATTRIBUTES.length() + 1);
      final String value = configEntry.getValue();
      graphvizAttributes.put(key, value);
    }

    if (graphvizAttributes.isEmpty())
    {
      return null;
    }

    return graphvizAttributes;
  }

  private String readGraphvizOpts(final Config config)
  {
    final String scGraphvizOptsCfg = config.getStringValue(GRAPH_GRAPHVIZ_OPTS,
                                                           "");
    if (!isBlank(scGraphvizOptsCfg))
    {
      LOGGER
        .log(Level.CONFIG,
             new StringFormat("Using additional Graphviz command-line options from config <%s>",
                              scGraphvizOptsCfg));
      return scGraphvizOptsCfg;
    }

    final String scGraphvizOptsProp = System.getProperty(SC_GRAPHVIZ_OPTS);
    if (!isBlank(scGraphvizOptsProp))
    {
      LOGGER
        .log(Level.CONFIG,
             new StringFormat("Using additional Graphviz command-line options from SC_GRAPHVIZ_OPTS system property <%s>",
                              scGraphvizOptsProp));
      return scGraphvizOptsProp;
    }

    final String scGraphvizOptsEnv = System.getenv(SC_GRAPHVIZ_OPTS);
    if (!isBlank(scGraphvizOptsEnv))
    {
      LOGGER
        .log(Level.CONFIG,
             new StringFormat("Using additional Graphviz command-line options from SC_GRAPHVIZ_OPTS environmental variable <%s>",
                              scGraphvizOptsEnv));
      return scGraphvizOptsEnv;
    }

    return "";
  }

}
