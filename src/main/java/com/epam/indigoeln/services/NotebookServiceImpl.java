package com.epam.indigoeln.services;

import com.epam.indigoeln.documents.Notebook;
import com.epam.indigoeln.repositories.NotebookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service("notebookService")
public class NotebookServiceImpl implements NotebookService {
    @Autowired
    private NotebookRepository repository;

    public Notebook createNewNotebook(Notebook notebook) {
        return repository.createNewNotebook(notebook);
    };

    public Collection<Notebook> getAllNotebooks() {
        return repository.getAllNotebooks();
    };

    public Notebook getNotebook(String id) {
        return repository.getNotebook(id);
    };

    public Notebook updateNotebook(Notebook notebook) {
        return repository.updateNotebook(notebook);
    };

    public void deleteNotebook(String id) {
        repository.deleteNotebook(id);
    };
}
