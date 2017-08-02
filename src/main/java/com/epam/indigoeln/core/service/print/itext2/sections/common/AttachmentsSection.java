package com.epam.indigoeln.core.service.print.itext2.sections.common;

import com.epam.indigoeln.core.service.print.itext2.model.common.AttachmentsModel;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.epam.indigoeln.web.rest.dto.FileDTO;
import com.lowagie.text.pdf.PdfPTable;

public class AttachmentsSection extends BasePdfSectionWithSimpleTitle<AttachmentsModel> {
    private static final String[] HEADERS = new String[]{
            "File Name", "Author", "Length (Bytes)", "Upload Date"
    };

    public AttachmentsSection(AttachmentsModel model) {
        super(model, "ATTACHMENTS");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(HEADERS, width);

        for (FileDTO fileDTO : model.getFiles()) {
            table.addCell(CellFactory.getCommonCell(fileDTO.getFilename()));
            table.addCell(CellFactory.getCommonCell(fileDTO.getAuthor().getId()));
            table.addCell(CellFactory.getCommonCell(FormatUtils.format(fileDTO.getLength())));
            table.addCell(CellFactory.getCommonCell(FormatUtils.format(fileDTO.getUploadDate())));
        }
        return table;
    }
}
