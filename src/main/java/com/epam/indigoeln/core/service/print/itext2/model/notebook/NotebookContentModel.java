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

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of SectionModel interface for notebook content.
 */
public class NotebookContentModel implements SectionModel {

    private List<ContentModelRow> contentModelRows;

    public NotebookContentModel(List<ContentModelRow> contentModelRows) {
        this.contentModelRows = contentModelRows;
    }

    public List<ContentModelRow> getContentModelRows() {
        return contentModelRows;
    }

    /**
     * Inner class which describes content's row.
     */
    public static class ContentModelRow {
        private String notebookExperiment;
        private Details details;
        private PdfImage reactionScheme;

        public ContentModelRow(String notebookExperiment, Details details, PdfImage reactionScheme) {
            this.notebookExperiment = notebookExperiment;
            this.details = details;
            this.reactionScheme = reactionScheme;
        }

        public String getNotebookExperiment() {
            return notebookExperiment;
        }

        public Details getDetails() {
            return details;
        }

        public PdfImage getReactionScheme() {
            return reactionScheme;
        }
    }

    public static class Details {
        private Date creationDate;
        private String title;
        private String author;
        private String status;

        public Details(ZonedDateTime creationDate, String title, String author, String status) {
            this.creationDate = Optional.ofNullable(creationDate).map(d -> Date.from(d.toInstant())).orElse(null);
            this.title = title;
            this.author = author;
            this.status = status;
        }

        public Date getCreationDate() {
            return creationDate;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getStatus() {
            return status;
        }
    }
}
