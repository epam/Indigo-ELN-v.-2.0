package com.epam.indigoeln.core.service.notebook;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class NotebookServiceImpl implements NotebookService {

    @Autowired
    private NotebookRepository repository;

    public Notebook createNewNotebook(Notebook notebook) {
        return repository.save(notebook);
    }

    public Collection<Notebook> getAllNotebooks() {
        return repository.findAll();
    }

    public Notebook getNotebook(String id) {
        return repository.findOne(id);
    }

    public Notebook updateNotebook(Notebook notebook) {
        return repository.save(notebook);
    }

    public void deleteNotebook(String id) {
        repository.delete(id);
    }
}
