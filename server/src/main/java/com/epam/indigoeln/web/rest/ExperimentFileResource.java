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
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.dto.FileDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.epam.indigoeln.web.rest.util.PaginationUtil;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping(ExperimentFileResource.URL_MAPPING)
public class ExperimentFileResource {

    static final String URL_MAPPING = "/api/experiment_files";

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentFileResource.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    /**
     * GET  /experiment_files?experimentId -> Returns metadata for all files of specified experiment<br/>.
     * Also use a <b>pageable</b> interface: <b>page</b>, <b>size</b>, <b>sort</b><br/>
     * <b>Example</b>: page=0&size=30&sort=firstname&sort=lastname,asc - retrieves all elements in specified order
     * (<b>firstname</b>: ASC, <b>lastname</b>: ASC) from 0 page with size equals to 30<br/>
     * <b>By default</b>: page = 0, size = 20 and no sort<br/>
     * <b>Available sort options</b>: filename, contentType, length, uploadDate
     *
     * @param experimentId Experiment id
     * @param notebookId   Notebook id
     * @param projectId    Project id
     * @return Returns all experiment files
     * @throws URISyntaxException If URI is not correct
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Returns all experiment files.")
    public ResponseEntity<List<FileDTO>> getAllFiles(
            @ApiParam("Experiment id") @RequestParam String experimentId,
            @ApiParam("Notebook id") @RequestParam String notebookId,
            @ApiParam("Project id") @RequestParam String projectId) throws URISyntaxException {
        String fullId = SequenceIdUtil.buildFullId(projectId, notebookId, experimentId);
        LOGGER.debug("REST request to get files's metadata for experiment: {}", fullId);
        Page<GridFSDBFile> page = fileService.getAllFilesByExperimentId(fullId);
        String urlParameter = "projectId=" + projectId + "&notebookId=" + notebookId
                + "&experimentId=" + experimentId;

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, URL_MAPPING
                + "?" + urlParameter);
        List<FileDTO> fileDTOs = page.getContent().stream().map(FileDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok().headers(headers).body(fileDTOs);

    }

    /**
     * GET  /experiment_files/:id -> Returns file with specified id.
     *
     * @param id Experiment file id
     * @return Returns experiment file by it's id
     */
    @ApiOperation(value = "Returns experiment file by it's id.")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> getFile(
            @ApiParam("Experiment file id.") @PathVariable("id") String id
    ) {
        LOGGER.debug("REST request to get experiment file: {}", id);
        GridFSDBFile file = fileService.getFileById(id);

        HttpHeaders headers = HeaderUtil.createAttachmentDescription(file.getFilename());
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(file.getInputStream()));
    }

    /**
     * POST  /experiment_files?experimentId -> Saves file for specified experiment.
     *
     * @param file         Experiment file
     * @param experimentId Identifier of the experiment
     * @return Created file
     * @throws URISyntaxException If URI is not correct
     */
    @ApiOperation(value = "Creates new file for the experiment.")
    @RequestMapping(method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FileDTO> saveFile(
            @ApiParam("Experiment file.") @RequestParam MultipartFile file,
            @ApiParam("Identifier of the experiment.") @RequestParam String experimentId
    ) throws URISyntaxException {
        LOGGER.debug("REST request to save file for experiment: {}", experimentId);
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            throw new IndigoRuntimeException("Unable to get file content.", e);
        }
        User user = userService.getUserWithAuthorities();
        GridFSFile gridFSFile = fileService.saveFileForExperiment(experimentId, inputStream,
                file.getOriginalFilename(), file.getContentType(), user);
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + gridFSFile.getId()))
                .body(new FileDTO(gridFSFile));
    }

    /**
     * DELETE  /experiment_files/:id -> Removes file with specified id.
     *
     * @param id Experiment file id
     */
    @ApiOperation(value = "Removes experiment file.")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteFile(
            @ApiParam("Experiment file id.") @PathVariable("id") String id
    ) {
        LOGGER.debug("REST request to remove experiment file: {}", id);
        fileService.deleteExperimentFile(id);
        return ResponseEntity.ok().build();
    }
}
