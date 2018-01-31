package com.epam.indigoeln.core.service.dashboard;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.config.DashboardProperties;
import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.service.print.itext2.utils.MongoExt;
import com.epam.indigoeln.core.service.project.ProjectService;
import com.epam.indigoeln.core.service.signature.SignatureService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.DashboardResource;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.DashboardDTO;
import com.epam.indigoeln.web.rest.dto.DashboardRowDTO;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.util.BatchComponentUtil.REACTION_DETAILS;

@Service
public class DashboardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardResource.class);
    private static final String EXCEPTION_MESSAGE = "Unable to get list of documents from signature service.";

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private DashboardProperties dashboardProperties;

    @Autowired
    private UserService userService;


    public DashboardDTO getDashboard() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REST request to get dashboard experiments");
        }

        User user = userService.getUserWithAuthorities();
        DashboardDTO dashboardDTO = new DashboardDTO();

        ZonedDateTime threshold = ZonedDateTime.now()
                .minus(dashboardProperties.getThresholdLevel(), dashboardProperties.getThresholdUnit());

        dashboardDTO.setOpenAndCompletedExp(getCurrentRows(user, threshold));
        dashboardDTO.setWaitingSignatureExp(getWaitingRows());
        dashboardDTO.setSubmittedAndSigningExp(getSubmittedRows(user, threshold));

        return dashboardDTO;
    }

    private List<DashboardRowDTO> getCurrentRows(User user, ZonedDateTime threshold) {
        // Open and Completed Experiments
        Map<String, Experiment> experiments = experimentRepository
                .findByAuthorAndStatusAndCreationDateAfter(user, ExperimentStatus.OPEN, threshold)
                .collect(Collectors.toMap(Experiment::getId, Function.identity()));

        return projectService.getEntities(experiments)
                .filter(t -> hasAccess(user, t))
                .map(t -> convert(t, null))
                .collect(Collectors.toList());
    }

    private List<DashboardRowDTO> getWaitingRows() {
        // Experiments Waiting Author’s Signature
        final Map<String, SignatureService.Document> waitingDocuments;
        try {
            waitingDocuments = signatureService.getDocumentsByUser().stream()
                    .filter(d -> d.isActionRequired()
                            && (d.getStatus() == SignatureService.ISSStatus.SIGNING
                            || d.getStatus() == SignatureService.ISSStatus.SUBMITTED))
                    .collect(Collectors.toMap(SignatureService.Document::getId, d -> d));
        } catch (IOException e) {
            LOGGER.error(EXCEPTION_MESSAGE, e);
            throw new IndigoRuntimeException(EXCEPTION_MESSAGE);
        }
        Map<String, Experiment> waitingExperiments = experimentRepository.findByDocumentIdIn(waitingDocuments.keySet())
                .collect(Collectors.toMap(Experiment::getId, Function.identity()));

        return projectService.getEntities(waitingExperiments)
                .map(t -> convert(t, waitingDocuments))
                .collect(Collectors.toList());
    }

    private List<DashboardRowDTO> getSubmittedRows(User user, ZonedDateTime threshold) {
        // Experiments Submitted by Author
        final Collection<Experiment> submittedExp = experimentRepository.findByAuthorOrSubmittedBy(user, user).stream()
                .filter(e -> e.getStatus() != ExperimentStatus.OPEN && e.getStatus() != ExperimentStatus.COMPLETED)
                .collect(Collectors.toList());
        final Set<String> submittedDocumentsIds = submittedExp
                .stream()
                .map(Experiment::getDocumentId)
                .collect(Collectors.toSet());
        Map<String, SignatureService.Document> submittedDocuments;
        if (!submittedDocumentsIds.isEmpty()) {
            try {
                submittedDocuments = signatureService.getDocumentsByIds(submittedDocumentsIds).stream()
                        .collect(Collectors.toMap(SignatureService.Document::getId, d -> d));
            } catch (IOException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
                throw new IndigoRuntimeException(EXCEPTION_MESSAGE);
            }
        } else {
            submittedDocuments = new HashMap<>();
        }

        Map<String, Experiment> experiments = submittedExp.stream()
                .filter(e -> e.getCreationDate().isAfter(threshold))
                .collect(Collectors.toMap(Experiment::getId, Function.identity()));

        return projectService.getEntities(experiments)
                .map(t -> convert(t, submittedDocuments))
                .collect(Collectors.toList());
    }

    private DashboardRowDTO convert(Triple<Project, Notebook, Experiment> t,
                                    Map<String, SignatureService.Document> documents) {
        final Project project = t.getLeft();
        final Notebook middle = t.getMiddle();
        final Experiment experiment = t.getRight();
        return convert(project, middle, experiment, Optional.ofNullable(documents)
                .map(d -> d.get(experiment.getDocumentId())).orElse(null));
    }

    /**
     * Checks if user has access to all entities.
     *
     * @param user user to check access for
     * @param t    entities to check (project, notebook, experiment)
     * @return true if user has access to all the entities
     */
    private boolean hasAccess(User user, Triple<Project, Notebook, Experiment> t) {
        final Project project = t.getLeft();
        if (project == null
                || !PermissionUtil.hasEditorAuthorityOrPermissions(user, project.getAccessList(),
                UserPermission.READ_ENTITY)) {
            return false;
        }
        final Notebook notebook = t.getMiddle();
        if (notebook == null
                || !PermissionUtil.hasEditorAuthorityOrPermissions(user, notebook.getAccessList(),
                UserPermission.READ_ENTITY)) {
            return false;
        }
        final Experiment experiment = t.getRight();
        return experiment != null
                && PermissionUtil.hasEditorAuthorityOrPermissions(user, experiment.getAccessList(),
                UserPermission.READ_ENTITY);
    }

    /**
     * Creates new dashboard row DTO.
     *
     * @param project    experiment project
     * @param notebook   experiment notebook
     * @param experiment experiment
     * @param document   experiment document (in signature service)
     * @return dashboard row DTO
     */
    private DashboardRowDTO convert(Project project, Notebook notebook, Experiment experiment,
                                    SignatureService.Document document) {
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

        List<String> coAuthorsIds = experiment.getComponents().stream()
                .filter(c -> REACTION_DETAILS.equals(c.getName()))
                .map(MongoExt::of)
                .flatMap(m -> m.streamStrings("coAuthors"))
                .collect(Collectors.toList());

        List<String> coAuthors = userService.getAllUsersByIds(coAuthorsIds);

        result.setCoAuthors(coAuthors);
        result.setProject(project.getName());
        result.setCreationDate(experiment.getCreationDate());
        result.setLastEditDate(experiment.getLastEditDate());

        if (document != null) {
            result.setWitnesses(document.getWitnesses().stream().map(
                    u -> new DashboardRowDTO.UserDTO(u.getFirstName(), u.getLastName())
            ).collect(Collectors.toList()));
            result.setComments(document.getWitnesses().stream().map(SignatureService.User::getComment)
                    .collect(Collectors.toList()));
        }

        return result;
    }
}
