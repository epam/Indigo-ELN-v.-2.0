package com.epam.indigoeln.core.service.experiment;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.exception.ConcurrencyException;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.core.util.WebSocketUtil;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentTreeNodeDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.dto.search.EntitiesIdsDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import com.epam.indigoeln.web.rest.util.permission.helpers.ExperimentPermissionHelper;
import com.google.common.util.concurrent.Striped;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.EXPERIMENT;
import static com.epam.indigoeln.core.util.BatchComponentUtil.*;
import static com.epam.indigoeln.core.util.WebSocketUtil.getRecipients;

/**
 * The ExperimentService provides methods for
 * experiment's data manipulation.
 */
@Service
public class ExperimentService {

    /**
     * Instance of CustomDtoMapper for mapping entity to dto to document.
     */
    @Autowired
    private CustomDtoMapper dtoMapper;

    /**
     * Repository for project's data manipulation.
     */
    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Repository for notebook's data manipulation.
     */
    @Autowired
    private NotebookRepository notebookRepository;

    /**
     * Repository for experiment's data manipulation.
     */
    @Autowired
    private ExperimentRepository experimentRepository;

    /**
     * Repository for component's data manipulation.
     */
    @Autowired
    private ComponentRepository componentRepository;

    /**
     * Repository for file manipulation.
     */
    @Autowired
    private FileRepository fileRepository;

    /**
     * Service for user's data manipulation.
     */
    @Autowired
    private UserService userService;

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private WebSocketUtil webSocketUtil;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Striped<Lock> locks = Striped.lazyWeakLock(2);

    private static List<Experiment> getExperimentsWithAccess(List<Experiment> experiments, User user) {
        return experiments == null ? Collections.emptyList()
                : experiments.stream().filter(experiment -> PermissionUtil.hasUser(
                experiment.getAccessList(), user)).collect(Collectors.toList());

    }

    public Experiment getExperiment(String experimentId) {
        return experimentRepository.findOne(experimentId);
    }

    public void saveExperiment(Experiment experiment) {
        experimentRepository.save(experiment);
    }

    /**
     * Returns all experiments of specified notebook for tree representation.
     *
     * @param projectId  Project's identifier
     * @param notebookId Notebook's identifier
     * @return List with tree representation of experiments of specified notebook
     */
    public List<ExperimentTreeNodeDTO> getAllExperimentTreeNodes(String projectId, String notebookId) {
        return getAllExperimentTreeNodes(projectId, notebookId, null);
    }

    /**
     * Returns all experiments of specified notebook for user for tree representation.
     *
     * @param projectId  Project's identifier
     * @param notebookId Notebook's identifier
     * @param user       User
     * @return List with tree representation of experiments of specified notebook for user
     */
    @SuppressWarnings("unchecked")
    public List<ExperimentTreeNodeDTO> getAllExperimentTreeNodes(String projectId, String notebookId, User user) {
        val notebookCollection = mongoTemplate.getCollection(Notebook.COLLECTION_NAME);
        val experimentCollection = mongoTemplate.getCollection(Experiment.COLLECTION_NAME);
        val userCollection = mongoTemplate.getCollection(User.COLLECTION_NAME);

        val notebook = notebookCollection
                .findOne(new BasicDBObject().append("_id", SequenceIdUtil.buildFullId(projectId, notebookId)));

        if (notebook == null) {
            throw EntityNotFoundException.createWithNotebookId(notebookId);
        }

        if (notebook.get("experiments") instanceof Iterable) {
            val experimentIds = new ArrayList<String>();
            ((Iterable) notebook.get("experiments"))
                    .forEach(e -> experimentIds.add(String.valueOf(String.valueOf(((DBRef) e).getId()))));

            val experiments = new ArrayList<DBObject>();
            experimentCollection.find(new BasicDBObject()
                    .append("_id", new BasicDBObject().append("$in", experimentIds))).forEach(experiments::add);


            Map<Object, Object> experimentsWithUsers = getExperimentsWithUsers(experiments, userCollection);

            List<Experiment> experimentsFromRepository = experimentRepository.findExperimentsByIdIn(experimentIds);

            if (user != null) {
                val notebookAccessList = new HashSet<UserPermission>();
                ((Iterable) notebook.get("accessList"))
                        .forEach(a -> notebookAccessList.add(new UserPermission((DBObject) a)));

                if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, notebookAccessList,
                        UserPermission.READ_ENTITY)) {
                    throw OperationDeniedException
                            .createNotebookSubEntitiesReadOperation(String.valueOf(notebook.get("_id")));
                }
            }

