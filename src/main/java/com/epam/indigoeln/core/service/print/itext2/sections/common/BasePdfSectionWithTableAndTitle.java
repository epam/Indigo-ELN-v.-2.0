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
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Abstract class which describes base functionality for pdf section with title and table.
 * @param <T> Class which implements SectionModel interface
 */
public abstract class BasePdfSectionWithTableAndTitle<T extends SectionModel> extends AbstractPdfSection<T> {
    private PdfPTable titleTable;
    private PdfPTable contentTable;

    private static final int MIN_SPACE_FOR_CONTENT_ON_PAGE = 40;

    BasePdfSectionWithTableAndTitle(T model) {
        super(model);
    }

    @Override
    protected List<PdfPTable> generateSectionElements(float width) {
        titleTable = generateTitleTable(width);
        contentTable = generateContentTable(width);

        PdfPTable table = TableFactory.createDefaultTable(1, width);
        table.setHeaderRows(1);

        PdfPCell title = CellFactory.getCommonCell(titleTable);
        title.setBorder(Rectangle.NO_BORDER);
        table.addCell(title);

        PdfPCell content = CellFactory.getCommonCell(contentTable);
        table.addCell(content);

        return StreamEx.of(table).filter(Objects::nonNull).toList();
    }

    protected abstract PdfPTable generateTitleTable(float width);

    protected abstract PdfPTable generateContentTable(float width);

    @Override
    public void addToDocument(Document document, PdfWriter writer) throws DocumentException {
        if (!isContentTableFittingPage(document, writer)) {
            document.newPage();
        }
        super.addToDocument(document, writer);
    }

    private boolean isContentTableFittingPage(Document document, PdfWriter writer) {
        float remainingPageHeight = writer.getVerticalPosition(true) - document.bottom();

        float titleTableHeight = Optional.of(titleTable).map(PdfPTable::getTotalHeight).orElse(0f);
        float contentTableHeaderHeight = Optional.of(contentTable).map(PdfPTable::getHeaderHeight).orElse(0f);
        float contentAvailableHeight = remainingPageHeight - titleTableHeight - contentTableHeaderHeight;

        return contentAvailableHeight >= MIN_SPACE_FOR_CONTENT_ON_PAGE;
    }
}
