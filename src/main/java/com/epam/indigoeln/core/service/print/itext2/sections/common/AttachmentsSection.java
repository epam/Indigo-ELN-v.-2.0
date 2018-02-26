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
package com.epam.indigoeln.core.service.print.itext2.sections.common;

import com.epam.indigoeln.core.service.print.itext2.model.common.AttachmentsModel;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.epam.indigoeln.web.rest.dto.FileDTO;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Extension of BasePdfSectionWithSimpleTitle class for attachments.
 */
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
