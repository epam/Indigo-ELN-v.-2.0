package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.ConceptDetailsModel;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.lang.StringUtils;

public class ConceptDetailsSection extends BasePdfSectionWithSimpleTitle<ConceptDetailsModel> {
    private static final String COMMA = ", ";

    public ConceptDetailsSection(ConceptDetailsModel model) {
        super(model, "CONCEPT DETAILS");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(4, width);

        PdfPTableHelper wrapper = new PdfPTableHelper(table);
        wrapper.addKeyValueCells("Created Date", model.getCreationDate());
        wrapper.addKeyValueCells("Therapeutic Area", model.getTherapeuticArea());
        wrapper.addKeyValueCells("Linked Experiment", model.getLinkedExperiment());
        wrapper.addKeyValueCells("Project Code", model.getProjectCode());
        wrapper.addKeyValueCells("Concept Keyword", StringUtils.join(model.getConceptKeywords(), COMMA));
        wrapper.addKeyValueCells("Designers", StringUtils.join(model.getDesigners(), COMMA));
        wrapper.addKeyValueCells("Co-authors", StringUtils.join(model.getCoauthors(), COMMA), 3);

        return table;
    }

}
