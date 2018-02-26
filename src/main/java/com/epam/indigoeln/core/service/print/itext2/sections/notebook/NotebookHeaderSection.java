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
package com.epam.indigoeln.core.service.print.itext2.sections.notebook;

import com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BaseHeaderSectionWithLogo;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Extension of BaseHeaderSectionWithLogo for notebook header.
 */
public class NotebookHeaderSection extends BaseHeaderSectionWithLogo<NotebookHeaderModel> {

    private static final float[] COLUMN_WIDTH = new float[]{1, 1, 1, 1.15f};

    public NotebookHeaderSection(NotebookHeaderModel model) {
        super(model);
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(COLUMN_WIDTH, width);

        PdfPTableHelper helper = new PdfPTableHelper(table);
        helper.addKeyValueCells("Project Name", model.getProjectName())
                .addKeyValueCells("Notebook Name", model.getNotebookName())
                .addKeyValueCells("Author", model.getAuthor())
                .addKeyValueCells("Creation Date", FormatUtils.formatSafe(model.getCreationDate()))
                .addKeyValueCells("Printed Page", model.getCurrentPage() + " of " + model.getTotalPages())
                .addKeyValueCells("Printed Date", FormatUtils.formatSafe(model.getPrintDate()));

        return table;
    }
}
