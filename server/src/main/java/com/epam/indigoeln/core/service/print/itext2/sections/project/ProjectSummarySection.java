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
package com.epam.indigoeln.core.service.print.itext2.sections.project;

import com.epam.indigoeln.core.service.print.itext2.model.project.ProjectSummaryModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Extension of BasePdfSectionWithSimpleTitle for project's summary.
 */
public class ProjectSummarySection extends BasePdfSectionWithSimpleTitle<ProjectSummaryModel> {
    public ProjectSummarySection(ProjectSummaryModel model) {
        super(model, "PROJECT SUMMARY");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(new float[]{1, 4}, width);
        PdfPTableHelper helper = new PdfPTableHelper(table);
        helper.addKeyValueCells("Project keywords", model.getKeywords());
        helper.addKeyValueCells("Literature", model.getLiterature());
        return table;
    }
}
