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

package schemacrawler.crawl;


import static sf.util.Utility.isBlank;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Level;

import schemacrawler.filter.InclusionRuleFilter;
import schemacrawler.schema.FunctionColumn;
import schemacrawler.schema.FunctionColumnType;
import schemacrawler.schema.ProcedureColumn;
import schemacrawler.schema.ProcedureColumnType;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerSQLException;
import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;

/**
 * A retriever uses database metadata to get the details about the
 * database procedures.
 *
 * @author Sualeh Fatehi
 */
final class RoutineColumnRetriever
  extends AbstractRetriever
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(RoutineColumnRetriever.class.getName());

  RoutineColumnRetriever(final RetrieverConnection retrieverConnection,
                         final MutableCatalog catalog,
                         final SchemaCrawlerOptions options)
    throws SQLException
  {
    super(retrieverConnection, catalog, options);
  }

  void retrieveFunctionColumns(final MutableFunction function,
                               final InclusionRule columnInclusionRule)
    throws SQLException
  {
    final InclusionRuleFilter<FunctionColumn> columnFilter = new InclusionRuleFilter<>(columnInclusionRule,
                                                                                       true);
    if (columnFilter.isExcludeAll())
    {
      LOGGER
        .log(Level.INFO,
             "Not retrieving function columns, since this was not requested");
      return;
    }

    int ordinalNumber = 0;
    try (final MetadataResultSet results = new MetadataResultSet(getMetaData()
      .getFunctionColumns(function.getSchema().getCatalogName(),
                          function.getSchema().getName(),
                          function.getName(),
                          null));)
    {
      while (results.next())
      {
        final String columnCatalogName = normalizeCatalogName(results
          .getString("FUNCTION_CAT"));
        final String schemaName = normalizeSchemaName(results
          .getString("FUNCTION_SCHEM"));
        final String functionName = results.getString("FUNCTION_NAME");
        final String columnName = results.getString("COLUMN_NAME");
        final String specificName = results.getString("SPECIFIC_NAME");

        final MutableFunctionColumn column = new MutableFunctionColumn(function,
                                                                       columnName);
        if (columnFilter.test(column) && function.getName().equals(functionName)
            && belongsToSchema(function, columnCatalogName, schemaName))
        {
          if (!isBlank(specificName)
              && !specificName.equals(function.getSpecificName()))
          {
            continue;
          }

          LOGGER.log(Level.FINE,
                     new StringFormat("Retrieving function column: %s.%s",
                                      function.getFullName(),
                                      columnName));

          final FunctionColumnType columnType = results
            .getEnumFromShortId("COLUMN_TYPE", FunctionColumnType.unknown);
          final int dataType = results.getInt("DATA_TYPE", 0);
          final String typeName = results.getString("TYPE_NAME");
          final int length = results.getInt("LENGTH", 0);
          final int precision = results.getInt("PRECISION", 0);
          final boolean isNullable = results
            .getShort("NULLABLE",
                      (short) DatabaseMetaData.functionNullableUnknown) == (short) DatabaseMetaData.functionNullable;
          final String remarks = results.getString("REMARKS");
          column.setOrdinalPosition(ordinalNumber++);
          column.setFunctionColumnType(columnType);
          column.setColumnDataType(lookupOrCreateColumnDataType(function
            .getSchema(), dataType, typeName));
          column.setSize(length);
          column.setPrecision(precision);
          column.setNullable(isNullable);
          column.setRemarks(remarks);

          column.addAttributes(results.getAttributes());

          function.addColumn(column);
        }
      }
    }
    catch (final AbstractMethodError | SQLFeatureNotSupportedException e)
    {
      logSQLFeatureNotSupported(new StringFormat("Could not retrieve columns for function %s",
                                                 function),
                                e);
    }
    catch (final SQLException e)
    {
      logPossiblyUnsupportedSQLFeature(new StringFormat("Could not retrieve columns for function %s",
                                                        function),
                                       e);
    }

  }

  void retrieveProcedureColumns(final MutableProcedure procedure,
                                final InclusionRule columnInclusionRule)
    throws SQLException
  {
    final InclusionRuleFilter<ProcedureColumn> columnFilter = new InclusionRuleFilter<>(columnInclusionRule,
                                                                                        true);
    if (columnFilter.isExcludeAll())
    {
      LOGGER
        .log(Level.INFO,
             "Not retrieving procedure columns, since this was not requested");
      return;
    }

    int ordinalNumber = 0;
    try (final MetadataResultSet results = new MetadataResultSet(getMetaData()
      .getProcedureColumns(procedure.getSchema().getCatalogName(),
                           procedure.getSchema().getName(),
                           procedure.getName(),
                           null));)
    {
      while (results.next())
      {
        final String columnCatalogName = normalizeCatalogName(results
          .getString("PROCEDURE_CAT"));
        final String schemaName = normalizeSchemaName(results
          .getString("PROCEDURE_SCHEM"));
        final String procedureName = results.getString("PROCEDURE_NAME");
        final String columnName = results.getString("COLUMN_NAME");
        final String specificName = results.getString("SPECIFIC_NAME");

        final MutableProcedureColumn column = new MutableProcedureColumn(procedure,
                                                                         columnName);
        if (columnFilter.test(column)
            && procedure.getName().equals(procedureName)
            && belongsToSchema(procedure, columnCatalogName, schemaName))
        {
          if (!isBlank(specificName)
              && !specificName.equals(procedure.getSpecificName()))
          {
            continue;
          }

          LOGGER.log(Level.FINE,
                     new StringFormat("Retrieving procedure column: %s.%s",
                                      procedure.getFullName(),
                                      columnName));

          final ProcedureColumnType columnType = results
            .getEnumFromShortId("COLUMN_TYPE", ProcedureColumnType.unknown);
          final int dataType = results.getInt("DATA_TYPE", 0);
          final String typeName = results.getString("TYPE_NAME");
          final int length = results.getInt("LENGTH", 0);
          final int precision = results.getInt("PRECISION", 0);
          final boolean isNullable = results
            .getShort("NULLABLE",
                      (short) DatabaseMetaData.procedureNullableUnknown) == (short) DatabaseMetaData.procedureNullable;
          final String remarks = results.getString("REMARKS");
          column.setOrdinalPosition(ordinalNumber++);
          column.setProcedureColumnType(columnType);
          column.setColumnDataType(lookupOrCreateColumnDataType(procedure
            .getSchema(), dataType, typeName));
          column.setSize(length);
          column.setPrecision(precision);
          column.setNullable(isNullable);
          column.setRemarks(remarks);

          column.addAttributes(results.getAttributes());

          procedure.addColumn(column);
        }
      }
    }
    catch (final SQLException e)
    {
      throw new SchemaCrawlerSQLException("Could not retrieve columns for procedure "
                                          + procedure,
                                          e);
    }

  }

}
