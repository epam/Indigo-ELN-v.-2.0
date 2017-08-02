package com.epam.indigoeln.core.service.print.itext2.sections.common;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPdfSection<T extends SectionModel> implements PdfSection {
    protected final T model;
    protected final List<PdfPTable> elements = new ArrayList<>();

    AbstractPdfSection(T model) {
        this.model = model;
    }

    @Override
    public final void init(float availableWidth) {
        elements.clear();
        elements.addAll(generateSectionElements(availableWidth));
    }

    protected abstract List<PdfPTable> generateSectionElements(float availableWidth);

    @Override
    public void addToDocument(Document document, PdfWriter writer) throws DocumentException {
        for (PdfPTable element : elements) {
            document.add(element);
        }
    }

    @Override
    public List<PdfPTable> getElements() {
        return elements;
    }

    @Override
    public float calcHeight() {
        return (float) elements.stream().mapToDouble(PdfPTable::getTotalHeight).sum();
    }
}
