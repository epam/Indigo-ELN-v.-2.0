package com.epam.indigoeln.web.rest;

import com.chemistry.enotebook.utils.sdf.SdUnit;
import com.epam.indigoeln.core.service.sd.SDService;
import com.epam.indigoeln.web.rest.dto.SDEntryDTO;
import com.mongodb.BasicDBObject;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/api/sd")
public class SDResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SDResource.class);

    @Autowired
    private SDService sdService;

    @RequestMapping(value = "/import", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<SDEntryDTO>> importFile(
            @RequestParam MultipartFile file
    ) throws IOException {
        final InputStream inputStream = file.getInputStream();
        try {
            final List<SDEntryDTO> result = sdService.getProductBatchModelsFromSDFile(inputStream).stream().map(sdu -> {
                SDEntryDTO dto = new SDEntryDTO();
                dto.setMol(sdu.getMol());
                dto.setProperties(sdu.getInfoPortion());
                return dto;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LOGGER.error("Error occurred while parsing SD file", e);
            //TODO:
            return null;
        }
    }

}
