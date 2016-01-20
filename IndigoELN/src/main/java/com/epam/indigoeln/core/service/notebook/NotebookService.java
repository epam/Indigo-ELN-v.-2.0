package com.epam.indigoeln.core.service.notebook;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;

import java.util.Collection;

public interface NotebookService {

    Notebook createNewNotebook(NotebookDTO notebookDTO);
    Collection<Notebook> getAllNotebooks();
    Notebook getNotebook(String id);
    Notebook updateNotebook(NotebookDTO notebookDTO);
    void deleteNotebook(String id);
}
