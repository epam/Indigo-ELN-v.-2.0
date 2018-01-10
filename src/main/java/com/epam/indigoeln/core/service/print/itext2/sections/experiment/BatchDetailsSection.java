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
