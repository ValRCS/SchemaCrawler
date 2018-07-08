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

package schemacrawler.tools.text.schema;


import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.analysis.associations.CatalogWithAssociations;
import schemacrawler.tools.analysis.counts.CatalogWithCounts;
import schemacrawler.tools.executable.BaseSchemaCrawlerCommand;
import schemacrawler.tools.options.TextOutputFormat;
import schemacrawler.tools.traversal.SchemaTraversalHandler;
import schemacrawler.tools.traversal.SchemaTraverser;
import schemacrawler.utility.NamedObjectSort;

/**
 * Basic SchemaCrawler executor.
 *
 * @author Sualeh Fatehi
 */
public final class SchemaTextRenderer
  extends BaseSchemaCrawlerCommand
{

  private SchemaTextOptions schemaTextOptions;

  public SchemaTextRenderer(final String command)
  {
    super(command);
  }

  @Override
  public void execute()
    throws Exception
  {
    // Null checks are done before execution

    loadSchemaTextOptions();

    // Determine what decorators to apply to the database
    Catalog aCatalog = catalog;
    if (schemaTextOptions.isShowWeakAssociations())
    {
      aCatalog = new CatalogWithAssociations(aCatalog);
    }
    if (schemaTextOptions.isShowRowCounts()
        || schemaCrawlerOptions.isHideEmptyTables())
    {
      aCatalog = new CatalogWithCounts(aCatalog,
                                       connection,
                                       schemaCrawlerOptions);
    }

    final SchemaTraversalHandler formatter = getSchemaTraversalHandler();

    final SchemaTraverser traverser = new SchemaTraverser();
    traverser.setCatalog(aCatalog);
    traverser.setHandler(formatter);
    traverser.setTablesComparator(NamedObjectSort
      .getNamedObjectSort(getSchemaTextOptions()
        .isAlphabeticalSortForTables()));
    traverser.setRoutinesComparator(NamedObjectSort
      .getNamedObjectSort(getSchemaTextOptions()
        .isAlphabeticalSortForRoutines()));

    traverser.traverse();

  }

  public final SchemaTextOptions getSchemaTextOptions()
  {
    loadSchemaTextOptions();
    return schemaTextOptions;
  }

  public final void setSchemaTextOptions(final SchemaTextOptions schemaTextOptions)
  {
    this.schemaTextOptions = schemaTextOptions;
  }

  private SchemaTextDetailType getSchemaTextDetailType()
  {
    SchemaTextDetailType schemaTextDetailType;
    try
    {
      schemaTextDetailType = SchemaTextDetailType.valueOf(command);
    }
    catch (final IllegalArgumentException e)
    {
      schemaTextDetailType = SchemaTextDetailType.schema;
    }
    return schemaTextDetailType;
  }

  private SchemaTraversalHandler getSchemaTraversalHandler()
    throws SchemaCrawlerException
  {
    final SchemaTextDetailType schemaTextDetailType = getSchemaTextDetailType();
    final SchemaTraversalHandler formatter;

    final String identifierQuoteString = identifiers.getIdentifierQuoteString();
    final TextOutputFormat outputFormat = TextOutputFormat
      .fromFormat(outputOptions.getOutputFormatValue());
    if (outputFormat == TextOutputFormat.json)
    {
      formatter = new SchemaJsonFormatter(schemaTextDetailType,
                                          schemaTextOptions,
                                          outputOptions,
                                          identifierQuoteString);
    }
    else if (schemaTextDetailType == SchemaTextDetailType.list)
    {
      formatter = new SchemaListFormatter(schemaTextDetailType,
                                          schemaTextOptions,
                                          outputOptions,
                                          identifierQuoteString);
    }
    else
    {
      formatter = new SchemaTextFormatter(schemaTextDetailType,
                                          schemaTextOptions,
                                          outputOptions,
                                          identifierQuoteString);
    }

    return formatter;
  }

  private void loadSchemaTextOptions()
  {
    if (schemaTextOptions == null)
    {
      schemaTextOptions = new SchemaTextOptionsBuilder()
        .fromConfig(additionalConfiguration).toOptions();
    }
  }

}
