package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api")
public class NotebookResource {

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private NotebookService notebookService;

    /**
     * POST /api/notebooks
     * Create new notebook
     **/
    @RequestMapping(value = "/notebooks", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public NotebookDTO createNewNotebook(@RequestBody NotebookDTO notebook) {
        Notebook newNotebook = new Notebook();

        /* Validation of a name (no data, wrong style, already exists) */
        if (!StringUtils.isEmpty(notebook.getName()) && notebook.getName().matches("^\\d{8}") &&
                !notebookRepository.findOneByName(notebook.getName()).isPresent()) {
            newNotebook = notebookService.createNewNotebook(notebook);
        }

        return new NotebookDTO(newNotebook);
    }

    /**
     * PUT /api/notebooks
     * Update notebook
     **/
    @RequestMapping(value = "/notebooks", method = RequestMethod.PUT)
    public void updateNotebook(@RequestBody NotebookDTO notebook) {
        notebookService.updateNotebook(notebook);
    }

    @RequestMapping(value = "/getAllNotebooksIds", method = RequestMethod.GET)
    public ModelAndView getAllNotebooks(ModelMap model) {
        Collection<Notebook> notebooks = notebookService.getAllNotebooks();
        List<String> ids = new ArrayList<String>();
        for (Notebook notebook: notebooks) {
            ids.add(notebook.getId());
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("notebookIds", ids);
        return modelAndView;
    }

    @RequestMapping(value = "/getNotebook", method = RequestMethod.GET)
    public ModelAndView getNotebook(ModelMap model) {
        Notebook notebook = notebookService.getNotebook((String)model.get("notebookId"));
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("notebook", notebook);
        return modelAndView;
    }

    @RequestMapping(value = "/deleteNotebook", method = RequestMethod.GET)
    public ModelAndView deleteNotebook(ModelMap model) {
        notebookService.deleteNotebook((String)model.get("notebookId"));
        ModelAndView modelAndView = new ModelAndView();
        return modelAndView;
    }
}