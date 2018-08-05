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


import schemacrawler.schemacrawler.Options;
import schemacrawler.utility.IdentifierQuotingStrategy;

public abstract class BaseTextOptions
  implements Options
{

  private boolean isAlphabeticalSortForRoutineColumns;
  private boolean isAlphabeticalSortForRoutines;
  private boolean isAlphabeticalSortForTableColumns;
  private boolean isAlphabeticalSortForTables = true;
  private boolean isAppendOutput;
  private boolean isNoFooter;
  private boolean isNoHeader;
  private boolean isNoSchemaCrawlerInfo;
  private boolean isShowDatabaseInfo;
  private boolean isShowJdbcDriverInfo;
  private boolean isShowUnqualifiedNames;
  private boolean isNoSchemaColors;
  private IdentifierQuotingStrategy identifierQuotingStrategy;

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

  protected void setAlphabeticalSortForRoutineColumns(final boolean isAlphabeticalSortForRoutineColumns)
  {
    this.isAlphabeticalSortForRoutineColumns = isAlphabeticalSortForRoutineColumns;
  }

  protected void setAlphabeticalSortForRoutines(final boolean isAlphabeticalSortForRoutines)
  {
    this.isAlphabeticalSortForRoutines = isAlphabeticalSortForRoutines;
  }

  protected void setAlphabeticalSortForTableColumns(final boolean isAlphabeticalSortForTableColumns)
  {
    this.isAlphabeticalSortForTableColumns = isAlphabeticalSortForTableColumns;
  }

  protected void setAlphabeticalSortForTables(final boolean isAlphabeticalSortForTables)
  {
    this.isAlphabeticalSortForTables = isAlphabeticalSortForTables;
  }

  protected void setAppendOutput(final boolean isAppendOutput)
  {
    this.isAppendOutput = isAppendOutput;
  }

  protected void setIdentifierQuotingStrategy(final IdentifierQuotingStrategy identifierQuotingStrategy)
  {
    if (identifierQuotingStrategy == null)
    {
      this.identifierQuotingStrategy = IdentifierQuotingStrategy.quote_none;
    }
    else
    {
      this.identifierQuotingStrategy = identifierQuotingStrategy;
    }
  }

  protected void setNoFooter(final boolean isNoFooter)
  {
    this.isNoFooter = isNoFooter;
  }

  protected void setNoHeader(final boolean isNoHeader)
  {
    this.isNoHeader = isNoHeader;
  }

  protected void setNoSchemaColors(final boolean isNoSchemaColors)
  {
    this.isNoSchemaColors = isNoSchemaColors;
  }

  protected void setNoSchemaCrawlerInfo(final boolean isNoSchemaCrawlerInfo)
  {
    this.isNoSchemaCrawlerInfo = isNoSchemaCrawlerInfo;
  }

  protected void setShowDatabaseInfo(final boolean isShowDatabaseInfo)
  {
    this.isShowDatabaseInfo = isShowDatabaseInfo;
  }

  protected void setShowJdbcDriverInfo(final boolean isShowJdbcDriverInfo)
  {
    this.isShowJdbcDriverInfo = isShowJdbcDriverInfo;
  }

  public void setShowUnqualifiedNames(final boolean isShowUnqualifiedNames)
  {
    this.isShowUnqualifiedNames = isShowUnqualifiedNames;
  }

}
