package com.epam.indigoeln.util;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.repository.sequenceid.SequenceIdRepository;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.core.service.project.ProjectService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.BDDMockito.given;

@Component
public class DatabaseUtil {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private NotebookService notebookService;
    @Autowired
    private ExperimentService experimentService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private NotebookRepository notebookRepository;
    @Autowired
    private ExperimentRepository experimentRepository;
    @Autowired
    private ComponentRepository componentRepository;
    @Autowired
    private SequenceIdRepository sequenceIdRepository;
    @Autowired
    private UserService userService;

    @MockBean
    private AuditorAware<User> auditorAware;

    public void init() {
        given(auditorAware.getCurrentAuditor()).willReturn(userService.getUserWithAuthoritiesByLogin("admin"));

        User admin = userService.getUserWithAuthoritiesByLogin("admin");
        for (int i = 0; i < 3; i++) {
            ProjectDTO projectDTO = new ProjectDTO();
            projectDTO.setName("Test Project" + i);
            projectDTO.setDescription("description" + i);
            projectDTO.setKeywords("keyword1 keyword2" + i);
            projectDTO.setReferences("references" + i);
            projectDTO.setTags(Arrays.asList("tag1" + i, "tag2" + i));
            ProjectDTO savedProject = projectService.createProject(projectDTO);

            ArrayList<NotebookDTO> notebooks = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                NotebookDTO notebookDTO = new NotebookDTO();
                notebookDTO.setName("nb name" + (3 * i + j));
                notebookDTO.setDescription("description" + (3 * i + j));
                NotebookDTO savedNotebook = notebookService.createNotebook(notebookDTO, savedProject.getId(), admin);

                ArrayList<ExperimentDTO> experiments = new ArrayList<>();
                for (int k = 0; k < 3; k++) {
                    ExperimentDTO experimentDTO = new ExperimentDTO();

                    ArrayList<ComponentDTO> components = new ArrayList<>();
                    for (int l = 0; l < 3; l++) {
                        ComponentDTO component = new ComponentDTO();
                        component.setName("component" + l);
                        component.setContent(new BasicDBObject("field", "value"));
                        components.add(component);
                    }
                    experimentDTO.setComponents(components);
                    ExperimentDTO savedExperiment = experimentService.createExperiment(experimentDTO, savedProject.getId(), savedNotebook.getId(), admin);
                    experiments.add(savedExperiment);
                }
                NotebookDTO notebookWithVersion = notebookService.getNotebookById(savedProject.getId(), savedNotebook.getId(), admin);
                notebookWithVersion.setExperiments(experiments);
                NotebookDTO updateNotebook = notebookService.updateNotebook(notebookWithVersion, savedProject.getId(), admin);
                notebooks.add(updateNotebook);
            }
            ProjectDTO projectWithId = projectService.getProjectById(savedProject.getId(), admin);
            projectWithId.setNotebooks(notebooks);
            projectService.updateProject(projectWithId, admin);
        }
    }

    public void dropDBs(){
        projectRepository.deleteAll();
        notebookRepository.deleteAll();
        experimentRepository.deleteAll();
        componentRepository.deleteAll();
        sequenceIdRepository.deleteAll();
    }
}
