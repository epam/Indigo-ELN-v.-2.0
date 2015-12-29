package com.epam.indigoeln.core.service.notebook;

import com.epam.indigoeln.core.model.Notebook;

import java.util.Collection;

public interface NotebookService {

    Notebook createNewNotebook(Notebook notebook);
    Collection<Notebook> getAllNotebooks();
    Notebook getNotebook(String id);
    Notebook updateNotebook(Notebook notebook);
    void deleteNotebook(String id);
}
