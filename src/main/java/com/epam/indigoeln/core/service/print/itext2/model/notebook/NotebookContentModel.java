package com.epam.indigoeln.core.service.print.itext2.model.notebook;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class NotebookContentModel implements SectionModel {

    private List<ContentModelRow> contentModelRows;

    public NotebookContentModel(List<ContentModelRow> contentModelRows) {
        this.contentModelRows = contentModelRows;
    }

    public List<ContentModelRow> getContentModelRows() {
        return contentModelRows;
    }

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
