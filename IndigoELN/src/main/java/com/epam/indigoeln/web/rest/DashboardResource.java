package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.signature.SignatureService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.DashboardDTO;
import com.epam.indigoeln.web.rest.dto.DashboardRowDTO;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    @Value("${dashboard.threshold.level}")
    private int thresholdLevel;

    @Value("${dashboard.threshold.unit}")
    private ChronoUnit thresholdUnit;

    /**
     * GET  /dashboard -> Returns dashboard experiments
     * 1. Experiments created by current user during one month which are in one of following statuses: 'Open', 'Completed'
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DashboardDTO> getDashboard() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REST request to get dashboard experiments");
        }
        User user = userService.getUserWithAuthorities();
        DashboardDTO dashboardDTO = new DashboardDTO();

        ZonedDateTime threshold = ZonedDateTime.now().minus(thresholdLevel, thresholdUnit);

        final Map<String, SignatureService.Document> documents;
        try {
            documents = signatureService.getDocuments()
                    .stream().collect(Collectors.toMap(SignatureService.Document::getId, d -> d));
        } catch (IOException e) {
            LOGGER.error("Unable to get list of documents from signature service.", e);
            throw new IndigoRuntimeException("Unable to get list of documents from signature service.");
        }
        final List<Experiment> signatureServiceExperiments = experimentRepository.findByDocumentsIds(documents.keySet());

        // Open and Completed Experiments
        final List<Experiment> openAndCompletedExp = experimentRepository.findByAuthorAndStatuses(user,
                Arrays.asList(ExperimentStatus.OPEN, ExperimentStatus.COMPLETED));
        dashboardDTO.setOpenAndCompletedExp(
                openAndCompletedExp.stream()
                        .filter(e -> e.getCreationDate().isAfter(threshold))
                        .map(this::getEntities)
                        .filter(t -> hasAccess(user, t))
                        .map(t -> convert(t, null)).collect(Collectors.toList())
        );

        // Experiments Waiting Author’s Signature
        final List<Experiment> waitingSignatureExp = signatureServiceExperiments.stream().filter(e -> {
            SignatureService.Document document = documents.get(e.getDocumentId());
            return document.getStatus() == SignatureService.ISSStatus.SIGNING || document.getStatus() == SignatureService.ISSStatus.SUBMITTED;
        }).collect(Collectors.toList());
        dashboardDTO.setWaitingSignatureExp(
                waitingSignatureExp.stream()
                        .map(this::getEntities)
                        .map(t -> convert(t, documents)).collect(Collectors.toList())
        );

        // Experiments Submitted by Author
        final List<Experiment> submittedAndSigningExp = signatureServiceExperiments.stream().filter(e -> {
            final String submittedById = Optional.ofNullable(e.getSubmittedBy()).map(User::getId).orElse(null);
            return user.getId().equals(submittedById);
        }).collect(Collectors.toList());
        dashboardDTO.setSubmittedAndSigningExp(
                submittedAndSigningExp.stream()
                        .filter(e -> e.getCreationDate().isAfter(threshold))
                        .map(this::getEntities)
                        .map(t -> convert(t, documents)).collect(Collectors.toList())
        );

        return ResponseEntity.ok(dashboardDTO);
    }

    private DashboardRowDTO convert(Triple<Project, Notebook, Experiment> t, Map<String, SignatureService.Document> documents) {
        final Project project = t.getLeft();
        final Notebook middle = t.getMiddle();
        final Experiment experiment = t.getRight();
        return convert(project, middle, experiment, Optional.ofNullable(documents)
                .map(d -> d.get(experiment.getDocumentId())).orElse(null));
    }

    /**
     * Gets all parent entities for experiment
     *
     * @param e experiment
     * @return entities for experiment including itself
     */
    private Triple<Project, Notebook, Experiment> getEntities(Experiment e) {
        final Notebook notebook = notebookRepository.findByExperimentId(e.getId());
        final Project project = projectRepository.findByNotebookId(notebook.getId());
        return Triple.of(project, notebook, e);
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
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, project.getAccessList(), UserPermission.READ_ENTITY)) {
            return false;
        }
        final Notebook notebook = t.getMiddle();
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, notebook.getAccessList(), UserPermission.READ_ENTITY)) {
            return false;
        }
        final Experiment experiment = t.getRight();
        return PermissionUtil.hasEditorAuthorityOrPermissions(user, experiment.getAccessList(), UserPermission.READ_ENTITY);
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

        result.setAuthor(Optional.ofNullable(experiment.getSubmittedBy()).map(
                u -> new DashboardRowDTO.UserDTO(u.getFirstName(), u.getLastName())
        ).orElse(null));

        result.setCoAuthors(Optional.ofNullable(experiment.getCoAuthors()).orElse(new ArrayList<>())
                .stream().map(
                        u -> new DashboardRowDTO.UserDTO(u.getFirstName(), u.getLastName())
                ).collect(Collectors.toList()));

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