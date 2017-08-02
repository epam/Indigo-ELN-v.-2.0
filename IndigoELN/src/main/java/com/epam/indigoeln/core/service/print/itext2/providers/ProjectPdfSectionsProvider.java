package com.epam.indigoeln.core.service.print.itext2.providers;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.service.print.itext2.PdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.model.common.AttachmentsModel;
import com.epam.indigoeln.core.service.print.itext2.model.project.ProjectHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.model.project.ProjectSummaryModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.AbstractPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.HeaderPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.AttachmentsSection;
import com.epam.indigoeln.core.service.print.itext2.sections.project.ProjectHeaderSection;
import com.epam.indigoeln.core.service.print.itext2.sections.project.ProjectSummarySection;
import com.epam.indigoeln.core.service.print.itext2.utils.LogoUtils;
import com.epam.indigoeln.web.rest.dto.FileDTO;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.data.domain.PageRequest;

import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectPdfSectionsProvider implements PdfSectionsProvider {
    private Project project;
    private FileRepository fileRepository;
    private List<GridFSDBFile> files;

    public ProjectPdfSectionsProvider(Project project, FileRepository fileRepository) {
        this.project = project;
        this.fileRepository = fileRepository;
        this.files = getFiles(project.getFileIds());
    }

    @Override
    public List<AbstractPdfSection> getContentSections() {
        return Arrays.asList(new ProjectSummarySection(new ProjectSummaryModel(
                        project.getKeywords(),
                        project.getReferences(),
                        project.getDescription()
                )),
                getAttachmentsSection());
    }

    @Override
    public HeaderPdfSection getHeaderSection() {
        ProjectHeaderModel model = new ProjectHeaderModel(
                LogoUtils.loadDefaultLogo(),
                project.getAuthor().getFullName(),
                project.getCreationDate(),
                project.getName(),
                Instant.now()
        );
        return new ProjectHeaderSection(model);
    }

    private AttachmentsSection getAttachmentsSection() {
        List<FileDTO> list = files.stream()
                .map(FileDTO::new)
                .collect(Collectors.toList());
        return new AttachmentsSection(new AttachmentsModel(list));
    }

    private List<GridFSDBFile> getFiles(Set<String> fileIds) {
        if (!fileIds.isEmpty()) {
            return fileRepository.findAll(fileIds, new PageRequest(0, fileIds.size())).getContent();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<InputStream> getExtraPdf() {
        return files.stream()
                .filter(f -> "application/pdf".equals(f.getContentType()))
                .map(GridFSDBFile::getInputStream)
                .collect(Collectors.toList());
    }
}
