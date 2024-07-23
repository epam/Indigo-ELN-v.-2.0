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

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * The class provides some useful extension methods for PdfPTable.
 */
public class PdfPTableHelper {
    private final PdfPTable originTable;

    public PdfPTableHelper(PdfPTable originTable) {
        this.originTable = originTable;
    }

    public PdfPTableHelper addKeyValueCells(String key, String value) {
        addKeyValueCells(key, value, 1, true);
        return this;
    }

    public PdfPTableHelper addKeyValueCellsNoBold(String key, String value) {
        addKeyValueCells(key, value, 1, false);
        return this;
    }

    public PdfPTableHelper addKeyValueCells(String key, String value, int valueColspan) {
        return addKeyValueCells(key, value, valueColspan, true);
    }


    /**
     * Added key and value cells to table.
     *
     * @param key          Key title
     * @param value        Value
     * @param valueColspan Span for column
     * @param bold         True if it should be bold
     * @return PdfPTableHelper instance
     * @see com.lowagie.text.pdf.PdfPCell
     */
    private PdfPTableHelper addKeyValueCells(String key, String value, int valueColspan, boolean bold) {
        PdfPCell keyCell = CellFactory.getCommonCell(key, bold);
        PdfPCell valueCell = CellFactory.getCommonCell(value);

        valueCell.setColspan(valueColspan);

        originTable.addCell(keyCell);
        originTable.addCell(valueCell);

        return this;
    }

    public PdfPTable getTable() {
        return originTable;
    }
}
