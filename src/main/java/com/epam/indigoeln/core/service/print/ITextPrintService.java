package com.epam.indigoeln.core.service.print;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.security.Authority;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.print.itext2.PdfGenerator;
import com.epam.indigoeln.core.service.print.itext2.providers.ExperimentPdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.providers.NotebookPdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.providers.ProjectPdfSectionsProvider;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.dto.print.PrintRequest;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

/**
 * Provides methods for working with pdf.
 */
@Service
public class ITextPrintService {
    private final ExperimentRepository experimentRepository;
    private final NotebookRepository notebookRepository;
    private final ProjectRepository projectRepository;
    private final FileRepository fileRepository;
    private final ExperimentService experimentService;
    private final UserService userService;
    private final UserRepository userRepository;

    private static final String PROJECT = "Project";
    private static final String NOTEBOOK = "Notebook";
    private static final String EXPERIMENT = "Experiment";

    @Autowired
    public ITextPrintService(ExperimentRepository experimentRepository,
                             NotebookRepository notebookRepository,
                             ProjectRepository projectRepository,
                             FileRepository fileRepository, ExperimentService experimentService,
                             UserService userService, UserRepository userRepository) {
        this.experimentRepository = experimentRepository;
        this.notebookRepository = notebookRepository;
        this.projectRepository = projectRepository;
        this.fileRepository = fileRepository;
        this.experimentService = experimentService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Generates pdf for experiment by id.
     *
     * @param projectId    Project's identifier
     * @param notebookId   Notebook's identifier
     * @param experimentId Experiment's identifier
     * @param printRequest Print parameters
     * @param user         User user
     * @return Generated pdf for experiment
     */
    public byte[] generateExperimentPdf(String projectId, String notebookId,
                                        String experimentId, PrintRequest printRequest, User user) {
        String notebookFullId = SequenceIdUtil.buildFullId(projectId, notebookId);
        String experimentFullId = SequenceIdUtil.buildFullId(projectId, notebookId, experimentId);

        Project project = findChecked(projectRepository, projectId, PROJECT);
        Notebook notebook = findChecked(notebookRepository, notebookFullId, NOTEBOOK);
        Experiment experiment = findChecked(experimentRepository, experimentFullId, EXPERIMENT);

        checkAccess(experiment, user, notebook);

        ExperimentPdfSectionsProvider provider = new ExperimentPdfSectionsProvider(project, notebook,
                experiment, fileRepository, printRequest, userRepository);
        PdfGenerator pdfGenerator = new PdfGenerator(provider);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pdfGenerator.generate(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Generates pdf for experiment.
     *
     * @param projectId    Project's identifier
     * @param notebookId   Notebook's identifier
     * @param experiment   Experiment for which need to generate pdf
     * @param printRequest Print parameters
     * @param user         User
     * @return Generated pdf for experiment
     */
    public byte[] generateExperimentPdf(String projectId, String notebookId,
                                        Experiment experiment, PrintRequest printRequest, User user) {
        String notebookFullId = SequenceIdUtil.buildFullId(projectId, notebookId);

        Project project = findChecked(projectRepository, projectId, PROJECT);
        Notebook notebook = findChecked(notebookRepository, notebookFullId, NOTEBOOK);

        checkAccess(experiment, user, notebook);

        ExperimentPdfSectionsProvider provider = new ExperimentPdfSectionsProvider(project,
                notebook, experiment, fileRepository, printRequest, userRepository);
        PdfGenerator pdfGenerator = new PdfGenerator(provider);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pdfGenerator.generate(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void checkAccess(Experiment experiment, User user, Notebook notebook) {
        if (!PermissionUtil.isContentEditor(user)) {
            if (notebook == null) {
                throw EntityNotFoundException.createWithNotebookChildId(experiment.getId());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    notebook.getAccessList(), UserPermission.READ_ENTITY,
                    experiment.getAccessList(), UserPermission.READ_ENTITY)) {
                throw OperationDeniedException.createExperimentReadOperation(experiment.getId());
            }
        }
    }

    /**
     * Generates pdf for notebook.
     *
     * @param projectId    Project's identifier
     * @param notebookId   Notebook's identifier
     * @param printRequest Print parameters
     * @param user         User
     * @return Generated pdf for notebook
     */
    public byte[] generateNotebookPdf(String projectId, String notebookId, PrintRequest printRequest, User user) {
        String notebookFullId = SequenceIdUtil.buildFullId(projectId, notebookId);
        Project project = findChecked(projectRepository, projectId, PROJECT);
        Notebook notebook = findChecked(notebookRepository, notebookFullId, NOTEBOOK);

        if (!PermissionUtil.isContentEditor(user)) {
            if (project == null) {
                throw EntityNotFoundException.createWithProjectChildId(notebook.getId());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    project.getAccessList(), UserPermission.READ_ENTITY,
                    notebook.getAccessList(), UserPermission.READ_ENTITY)) {
                throw OperationDeniedException.createNotebookReadOperation(notebook.getId());
            }
            if (printRequest.withContent() && !user.getAuthorities().contains(Authority.EXPERIMENT_READER)){
                throw OperationDeniedException.createNotebookSubEntitiesReadOperation(notebook.getId());
            }
        }

        NotebookPdfSectionsProvider provider = new NotebookPdfSectionsProvider(project,
                notebook, experimentService, userService, printRequest);
        PdfGenerator pdfGenerator = new PdfGenerator(provider);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pdfGenerator.generate(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Generates pdf for project.
     *
     * @param projectId    Project's identifier
     * @param printRequest Print parameters
     * @param user         User
     * @return Generated pdf for project
     */
    public byte[] generateProjectPdf(String projectId, PrintRequest printRequest, User user) {
        Project project = findChecked(projectRepository, projectId, PROJECT);
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, project.getAccessList(),
                UserPermission.READ_ENTITY)) {
            throw OperationDeniedException.createProjectReadOperation(project.getId());
        }

        ProjectPdfSectionsProvider provider = new ProjectPdfSectionsProvider(project, fileRepository, printRequest);
        PdfGenerator pdfGenerator = new PdfGenerator(provider);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pdfGenerator.generate(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


    private static <T> T findChecked(CrudRepository<T, String> repository, String id, String entity) {
        return Optional.ofNullable(repository.findOne(id))
                .orElseThrow(() -> new UnknownEntityException(entity, id));
    }

    private static class ITextPrintServiceException extends RuntimeException {
        ITextPrintServiceException(String message) {
            super(message);
        }
    }

    private static class UnknownEntityException extends ITextPrintServiceException {
        UnknownEntityException(String entityName, String entityId) {
            super(entityName + " with id: '" + entityId + "' does not exist");
        }
    }
}
