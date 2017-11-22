package com.epam.indigoeln.core.service.print.itext2.utils;

import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.util.Arrays;

/**
 * Table factory for creating and format table
 */
public class TableFactory {
    private TableFactory() {
    }

    /**
     * @param headerCellHorizontalAlignment horizontal alignment for table header cells. <br>
     *                                      <CODE>Element.ALIGN_CENTER</CODE> for example.
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
     * Creates default table
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
     * Creates default table
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
     * Creates default table
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
     * Creates default table
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