            List<ExperimentTreeNodeDTO> result = experiments.stream()
                    .filter(experiment -> {
                        val experimentAccessList = new HashSet<UserPermission>();
                        ((Iterable) experiment.get("accessList"))
                                .forEach(a -> experimentAccessList.add(new UserPermission((DBObject) a)));
                        return PermissionUtil.hasUser(experimentAccessList, user);
                    })
                    .map(ExperimentTreeNodeDTO::new)
                    .collect(Collectors.toList());

            result.forEach(e -> {
                Optional<BasicDBObject> opt = (Optional<BasicDBObject>) experimentsWithUsers.get(e.getFullId());
                Optional<Experiment> experiment = experimentsFromRepository.stream()
                        .filter(exp -> e.getFullId().equals(exp.getId()))
                        .findFirst();
                if (opt.isPresent()) {
                    e.setAuthorFullName(String.format("%s %s",
                            opt.get().get("first_name"), opt.get().get("last_name")));
                } else {
                    e.setAuthorFullName("");
                }
                String image = "";
                if (experiment.isPresent()) {
                    List<Component> components = experiment.get().getComponents();
                    image = components.get(components.size() - 1).getContent().get("image").toString();
                }
                e.setReactionImage(image);
            });

            result = result.stream().sorted(TreeNodeDTO.NAME_COMPARATOR).collect(Collectors.toList());

