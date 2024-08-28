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
package com.epam.indigoeln.core.service.print.itext2.model.notebook;

import com.epam.indigoeln.core.service.print.itext2.model.common.BaseHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;

import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * Implementation of SectionModel interface for notebook header.
 */
public class NotebookHeaderModel extends BaseHeaderModel {
    private String author;
    private ZonedDateTime creationDate;
    private String notebookName;
    private String projectName;
    private Instant printDate;

    public NotebookHeaderModel(PdfImage logo, String author, ZonedDateTime creationDate,
                               String notebookName, String projectName, Instant printDate) {
        super(logo);
        this.author = author;
        this.creationDate = creationDate;
        this.notebookName = notebookName;
        this.projectName = projectName;
        this.printDate = printDate;
    }

    public String getAuthor() {
        return author;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public String getNotebookName() {
        return notebookName;
    }

    public Instant getPrintDate() {
        return printDate;
    }

    public String getProjectName() {
        return projectName;
    }
}
