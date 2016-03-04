package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.print.PhantomJsService;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.google.common.collect.ImmutableMap;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.UUID;

import static com.epam.indigoeln.core.service.util.TempFileUtil.TEMP_FILE_PREFIX;

@RestController
@RequestMapping("/api/print")
public class PrintResource {

    @Autowired
    private PhantomJsService phantomJsService;

    private final Logger log = LoggerFactory.getLogger(PrintResource.class);

    @RequestMapping(method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> createPdf(@RequestBody HtmlWrapper wrapper) throws FileNotFoundException {
        File file = null;
        String fileName = String.format("%s%s.pdf", TEMP_FILE_PREFIX, UUID.randomUUID().toString());
        try {
            file = FileUtils.getFile(FileUtils.getTempDirectory(), fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] screenshot = phantomJsService.takesScreenshot(wrapper.html, OutputType.BYTES);
            Image image = Image.getInstance(screenshot);
            image.scalePercent(75);
            Document document = new Document(new Rectangle(image.getScaledWidth(), image.getScaledHeight()), 0, 0, 0, 0);
            PdfWriter.getInstance(document, fileOutputStream);
            document.open();
            document.add(image);
            document.close();
        } catch (Exception e) {
            FileUtils.deleteQuietly(file);
        }
        return ResponseEntity.ok(ImmutableMap.of("fileName", fileName));
    }

    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> download(@PathParam("fileName") String fileName) {
        File file = null;
        try {
            file = FileUtils.getFile(FileUtils.getTempDirectory(), fileName);
            HttpHeaders headers = HeaderUtil.createAttachmentDescription(fileName);
            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(new FileInputStream(file)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    public static class HtmlWrapper {
        public String html;
    }
}