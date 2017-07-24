package com.epam.indigoeln.core.service.print.itext2.model.project;

import com.epam.indigoeln.core.service.print.itext2.model.BaseHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.image.PdfImage;

import java.time.Instant;
import java.time.ZonedDateTime;

public class ProjectHeaderModel extends BaseHeaderModel implements SectionModel {
    private String author;
    private ZonedDateTime creationDate;
    private String projectName;
    private Instant printDate;

    public ProjectHeaderModel(PdfImage logo, String author, ZonedDateTime creationDate,
                              String projectName, Instant printDate) {
        super(logo);
        this.author = author;
        this.creationDate = creationDate;
        this.projectName = projectName;
        this.printDate = printDate;
    }

    public String getAuthor() {
        return author;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public String getProjectName() {
        return projectName;
    }

    public Instant getPrintDate() {
        return printDate;
    }
}
