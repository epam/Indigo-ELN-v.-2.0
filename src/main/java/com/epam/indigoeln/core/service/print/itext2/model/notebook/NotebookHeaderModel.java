package com.epam.indigoeln.core.service.print.itext2.model.notebook;

import com.epam.indigoeln.core.service.print.itext2.model.common.BaseHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;
import java.time.Instant;
import java.time.ZonedDateTime;

public class NotebookHeaderModel extends BaseHeaderModel implements SectionModel {
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
