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

public class PdfPostProcessor implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfPostProcessor.class);

    private final PdfStamper stamper;
    private final PdfLayout layout;

    public PdfPostProcessor(OutputStream output, InputStream input, PdfLayout layout) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(input);
        stamper = new PdfStamper(reader, output);
        this.layout = layout;
    }

    public float drawCentralized(List<PdfPTable> elements, int page, float y) {
        float currentY = y;
        for (PdfPTable element : elements) {
            currentY = drawCentralized(element, page, currentY);
        }
        return y - currentY;
    }

    public float drawCentralized(PdfPTable table, int page, float y) {
        float x = (layout.getPageSize().getWidth() - table.getTotalWidth()) / 2.0f;
        return table.writeSelectedRows(0, -1, x, y, stamper.getOverContent(page));
    }

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
