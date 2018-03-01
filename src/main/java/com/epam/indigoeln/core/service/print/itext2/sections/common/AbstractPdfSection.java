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
package com.epam.indigoeln.core.service.print.itext2.sections.common;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class which provide base functionality for sections.
 *
 * @param <T>
 */
public abstract class AbstractPdfSection<T extends SectionModel> implements PdfSection {
    protected final T model;
    private final List<PdfPTable> elements = new ArrayList<>();

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
