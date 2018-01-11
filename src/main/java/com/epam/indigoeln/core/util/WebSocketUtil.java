package com.epam.indigoeln.core.util;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for getting host name and updating entities.
 */
@Component
public class WebSocketUtil {

    private final SimpMessagingTemplate template;
    private static final String PROJECT_ID = "projectId";
    private static final String NOTEBOOK_ID = "notebookId";
    private static final String EXPERIMENT_ID = "experimentId";
    private static final String ENTITY = "entity";
    private static final String USER = "user";
    private static final String VERSION = "version";
    private static final String ENTITY_DESTINATION = "/topic/entity_updated";
    private static final String USER_DESTINATION = "/queue/user_permissions_changed";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketUtil.class);

    @Autowired
    public WebSocketUtil(SimpMessagingTemplate template) {
        this.template = template;
    }

    /**
     * Updates project.
     *
     * @param user    User
     * @param project Project
     */
    public void updateProject(User user, Project project) {
        Map<String, Object> data = new HashMap<>();
        data.put(USER, user.getId());
        Map<String, Object> entity = new HashMap<>();
        entity.put(PROJECT_ID, project.getId());
        data.put(ENTITY, entity);
        data.put(VERSION, project.getVersion());
        template.convertAndSend(ENTITY_DESTINATION, data);
    }

    /**
     * Updates notebook.
     *
     * @param user      User
     * @param projectId Project's id
     * @param notebook  Notebook
     */
    public void updateNotebook(User user, String projectId, Notebook notebook) {
        Map<String, Object> data = new HashMap<>();
        data.put(USER, user.getId());
        Map<String, Object> entity = new HashMap<>();
        entity.put(PROJECT_ID, projectId);
        entity.put(NOTEBOOK_ID, SequenceIdUtil.extractShortId(notebook));
        data.put(ENTITY, entity);
        data.put(VERSION, notebook.getVersion());
        template.convertAndSend(ENTITY_DESTINATION, data);
    }

    /**
     * Updates experiment.
     *
     * @param user       User
     * @param projectId  Project's id
     * @param notebookId Notebook's id
     * @param experiment Experiment
     */
    public void updateExperiment(User user, String projectId, String notebookId, Experiment experiment) {
        Map<String, Object> data = new HashMap<>();
        data.put(USER, user.getId());
        Map<String, Object> entity = new HashMap<>();
        entity.put(PROJECT_ID, projectId);
        entity.put(NOTEBOOK_ID, notebookId);
        entity.put(EXPERIMENT_ID, SequenceIdUtil.extractShortId(experiment));
        data.put(ENTITY, entity);
        data.put(VERSION, experiment.getVersion());
        template.convertAndSend(ENTITY_DESTINATION, data);
    }

    /**
     * Updates user.
     *
     * @param login User's login
     */
    public void updateUser(String login) {
        template.convertAndSendToUser(login, USER_DESTINATION,
                Collections.singletonMap("message", "User's permissions were changed. User should relogin"));
    }

    /**
     * Returns host name.
     *
     * @return Host name
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            LOGGER.trace("Error getting host name", e);
            return "Unknown";
        }
    }
}