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
import com.epam.indigoeln.core.chemistry.domain.SDFileInfo;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.sd.SDExportItem;
import com.epam.indigoeln.core.service.sd.SDService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.service.util.TempFileUtil;
import com.epam.indigoeln.web.rest.dto.SDEntryDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.websocket.server.PathParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sd")
public class SDResource {

    private static final String EXPORT_FILE_NAME = "export.sdf";

    @Autowired
    private SDService sdService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Imports sdf file.")
    @PostMapping(value = "/import", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<SDEntryDTO>> importFile(
            @Parameter(description = "File") @RequestParam MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            try (Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                List<SDEntryDTO> result = sdService.parse(r)
                        .stream()
                        .map(sdu -> {
                            SDEntryDTO dto = new SDEntryDTO();
                            dto.setMol(sdu.getMol());
                            dto.setProperties(sdu.getInfoPortion());
                            return dto;
                        })
                        .collect(Collectors.toList());

                return ResponseEntity.ok(result);
            }
        } catch (IOException e) {
            throw new IndigoRuntimeException("Error occurred while parsing SD file.", e);
        }
    }

    @Operation(summary = "Exports sdf file.")
    @PostMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> exportFile(
            @Parameter(description = "List of sdf items.") @RequestBody List<SDExportItem> items) throws IOException {
        try {
            SDFileInfo sdFileInfo = sdService.create(items);

            User user = userService.getUserWithAuthorities();

            String fileName = user.getLogin() + "_" + System.currentTimeMillis() + ".sdf";
            String sdfileStr = sdFileInfo.getSdfileStr();

            File file = TempFileUtil.saveToTempDirectory(sdfileStr == null
                    ? new byte[]{} : sdfileStr.getBytes(StandardCharsets.UTF_8), fileName);

            return ResponseEntity.ok(ImmutableMap.of("fileName", file.getName()));
        } catch (Exception e) {
            throw new IndigoRuntimeException("Error occurred while creating SD file.", e);
        }
    }

    @Operation(summary = "Download file.")
    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadFile(@Parameter(description = "File name") @PathParam("fileName") String fileName)
            throws IOException {
        File file = FileUtils.getFile(FileUtils.getTempDirectory(), fileName);

        try (InputStream is = new FileInputStream(file)) {
            byte[] bytes = IOUtils.toByteArray(is);

            HttpHeaders headers = HeaderUtil.createAttachmentDescription(EXPORT_FILE_NAME);

            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (IOException e) {
            throw new IndigoRuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }
}
