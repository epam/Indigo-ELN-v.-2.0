package com.epam.indigoeln.web.rest;

import com.chemistry.enotebook.domain.SDFileInfo;
import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.service.sd.SDService;
import com.epam.indigoeln.web.rest.dto.SDEntryDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/api/sd")
public class SDResource {

    private static final String EXPORT_FILE_NAME = "export.sdf";

    @Autowired
    private SDService sdService;

    @Autowired
    private ComponentRepository componentRepository;

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

    @RequestMapping(value = "/export", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> exportFile(@RequestParam("batches") String fullNbkBatchNumbersStr) throws IOException {
        try {
            final List<String> fullNbkBatchNumbers = Arrays.asList(fullNbkBatchNumbersStr.split(","));
            final List<Component> components = componentRepository.findBatchSummariesByFullBatchNumbers(fullNbkBatchNumbers);
            final List<BasicDBObject> batches = components.stream().map(Component::getContent)
                    .map(c -> (BasicDBList) c.get("batches"))
                    .flatMap(List::stream)
                    .map(o -> (BasicDBObject) o)
                    .filter(b -> fullNbkBatchNumbers.contains(b.getString("fullNbkBatch")))
                    .collect(Collectors.toList());
            final SDFileInfo sdFileInfo = sdService.create(batches);
            final ByteArrayInputStream bais = new ByteArrayInputStream(sdFileInfo.getSdfileStr().getBytes());
            HttpHeaders headers = HeaderUtil.createAttachmentDescription(EXPORT_FILE_NAME);
            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(bais));
        } catch (Exception e) {
            throw new IndigoRuntimeException("Error occurred while creating SD file.", e);
        }
    }

}
