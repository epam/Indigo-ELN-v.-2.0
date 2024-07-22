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

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.print.itext2.PdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.model.common.DescriptionModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.SvgPdfImage;
import com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookContentModel;
import com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.AbstractPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.DescriptionSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.HeaderPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.notebook.NotebookContentSection;
import com.epam.indigoeln.core.service.print.itext2.sections.notebook.NotebookHeaderSection;
import com.epam.indigoeln.core.service.print.itext2.utils.LogoUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.MongoExt;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.UserDTO;
import com.epam.indigoeln.web.rest.dto.print.PrintRequest;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookContentModel.ContentModelRow;
import static com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookContentModel.Details;
import static com.epam.indigoeln.core.util.BatchComponentUtil.*;

/**
 * The class is responsible for mapping notebook to a list of pdf sections used by pdf generator.
 */
public final class NotebookPdfSectionsProvider implements PdfSectionsProvider {
    private final Project project;
    private final Notebook notebook;
    private final ExperimentService experimentService;
    private final UserService userService;
    private final PrintRequest printRequest;

    public NotebookPdfSectionsProvider(Project project, Notebook notebook, ExperimentService experimentService,
                                       UserService userService, PrintRequest printRequest) {
        this.project = project;
        this.notebook = notebook;
        this.experimentService = experimentService;
        this.userService = userService;
        this.printRequest = printRequest;
    }


    @Override
    public List<AbstractPdfSection> getContentSections() {
        ArrayList<AbstractPdfSection> list = new ArrayList<>();
        list.add(new DescriptionSection(new DescriptionModel(notebook.getDescription(), "NOTEBOOK")));
        if (printRequest.withContent()) {
            list.add(getContentSection());
        }
        return list;
    }

    @Override
    public HeaderPdfSection getHeaderSection() {
        NotebookHeaderModel model = new NotebookHeaderModel(
                LogoUtils.loadDefaultLogo(),
                notebook.getAuthor().getFullName(),
                notebook.getCreationDate(),
                notebook.getName(),
                project.getName(), Instant.now()
        );
        return new NotebookHeaderSection(model);
    }

    private AbstractPdfSection getContentSection() {
        User user = userService.getUserWithAuthorities();
        List<ExperimentDTO> experiments = experimentService
                .getAllExperimentNotebookSummary(project.getId(), SequenceIdUtil.extractShortId(notebook), user);
        List<ContentModelRow> rows = experiments.stream()
                .map(this::getContentRow)
                .collect(Collectors.toList());

        return new NotebookContentSection(new NotebookContentModel(rows));
    }

    private ContentModelRow getContentRow(ExperimentDTO experiment) {
        Optional<String> title1 = experiment.getComponents().stream()
                .filter(e -> REACTION_DETAILS.equals(e.getName()))
                .map(ComponentDTO::getContent)
                .map(MongoExt::of)
                .map(m -> m.getString("title"))
                .findAny();

        Optional<String> title2 = experiment.getComponents().stream()
                .filter(e -> CONCEPT_DETAILS.equals(e.getName()))
                .map(ComponentDTO::getContent)
                .map(MongoExt::of)
                .map(m -> m.getString("title"))
                .findAny();

        Optional<String> image = experiment.getComponents().stream()
                .filter(e -> REACTION.equals(e.getName()))
                .map(ComponentDTO::getContent)
                .map(MongoExt::of)
                .map(m -> m.getString("image"))
                .findAny();

        return new ContentModelRow(
                notebook.getName() + "-" + experiment.getFullName(),
                new Details(
                        experiment.getCreationDate(),
                        title1.orElse(title2.orElse(StringUtils.EMPTY)),
                        Optional.ofNullable(experiment.getAuthor()).map(UserDTO::getFullName).orElse(""),
                        experiment.getStatus().toString()),
                new SvgPdfImage(image.orElse(""))
        );
    }
}
