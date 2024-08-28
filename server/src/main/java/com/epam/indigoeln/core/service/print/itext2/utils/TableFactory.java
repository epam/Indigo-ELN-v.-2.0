/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.print.itext2.utils;

import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.util.Arrays;

/**
 * Table factory for creating and format table.
 */
public final class TableFactory {
    private TableFactory() {
    }

    /**
     * Creates default table.
     *
     * @param headers                       Headers
     * @param columnWidth                   Column's width
     * @param width                         Wight
     * @param headerCellHorizontalAlignment horizontal alignment for table header cells. <br>
     *                                      <CODE>Element.ALIGN_CENTER</CODE> for example.
     * @return Created table
     */
    public static PdfPTable createDefaultTable(String[] headers, float[] columnWidth,
                                               float width, int headerCellHorizontalAlignment) {
        if (headers.length != columnWidth.length) {
            throw new PdfPTableFactoryException("Invalid header definitions");
        }

        PdfPTable table = createDefaultTable(columnWidth, width);
        table.setHeaderRows(1);
        for (String header : headers) {
            PdfPCell commonCell = CellFactory.getCommonCell(header, true);
            commonCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            commonCell.setHorizontalAlignment(headerCellHorizontalAlignment);
            table.addCell(commonCell);
        }

        return table;
    }

    /**
     * Creates default table.
     *
     * @param headers     Array with headers
     * @param columnWidth Width of column
     * @param width       Width
     * @return Created table
     * @see com.lowagie.text.pdf.PdfPTable
     */
    public static PdfPTable createDefaultTable(String[] headers, float[] columnWidth, float width) {
        return createDefaultTable(headers, columnWidth, width, Element.ALIGN_CENTER);
    }

    /**
     * Creates default table.
     *
     * @param headers Array with headers
     * @param width   Width
     * @return Created table
     * @see com.lowagie.text.pdf.PdfPTable
     */
    public static PdfPTable createDefaultTable(String[] headers, float width) {
        float[] columnWidth = new float[headers.length];
        Arrays.fill(columnWidth, 1f);
        return createDefaultTable(headers, columnWidth, width);
    }

    /**
     * Creates default table.
     *
     * @param columnWidth Width of column
     * @param width       Width
     * @return Created table
     * @see com.lowagie.text.pdf.PdfPTable
     */
    public static PdfPTable createDefaultTable(float[] columnWidth, float width) {
        PdfPTable table = new PdfPTable(columnWidth);
        return withDefaultSettings(width, table);
    }

    /**
     * Creates default table.
     *
     * @param contentColumns The amount of columns
     * @param width          Width
     * @return Created table
     * @see com.lowagie.text.pdf.PdfPTable
     */
    public static PdfPTable createDefaultTable(int contentColumns, float width) {
        PdfPTable table = new PdfPTable(contentColumns);
        return withDefaultSettings(width, table);
    }


    private static PdfPTable withDefaultSettings(float width, PdfPTable table) {
        table.setWidthPercentage(100);
        table.setLockedWidth(true);
        table.setTotalWidth(width);
        return table;
    }

    static class PdfPTableFactoryException extends RuntimeException {
        PdfPTableFactoryException(String s) {
            super(s);
        }
    }
}
