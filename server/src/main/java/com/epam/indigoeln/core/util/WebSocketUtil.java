/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.util;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.web.rest.util.permission.helpers.PermissionChanges;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;

/**
 * Utility class for getting host name and updating entities.
 */
@Slf4j
@Component
public class WebSocketUtil {

    private static final long SLEEP_MILLIS = 100;

    private final SimpMessagingTemplate template;

    private static final String PROJECT_ID = "projectId";
    private static final String NOTEBOOK_ID = "notebookId";
    private static final String EXPERIMENT_ID = "experimentId";
    private static final String ENTITY = "entity";
    private static final String USER = "user";
    private static final String VERSION = "version";
    private static final String ENTITY_DESTINATION = "/topic/entity_updated";
    private static final String SUB_ENTITY_CHANGES = "/topic/sub_entity_changes";
    private static final String USER_DESTINATION = "/queue/user_permissions_changed";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketUtil.class);

    @Autowired
    public WebSocketUtil(SimpMessagingTemplate template) {
        this.template = template;
    }


    public void newProject(User user, Stream<String> recipients) {
        new Thread(getNewProjectNotification(user, recipients)).start();
    }

    /**
     * Returns sub_entity_changes notification for root of the entity tree.
     *
     * @param user       Author of changes
     * @param recipients user-names of recipients
     * @return sub_entity_changes notification for root of the entity tree
     */
    public DelayedNotificationRunnable getNewProjectNotification(User user, Stream<String> recipients) {
        return new DelayedNotificationRunnable(() -> {
            Map<String, Object> data = new HashMap<>();
            data.put(USER, user.getId());
            data.put(ENTITY, null);
            recipients.forEach(userName -> template.convertAndSendToUser(userName, SUB_ENTITY_CHANGES, data));
        });
    }

    /**
     * Returns entity_updated notification for project.
     *
     * @param user       User
     * @param project    Project
     * @param recipients user-names of recipients
     * @return entity_updated notification for project
     */
    public DelayedNotificationRunnable getUpdateProjectNotification(User user, Project project, Stream<String> recipients) {
        return getProjectNotification(user, project, recipients, ENTITY_DESTINATION);
    }

    /**
     * Returns sub_entity_changes notification for project.
     *
     * @param user       User
     * @param project    Project
     * @param recipients user-names of recipients
     * @return sub_entity_changes notification for project
     */
    public DelayedNotificationRunnable getProjectSubEntityNotification(User user, Project project, Stream<String> recipients) {
        return getProjectNotification(user, project, recipients, SUB_ENTITY_CHANGES);
    }

    private DelayedNotificationRunnable getProjectNotification(User user,
                                                               Project project,
                                                               Stream<String> recipients,
                                                               String notificationDestination
    ) {
        return new DelayedNotificationRunnable(() -> {
            Map<String, Object> data = new HashMap<>();
            data.put(USER, user.getId());
            Map<String, Object> entity = new HashMap<>();
            entity.put(PROJECT_ID, project.getId());
            data.put(ENTITY, entity);
            data.put(VERSION, project.getVersion());
            recipients.forEach(userName -> template.convertAndSendToUser(userName, notificationDestination, data));
        });
    }

    /**
     * Returns entity_updated notification for notebook.
     *
     * @param user       User
     * @param projectId  Project's id
     * @param notebook   Notebook
     * @param recipients user-names of recipients
     */
    public DelayedNotificationRunnable getUpdateNotebookNotification(User user, String projectId, Notebook notebook, Stream<String> recipients) {
        return getNotebookNotification(user, projectId, notebook, recipients, ENTITY_DESTINATION);
    }

    /**
     * Returns SUB_ENTITY_CHANGES notification for notebook.
     *
     * @param user       User
     * @param projectId  Project's id
     * @param notebook   Notebook
     * @param recipients user-names of recipients
     */
    public DelayedNotificationRunnable getNotebookSubEntityNotification(User user,
                                                                        String projectId,
                                                                        Notebook notebook,
                                                                        Stream<String> recipients
    ) {
        return getNotebookNotification(user, projectId, notebook, recipients, SUB_ENTITY_CHANGES);
    }

    private DelayedNotificationRunnable getNotebookNotification(User user,
                                                                String projectId,
                                                                Notebook notebook,
                                                                Stream<String> recipients,
                                                                String publishAddress
    ) {
        return new DelayedNotificationRunnable(() -> {
            Map<String, Object> data = new HashMap<>();
            data.put(USER, user.getId());
            Map<String, Object> entity = new HashMap<>();
            entity.put(PROJECT_ID, projectId);
            entity.put(NOTEBOOK_ID, SequenceIdUtil.extractShortId(notebook));
            data.put(ENTITY, entity);
            data.put(VERSION, notebook.getVersion());
            recipients.forEach(userName -> template.convertAndSendToUser(userName, publishAddress, data));
        });
    }


    /**
     * Updates experiment.
     *
     * @param user       User
     * @param projectId  Project's id
     * @param notebookId Notebook's id
     * @param experiment Experiment
     * @param recipients user-names of recipients
     */
    public void updateExperiment(User user,
                                 String projectId,
                                 String notebookId,
                                 Experiment experiment,
                                 Stream<String> recipients
    ) {
        new Thread(
                getUpdateExperimentNotification(user, projectId, notebookId, experiment, recipients))
                .start();
    }

    /**
     * Returns entity_updated notification for  experiment.
     *
     * @param user       User
     * @param projectId  Project's id
     * @param notebookId Notebook's id
     * @param experiment Experiment
     * @param recipients user-names of recipients
     */
    public DelayedNotificationRunnable getUpdateExperimentNotification(User user,
                                                                       String projectId,
                                                                       String notebookId,
                                                                       Experiment experiment,
                                                                       Stream<String> recipients
    ) {
        return new DelayedNotificationRunnable(() -> {
            Map<String, Object> data = new HashMap<>();
            data.put(USER, user.getId());
            Map<String, Object> entity = new HashMap<>();
            entity.put(PROJECT_ID, projectId);
            entity.put(NOTEBOOK_ID, SequenceIdUtil.extractShortId(notebookId));
            entity.put(EXPERIMENT_ID, SequenceIdUtil.extractShortId(experiment));
            data.put(ENTITY, entity);
            data.put(VERSION, experiment.getVersion());
            recipients.forEach(userName -> template.convertAndSendToUser(userName, ENTITY_DESTINATION, data));
        });
    }

    /**
     * Updates user.
     *
     * @param login User's login
     */
    public void updateUser(String login) {
        new Thread(new DelayedNotificationRunnable(() -> template.convertAndSendToUser(login, USER_DESTINATION,
                singletonMap("message", "User's permissions were changed. User should relogin")))).start();
    }


    /**
     * User names to notify about entity updates.
     *
     * @param contentEditors users with CONTENT_EDITOR authorities.
     * @param updatedEntity  updated entity
     * @param exceptUser     user name of user that shouldn't be notified
     * @return stream of user-names that should be notified about updatedEntity changing
     */
    public static Stream<String> getEntityUpdateRecipients(Set<User> contentEditors, BasicModelObject updatedEntity, String exceptUser) {
        return Stream.concat(
                contentEditors.stream(),
                updatedEntity.getAccessList().stream().map(UserPermission::getUser)
        )
                .map(User::getId)
                .distinct()
                .filter(userName -> !userName.equals(exceptUser));
    }

    /**
     * User names to notify about added or removed sub-entity for them.
     *
     * @param permissionChanges permission changes and entity
     * @return stream of user-names that should be notified about permissionChanges
     */
    public static Stream<String> getSubEntityChangesRecipients(
            PermissionChanges<?> permissionChanges
    ) {
        return getSubEntityChangesRecipients(permissionChanges, emptySet());
    }

    /**
     * User names to notify about added or removed sub-entity for them.
     *
     * @param permissionChanges permission changes and entity
     * @param contentEditors    users with CONTENT_EDITOR authorities.
     * @return stream of user-names that should be notified about permissionChanges
     */
    public static Stream<String> getSubEntityChangesRecipients(
            PermissionChanges<?> permissionChanges, Set<User> contentEditors
    ) {
        return Stream.concat(
                contentEditors.stream(),
                Stream.concat(
                        permissionChanges.getNewPermissions().stream().map(UserPermission::getUser),
                        permissionChanges.getRemovedPermissions().stream().map(UserPermission::getUser)))
                .map(User::getId)
                .distinct();
    }

    /**
     * User names to notify about added or removed sub-entity for them.
     *
     * @param permissionChanges permission changes and entity
     * @return stream of user-names that should be notified about permissionChanges
     */
    public static <E extends BasicModelObject> Stream<String> getSubEntityChangesRecipients(
            Collection<PermissionChanges<E>> permissionChanges
    ) {
        return getSubEntityChangesRecipients(permissionChanges, emptySet());
    }

    /**
     * User names to notify about added or removed sub-entity for them.
     *
     * @param permissionChanges permission changes and entity
     * @param contentEditors    users with CONTENT_EDITOR authorities.
     * @return stream of user-names that should be notified about permissionChanges
     */
    public static <E extends BasicModelObject> Stream<String> getSubEntityChangesRecipients(
            Collection<PermissionChanges<E>> permissionChanges, Set<User> contentEditors
    ) {
        return Stream.concat(
                contentEditors.stream(),
                Stream.concat(
                        permissionChanges.stream()
                                .flatMap(e -> e.getNewPermissions().stream()),
                        permissionChanges.stream()
                                .flatMap(e -> e.getRemovedPermissions().stream())
                )
                        .map(UserPermission::getUser))
                .map(User::getId)
                .distinct();

    }

    /**
     * Send all notifications.
     *
     * @param notifications notifications to send
     */
    public void sendAll(List<DelayedNotificationRunnable> notifications) {
        notifications.forEach(runnable -> new Thread(runnable).start());
    }

    public static class DelayedNotificationRunnable implements Runnable {

        private final Runnable runnable;

        private DelayedNotificationRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
                log.warn("Thread sleep was interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
            runnable.run();
        }
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
