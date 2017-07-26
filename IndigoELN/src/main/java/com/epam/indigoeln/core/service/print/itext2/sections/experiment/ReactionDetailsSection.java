package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.utils.DateTimeUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.ReactionDetailsModel;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.lang3.StringUtils;

public class ReactionDetailsSection extends BasePdfSectionWithSimpleTitle<ReactionDetailsModel> {
    private static final String COMMA = ", ";

    public ReactionDetailsSection(ReactionDetailsModel model) {
        super(model, "REACTION DETAILS");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(new float[]{4, 5, 4, 5}, width);

        PdfPTableHelper wrapper = new PdfPTableHelper(table);
        wrapper.addKeyValueCells("Created Date", DateTimeUtils.formatSafe(model.getCreationDate()));
        wrapper.addKeyValueCells("Therapeutic Area", model.getTherapeuticArea());
        wrapper.addKeyValueCells("Continued From", StringUtils.join(model.getContinuedFrom(),COMMA));
        wrapper.addKeyValueCells("Project Code", model.getProjectCode());
        wrapper.addKeyValueCells("Continued To", StringUtils.join(model.getContinuedTo(),COMMA));
        wrapper.addKeyValueCells("Project Alias", model.getProjectAlias());
        wrapper.addKeyValueCells("Linked Experiment", StringUtils.join(model.getLinkedExperiment(),COMMA));
        wrapper.addKeyValueCells("Literature Reference", model.getLiteratureReference());
        wrapper.addKeyValueCells("Co-authors", StringUtils.join(model.getCoauthors(),COMMA), 3);

        return table;
    }

}
