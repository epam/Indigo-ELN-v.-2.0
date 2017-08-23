package com.epam.indigoeln.core.util;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebSocketUtil {

    private final SimpMessagingTemplate template;
    private static final String PROJECT_ID = "projectId";
    private static final String NOTEBOOK_ID = "notebookId";
    private static final String EXPERIMENT_ID = "";
    private static final String ENTITY = "entity";
    private static final String USER = "user";
    private static final String VERSION = "version";
    private static final String DESTINATION = "/topic/entity_updated";

    @Autowired
    public WebSocketUtil(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void updateProject(User user, Project project) {
        Map<String, Object> data = new HashMap<>();
        data.put(USER, user.getId());
        Map<String, Object> entity = new HashMap<>();
        entity.put(PROJECT_ID, project.getId());
        data.put(ENTITY, entity);
        data.put(VERSION, project.getVersion());
        template.convertAndSend(DESTINATION, data);
    }

    public void updateNotebook(User user, String projectId, Notebook notebook){
        Map<String, Object> data = new HashMap<>();
        data.put(USER, user.getId());
        Map<String, Object> entity = new HashMap<>();
        entity.put(PROJECT_ID, projectId);
        entity.put(NOTEBOOK_ID, SequenceIdUtil.extractShortId(notebook));
        data.put(ENTITY, entity);
        data.put(VERSION, notebook.getVersion());
        template.convertAndSend(DESTINATION, data);
    }

    public void updateExperiment(User user, String projectId, String notebookId, Experiment experiment){
        Map<String, Object> data = new HashMap<>();
        data.put(USER, user.getId());
        Map<String, Object> entity = new HashMap<>();
        entity.put(PROJECT_ID, projectId);
        entity.put(NOTEBOOK_ID, notebookId);
        entity.put(EXPERIMENT_ID, SequenceIdUtil.extractShortId(experiment));
        data.put(ENTITY, entity);
        data.put(VERSION, experiment.getVersion());
        template.convertAndSend(DESTINATION, data);
    }
}
