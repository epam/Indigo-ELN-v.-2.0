package com.epam.indigoeln.core.service.notebook;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class NotebookServiceImpl implements NotebookService {

    @Autowired
    private NotebookRepository repository;

    /** Save notebook to notebook collection **/
    public Notebook createNewNotebook(NotebookDTO notebookDTO) {
        Notebook notebook = new Notebook();
        notebook.setName(notebookDTO.getName());

        return repository.save(notebook);
    }

    /** Get all notebooks from notebook collection **/
    public Collection<Notebook> getAllNotebooks() {
        return repository.findAll();
    }

    /** Get specific notebook by id **/
    public Notebook getNotebook(String id) {
        return repository.findOne(id);
    }

    /** Update notebook with new data **/
    public Notebook updateNotebook(NotebookDTO notebookDTO) {
        Notebook notebook = new Notebook();
        notebook.setName(notebookDTO.getName());

        return repository.save(notebook);
    }

    /** Delete notebook from notebook collection **/
    public void deleteNotebook(String id) {
        repository.delete(id);
    }
}
