/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.print.itext2.providers;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.service.print.itext2.PdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.model.common.AttachmentsModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.DescriptionModel;
import com.epam.indigoeln.core.service.print.itext2.model.project.ProjectHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.model.project.ProjectSummaryModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.AbstractPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.AttachmentsSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.DescriptionSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.HeaderPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.project.ProjectHeaderSection;
import com.epam.indigoeln.core.service.print.itext2.sections.project.ProjectSummarySection;
import com.epam.indigoeln.core.service.print.itext2.utils.LogoUtils;
import com.epam.indigoeln.web.rest.dto.FileDTO;
import com.epam.indigoeln.web.rest.dto.print.PrintRequest;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.data.domain.PageRequest;

import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The class is responsible for mapping project to a list of pdf sections used by pdf generator.
 */
public class ProjectPdfSectionsProvider implements PdfSectionsProvider {
    private Project project;
    private FileRepository fileRepository;
    private List<GridFSDBFile> files;
    private PrintRequest printRequest;

    public ProjectPdfSectionsProvider(Project project, FileRepository fileRepository, PrintRequest printRequest) {
        this.project = project;
        this.fileRepository = fileRepository;
        this.files = getFiles(project.getFileIds());
        this.printRequest = printRequest;
    }

    @Override
    public List<AbstractPdfSection> getContentSections() {
        return Arrays.asList(
                getSummarySection(),
                getDescriptionSection(),
                getAttachmentsSection()
        );
    }

    private AbstractPdfSection getSummarySection() {
        return new ProjectSummarySection(new ProjectSummaryModel(
                project.getKeywords(),
                project.getReferences()
        ));
    }

    private AbstractPdfSection getDescriptionSection() {
        return new DescriptionSection(new DescriptionModel(project.getDescription(), "PROJECT"));
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
        if (printRequest.includeAttachments()) {
            return files.stream()
                    .filter(f -> "application/pdf".equals(f.getContentType()))
                    .map(GridFSDBFile::getInputStream)
                    .collect(Collectors.toList());
        } else {
            return PdfSectionsProvider.super.getExtraPdf();
        }
    }
}
