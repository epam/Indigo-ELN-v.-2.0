package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.Notebook;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

public interface NotebookRepository {
    public Notebook createNewNotebook(Notebook notebook);
    public Collection<Notebook> getAllNotebooks();
    public Notebook getNotebook(String id);
    public Notebook updateNotebook(Notebook notebook);
    public void deleteNotebook(String id);
}
