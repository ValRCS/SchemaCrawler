/* 
 *
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2008, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */

package schemacrawler.tools.util;


import schemacrawler.tools.OutputFormat;
import schemacrawler.tools.util.TableCell.Align;
import sf.util.Utilities;

/**
 * Methods to format entire rows of output as HTML.
 * 
 * @author Sualeh Fatehi
 */
abstract class BaseTextFormattingHelper
  implements TextFormattingHelper
{
  final OutputFormat outputFormat;

  BaseTextFormattingHelper(OutputFormat outputFormat)
  {
    this.outputFormat = outputFormat;
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.util.TextFormattingHelper#createDefinitionRow(java.lang.String)
   */
  public String createDefinitionRow(final String definition)
  {
    final TableRow row = new TableRow(outputFormat);
    row.add(new TableCell("", "ordinal", outputFormat));
    row.add(new TableCell(definition,
                          0,
                          Align.left,
                          2,
                          "definition",
                          outputFormat));
    return row.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.util.TextFormattingHelper#createDetailRow(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public String createDetailRow(final String ordinal,
                                final String subName,
                                final String type)
  {
    final int subNameWidth = 32;
    final int typeWidth = 28;

    final TableRow row = new TableRow(outputFormat);
    if (Utilities.isBlank(ordinal))
    {
      row.add(new TableCell("", "ordinal", outputFormat));
    }
    else
    {
      row
        .add(new TableCell(ordinal, 2, Align.left, 1, "ordinal", outputFormat));
    }
    row.add(new TableCell(subName,
                          subNameWidth,
                          Align.left,
                          1,
                          "subname",
                          outputFormat));
    row
      .add(new TableCell(type, typeWidth, Align.left, 1, "type", outputFormat));
    return row.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.util.TextFormattingHelper#createEmptyRow()
   */
  public String createEmptyRow()
  {
    return new TableRow(outputFormat, 4).toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.util.TextFormattingHelper#createNameRow(java.lang.String,
   *      java.lang.String)
   */
  public String createNameRow(final String name,
                              final String description,
                              boolean underscore)
  {
    int nameWidth = 34;
    int descriptionWidth = 36;
    // Adjust widths
    if (name.length() > nameWidth && description.length() < descriptionWidth)
    {
      descriptionWidth = Math.max(description.length(),
                                  descriptionWidth
                                      - (name.length() - nameWidth));
    }
    if (description.length() > descriptionWidth && name.length() < nameWidth)
    {
      nameWidth = Math.max(name.length(),
                           nameWidth
                               - (description.length() - descriptionWidth));
    }

    String nameRowString;
    final TableRow row = new TableRow(outputFormat);
    row.add(new TableCell(name,
                          nameWidth,
                          Align.left,
                          2,
                          "name" + (underscore? " underscore": ""),
                          outputFormat));
    row.add(new TableCell(description,
                          descriptionWidth,
                          Align.right,
                          1,
                          "description" + (underscore? " underscore": ""),
                          outputFormat));
    nameRowString = row.toString();

    if (underscore && outputFormat != OutputFormat.html)
    {
      nameRowString = nameRowString + Utilities.NEWLINE
                      + FormatUtils.repeat("-", FormatUtils.MAX_LINE_LENGTH);
    }

    return nameRowString;
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.util.TextFormattingHelper#createNameValueRow(java.lang.String,
   *      java.lang.String)
   */
  public String createNameValueRow(final String name, final String value)
  {
    final int nameWidth = 36;

    final TableRow row = new TableRow(outputFormat);
    row.add(new TableCell(name, nameWidth, Align.left, 1, "", outputFormat));
    row.add(new TableCell(value, "", outputFormat));
    return row.toString();
  }

  /**
   * Called to handle the row output.
   * 
   * @param columnData
   *        Column data
   * @throws QueryExecutorException
   *         On an exception
   */
  public String createRow(final String[] columnData)
  {
    OutputFormat outputFormat = this.outputFormat;
    if (outputFormat == OutputFormat.text)
    {
      outputFormat = OutputFormat.csv;
    }
    final TableRow row = new TableRow(outputFormat);
    for (int i = 0; i < columnData.length; i++)
    {
      row.add(new TableCell(columnData[i], "", outputFormat));
    }
    return row.toString();
  }

  /**
   * Called to handle the header output. Handler to be implemented by
   * subclass.
   * 
   * @param columnNames
   *        Column names
   * @throws QueryExecutorException
   *         On an exception
   */
  public String createRowHeader(final String[] columnNames)
  {
    OutputFormat outputFormat = this.outputFormat;
    if (outputFormat == OutputFormat.text)
    {
      outputFormat = OutputFormat.csv;
    }
    final TableRow row = new TableRow(outputFormat);
    for (int i = 0; i < columnNames.length; i++)
    {
      row.add(new TableCell(columnNames[i], "name", outputFormat));
    }
    return row.toString();
  }

}
