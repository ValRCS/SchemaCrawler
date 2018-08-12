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


import static java.util.Objects.requireNonNull;
import static sf.util.Utility.isBlank;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;

import schemacrawler.filter.InclusionRuleFilter;
import schemacrawler.schema.Function;
import schemacrawler.schema.FunctionColumn;
import schemacrawler.schema.FunctionColumnType;
import schemacrawler.schema.FunctionReturnType;
import schemacrawler.schema.Procedure;
import schemacrawler.schema.ProcedureColumn;
import schemacrawler.schema.ProcedureColumnType;
import schemacrawler.schema.ProcedureReturnType;
import schemacrawler.schema.Schema;
import schemacrawler.schema.SchemaReference;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.InformationSchemaKey;
import schemacrawler.schemacrawler.InformationSchemaViews;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerSQLException;
import schemacrawler.utility.Query;
import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;

/**
 * A retriever uses database metadata to get the details about the
 * database procedures.
 *
 * @author Sualeh Fatehi
 */
final class RoutineRetriever
  extends AbstractRetriever
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(RoutineRetriever.class.getName());

  RoutineRetriever(final RetrieverConnection retrieverConnection,
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

  void retrieveFunctions(final NamedObjectList<SchemaReference> schemas,
                         final InclusionRule routineInclusionRule)
    throws SQLException
  {
    requireNonNull(schemas, "No schemas provided");

    final InclusionRuleFilter<Function> functionFilter = new InclusionRuleFilter<>(routineInclusionRule,
                                                                                   false);
    if (functionFilter.isExcludeAll())
    {
      LOGGER.log(Level.INFO,
                 "Not retrieving functions, since this was not requested");
      return;
    }

    final MetadataRetrievalStrategy functionRetrievalStrategy = getRetrieverConnection()
      .getFunctionRetrievalStrategy();
    switch (functionRetrievalStrategy)
    {
      case data_dictionary_all:
        LOGGER
          .log(Level.INFO,
               "Retrieving functions, using fast data dictionary retrieval");
        retrieveFunctionsFromDataDictionary(schemas, functionFilter);
        break;

      case metadata_all:
        LOGGER.log(Level.INFO,
                   "Retrieving functions, using fast meta-data retrieval");
        retrieveFunctionsFromMetadataForAllFunctions(schemas, functionFilter);
        break;

      case metadata:
        LOGGER.log(Level.INFO, "Retrieving functions");
        retrieveFunctionsFromMetadata(schemas, functionFilter);
        break;

      default:
        break;
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

  void retrieveProcedures(final NamedObjectList<SchemaReference> schemas,
                          final InclusionRule routineInclusionRule)
    throws SQLException
  {
    requireNonNull(schemas, "No schemas provided");

    final InclusionRuleFilter<Procedure> procedureFilter = new InclusionRuleFilter<>(routineInclusionRule,
                                                                                     false);
    if (procedureFilter.isExcludeAll())
    {
      LOGGER.log(Level.INFO,
                 "Not retrieving procedures, since this was not requested");
      return;
    }

    final MetadataRetrievalStrategy procedureRetrievalStrategy = getRetrieverConnection()
      .getProcedureRetrievalStrategy();
    switch (procedureRetrievalStrategy)
    {
      case data_dictionary_all:
        LOGGER
          .log(Level.INFO,
               "Retrieving procedures, using fast data dictionary retrieval");
        retrieveProceduresFromDataDictionary(schemas, procedureFilter);
        break;

      case metadata_all:
        LOGGER.log(Level.INFO,
                   "Retrieving procedures, using fast meta-data retrieval");
        retrieveProceduresFromMetadataForAllProcedures(schemas,
                                                       procedureFilter);
        break;

      case metadata:
        LOGGER.log(Level.INFO, "Retrieving procedures");
        retrieveProceduresFromMetadata(schemas, procedureFilter);
        break;

      default:
        break;
    }

  }

  private void createFunction(final MetadataResultSet results,
                              final NamedObjectList<SchemaReference> schemas,
                              final InclusionRuleFilter<Function> functionFilter)
  {
    final String catalogName = normalizeCatalogName(results
      .getString("FUNCTION_CAT"));
    final String schemaName = normalizeSchemaName(results
      .getString("FUNCTION_SCHEM"));
    final String functionName = results.getString("FUNCTION_NAME");
    LOGGER.log(Level.FINE,
               new StringFormat("Retrieving function <%s.%s.%s>",
                                catalogName,
                                schemaName,
                                functionName));

    if (isBlank(functionName))
    {
      return;
    }

    final FunctionReturnType functionType = results
      .getEnumFromShortId("FUNCTION_TYPE", FunctionReturnType.unknown);
    final String remarks = results.getString("REMARKS");
    final String specificName = results.getString("SPECIFIC_NAME");

    final Optional<SchemaReference> optionalSchema = schemas
      .lookup(Arrays.asList(catalogName, schemaName));
    if (!optionalSchema.isPresent())
    {
      return;
    }
    final Schema schema = optionalSchema.get();

    final MutableFunction function = new MutableFunction(schema, functionName);
    if (functionFilter.test(function))
    {
      function.setReturnType(functionType);
      function.setSpecificName(specificName);
      function.setRemarks(remarks);
      function.addAttributes(results.getAttributes());

      catalog.addRoutine(function);
    }
  }

  private void createProcedure(final MetadataResultSet results,
                               final NamedObjectList<SchemaReference> schemas,
                               final InclusionRuleFilter<Procedure> procedureFilter)
  {
    final String catalogName = normalizeCatalogName(results
      .getString("PROCEDURE_CAT"));
    final String schemaName = normalizeSchemaName(results
      .getString("PROCEDURE_SCHEM"));
    final String procedureName = results.getString("PROCEDURE_NAME");
    LOGGER.log(Level.FINE,
               new StringFormat("Retrieving procedure <%s.%s.%s>",
                                catalogName,
                                schemaName,
                                procedureName));
    if (isBlank(procedureName))
    {
      return;
    }
    final ProcedureReturnType procedureType = results
      .getEnumFromShortId("PROCEDURE_TYPE", ProcedureReturnType.unknown);
    final String remarks = results.getString("REMARKS");
    final String specificName = results.getString("SPECIFIC_NAME");

    final Optional<SchemaReference> optionalSchema = schemas
      .lookup(Arrays.asList(catalogName, schemaName));
    if (!optionalSchema.isPresent())
    {
      return;
    }
    final Schema schema = optionalSchema.get();

    final MutableProcedure procedure = new MutableProcedure(schema,
                                                            procedureName);
    if (procedureFilter.test(procedure))
    {
      procedure.setReturnType(procedureType);
      procedure.setSpecificName(specificName);
      procedure.setRemarks(remarks);
      procedure.addAttributes(results.getAttributes());

      catalog.addRoutine(procedure);
    }
  }

  private void retrieveFunctionsFromDataDictionary(final NamedObjectList<SchemaReference> schemas,
                                                   final InclusionRuleFilter<Function> functionFilter)
    throws SQLException
  {
    final InformationSchemaViews informationSchemaViews = getRetrieverConnection()
      .getInformationSchemaViews();
    if (!informationSchemaViews.hasQuery(InformationSchemaKey.FUNCTIONS))
    {
      throw new SchemaCrawlerSQLException("No functions SQL provided", null);
    }
    final Query functionsSql = informationSchemaViews
      .getQuery(InformationSchemaKey.FUNCTIONS);
    final Connection connection = getDatabaseConnection();
    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results = new MetadataResultSet(functionsSql,
                                                                statement,
                                                                getSchemaInclusionRule());)
    {
      results.setDescription("retrieveFunctionsFromDataDictionary");
      int numFunctions = 0;
      while (results.next())
      {
        numFunctions = numFunctions + 1;
        createFunction(results, schemas, functionFilter);
      }
      LOGGER.log(Level.INFO,
                 new StringFormat("Processed %d functions", numFunctions));
    }
  }

  private void retrieveFunctionsFromMetadata(final NamedObjectList<SchemaReference> schemas,
                                             final InclusionRuleFilter<Function> functionFilter)
  {
    for (final Schema schema: schemas)
    {
      LOGGER
        .log(Level.INFO,
             new StringFormat("Retrieving functions for schema <%s>", schema));

      final String catalogName = schema.getCatalogName();
      final String schemaName = schema.getName();

      try (final MetadataResultSet results = new MetadataResultSet(getMetaData()
        .getFunctions(catalogName, schemaName, "%"));)
      {
        results.setDescription("retrieveFunctionsFromMetadata");
        int numFunctions = 0;
        while (results.next())
        {
          numFunctions = numFunctions + 1;
          createFunction(results, schemas, functionFilter);
        }
        LOGGER.log(Level.INFO,
                   new StringFormat("Processed %d functions", numFunctions));
      }
      catch (final AbstractMethodError | SQLFeatureNotSupportedException e)
      {
        logSQLFeatureNotSupported(new StringFormat("Could not retrieve functions"),
                                  e);
      }
      catch (final SQLException e)
      {
        logPossiblyUnsupportedSQLFeature(new StringFormat("Could not retrieve functions"),
                                         e);
      }
    }
  }

  private void retrieveFunctionsFromMetadataForAllFunctions(final NamedObjectList<SchemaReference> schemas,
                                                            final InclusionRuleFilter<Function> functionFilter)
    throws SQLException
  {
    try (final MetadataResultSet results = new MetadataResultSet(getMetaData()
      .getFunctions(null, null, "%"));)
    {
      results.setDescription("retrieveFunctionsFromMetadataForAllFunctions");
      int numFunctions = 0;
      while (results.next())
      {
        numFunctions = numFunctions + 1;
        createFunction(results, schemas, functionFilter);
      }
      LOGGER.log(Level.INFO,
                 new StringFormat("Processed %d functions", numFunctions));
    }
    catch (final AbstractMethodError | SQLFeatureNotSupportedException e)
    {
      logSQLFeatureNotSupported(new StringFormat("Could not retrieve functions"),
                                e);
    }
    catch (final SQLException e)
    {
      logPossiblyUnsupportedSQLFeature(new StringFormat("Could not retrieve functions"),
                                       e);
    }
  }

  private void retrieveProceduresFromDataDictionary(final NamedObjectList<SchemaReference> schemas,
                                                    final InclusionRuleFilter<Procedure> procedureFilter)
    throws SQLException
  {
    final InformationSchemaViews informationSchemaViews = getRetrieverConnection()
      .getInformationSchemaViews();
    if (!informationSchemaViews.hasQuery(InformationSchemaKey.PROCEDURES))
    {
      throw new SchemaCrawlerSQLException("No procedures SQL provided", null);
    }
    final Query proceduresSql = informationSchemaViews
      .getQuery(InformationSchemaKey.PROCEDURES);
    final Connection connection = getDatabaseConnection();
    try (final Statement statement = connection.createStatement();
        final MetadataResultSet results = new MetadataResultSet(proceduresSql,
                                                                statement,
                                                                getSchemaInclusionRule());)
    {
      results.setDescription("retrieveProceduresFromDataDictionary");
      int numProcedures = 0;
      while (results.next())
      {
        numProcedures = numProcedures + 1;
        createProcedure(results, schemas, procedureFilter);
      }
      LOGGER.log(Level.INFO,
                 new StringFormat("Processed %d procedures", numProcedures));
    }
  }

  private void retrieveProceduresFromMetadata(final NamedObjectList<SchemaReference> schemas,
                                              final InclusionRuleFilter<Procedure> procedureFilter)
    throws SQLException
  {
    for (final Schema schema: schemas)
    {
      LOGGER
        .log(Level.INFO,
             new StringFormat("Retrieving procedures for schema <%s>", schema));

      final String catalogName = schema.getCatalogName();
      final String schemaName = schema.getName();

      try (final MetadataResultSet results = new MetadataResultSet(getMetaData()
        .getProcedures(catalogName, schemaName, "%"));)
      {
        results.setDescription("retrieveProceduresFromMetadata");
        int numProcedures = 0;
        while (results.next())
        {
          numProcedures = numProcedures + 1;
          createProcedure(results, schemas, procedureFilter);
        }
        LOGGER.log(Level.INFO,
                   new StringFormat("Processed %d procedures", numProcedures));
      }
    }
  }

  private void retrieveProceduresFromMetadataForAllProcedures(final NamedObjectList<SchemaReference> schemas,
                                                              final InclusionRuleFilter<Procedure> procedureFilter)
    throws SQLException
  {
    try (final MetadataResultSet results = new MetadataResultSet(getMetaData()
      .getProcedures(null, null, "%"));)
    {
      results.setDescription("retrieveProceduresFromMetadataForAllProcedures");
      int numProcedures = 0;
      while (results.next())
      {
        numProcedures = numProcedures + 1;
        createProcedure(results, schemas, procedureFilter);
      }
      LOGGER.log(Level.INFO,
                 new StringFormat("Processed %d procedures", numProcedures));
    }
  }

}
