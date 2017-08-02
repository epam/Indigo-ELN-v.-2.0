package com.epam.indigoeln.core.service.print.itext2.providers;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.print.itext2.PdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.model.common.DescriptionModel;
import com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookContentModel;
import com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.DescriptionSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.HeaderPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.AbstractPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.notebook.NotebookContentSection;
import com.epam.indigoeln.core.service.print.itext2.sections.notebook.NotebookHeaderSection;
import com.epam.indigoeln.core.service.print.itext2.utils.LogoUtils;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookContentModel.*;

/**
 * The class is responsible for mapping notebook to a list of pdf sections used by pdf generator.
 */
public final class NotebookPdfSectionsProvider implements PdfSectionsProvider {
    private final Project project;
    private final Notebook notebook;
    private final ExperimentService experimentService;
    private final UserService userService;

    public NotebookPdfSectionsProvider(Project project, Notebook notebook, ExperimentService experimentService, UserService userService) {
        this.project = project;
        this.notebook = notebook;
        this.experimentService = experimentService;
        this.userService = userService;
    }


    @Override
    public List<AbstractPdfSection> getContentSections() {
        return Arrays.asList(
                new DescriptionSection(new DescriptionModel(notebook.getDescription(), "NOTEBOOK")),
                getContentSection()
        );
    }

    @Override
    public HeaderPdfSection getHeaderSection() {
        NotebookHeaderModel model = new NotebookHeaderModel(
                LogoUtils.loadDefaultLogo(),
                notebook.getAuthor().getFullName(),
                notebook.getCreationDate(),
                notebook.getName(),
                project.getName(), Instant.now()
        );
        return new NotebookHeaderSection(model);
    }

    private AbstractPdfSection getContentSection(){
        User user = userService.getUserWithAuthorities();
        List<ExperimentDTO> experiments = experimentService.getAllExperimentNotebookSummary(project.getId(), notebook.getId(), user);
        List<ContentModelRow> rows = experiments.stream()
                .map(NotebookPdfSectionsProvider::getContentRow)
                .collect(Collectors.toList());

        return new NotebookContentSection(new NotebookContentModel(rows));
    }

    private static ContentModelRow getContentRow(ExperimentDTO experiment){
        return null;
    }
}
