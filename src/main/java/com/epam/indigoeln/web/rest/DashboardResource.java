package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.config.DashboardProperties;
import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.print.itext2.utils.MongoExt;
import com.epam.indigoeln.core.service.signature.SignatureService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.DashboardDTO;
import com.epam.indigoeln.web.rest.dto.DashboardRowDTO;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import com.mongodb.DBRef;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.indigoeln.core.util.BatchComponentUtil.REACTION_DETAILS;

@Api
@RestController
@RequestMapping(DashboardResource.URL_MAPPING)
public class DashboardResource {

    static final String URL_MAPPING = "/api/dashboard";

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardResource.class);

    @Autowired
    private ExperimentRepository experimentRepository;
    @Autowired
    private NotebookRepository notebookRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private SignatureService signatureService;
    @Autowired
    private UserService userService;
    @Autowired
    private DashboardProperties dashboardProperties;

    /**
     * GET  /dashboard -> Returns dashboard experiments
     * 1. Experiments created by current user during one month which are in one of following statuses: 'Open', 'Completed'
     */
    @ApiOperation(value = "Returns dashboard content.", produces = "application/json")
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DashboardDTO> getDashboard() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REST request to get dashboard experiments");
        }

        User user = userService.getUserWithAuthorities();
        DashboardDTO dashboardDTO = new DashboardDTO();

        ZonedDateTime threshold = ZonedDateTime.now().minus(dashboardProperties.getThresholdLevel(), dashboardProperties.getThresholdUnit());

        dashboardDTO.setOpenAndCompletedExp(getCurrentRows(user, threshold));
        dashboardDTO.setWaitingSignatureExp(getWaitingRows());
        dashboardDTO.setSubmittedAndSigningExp(getSubmittedRows(user, threshold));

        return ResponseEntity.ok(dashboardDTO);
    }

    private List<DashboardRowDTO> getCurrentRows(User user, ZonedDateTime threshold) {
        // Open and Completed Experiments
        Map<String, Experiment>  experiments = experimentRepository.findByAuthorAndStatusAndCreationDateAfter(user, ExperimentStatus.OPEN, threshold)
                .collect(Collectors.toMap(Experiment::getId, Function.identity()));

        return getEntities(experiments)
                .filter(t -> hasAccess(user, t))
                .map(t -> convert(t, null))
                .collect(Collectors.toList());
    }

    private Stream<Triple<Project, Notebook, Experiment>> getEntities(Map<String, Experiment>  experiments) {
        List<DBRef> experimentIds = experiments.keySet().stream()
                .map(k -> new DBRef("experiment", k))
                .collect(Collectors.toList());

        Map<String, Notebook> notebooks = notebookRepository.findByExperimentsIds(experimentIds)
                .collect(Collectors.toMap(Notebook::getId, Function.identity()));

        List<DBRef> notebookIds = notebooks.keySet().stream()
                .map(k -> new DBRef("notebook", k))
                .collect(Collectors.toList());

        Collection<Project> projects = projectRepository.findByNotebookIds(notebookIds);

        return projects.parallelStream()
                .flatMap(p -> p.getNotebooks().stream()
                        .filter(n -> notebooks.get(n.getId()) != null)
                        .flatMap(n -> n.getExperiments().stream()
                                .filter(e -> experiments.get(e.getId()) != null)
                                .map(e -> Triple.of(p, n, e))));
    }

    private List<DashboardRowDTO> getWaitingRows() {
        // Experiments Waiting Author’s Signature
        final Map<String, SignatureService.Document> waitingDocuments;
        try {
            waitingDocuments = signatureService.getDocumentsByUser().stream()
                    .filter(d -> d.isActionRequired() && (d.getStatus() == SignatureService.ISSStatus.SIGNING || d.getStatus() == SignatureService.ISSStatus.SUBMITTED))
                    .collect(Collectors.toMap(SignatureService.Document::getId, d -> d));
        } catch (IOException e) {
            LOGGER.error("Unable to get list of documents from signature service.", e);
            throw new IndigoRuntimeException("Unable to get list of documents from signature service.");
        }
        Map<String, Experiment> waitingExperiments = experimentRepository.findByDocumentIdIn(waitingDocuments.keySet())
                .collect(Collectors.toMap(Experiment::getId, Function.identity()));

        return getEntities(waitingExperiments)
                .map(t -> convert(t, waitingDocuments))
                .collect(Collectors.toList());
    }

    private List<DashboardRowDTO> getSubmittedRows(User user, ZonedDateTime threshold) {
        // Experiments Submitted by Author
        final Collection<Experiment> submittedExp = experimentRepository.findByAuthorOrSubmittedBy(user, user).stream()
                .filter(e -> e.getStatus() != ExperimentStatus.OPEN && e.getStatus() != ExperimentStatus.COMPLETED)
                .collect(Collectors.toList());
        final Set<String> submittedDocumentsIds = submittedExp.stream().map(Experiment::getDocumentId).collect(Collectors.toSet());
        Map<String, SignatureService.Document> submittedDocuments;
        if (!submittedDocumentsIds.isEmpty()) {
            try {
                submittedDocuments = signatureService.getDocumentsByIds(submittedDocumentsIds).stream()
                        .collect(Collectors.toMap(SignatureService.Document::getId, d -> d));
            } catch (IOException e) {
                LOGGER.error("Unable to get list of documents from signature service.", e);
                throw new IndigoRuntimeException("Unable to get list of documents from signature service.");
            }
        } else {
            submittedDocuments = new HashMap<>();
        }

        Map<String, Experiment> experiments = submittedExp.stream()
                .filter(e -> e.getCreationDate().isAfter(threshold))
                .collect(Collectors.toMap(Experiment::getId, Function.identity()));

        return getEntities(experiments)
                .map(t -> convert(t, submittedDocuments))
                .collect(Collectors.toList());
    }

    private DashboardRowDTO convert(Triple<Project, Notebook, Experiment> t, Map<String, SignatureService.Document> documents) {
        final Project project = t.getLeft();
        final Notebook middle = t.getMiddle();
        final Experiment experiment = t.getRight();
        return convert(project, middle, experiment, Optional.ofNullable(documents)
                .map(d -> d.get(experiment.getDocumentId())).orElse(null));
    }

    /**
     * Checks if user has access to all entities
     *
     * @param user user to check access for
     * @param t    entities to check (project, notebook, experiment)
     * @return true if user has access to all the entities
     */
    private boolean hasAccess(User user, Triple<Project, Notebook, Experiment> t) {
        final Project project = t.getLeft();
        if (project == null || !PermissionUtil.hasEditorAuthorityOrPermissions(user, project.getAccessList(), UserPermission.READ_ENTITY)) {
            return false;
        }
        final Notebook notebook = t.getMiddle();
        if (notebook == null || !PermissionUtil.hasEditorAuthorityOrPermissions(user, notebook.getAccessList(), UserPermission.READ_ENTITY)) {
            return false;
        }
        final Experiment experiment = t.getRight();
        return experiment != null && PermissionUtil.hasEditorAuthorityOrPermissions(user, experiment.getAccessList(), UserPermission.READ_ENTITY);
    }

    /**
     * Creates new dashboard row DTO
     *
     * @param project    experiment project
     * @param notebook   experiment notebook
     * @param experiment experiment
     * @param document   experiment document (in signature service)
     * @return dashboard row DTO
     */
    private DashboardRowDTO convert(Project project, Notebook notebook, Experiment experiment, SignatureService.Document document) {
        DashboardRowDTO result = new DashboardRowDTO();

        final List<ComponentDTO> components =
                experiment.getComponents().stream().map(ComponentDTO::new).collect(Collectors.toList());

        // We get title from 'Reaction details' component or from 'Concept details' component
        final Optional<String> reactionTitle = BatchComponentUtil.getReactionDetails(components)
                .map(BatchComponentUtil::getComponentTitle);
        String title = reactionTitle.orElseGet(() -> {
            final Optional<String> conceptTitle = BatchComponentUtil.getConceptDetails(components)
                    .map(BatchComponentUtil::getComponentTitle);
            return conceptTitle.orElse(null);
        });

        result.setProjectId(SequenceIdUtil.extractShortId(project));
        result.setNotebookId(SequenceIdUtil.extractShortId(notebook));
        result.setExperimentId(SequenceIdUtil.extractShortId(experiment));
        result.setId(notebook.getName() + "-" + experiment.getName());
        result.setName(title);
        result.setStatus(experiment.getStatus());

        final User author = experiment.getAuthor();
        result.setAuthor(new DashboardRowDTO.UserDTO(author.getFirstName(), author.getLastName()));
        result.setSubmitter(Optional.ofNullable(experiment.getSubmittedBy()).map(
                u -> new DashboardRowDTO.UserDTO(u.getFirstName(), u.getLastName())
        ).orElse(null));

        List<String> coAuthors = experiment.getComponents().stream()
                .filter(c -> REACTION_DETAILS.equals(c.getName()))
                .map(MongoExt::of)
                .flatMap(m -> m.streamObjects("coAuthors"))
                .map(a -> a.getString("name"))
                .collect(Collectors.toList());

        result.setCoAuthors(coAuthors);
        result.setProject(project.getName());
        result.setCreationDate(experiment.getCreationDate());
        result.setLastEditDate(experiment.getLastEditDate());

        if (document != null) {
            result.setWitnesses(document.getWitnesses().stream().map(
                    u -> new DashboardRowDTO.UserDTO(u.getFirstname(), u.getLastname())
            ).collect(Collectors.toList()));
            result.setComments(document.getWitnesses().stream().map(SignatureService.User::getComment)
                    .collect(Collectors.toList()));
        }

        return result;
    }

}