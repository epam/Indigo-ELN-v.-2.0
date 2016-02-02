package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.security.AuthoritiesConstants;
import com.epam.indigoeln.core.service.file.FileService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.FileDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.epam.indigoeln.web.rest.util.PaginationUtil;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ProjectFileResource.URL_MAPPING)
//@Controller
public class ProjectFileResource {

    static final String URL_MAPPING = "/api/project_files";

    private final Logger log = LoggerFactory.getLogger(ProjectFileResource.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    /**
     * GET  /project_files?projectId -> Returns metadata for all files of specified project<br/>
     * Also use a <b>pageable</b> interface: <b>page</b>, <b>size</b>, <b>sort</b><br/>
     * page=0&size=30&sort=firstname&sort=lastname,asc - retrieves all elements in specified order
     * (<b>firstname</b>: ASC, <b>lastname</b>: ASC) from 0 page with size equals to 30<br/>
     * By default: <b>page</b> = 0, <b>size</b> = 20 and no <b>sort</b><br/>
     * Available <b>sort</b> options: <b>filename</b>, <b>contentType</b>, <b>length</b>, <b>uploadDate</b>
     */
    @RequestMapping(method = RequestMethod.GET)
    @Secured(AuthoritiesConstants.PROJECT_READER)
    public ResponseEntity<List<FileDTO>> getAllFiles(@RequestParam String projectId,
                                                     Pageable pageable)
            throws URISyntaxException {
        log.debug("REST request to get files's metadata for project: {}", projectId);
        Page<GridFSDBFile> page = fileService.getAllFilesByProjectId(projectId, pageable);
        String urlParameter = "projectId=" + projectId;

        List<FileDTO> fileDTOs = page.getContent().stream().map(FileDTO::new).collect(Collectors.toList());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, URL_MAPPING + "?" + urlParameter);
        return new ResponseEntity<>(fileDTOs, headers, HttpStatus.OK);
    }

    /**
     * GET  /project_files/:id -> Returns file with specified id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Secured(AuthoritiesConstants.PROJECT_READER)
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("id") String id) {
        log.debug("REST request to get project file: {}", id);
        GridFSDBFile file = fileService.getFileById(id);
        HttpHeaders headers = HeaderUtil.createAttachmentDescription(file.getFilename());
        return new ResponseEntity<>(new InputStreamResource(file.getInputStream()), headers, HttpStatus.OK);
    }

    /**
     * POST  /project_files?projectId -> Saves file for specified project
     */
    @RequestMapping(method = RequestMethod.POST)
    @Secured(AuthoritiesConstants.PROJECT_CREATOR)
    public ResponseEntity<FileDTO> saveFile(@RequestParam MultipartFile file, @RequestParam String projectId)
            throws URISyntaxException, IOException {
        log.debug("REST request to save file for project: {}", projectId);
        User user = userService.getUserWithAuthorities();
        GridFSFile gridFSFile = fileService.saveFileForProject(projectId, file.getInputStream(),
                file.getOriginalFilename(), file.getContentType(), user);
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + gridFSFile.getId()))
                .body(new FileDTO(gridFSFile));
    }

    /**
     * DELETE  /project_files/:id -> Removes file with specified id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @Secured(AuthoritiesConstants.PROJECT_CREATOR)
    public ResponseEntity<?> deleteFile(@PathVariable("id") String id) {
        log.debug("REST request to remove project file: {}", id);
        fileService.deleteProjectFile(id);
        return ResponseEntity.ok(null);
    }
}