            return result;


        }

        return Collections.emptyList();
    }

    public ExperimentTreeNodeDTO getExperimentAsTreeNode(String projectId, String notebookId, String experimentId) {
        String experimentFullId = String.format("%s-%s-%s", projectId, notebookId, experimentId);
        ExperimentTreeNodeDTO result;
        val experimentCollection = mongoTemplate.getCollection(Experiment.COLLECTION_NAME);
        val experiment = experimentCollection
                .findOne(new BasicDBObject().append("_id", experimentFullId));

        if (experiment == null) {
            throw EntityNotFoundException.createWithExperimentId(experimentId);
        }

        result = new ExperimentTreeNodeDTO(experiment);
        Experiment e = experimentRepository.findOne(experimentFullId);
        if (e != null) {
            List<Component> components = e.getComponents();
            result.setReactionImage(components.get(components.size() - 1).getContent().get("image").toString());
            result.setAuthorFullName(e.getAuthor().getFullName());
        }
        return result;
    }


    private Map<Object, Object> getExperimentsWithUsers(List<DBObject> experiments, DBCollection userCollection) {
        List<Object> userIds = new ArrayList<>();
        List<Object> users = new ArrayList<>();
        Map<Object, Object> experimentsWithUsers = new HashMap<>();
        experiments.forEach(e -> {
            Object authorId = ((DBRef) e.get("author")).getId();
            experimentsWithUsers.put(e.get("_id"), authorId);
            userIds.add(authorId);
        });
        userCollection.find(new BasicDBObject().append("_id", new BasicDBObject().append("$in", userIds)))
                .forEach(users::add);

        experimentsWithUsers.entrySet().forEach(e -> e.setValue(users.stream()
                .filter(u -> ((BasicDBObject) u).get("_id").equals(e.getValue())).findFirst()));

        return experimentsWithUsers;
    }

    /**
     * Returns all experiments of specified notebook for user.
     *
     * @param projectId  Project's identifier
     * @param notebookId Notebook's identifier
     * @param user       User
     * @return List with experiments of specified notebook
     */
    public List<ExperimentDTO> getAllExperimentNotebookSummary(String projectId, String notebookId, User user) {
        Collection<Experiment> experiments = getAllExperiments(projectId, notebookId,
                PermissionUtil.isContentEditor(user) ? null : user);
        return experiments.stream()
                .sorted(Comparator.comparing((Function<Experiment, String>) BasicModelObject::getName)
                        .thenComparingInt(Experiment::getExperimentVersion))
                .map(ExperimentDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * If user is null, then retrieve experiments without checking for UserPermissions.
     * Otherwise, use checking for UserPermissions
     *
     * @param projectId  Project's identifier
     * @param notebookId Notebook's identifier
     * @param user       User
     * @return Returns experiments
     */
    private Collection<Experiment> getAllExperiments(String projectId, String notebookId, User user) {
        Notebook notebook = Optional.ofNullable(notebookRepository
                .findOne(SequenceIdUtil.buildFullId(projectId, notebookId))).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId));

        if (user == null) {
            return notebook.getExperiments();
        }

        // Check of EntityAccess (User must have "Read Entity" permission in notebook's access list)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, notebook.getAccessList(),
                UserPermission.READ_ENTITY)) {
            throw OperationDeniedException.createNotebookSubEntitiesReadOperation(notebook.getId());
        }

        return getExperimentsWithAccess(notebook.getExperiments(), user);
    }

    /**
     * Returns experiment with specified experiment's id according to User permissions.
     *
     * @param projectId  Project's identifier
     * @param notebookId Notebook's identifier
     * @param id         Experiment's identifier
     * @param user       User
     * @return Experiment with specified experiment's id
     */
    public ExperimentDTO getExperiment(String projectId, String notebookId, String id, User user) {
        Experiment experiment = Optional
                .ofNullable(experimentRepository.findOne(SequenceIdUtil.buildFullId(projectId, notebookId, id)))
                .orElseThrow(() -> EntityNotFoundException.createWithExperimentId(id));

        // Check of EntityAccess (User must have "Read Entity" permission in notebook's access list and
        // "Read Entity" in experiment's access list, or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Notebook notebook = notebookRepository.findByExperimentId(experiment.getId());
            if (notebook == null) {
                throw EntityNotFoundException.createWithNotebookChildId(experiment.getId());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    notebook.getAccessList(), UserPermission.READ_ENTITY,
                    experiment.getAccessList(), UserPermission.READ_ENTITY)) {
                throw OperationDeniedException.createExperimentReadOperation(experiment.getId());
            }
        }
        return new ExperimentDTO(experiment);
    }

    /**
     * Creates experiment with OWNER's permissions for current user.
     *
     * @param experimentDTO Experiment to create
     * @param projectId     Project's identifier
     * @param notebookId    Notebook's identifier
     * @param user          User
     * @return Created experiment's DTO object
     */
    public ExperimentDTO createExperiment(ExperimentDTO experimentDTO, String projectId,
                                          String notebookId, User user) {
        Project project = projectRepository.findOne(projectId);
        if (project == null) {
            throw EntityNotFoundException.createWithProjectId(projectId);
        }
        Notebook notebook = Optional.ofNullable(notebookRepository
                .findOne(SequenceIdUtil.buildFullId(projectId, notebookId))).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId));

        // check of EntityAccess (User must have "Create Sub-Entity" permission in notebook's access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, notebook.getAccessList(),
                UserPermission.CREATE_SUB_ENTITY)) {
            throw OperationDeniedException.createNotebookSubEntityCreateOperation(notebook.getId());
        }

        Experiment experiment = dtoMapper.convertFromDTO(experimentDTO);
        experiment.setStatus(ExperimentStatus.OPEN);

        if (experimentDTO.getTemplate() != null) {
            Template tmpl = new Template();
            tmpl.setTemplateContent(experimentDTO.getTemplate().getTemplateContent());
            experiment.setTemplate(tmpl);
        }
        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userService, experiment.getAccessList());
        // add OWNER's permissions for specified User to experiment
        Pair<Boolean, Boolean> changes =
                ExperimentPermissionHelper.fillNewExperimentsPermissions(project, notebook, experiment, user);

        //increment sequence Id
        experiment.setId(sequenceIdService.getNextExperimentId(projectId, notebookId));

        experiment.setComponents(updateComponents(null, experiment.getComponents(), experiment.getId()));

        //generate name
        experiment.setName(SequenceIdUtil.generateExperimentName(experiment));

        //set latest version
        experiment.setExperimentVersion(1);
        experiment.setLastVersion(true);
        experiment.compileExperimentFullName(notebook.getName());

        Experiment savedExperiment = experimentRepository.save(experiment);

        notebook.getExperiments().add(savedExperiment);
        Notebook savedNotebook = notebookRepository.save(notebook);
        Set<User> contentEditors = userService.getContentEditors();
        webSocketUtil.updateNotebook(user, projectId, savedNotebook, getRecipients(contentEditors, savedNotebook));

        if (changes.getRight()) {
            Project savedProject = projectRepository.save(project);
            webSocketUtil.updateProject(user, savedProject, getRecipients(contentEditors, savedProject));
        }

        return new ExperimentDTO(savedExperiment);
    }

    /**
     * Creates experiment's version with OWNER's permissions for current user.
     *
     * @param experimentName Experiment's name
     * @param projectId      Project's identifier
     * @param notebookId     Notebook's identifier
     * @param user           User
     * @return Created experiment's version
     */
    public ExperimentDTO versionExperiment(String experimentName, String projectId, String notebookId, User user) {
        if (StringUtils.isEmpty(experimentName)) {
            throw new IllegalArgumentException("Experiment name cannot be null.");
        }
        Project project = projectRepository.findOne(projectId);
        if (project == null) {
            throw EntityNotFoundException.createWithProjectId(projectId);
        }
        Notebook notebook = Optional.ofNullable(notebookRepository
                .findOne(SequenceIdUtil.buildFullId(projectId, notebookId))).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId));

        // check of EntityAccess (User must have "Create Sub-Entity" permission in notebook's access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, notebook.getAccessList(),
                UserPermission.CREATE_SUB_ENTITY)) {
            throw OperationDeniedException.createNotebookSubEntityCreateOperation(notebook.getId());
        }

        notebook.getExperiments().stream()
                .filter(e -> experimentName.equals(e.getName()) && ExperimentStatus.OPEN.equals(e.getStatus()))
                .findAny().ifPresent(e -> {
            throw OperationDeniedException.createVersionExperimentOperation(e.getId());
        });

        // Update previous version
        Experiment lastVersion = notebook.getExperiments().stream().filter(e -> e.isLastVersion()
                && experimentName.equals(e.getName()))
                .findFirst().orElseThrow(() -> EntityNotFoundException.createWithExperimentName(experimentName));
        lastVersion.setLastVersion(false);
        experimentRepository.save(lastVersion);

        int newExperimentVersion = lastVersion.getExperimentVersion() + 1;

        // Save new version
        Experiment newVersion = new Experiment();
        String id;
        if (lastVersion.getExperimentVersion() > 1) {
            id = lastVersion.getId().split("_")[0];
        } else {
            id = lastVersion.getId();
        }
        newVersion.setId(id + "_" + newExperimentVersion);
        newVersion.setName(experimentName);
        newVersion.setAccessList(lastVersion.getAccessList());
        PermissionUtil.addOwnerToAccessList(newVersion.getAccessList(), user, EXPERIMENT);
        newVersion.setTemplate(lastVersion.getTemplate());
        newVersion.setStatus(ExperimentStatus.OPEN);
        final List<Component> components = lastVersion.getComponents();
        components.forEach(c -> c.setId(null));
        final List<Component> newComponents = updateComponents(Collections.emptyList(), components, newVersion.getId());
        newVersion.setComponents(newComponents);
        newVersion.setLastVersion(true);
        newVersion.setExperimentVersion(newExperimentVersion);
        newVersion.compileExperimentFullName(notebook.getName());

        final Experiment savedNewVersion = experimentRepository.save(newVersion);
        notebook.getExperiments().add(savedNewVersion);
        Notebook savedNotebook = notebookRepository.save(notebook);
        webSocketUtil.updateNotebook(user, projectId, savedNotebook,
                getRecipients(userService.getContentEditors(), savedNotebook));

        return new ExperimentDTO(savedNewVersion);
    }

    /**
     * Updates experiment according to permissions.
     *
     * @param projectId     Project's identifier
     * @param notebookId    Notebook's identifier
     * @param experimentDTO Experiment to create
     * @param user          User
     * @return Created experiment's DTO object
     */
    public ExperimentDTO updateExperiment(String projectId, String notebookId, ExperimentDTO experimentDTO, User user) {
        Lock lock = locks.get(projectId);
        ExperimentDTO result;
        try {
            lock.lock();
            Experiment experimentFromDB = Optional.ofNullable(experimentRepository
                    .findOne(SequenceIdUtil.buildFullId(projectId, notebookId, experimentDTO.getId()))).
                    orElseThrow(() -> EntityNotFoundException.createWithExperimentId(experimentDTO.getId()));

            // Check of EntityAccess (User must have "Read Entity" permission in notebook's access list and
            // "Update Entity" in experiment's access list, or must have CONTENT_EDITOR authority)
            checkAccess(user, experimentFromDB);

            Experiment experimentForSave = dtoMapper.convertFromDTO(experimentDTO);

            //Check experiment version before component's saving
            if (!experimentForSave.getVersion().equals(experimentFromDB.getVersion())) {
                throw ConcurrencyException.createWithExperimentName(experimentFromDB.getName(),
                        new IndigoRuntimeException());
            }

            if (experimentFromDB.getStatus() != ExperimentStatus.OPEN) {
                throw OperationDeniedException.createNotOpenExperimentUpdateOperation(experimentFromDB.getId());
            }

            if (experimentDTO.getTemplate() != null) {
                Template tmpl = new Template();
                tmpl.setTemplateContent(experimentDTO.getTemplate().getTemplateContent());
                experimentForSave.setTemplate(tmpl);
            }


            // check of user permissions's correctness in access control list
            PermissionUtil.checkCorrectnessOfAccessList(userService, experimentForSave.getAccessList());

            experimentFromDB.setTemplate(experimentForSave.getTemplate());
            experimentFromDB.setComments(experimentForSave.getComments());
            experimentFromDB.setStatus(experimentForSave.getStatus());
            experimentFromDB.setDocumentId(experimentForSave.getDocumentId());
            experimentFromDB.setSubmittedBy(experimentForSave.getSubmittedBy());
            experimentFromDB.setVersion(experimentForSave.getVersion());

            experimentFromDB.setComponents(updateComponents(experimentFromDB.getComponents(),
                    experimentForSave.getComponents(), experimentFromDB.getId()));

            // add all users as VIEWER to project and to notebook
            Notebook notebook = Optional.ofNullable(notebookRepository.findOne(SequenceIdUtil
                    .buildFullId(projectId, notebookId))).
                    orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId));
            Project project = Optional.ofNullable(projectRepository.findOne(projectId)).
                    orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));

            Pair<Boolean, Boolean> update = ExperimentPermissionHelper.changeExperimentPermissions(
                    project, notebook, experimentFromDB, experimentForSave.getAccessList());

            Experiment savedExperiment;
            try {
                savedExperiment = experimentRepository.save(experimentFromDB);
            } catch (OptimisticLockingFailureException e) {
                throw ConcurrencyException.createWithExperimentName(experimentFromDB.getName(), e);
            }

            Set<User> contentEditors = userService.getContentEditors();
            if (update.getLeft()) {
                Notebook savedNotebook = notebookRepository.save(notebook);
                webSocketUtil.updateNotebook(
                        user, projectId, savedNotebook, getRecipients(contentEditors, savedNotebook));
            }

            if (update.getRight()) {
                Project savedProject = projectRepository.save(project);
                webSocketUtil.updateProject(user, savedProject, getRecipients(contentEditors, savedProject));
            }

            result = new ExperimentDTO(savedExperiment);

            webSocketUtil.updateExperiment(user, projectId, notebookId, savedExperiment,
                    getRecipients(contentEditors, savedExperiment)
                            .filter(userId -> !userId.equals(user.getId())));
        } finally {
            lock.unlock();
        }
        return result;
    }

    private void checkAccess(User user, Experiment experimentFromDB) {
        if (!PermissionUtil.isContentEditor(user)) {
            Notebook notebook = notebookRepository.findByExperimentId(experimentFromDB.getId());
            if (notebook == null) {
                throw EntityNotFoundException.createWithNotebookChildId(experimentFromDB.getId());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    notebook.getAccessList(), UserPermission.READ_ENTITY,
                    experimentFromDB.getAccessList(), UserPermission.UPDATE_ENTITY)) {
                throw OperationDeniedException.createExperimentUpdateOperation(experimentFromDB.getId());
            }
        }
    }

    /**
     * Reopen experiment according to permissions.
     *
     * @param projectId    Project's identifier
     * @param notebookId   Notebook's identifier
     * @param experimentId Experiment's identifier
     * @param version      Experiment's version
     * @param user         User
     * @return Experiment's DTO object
     */
    public ExperimentDTO reopenExperiment(String projectId, String notebookId, String experimentId,
                                          Long version, User user) {
        Lock lock = locks.get(projectId);
        ExperimentDTO result;
        try {
            lock.lock();
            Experiment experimentFromDB = Optional.ofNullable(experimentRepository
                    .findOne(SequenceIdUtil.buildFullId(projectId, notebookId, experimentId))).
                    orElseThrow(() -> EntityNotFoundException.createWithExperimentId(experimentId));

            // Check of EntityAccess (User must have "Read Entity" permission in notebook's access list and
            // "Update Entity" in experiment's access list, or must have CONTENT_EDITOR authority)
            checkAccess(user, experimentFromDB);

            //Check experiment version before component's saving
            if (!version.equals(experimentFromDB.getVersion())) {
                throw ConcurrencyException.createWithExperimentName(experimentFromDB.getName(),
                        new IndigoRuntimeException());
            }

            if (experimentFromDB.getStatus() == ExperimentStatus.OPEN) {
                throw OperationDeniedException.createExperimentReopenOperation(experimentFromDB.getId());
            }

            if (!experimentFromDB.isLastVersion()) {
                throw OperationDeniedException
                        .createExperimentReopenOperation(String.format("You cannot open experiment version %s, use the "
                                        + "latest version for edit.",
                                String.valueOf(experimentFromDB.getExperimentVersion())), experimentFromDB.getId());
            }

            experimentFromDB.setStatus(ExperimentStatus.OPEN);

            Experiment savedExperiment;
            try {
                savedExperiment = experimentRepository.save(experimentFromDB);
            } catch (OptimisticLockingFailureException e) {
                throw ConcurrencyException.createWithExperimentName(experimentFromDB.getName(), e);
            }
            result = new ExperimentDTO(savedExperiment);
            webSocketUtil.updateExperiment(user, projectId, notebookId, savedExperiment,
                    getRecipients(userService.getContentEditors(), savedExperiment)
                            .filter(userName -> !userName.equals(user.getId())));
        } finally {
            lock.unlock();
        }
        return result;
    }

    private List<Component> updateComponents(List<Component> oldComponents, List<Component> newComponents,
                                             String experimentId) {

        List<Component> componentsFromDb = oldComponents != null ? oldComponents : Collections.emptyList();
        List<String> componentIdsForRemove = componentsFromDb.stream().filter(Objects::nonNull).map(Component::getId)
                .collect(Collectors.toList());
        DBRef dbRef = new DBRef(Experiment.COLLECTION_NAME, experimentId);

        List<Component> componentsForSave = new ArrayList<>();
        for (Component component : newComponents) {
            updateRegistrationStatus(component, componentsFromDb);
            if (component.getId() != null) {
                Optional<Component> existing = componentsFromDb.stream().filter(Objects::nonNull)
                        .filter(c -> c.getId().equals(component.getId())).findFirst();
                if (existing.isPresent()) {
                    Component componentForSave = existing.get();
                    componentForSave.setContent(component.getContent());
                    componentIdsForRemove.remove(componentForSave.getId());
                    componentForSave.setName(componentForSave.getName());
                    componentForSave.setExperiment(dbRef);
                    componentsForSave.add(componentForSave);
                } else {
                    throw new ValidationException("Cannot find component with id=" + component.getId());
                }
            } else {
                component.setExperiment(dbRef);
                componentsForSave.add(component);
            }
        }

        componentRepository.delete(
                componentsFromDb.stream()
                        .filter(Objects::nonNull)
                        .filter(c -> componentIdsForRemove.contains(c.getId()))
                        .collect(Collectors.toList()));

        return componentRepository.save(componentsForSave);
    }

    private void updateRegistrationStatus(Component newComponent, List<Component> oldComponents) {
        if (PRODUCT_BATCH_SUMMARY.equals(newComponent.getName())) {
            List<Map<String, Object>> newBatches = BatchComponentUtil
                    .retrieveBatchesFromClient(Collections.singletonList(newComponent));
            List<Map<String, Object>> oldBatches = BatchComponentUtil.retrieveBatchesFromClient(oldComponents);

            for (Map<String, Object> oldBatch : oldBatches) {
                String status = (String) oldBatch.get(COMPONENT_FIELD_REGISTRATION_STATUS);
                if (StringUtils.isNotBlank(status)) {
                    updateBatch(oldBatch, newBatches);
                }
            }

            newBatches.sort(((o1, o2) -> {
                String num1 = (String) o1.get(COMPONENT_FIELD_NBK_BATCH);
                String num2 = (String) o2.get(COMPONENT_FIELD_NBK_BATCH);
                return num1.compareTo(num2);
            }));
            if (!newBatches.isEmpty()) {
                newComponent.getContent().put(COMPONENT_FIELD_BATCHES, newBatches);
            }
        }
    }

    private void updateBatch(Map<String, Object> oldBatch, List<Map<String, Object>> newBatches) {
        String nbkBatch = (String) oldBatch.get(COMPONENT_FIELD_NBK_BATCH);
        Optional<Map<String, Object>> batch = newBatches.stream()
                .filter(b -> b.get(COMPONENT_FIELD_NBK_BATCH) != null
                        && b.get(COMPONENT_FIELD_NBK_BATCH).equals(nbkBatch))
                .findAny();

        if (batch.isPresent()) {
            Map<String, Object> newBatch = batch.get();
            newBatch.clear();
            newBatch.putAll(oldBatch);
        } else {
            newBatches.add(oldBatch);
        }
    }

    /**
     * Removes experiment.
     *
     * @param id         Experiment's identifier
     * @param projectId  Project's identifier
     * @param notebookId Notebook's identifier
     */
    public void deleteExperiment(String id, String projectId, String notebookId) {
        Experiment experiment = Optional.ofNullable(experimentRepository
                .findOne(SequenceIdUtil.buildFullId(projectId, notebookId, id))).
                orElseThrow(() -> EntityNotFoundException.createWithExperimentId(id));

        Notebook notebook = Optional.ofNullable(notebookRepository.findOne(notebookId)).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookChildId(experiment.getId()));

        notebook.getExperiments().remove(experiment);
        notebookRepository.save(notebook);

        //delete experiment components
        Optional.ofNullable(experiment.getComponents()).ifPresent(components ->
                componentRepository.deleteAllById(components.stream()
                        .map(Component::getId).collect(Collectors.toList()))
        );

        fileRepository.delete(experiment.getFileIds());
        experimentRepository.delete(experiment);
    }

    /**
     * Returns lock for project
     *
     * @param projectId project id
     * @return lock for project
     */
    public Lock getLock(String projectId) {
        return locks.get(projectId);
    }

    /**
     * Searches experiments by full name with project name and version
     *
     * @param experimentFullName experiment full name
     * @param pageable           page, size, sort settings
     * @return ids list of project, notebook, experiment and experiment full name
     */
    public List<EntitiesIdsDTO> findExperimentsByFullName(User user, String experimentFullName, Pageable pageable) {
        List<Experiment> experiments = PermissionUtil.isContentEditor(user)
                ? experimentRepository.findExperimentsByFullNameStartingWith(experimentFullName, pageable)
                : experimentRepository.findExperimentsByFullNameStartingWithAndHasAccess(
                experimentFullName,
                user.getId(),
                UserPermission.READ_ENTITY,
                pageable);

        return experiments.stream()
                .map(EntitiesIdsDTO::new)
                .collect(Collectors.toList());
    }

    public void deleteAll() {
        experimentRepository.deleteAll();
    }
}
