package com.epam.indigoeln.core.service.print.itext2.utils;

import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.util.Arrays;

public class TableFactory {
    private TableFactory() {
    }

    /**
     * @param headerCellHorizontalAlignment horizontal alignment for table header cells. <br>
     *                                  <CODE>Element.ALIGN_CENTER</CODE> for example.
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

    public static PdfPTable createDefaultTable(String[] headers, float[] columnWidth, float width) {
        return createDefaultTable(headers, columnWidth, width, Element.ALIGN_CENTER);
    }

    public static PdfPTable createDefaultTable(String[] headers, float width) {
        float[] columnWidth = new float[headers.length];
        Arrays.fill(columnWidth, 1f);
        return createDefaultTable(headers, columnWidth, width);
    }

    public static PdfPTable createDefaultTable(float[] columnWidth, float width) {
        PdfPTable table = new PdfPTable(columnWidth);
        return withDefaultSettings(width, table);
    }

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
