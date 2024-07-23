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

import com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchDetailsModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.lang3.StringUtils;

/**
 * Extension of BasePdfSectionWithSimpleTitle for batch details section.
 */
public class BatchDetailsSection extends BasePdfSectionWithSimpleTitle<BatchDetailsModel> {
    private static final float[] COLUMN_WIDTH = new float[]{1, 2};

    public BatchDetailsSection(BatchDetailsModel model) {
        super(model, "BATCH DETAILS");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        String[] headers = {"For Notebook Batch:", model.getFullNbkBatch()};
        PdfPTable table = TableFactory.createDefaultTable(headers, COLUMN_WIDTH, width, Element.ALIGN_LEFT);

        PdfPTableHelper pdfPTableHelper = new PdfPTableHelper(table);
        pdfPTableHelper.addKeyValueCellsNoBold("Registered Date", FormatUtils.formatBatchDetails(model
                .getRegistrationDate()));
        pdfPTableHelper.addKeyValueCellsNoBold("Structure Comments", model.getStructureComments());
        pdfPTableHelper.addKeyValueCellsNoBold("Compound Source", model.getSource());
        pdfPTableHelper.addKeyValueCellsNoBold("Source Detail", model.getSourceDetail());
        pdfPTableHelper.addKeyValueCellsNoBold("Batch Owner", StringUtils.join(model.getBatchOwner(), ", "));
        pdfPTableHelper.addKeyValueCellsNoBold("Calculated Batch MW", FormatUtils.formatDecimal(model.getMolWeight()));
        pdfPTableHelper.addKeyValueCellsNoBold("Calculated Batch MF", model.getFormula());
        pdfPTableHelper.addKeyValueCellsNoBold("Residual Solvents", model.getResidualSolvent());
        pdfPTableHelper.addKeyValueCellsNoBold("Solubility in Solvents", model.getSolubility());
        pdfPTableHelper.addKeyValueCellsNoBold("Precursor/Reactant IDs", model.getPrecursors());
        pdfPTableHelper.addKeyValueCellsNoBold("External Supplier", model.getExternalSupplier());
        pdfPTableHelper.addKeyValueCellsNoBold("Hazards", model.getHealthHazards());
        pdfPTableHelper.addKeyValueCellsNoBold("Handling", model.getHandlingPrecautions());
        pdfPTableHelper.addKeyValueCellsNoBold("Storage", model.getStorageInstructions());

        return table;
    }
}
