package com.epam.indigoeln.core.service.print.itext2.providers;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.service.print.itext2.PdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.model.common.DescriptionModel;
import com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.DescriptionSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.HeaderPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.AbstractPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.notebook.NotebookHeaderSection;
import com.epam.indigoeln.core.service.print.itext2.utils.LogoUtils;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * The class is responsible for mapping notebook to a list of pdf sections used by pdf generator.
 */
public final class NotebookPdfSectionsProvider implements PdfSectionsProvider {
    private final Project project;
    private final Notebook notebook;

    public NotebookPdfSectionsProvider(Project project, Notebook notebook) {
        this.project = project;
        this.notebook = notebook;
    }


    @Override
    public List<AbstractPdfSection> getContentSections() {
        return Arrays.asList(new DescriptionSection(new DescriptionModel(notebook.getDescription(), "NOTEBOOK")));
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
}
