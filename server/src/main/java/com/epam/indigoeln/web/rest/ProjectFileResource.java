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
package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.file.FileService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.FileDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.epam.indigoeln.web.rest.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ProjectFileResource.URL_MAPPING)
public class ProjectFileResource {

    static final String URL_MAPPING = "/api/project_files";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectFileResource.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    /**
     * GET  /project_files?projectId -> Returns metadata for all files of specified project<br/>.
     * Also use a <b>pageable</b> interface: <b>page</b>, <b>size</b>, <b>sort</b><br/>
     * <b>Example</b>: page=0&size=30&sort=firstname&sort=lastname,asc - retrieves all elements in specified order
     * (<b>firstname</b>: ASC, <b>lastname</b>: ASC) from 0 page with size equals to 30<br/>
     * <b>By default</b>: page = 0, size = 20 and no sort<br/>
     * <b>Available sort options</b>: filename, contentType, length, uploadDate
     *
     * @param projectId Project's identifier
     * @param pageable  Pagination information
     * @return Returns metadata for all files of specified project
     * @throws URISyntaxException If URI is not correct
     */
    @Operation(summary = "Returns all project files (with paging).")
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FileDTO>> getAllFiles(
            @Parameter(description = "Identifier of the project to get files for.") @RequestParam String projectId,
            @Parameter(description = "Paging data.") Pageable pageable)
            throws URISyntaxException {
        LOGGER.debug("REST request to get files's metadata for project: {}", projectId);
        Page<GridFsResource> page;
        if (StringUtils.isEmpty(projectId)) {
            page = new PageImpl<>(new ArrayList<>());
        } else {
            page = fileService.getAllFilesByProjectId(projectId, pageable);
        }
        String urlParameter = "projectId=" + projectId;

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, URL_MAPPING + "?" + urlParameter);
        List<FileDTO> fileDTOs = page.getContent().stream().map(FileDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok().headers(headers).body(fileDTOs);
    }

    /**
     * POST  /project_files?projectId -> Saves file for specified project.
     *
     * @param file      File
     * @param projectId Project's identifier
     * @return File
     * @throws URISyntaxException If URI is nor correct
     */
    @Operation(summary = "Creates new file for the project.")
    @RequestMapping(method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FileDTO> saveFile(
            @Parameter(description = "Experiment file.") @RequestParam MultipartFile file,
            @Parameter(description = "Identifier of the project.") @RequestParam String projectId
    ) throws URISyntaxException {
        LOGGER.debug("REST request to save file for project: {}", projectId);
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            throw new IndigoRuntimeException("Unable to get file content.", e);
        }
        User user = userService.getUserWithAuthorities();
        GridFsResource gridFSFile = fileService.saveFileForProject(projectId, inputStream,
                file.getOriginalFilename(), file.getContentType(), user);
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + gridFSFile.getGridFSFile().getObjectId()))
                .body(new FileDTO(gridFSFile));
    }

    /**
     * GET  /project_files/:id -> Returns file with specified id.
     *
     * @param id Identifier
     * @return Returns file with specified id
     */
    @Operation(summary = "Returns project file by it's id.")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> getFile(
            @Parameter(description = "Project file id.") @PathVariable("id") String id
    ) throws IOException {
        LOGGER.debug("REST request to get project file: {}", id);
        GridFsResource file = fileService.getFileById(id);

        HttpHeaders headers = HeaderUtil.createAttachmentDescription(file.getFilename());
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(file.getInputStream()));
    }

    /**
     * DELETE  /project_files/:id -> Removes file with specified id.
     *
     * @param id Identifier
     */
    @Operation(summary = "Removes project file.")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "Project file id.") @PathVariable("id") String id
    ) {
        LOGGER.debug("REST request to remove project file: {}", id);
        fileService.deleteProjectFile(id);
        return ResponseEntity.ok().build();
    }
}
