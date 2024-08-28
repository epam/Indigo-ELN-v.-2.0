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

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Provides functionality for working with pdf after generating.
 */
public class PdfPostProcessor implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfPostProcessor.class);

    private final PdfStamper stamper;
    private final PdfLayout layout;

    public PdfPostProcessor(OutputStream output, InputStream input, PdfLayout layout)
            throws IOException, DocumentException {
        PdfReader reader = new PdfReader(input);
        this.stamper = new PdfStamper(reader, output);
        this.layout = layout;
    }

    /**
     * Draws input elements.
     *
     * @param elements List with elements to draw
     * @param page     Page of file
     * @param y        Coordinate
     * @return Current coordinate after drawing
     * @see com.lowagie.text.pdf.PdfPTable
     */
    public float drawCentralized(List<PdfPTable> elements, int page, float y) {
        float currentY = y;
        for (PdfPTable element : elements) {
            currentY = drawCentralized(element, page, currentY);
        }
        return y - currentY;
    }

    /**
     * Draws input element.
     *
     * @param table Table
     * @param page  Page of file
     * @param y     Coordinate
     * @return Current coordinate after drawing
     * @see com.lowagie.text.pdf.PdfPTable
     */
    float drawCentralized(PdfPTable table, int page, float y) {
        float x = (layout.getPageSize().getWidth() - table.getTotalWidth()) / 2.0f;
        return table.writeSelectedRows(0, -1, x, y, stamper.getOverContent(page));
    }

    /**
     * @return Returns reader from PdfStamper's instance
     * @see com.lowagie.text.pdf.PdfStamper
     */
    public PdfReader getReader() {
        return stamper.getReader();
    }

    @Override
    public void close() {
        try {
            stamper.close();
        } catch (Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
    }
}
