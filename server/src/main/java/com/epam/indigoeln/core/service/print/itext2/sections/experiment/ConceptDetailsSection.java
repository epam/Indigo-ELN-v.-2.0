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
package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.experiment.ConceptDetailsModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.lang3.StringUtils;

/**
 * Extension of BasePdfSectionWithSimpleTitle for concept details section.
 */
public class ConceptDetailsSection extends BasePdfSectionWithSimpleTitle<ConceptDetailsModel> {
    private static final String COMMA = ", ";
    private static final float[] COLUMNS_WIDTH = new float[]{4, 5, 4, 5};

    public ConceptDetailsSection(ConceptDetailsModel model) {
        super(model, "CONCEPT DETAILS");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(COLUMNS_WIDTH, width);

        PdfPTableHelper wrapper = new PdfPTableHelper(table);
        wrapper.addKeyValueCells("Created Date", FormatUtils.formatSafe(model.getCreationDate()));
        wrapper.addKeyValueCells("Therapeutic Area", model.getTherapeuticArea());
        wrapper.addKeyValueCells("Linked Experiment", StringUtils.join(model.getLinkedExperiment(), COMMA));
        wrapper.addKeyValueCells("Project Code", model.getProjectCode());
        wrapper.addKeyValueCells("Designers", StringUtils.join(model.getDesigners(), COMMA));
        wrapper.addKeyValueCells("Co-authors", StringUtils.join(model.getCoAuthors(), COMMA));
        wrapper.addKeyValueCells("Concept Keyword", model.getConceptKeywords(), 3);

        return table;
    }
}
