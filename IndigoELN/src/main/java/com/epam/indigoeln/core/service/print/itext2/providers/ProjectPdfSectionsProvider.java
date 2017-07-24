package com.epam.indigoeln.core.service.print.itext2.providers;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.service.print.itext2.PdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.model.project.ProjectHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.model.project.ProjectSummaryModel;
import com.epam.indigoeln.core.service.print.itext2.sections.AbstractPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.HeaderPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.project.ProjectHeaderSection;
import com.epam.indigoeln.core.service.print.itext2.sections.project.ProjectSummarySection;

import java.time.Instant;
import java.util.List;

import static java.util.Collections.singletonList;

public class ProjectPdfSectionsProvider implements PdfSectionsProvider {
    private Project project;

    public ProjectPdfSectionsProvider(Project project) {
        this.project = project;
    }

    @Override
    public List<AbstractPdfSection> getContentSections() {
        return singletonList(new ProjectSummarySection(new ProjectSummaryModel(
                project.getKeywords(),
                "TODO",
                project.getDescription()
        )));
    }

    @Override
    public HeaderPdfSection getHeaderSection() {
        ProjectHeaderModel model = new ProjectHeaderModel(
                LogoUtils.loadDefaultLogo(),
                project.getAuthor().getFullName(),
                project.getCreationDate(),
                project.getName(),
                Instant.now()
        );
        return new ProjectHeaderSection(model);
    }
}
