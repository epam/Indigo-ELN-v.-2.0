package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.PreferredCompoundsModel;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPTable;

public class PreferedCompoundsSection extends BasePdfSectionWithSimpleTitle<PreferredCompoundsModel> {
    private static String[] headers = new String[]{
            "Structure", "Notebook Batch #", "Mol Weight", "Mol Formula", "Structure Comments"
    };

    public PreferedCompoundsSection(PreferredCompoundsModel model) {
        super(model, "PREFERRED COMPOUNDS");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(headers, width);

        model.getRows().forEach(row -> {
            String structure = row.getStructure().getVirtualCompoundId() + System.lineSeparator() + row.getStructure().getName() +
                               " " + row.getStructure().getDescription();

            table.addCell(CellFactory.getCommonCell(structure));
            table.addCell(CellFactory.getCommonCell(row.getNotebookBatchNumber()));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getMolWeight())));
            table.addCell(CellFactory.getCommonCell(row.getMolFormula()));
            table.addCell(CellFactory.getCommonCell(row.getStructureComments()));
        });

        return table;
    }
}
