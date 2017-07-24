package com.epam.indigoeln.core.service.print.itext2.providers;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.service.print.itext2.PdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.sections.HeaderPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.AbstractPdfSection;

import java.util.List;

/**
 * The class is responsible for mapping experiment to a list of pdf sections used by pdf generator.
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
        throw new UnsupportedOperationException();
    }

    @Override
    public HeaderPdfSection getHeaderSection() {
        throw new UnsupportedOperationException();
    }
}
