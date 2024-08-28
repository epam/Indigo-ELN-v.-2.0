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
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Abstract class which describes base functionality for pdf section with simple title.
 *
 * @param <T> Class which implement SectionModel interface
 */
public abstract class BasePdfSectionWithSimpleTitle<T extends SectionModel> extends BasePdfSectionWithTableAndTitle<T> {
    private final String title;

    public BasePdfSectionWithSimpleTitle(T model, String title) {
        super(model);
        this.title = title;
    }

    @Override
    protected PdfPTable generateTitleTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(1, width);
        PdfPCell titleCell = CellFactory.getSimpleTitleCell(title);
        table.addCell(titleCell);
        return table;
    }
}
