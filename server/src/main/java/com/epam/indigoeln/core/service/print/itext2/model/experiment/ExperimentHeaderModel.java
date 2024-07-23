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
package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.common.BaseHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;

import java.time.Instant;

/**
 * Implementation of SectionModel interface for experiment header.
 */
public class ExperimentHeaderModel extends BaseHeaderModel {
    private Instant printDate;
    private String author;
    private String notebookExperiment;
    private String projectName;
    private String status;
    private String title;

    public ExperimentHeaderModel(PdfImage logo, Instant printDate,
                                 String author, String notebookExperiment,
                                 String projectName, String status, String title) {
        super(logo);

        this.printDate = printDate;
        this.author = author;
        this.notebookExperiment = notebookExperiment;
        this.projectName = projectName;
        this.status = status;
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public String getNotebookExperiment() {
        return notebookExperiment;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getStatus() {
        return status;
    }

    public Instant getPrintDate() {
        return printDate;
    }

    public String getTitle() {
        return title;
    }
}
