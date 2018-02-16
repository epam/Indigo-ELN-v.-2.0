package com.epam.indigoeln.core.service.dashboard;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.config.DashboardProperties;
import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.service.signature.SignatureService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.DashboardResource;
import com.epam.indigoeln.web.rest.dto.DashboardDTO;
import com.epam.indigoeln.web.rest.dto.DashboardRowDTO;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardResource.class);

    private static final String EXCEPTION_MESSAGE = "Unable to get list of documents from signature service.";

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private DashboardProperties dashboardProperties;

    @Autowired
    private UserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Create the current user's dashboard information.
     *
     * @return the current user's dashboard information
     */
    @SuppressWarnings("unchecked")
    public DashboardDTO getDashboard() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REST request to get dashboard experiments");
        }

        val dashboardDTO = new DashboardDTO();

        // Necessary collections

        val projectCollection = mongoTemplate.getCollection(Project.COLLECTION_NAME);
        val notebookCollection = mongoTemplate.getCollection(Notebook.COLLECTION_NAME);
        val experimentCollection = mongoTemplate.getCollection(Experiment.COLLECTION_NAME);
        val componentCollection = mongoTemplate.getCollection(Component.COLLECTION_NAME);
        val userCollection = mongoTemplate.getCollection(User.COLLECTION_NAME);

        // Current user and date threshold

        val user = userService.getUserWithAuthorities();

        val zonedThreshold = ZonedDateTime.now()
                .minus(dashboardProperties.getThresholdLevel(), dashboardProperties.getThresholdUnit());
        val threshold = new Date(zonedThreshold.toEpochSecond() * 1000);

        // Load all necessary entities

        val experiments = new HashMap<Object, DBObject>();
        experimentCollection
                .find(new BasicDBObject("$or", Arrays.asList(
                        new BasicDBObject("author.$id", user.getId()),
                        new BasicDBObject("submittedBy.$id", user.getId()))))
                .forEach(e -> experiments.put(e.get("_id"), e));

        val notebooks = new HashMap<Object, DBObject>();
        notebookCollection
                .find(new BasicDBObject("experiments.$id", new BasicDBObject("$in", experiments.keySet())))
                .forEach(n -> notebooks.put(n.get("_id"), n));

        val projects = new HashMap<Object, DBObject>();
        projectCollection
                .find(new BasicDBObject("notebooks.$id", new BasicDBObject("$in", notebooks.keySet())))
                .forEach(p -> projects.put(p.get("_id"), p));

        val componentIds = new HashSet<Object>();
        experiments.values().forEach(e -> ((Iterable) e.get("components")).forEach(c -> componentIds.add(((DBRef) c).getId())));

        val components = new HashMap<Object, DBObject>();
        componentCollection
                .find(new BasicDBObject()
                        .append("_id", new BasicDBObject("$in", componentIds))
                        .append("$or", Arrays.asList(
                                new BasicDBObject("name", "reactionDetails"),
                                new BasicDBObject("name", "conceptDetails"))))
                .forEach(c -> components.put(c.get("_id"), c));

        val userIds = new HashSet<Object>();
        experiments.values()
                .forEach(e -> {
                    if (e.get("author") != null) {
                        userIds.add(((DBRef) e.get("author")).getId());
                    }
                    if (e.get("submittedBy") != null) {
                        userIds.add(((DBRef) e.get("submittedBy")).getId());
                    }
                });
        components.values()
                .forEach(c -> {
                    if (c.get("content") != null && ((DBObject) c.get("content")).get("coAuthors") != null) {
                        ((Iterable) ((DBObject) c.get("content")).get("coAuthors")).forEach(userIds::add);
                    }
                });

        val users = new HashMap<Object, DBObject>();
        userCollection.find(new BasicDBObject("_id", new BasicDBObject("$in", userIds))).forEach(u -> users.put(u.get("_id"), u));

        // Fill dashboard

        dashboardDTO.setOpenAndCompletedExp(openExperiments(threshold, user, projects, notebooks, experiments, components, users));
        dashboardDTO.setWaitingSignatureExp(waitingExperiments(user, projects, notebooks, experiments, components, users));
        dashboardDTO.setSubmittedAndSigningExp(submittedExperiments(threshold, user, projects, notebooks, experiments, components, users));

        return dashboardDTO;
    }

    /**
     * Get opened experiments for current user.
     *
     * @param threshold   date threshold
     * @param user        current user
     * @param projects    current user projects
     * @param notebooks   current user notebooks
     * @param experiments current user experiments
     * @param components  necessary components
     * @param users       necessary users
     * @return opened experiments for current user.
     */
    private List<DashboardRowDTO> openExperiments(Date threshold,
                                                  User user,
                                                  Map<Object, DBObject> projects,
                                                  Map<Object, DBObject> notebooks,
                                                  Map<Object, DBObject> experiments,
                                                  Map<Object, DBObject> components,
                                                  Map<Object, DBObject> users) {
        val openExperiments = new HashMap<Object, DBObject>();
        experiments
                .forEach((k, v) -> {
                    if (threshold.before((Date) v.get("creationDate")) && "open".equalsIgnoreCase(String.valueOf(v.get("status")))) {
                        openExperiments.put(k, v);
                    }
                });

        return openExperiments.values().stream()
                .map(e -> getEntities(e, notebooks, projects))
                .filter(t -> hasAccess(user, t))
                .map(t -> convert(t, components, users, null))
                .collect(Collectors.toList());
    }


    /**
     * Get experiments which are waiting for user's signature.
     *
     * @param user        current user
     * @param projects    current user projects
     * @param notebooks   current user notebooks
     * @param experiments current user experiments
     * @param components  necessary components
     * @param users       necessary users
     * @return experiments which are waiting for user's signature
     */
    private List<DashboardRowDTO> waitingExperiments(User user,
                                                     Map<Object, DBObject> projects,
                                                     Map<Object, DBObject> notebooks,
                                                     Map<Object, DBObject> experiments,
                                                     Map<Object, DBObject> components,
                                                     Map<Object, DBObject> users) {
        val waitingDocuments = new HashMap<String, SignatureService.Document>();
        try {
            signatureService.getDocumentsByUser().stream()
                    .filter(d -> d.isActionRequired()
                            && (d.getStatus() == SignatureService.ISSStatus.SIGNING
                            || d.getStatus() == SignatureService.ISSStatus.SUBMITTED))
                    .forEach(d -> waitingDocuments.put(d.getId(), d));
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_MESSAGE, e);
            throw new IndigoRuntimeException(EXCEPTION_MESSAGE);
        }

        val waitingExperiments = new HashMap<Object, DBObject>();
        experiments
                .forEach((k, v) -> {
                    if (v.get("documentId") != null && waitingDocuments.containsKey(v.get("documentId"))) {
                        waitingExperiments.put(k, v);
                    }
                });

        return waitingExperiments.values().stream()
                .map(e -> getEntities(e, notebooks, projects))
                .filter(t -> hasAccess(user, t))
                .map(t -> convert(t, components, users, waitingDocuments))
                .collect(Collectors.toList());
    }

    /**
     * Get submitted experiments for current user.
     *
     * @param threshold   date threshold
     * @param user        current user
     * @param projects    current user projects
     * @param notebooks   current user notebooks
     * @param experiments current user experiments
     * @param components  necessary components
     * @param users       necessary users
     * @return submitted experiments for current user.
     */
    private List<DashboardRowDTO> submittedExperiments(Date threshold,
                                                       User user,
                                                       Map<Object, DBObject> projects,
                                                       Map<Object, DBObject> notebooks,
                                                       Map<Object, DBObject> experiments,
                                                       Map<Object, DBObject> components,
                                                       Map<Object, DBObject> users) {
        val submittedExperiments = new HashMap<Object, DBObject>();
        experiments
                .forEach((k, v) -> {
                    if (threshold.before((Date) v.get("creationDate")) && v.get("documentId") != null && !"open".equals(v.get("status")) && !"completed".equals(v.get("status"))) {
                        submittedExperiments.put(k, v);
                    }
                });

        val submittedDocuments = new HashMap<String, SignatureService.Document>();
        try {
            signatureService.getDocumentsByIds(submittedExperiments.values().stream().map(e -> String.valueOf(e.get("documentId"))).collect(Collectors.toList()))
                    .forEach(d -> submittedDocuments.put(d.getId(), d));
        } catch (IOException e) {
            LOGGER.error(EXCEPTION_MESSAGE, e);
            throw new IndigoRuntimeException(EXCEPTION_MESSAGE);
        }

        return submittedExperiments.values().stream()
                .map(e -> getEntities(e, notebooks, projects))
                .filter(t -> hasAccess(user, t))
                .map(t -> convert(t, components, users, submittedDocuments))
                .collect(Collectors.toList());
    }

    /**
     * Convert experiment to dashboard row.
     *
     * @param t          triple project-notebook-experiment
     * @param components necessary components
     * @param users      necessary users
     * @param documents  documents for this experiment
     * @return dashboard row for experiment
     */
    @SuppressWarnings("unchecked")
    private DashboardRowDTO convert(
            Triple<DBObject, DBObject, DBObject> t,
            Map<Object, DBObject> components,
            Map<Object, DBObject> users,
            Map<String, SignatureService.Document> documents) {
        val project = t.getLeft();
        val notebook = t.getMiddle();
        val experiment = t.getRight();

        val result = new DashboardRowDTO();

        result.setProjectId(SequenceIdUtil.extractShortId(String.valueOf(project.get("_id"))));
        result.setNotebookId(SequenceIdUtil.extractShortId(String.valueOf(notebook.get("_id"))));
        result.setExperimentId(SequenceIdUtil.extractShortId(String.valueOf(experiment.get("_id"))));
        result.setId(notebook.get("name") + "-" + experiment.get("name"));
        result.setStatus(ExperimentStatus.fromValue(String.valueOf(experiment.get("status"))));
        result.setProject(String.valueOf(project.get("name")));
        result.setCreationDate(ZonedDateTime.ofInstant(((Date) experiment.get("creationDate")).toInstant(), ZoneOffset.UTC));
        result.setLastEditDate(ZonedDateTime.ofInstant(((Date) experiment.get("lastEditDate")).toInstant(), ZoneOffset.UTC));

        Iterable coAuthors = null;
        String title = null;
        for (val c : (Iterable) experiment.get("components")) {
            val component = components.get(((DBRef) c).getId());
            if (component != null) {
                if ("reactionDetails".equals(component.get("name")) || "conceptDetails".equals(component.get("name"))) {
                    val content = (DBObject) component.get("content");
                    if (content != null) {
                        title = content.get("title") == null ? null : String.valueOf(content.get("title"));
                        coAuthors = (Iterable) content.get("coAuthors");
                    }
                    break;
                }
            }
        }
        result.setName(title);

        if (documents != null && experiment.get("documentId") != null) {
            val document = documents.get(experiment.get("documentId"));
            if (document != null) {
                result.setWitnesses(
                        document.getWitnesses().stream()
                                .map(u -> new DashboardRowDTO.UserDTO(u.getFirstName(), u.getLastName()))
                                .collect(Collectors.toList()));

                result.setComments(
                        document.getWitnesses().stream()
                                .map(SignatureService.User::getComment)
                                .collect(Collectors.toList()));
            }
        }

        if (experiment.get("author") != null) {
            val author = users.get(((DBRef) experiment.get("author")).getId());
            if (author != null) {
                result.setAuthor(new DashboardRowDTO.UserDTO(String.valueOf(author.get("first_name")), String.valueOf(author.get("last_name"))));
            }
        }

        if (experiment.get("submittedBy") != null) {
            val submittedBy = users.get(((DBRef) experiment.get("submittedBy")).getId());
            if (submittedBy != null) {
                result.setSubmitter(new DashboardRowDTO.UserDTO(String.valueOf(submittedBy.get("first_name")), String.valueOf(submittedBy.get("last_name"))));
            }
        }

        if (coAuthors != null) {
            val ca = new ArrayList<String>();

            coAuthors.forEach(c -> {
                if (c != null) {
                    ca.add(String.valueOf(c));
                }
            });

            result.setCoAuthors(ca);
        }

        return result;
    }

    /**
     * Get notebook and project for experiment.
     *
     * @param experiment experiment
     * @param notebooks  all notebooks
     * @param projects   all projects
     * @return triple with notebook and project for experiment
     */
    @SuppressWarnings("unchecked")
    private Triple<DBObject, DBObject, DBObject> getEntities(DBObject experiment, Map<Object, DBObject> notebooks, Map<Object, DBObject> projects) {
        val notebook = experiment == null ? null : notebooks.values().stream()
                .filter(n -> {
                    for (val e : (Iterable) n.get("experiments")) {
                        if (experiment.get("_id").equals(((DBRef) e).getId())) {
                            return true;
                        }
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);

        val project = notebook == null ? null : projects.values().stream()
                .filter(p -> {
                    for (val n : (Iterable) p.get("notebooks")) {
                        if (notebook.get("_id").equals(((DBRef) n).getId())) {
                            return true;
                        }
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);

        return Triple.of(project, notebook, experiment);
    }

    /**
     * Check user access for project, notebook and experiment.
     *
     * @param user current user
     * @param t    triple project-notebook-experiment
     * @return true if user has access to each of project/notebook/experiment
     */
    @SuppressWarnings("unchecked")
    private boolean hasAccess(User user, Triple<DBObject, DBObject, DBObject> t) {
        val project = t.getLeft();
        val notebook = t.getMiddle();
        val experiment = t.getRight();

        if (project == null) {
            return false;
        }
        val projectAccessList = new HashSet<UserPermission>();
        ((Iterable) project.get("accessList")).forEach(a -> projectAccessList.add(new UserPermission((DBObject) a)));

        if (notebook == null) {
            return false;
        }
        val notebookAccessList = new HashSet<UserPermission>();
        ((Iterable) notebook.get("accessList")).forEach(a -> notebookAccessList.add(new UserPermission((DBObject) a)));

        if (experiment == null) {
            return false;
        }
        val experimentAccessList = new HashSet<UserPermission>();
        ((Iterable) experiment.get("accessList")).forEach(a -> experimentAccessList.add(new UserPermission((DBObject) a)));

        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, projectAccessList, UserPermission.READ_ENTITY)) {
            return false;
        }

        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, notebookAccessList, UserPermission.READ_ENTITY)) {
            return false;
        }

        return PermissionUtil.hasEditorAuthorityOrPermissions(user, experimentAccessList, UserPermission.READ_ENTITY);
    }
}
