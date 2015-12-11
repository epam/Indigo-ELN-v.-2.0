package com.epam.indigoeln.services;

import com.epam.indigoeln.documents.Notebook;

import java.util.Collection;

public interface NotebookService {
    public Notebook createNewNotebook(Notebook notebook);
    public Collection<Notebook> getAllNotebooks();
    public Notebook getNotebook(String id);
    public Notebook updateNotebook(Notebook notebook);
    public void deleteNotebook(String id);
}
