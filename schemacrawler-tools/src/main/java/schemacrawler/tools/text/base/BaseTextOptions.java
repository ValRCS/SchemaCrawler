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

package schemacrawler.tools.text.base;


import static java.util.Objects.requireNonNull;

import schemacrawler.schemacrawler.Options;
import schemacrawler.utility.IdentifierQuotingStrategy;

public abstract class BaseTextOptions
  implements Options
{

  private final boolean isAlphabeticalSortForTables;
  private final boolean isAlphabeticalSortForTableColumns;
  private final boolean isAlphabeticalSortForRoutines;
  private final boolean isAlphabeticalSortForRoutineColumns;
  private final boolean isAppendOutput;
  private final boolean isNoFooter;
  private final boolean isNoHeader;
  private final boolean isNoSchemaCrawlerInfo;
  private final boolean isShowDatabaseInfo;
  private final boolean isShowJdbcDriverInfo;
  private final boolean isShowUnqualifiedNames;
  private final boolean isNoSchemaColors;
  private final IdentifierQuotingStrategy identifierQuotingStrategy;

  protected BaseTextOptions(final BaseTextOptionsBuilder<?, ? extends BaseTextOptions> builder)
  {
    requireNonNull(builder, "No builder provided");

    isAlphabeticalSortForTables = builder.isAlphabeticalSortForTables;
    isAlphabeticalSortForTableColumns = builder.isAlphabeticalSortForTableColumns;
    isAlphabeticalSortForRoutines = builder.isAlphabeticalSortForRoutines;
    isAlphabeticalSortForRoutineColumns = builder.isAlphabeticalSortForRoutineColumns;
    isAppendOutput = builder.isAppendOutput;
    isNoFooter = builder.isNoFooter;
    isNoHeader = builder.isNoHeader;
    isNoSchemaCrawlerInfo = builder.isNoSchemaCrawlerInfo;
    isShowDatabaseInfo = builder.isShowDatabaseInfo;
    isShowJdbcDriverInfo = builder.isShowJdbcDriverInfo;
    isShowUnqualifiedNames = builder.isShowUnqualifiedNames;
    isNoSchemaColors = builder.isNoSchemaColors;
    identifierQuotingStrategy = builder.identifierQuotingStrategy;
  }

  public IdentifierQuotingStrategy getIdentifierQuotingStrategy()
  {
    return identifierQuotingStrategy;
  }

  public boolean isAlphabeticalSortForRoutineColumns()
  {
    return isAlphabeticalSortForRoutineColumns;
  }

  public boolean isAlphabeticalSortForRoutines()
  {
    return isAlphabeticalSortForRoutines;
  }

  public boolean isAlphabeticalSortForTableColumns()
  {
    return isAlphabeticalSortForTableColumns;
  }

  public boolean isAlphabeticalSortForTables()
  {
    return isAlphabeticalSortForTables;
  }

  public boolean isAppendOutput()
  {
    return isAppendOutput;
  }

  public boolean isNoFooter()
  {
    return isNoFooter;
  }

  public boolean isNoHeader()
  {
    return isNoHeader;
  }

  public boolean isNoInfo()
  {
    return isNoSchemaCrawlerInfo && !isShowDatabaseInfo
           && !isShowJdbcDriverInfo;
  }

  public boolean isNoSchemaColors()
  {
    return isNoSchemaColors;
  }

  public boolean isNoSchemaCrawlerInfo()
  {
    return isNoSchemaCrawlerInfo;
  }

  public boolean isShowDatabaseInfo()
  {
    return isShowDatabaseInfo;
  }

  public boolean isShowJdbcDriverInfo()
  {
    return isShowJdbcDriverInfo;
  }

  public boolean isShowUnqualifiedNames()
  {
    return isShowUnqualifiedNames;
  }

}
