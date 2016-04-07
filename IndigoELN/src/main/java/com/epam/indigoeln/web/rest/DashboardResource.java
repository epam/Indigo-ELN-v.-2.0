package com.epam.indigoeln.web.rest;

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
import com.epam.indigoeln.web.rest.dto.DashboardExperimentDTO;
import com.epam.indigoeln.web.rest.dto.UserDTO;
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
    public ResponseEntity<DashboardDTO> getDashboard() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REST request to get dashboard experiments");
        }
        User user = userService.getUserWithAuthorities();
        DashboardDTO dashboardDTO = new DashboardDTO();

        ZonedDateTime date = ZonedDateTime.now().minus(thresholdLevel, thresholdUnit);
        final List<Experiment> openAndCompletedExp = experimentRepository.findByAuthorAndStatusesCreatedAfter(user,
                Arrays.asList(ExperimentStatus.OPEN, ExperimentStatus.COMPLETED), date);
        dashboardDTO.setOpenAndCompletedExp(convert(user, openAndCompletedExp, true));

        final List<String> documentsIds = signatureService.getDocumentsIds(Arrays.asList(SignatureService.ISSStatus.SIGNING,
                SignatureService.ISSStatus.SUBMITTED));
        final List<Experiment> waitingSignatureExp = experimentRepository.findByDocumentsIds(documentsIds);
        dashboardDTO.setWaitingSignatureExp(convert(user, waitingSignatureExp, false));

//        final Collection<Experiment> submittedAndSigningExp = experimentRepository.findBySubmittedBy(user);
//        dashboardDTO.setSubmittedAndSigningExp(convert(user, submittedAndSigningExp));        
        
        return ResponseEntity.ok(dashboardDTO);
    }

    private List<DashboardExperimentDTO> convert(User user, Collection<Experiment> experiments, boolean filter) {
        return experiments.stream().map(this::getEntities).filter(t -> !filter || hasAccess(user, t))
                .map(this::convert).collect(Collectors.toList());
    }

    private Triple<Project, Notebook, Experiment> getEntities(Experiment e) {
        final Notebook notebook = notebookRepository.findByExperimentId(e.getId());
        final Project project = projectRepository.findByNotebookId(notebook.getId());
        return Triple.of(project, notebook, e);
    }

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

    private DashboardExperimentDTO convert(Triple<Project, Notebook, Experiment> t) {
        DashboardExperimentDTO result = new DashboardExperimentDTO();

        final Project project = t.getLeft();
        final Notebook notebook = t.getMiddle();
        final Experiment experiment = t.getRight();

        final List<ComponentDTO> components =
                experiment.getComponents().stream().map(ComponentDTO::new).collect(Collectors.toList());

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
        result.setId(experiment.getId());
        result.setName(title);
        result.setStatus(experiment.getStatus());
        result.setAuthor(Optional.ofNullable(experiment.getSubmittedBy()).map(UserDTO::new).orElse(null));
        result.setCoAuthors(Optional.ofNullable(experiment.getCoAuthors()).orElse(new ArrayList<>())
                .stream().map(UserDTO::new).collect(Collectors.toList()));
        result.setProject(project.getName());
        result.setCreationDate(experiment.getCreationDate());
        result.setLastEditDate(experiment.getLastEditDate());

        return result;
    }

}