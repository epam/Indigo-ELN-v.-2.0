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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.websocket.server.PathParam;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/api/sd")
public class SDResource {

    private static final String EXPORT_FILE_NAME = "export.sdf";

    @Autowired
    private SDService sdService;

    @Autowired
    private UserService userService;

    @PostMapping(value = "/import", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<SDEntryDTO>> importFile(@RequestParam MultipartFile file) throws IOException {
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
        } catch (Exception e) {
            throw new IndigoRuntimeException("Error occurred while parsing SD file.", e);
        }
    }

    @PostMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> exportFile(@RequestBody List<SDExportItem> items) throws IOException {
        try {
            SDFileInfo sdFileInfo = sdService.create(items);

            User user = userService.getUserWithAuthorities();

            String fileName = user.getLogin() + "_" + System.currentTimeMillis() + ".sdf";
            String sdfileStr = sdFileInfo.getSdfileStr();

            File file = TempFileUtil.saveToTempDirectory(sdfileStr == null ? new byte[]{} : sdfileStr.getBytes(StandardCharsets.UTF_8), fileName);

            return ResponseEntity.ok(ImmutableMap.of("fileName", file.getName()));
        } catch (Exception e) {
            throw new IndigoRuntimeException("Error occurred while creating SD file.", e);
        }
    }

    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadFile(@ApiParam("File name") @PathParam("fileName") String fileName) throws IOException {
        File file = FileUtils.getFile(FileUtils.getTempDirectory(), fileName);

        try (InputStream is = new FileInputStream(file)) {
            byte[] bytes = IOUtils.toByteArray(is);

            HttpHeaders headers = HeaderUtil.createAttachmentDescription(EXPORT_FILE_NAME);

            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            throw new IndigoRuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }
}
