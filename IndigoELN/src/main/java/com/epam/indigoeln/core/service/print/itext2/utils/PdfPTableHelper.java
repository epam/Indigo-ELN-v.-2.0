package com.epam.indigoeln.core.service.print.itext2.utils;

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * The class provides some useful extension methods for PdfPTable.
 */
public class PdfPTableHelper {
    private final PdfPTable originTable;

    /**
     * @param originTable the table which state will be really modified by the wrapper
     */
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
        return addKeyValueCells(key, value , valueColspan, true);
    }


    public PdfPTableHelper addKeyValueCells(String key, String value, int valueColspan, boolean bold) {
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
