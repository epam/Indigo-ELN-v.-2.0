package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.DashboardDTO;
import com.epam.indigoeln.web.rest.dto.DashboardExperimentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
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
        ZonedDateTime date = ZonedDateTime.now().minus(thresholdLevel, thresholdUnit);
        dashboardDTO.setOpenAndCompletedExp(experimentRepository.findByAuthorAndStatusesCreatedAfter(user,
                Arrays.asList(ExperimentStatus.OPEN, ExperimentStatus.COMPLETED), date)
                .stream().map(e -> {
                    final Notebook notebook = notebookRepository.findByExperimentId(e.getId());
                    final Project project = projectRepository.findByNotebookId(notebook.getId());
                    return new DashboardExperimentDTO(e, notebook, project);
                }).collect(Collectors.toList()));
        return ResponseEntity.ok(dashboardDTO);
    }

}