package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.common.BaseHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;

import java.time.Instant;

public class ExperimentHeaderModel extends BaseHeaderModel implements SectionModel {
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
