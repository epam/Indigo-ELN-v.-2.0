package com.epam.indigoeln.web.controller;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequestMapping("/service")
@Controller("notebookController")
public class NotebookController {
    @Autowired
    private NotebookService service;

    @RequestMapping(value = "/createNewNotebook", method = RequestMethod.GET)
    public ModelAndView createNewNotebook(ModelMap model) {
        Notebook notebook = service.createNewNotebook((Notebook)model.get("notebook"));
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("notebook", notebook);
        return modelAndView;
    }

    @RequestMapping(value = "/getAllNotebooksIds", method = RequestMethod.GET)
    public ModelAndView getAllNotebooks(ModelMap model) {
        Collection<Notebook> notebooks = service.getAllNotebooks();
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
        Notebook notebook = service.getNotebook((String)model.get("notebookId"));
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("notebook", notebook);
        return modelAndView;
    }

    @RequestMapping(value = "/updateNotebook", method = RequestMethod.GET)
    public ModelAndView updateNotebook(ModelMap model) {
        Notebook notebook = service.updateNotebook((Notebook)model.get("notebook"));
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("notebook", notebook);
        return modelAndView;
    }

    @RequestMapping(value = "/deleteNotebook", method = RequestMethod.GET)
    public ModelAndView deleteNotebook(ModelMap model) {
        service.deleteNotebook((String)model.get("notebookId"));
        ModelAndView modelAndView = new ModelAndView();
        return modelAndView;
    }
}