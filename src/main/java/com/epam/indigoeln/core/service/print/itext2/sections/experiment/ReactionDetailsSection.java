package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.utils.DateTimeUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.ReactionDetailsModel;
import com.lowagie.text.pdf.PdfPTable;

public class ReactionDetailsSection extends BasePdfSectionWithSimpleTitle<ReactionDetailsModel> {
    public ReactionDetailsSection(ReactionDetailsModel model) {
        super(model, "REACTION DETAILS");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(new float[]{4, 5, 4, 5}, width);

        PdfPTableHelper wrapper = new PdfPTableHelper(table);
        wrapper.addKeyValueCells("Created Date", DateTimeUtils.formatSafe(model.getCreationDate()));
        wrapper.addKeyValueCells("Therapeutic Area", model.getTherapeuticArea());
        wrapper.addKeyValueCells("Continued From", model.getContinuedFrom());
        wrapper.addKeyValueCells("Project Code", model.getProjectCode());
        wrapper.addKeyValueCells("Continued To", model.getContinuedTo());
        wrapper.addKeyValueCells("Project Alias", model.getProjectAlias());
        wrapper.addKeyValueCells("Linked Experiment", model.getLinkedExperiment());
        wrapper.addKeyValueCells("Literature Reference", model.getLitretureReference());
        wrapper.addKeyValueCells("Co-authors", model.getCoauthors(), 3);

        return table;
    }

}
