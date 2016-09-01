package com.epam.indigoeln.web.rest;

import com.chemistry.enotebook.domain.SDFileInfo;
import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.service.sd.SDService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.service.util.TempFileUtil;
import com.epam.indigoeln.web.rest.dto.SDEntryDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.google.common.collect.ImmutableMap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.websocket.server.PathParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/api/sd")
public class SDResource {

    private static final String EXPORT_COMPONENT_BATCH = "batch";
    private static final String EXPORT_COMPONENT_COMPOUND = "compound";

    private static final String EXPORT_FILE_NAME = "export.sdf";

    @Autowired
    private SDService sdService;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/import", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<SDEntryDTO>> importFile(
            @RequestParam MultipartFile file
    ) throws IOException {
        final InputStream inputStream = file.getInputStream();
        try {
            final List<SDEntryDTO> result = sdService.parse(inputStream).stream().map(sdu -> {
                SDEntryDTO dto = new SDEntryDTO();
                dto.setMol(sdu.getMol());
                dto.setProperties(sdu.getInfoPortion());
                return dto;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new IndigoRuntimeException("Error occurred while parsing SD file.", e);
        }
    }

    @RequestMapping(value = "/export", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> exportFile(@RequestParam("component") String component, @RequestBody List<String> fullNbkBatchNumbers) throws IOException {

        List<BasicDBObject> sdItems = null;
        if (EXPORT_COMPONENT_BATCH.equals(component)) {
            List<Component> components = componentRepository.findBatchSummariesByFullBatchNumbers(fullNbkBatchNumbers);
            sdItems = components.stream().map(Component::getContent)
                    .map(c -> (BasicDBList) c.get("batches"))
                    .flatMap(List::stream)
                    .map(o -> (BasicDBObject) o)
                    .filter(b -> fullNbkBatchNumbers.contains(b.getString("fullNbkBatch")))
                    .collect(Collectors.toList());
        } else if (EXPORT_COMPONENT_COMPOUND.equals(component)) {
            List<Component> components = componentRepository.findPreferredCompoundSummariesByFullBatchNumbers(fullNbkBatchNumbers);
            sdItems = components.stream().map(Component::getContent)
                    .map(c -> (BasicDBList) c.get("compounds"))
                    .flatMap(List::stream)
                    .map(o -> (BasicDBObject) o)
                    .filter(b -> fullNbkBatchNumbers.contains(b.getString("fullNbkBatch")))
                    .collect(Collectors.toList());
        }

        SDFileInfo sdFileInfo;
        try {
            sdFileInfo = sdService.create(sdItems);
        } catch (Exception e) {
            throw new IndigoRuntimeException("Error occurred while creating SD file.", e);
        }
        final User user = userService.getUserWithAuthorities();
        String fileName = user.getLogin() + "_" + System.currentTimeMillis() + ".sdf";
        final String sdfileStr = sdFileInfo.getSdfileStr();
        final File file = TempFileUtil.saveToTempDirectory(sdfileStr == null ? new byte[]{} : sdfileStr.getBytes(), fileName);
        return ResponseEntity.ok(ImmutableMap.of("fileName", file.getName()));
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> downloadFile(
            @ApiParam("File name") @PathParam("fileName") String fileName
    ) throws IOException {
        File file = FileUtils.getFile(FileUtils.getTempDirectory(), fileName);
        try {  //NOSONAR: spring will close stream, after it will send bytes to client
            InputStream is = new FileInputStream(file);
            HttpHeaders headers = HeaderUtil.createAttachmentDescription(EXPORT_FILE_NAME);
            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(is));
        } catch (Exception e) {
            throw new IndigoRuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

}
