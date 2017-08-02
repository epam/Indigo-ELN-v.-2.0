package com.epam.indigoeln.core.service.print.itext2.sections.common;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.util.List;

public interface PdfSection {
    void init(float availableWidth);

    void addToDocument(Document document, PdfWriter writer) throws DocumentException;

    float calcHeight();

    List<PdfPTable> getElements();
}
