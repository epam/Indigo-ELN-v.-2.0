package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.service.print.HtmlWrapper;
import com.epam.indigoeln.core.service.print.ITextPrintService;
import com.epam.indigoeln.core.service.print.PhantomJsService;
import com.epam.indigoeln.core.service.util.TempFileUtil;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.*;
import java.util.Map;

@Api
@RestController
@RequestMapping("/api/print")
public class PrintResource {

    public static final String HEADER =
            "<table width=\"100%\" border=\"0\"><tr><td>HeaderPageEvent</td><td align=\"right\">Some title</td></tr></table>";

    @Autowired
    private PhantomJsService phantomJsService;
    @Autowired
    private ITextPrintService iTextPrintService;

    @ApiOperation(value = "Converts HTML printout to PDF.", produces = "application/json")
    @RequestMapping(method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> createPdf(
            @ApiParam("HTML printout") @RequestBody HtmlWrapper wrapper) throws FileNotFoundException {
        String fileName = phantomJsService.createPdf(wrapper);
        return ResponseEntity.ok(ImmutableMap.of("fileName", fileName));
    }

    @ApiOperation(value = "Returns file content by it's name.", produces = "application/json")
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> download(
            @ApiParam("File name") @PathParam("fileName") String fileName
    ) {
        File file = FileUtils.getFile(FileUtils.getTempDirectory(), fileName);
        try (InputStream is = new FileInputStream(file)) {
            if (fileName.startsWith(TempFileUtil.TEMP_FILE_PREFIX)) {
                fileName = fileName.substring(TempFileUtil.TEMP_FILE_PREFIX.length());
            }
            byte[] bytes = IOUtils.toByteArray(is);
            HttpHeaders headers = HeaderUtil.createAttachmentDescription(fileName);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            throw new IndigoRuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/itext/{projectId}/{notebookId}/{experimentId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void createExperimentPdf(@PathVariable String projectId,
                                    @PathVariable String notebookId,
                                    @PathVariable String experimentId,
                                    HttpServletResponse response) throws IOException {
        String fileName = "report-" + SequenceIdUtil.buildFullId(projectId, notebookId, experimentId) + ".pdf";
        setPdfCommonHeaders(response, fileName);
        iTextPrintService.generateNotebookPdf(projectId, notebookId, experimentId, response.getOutputStream());
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/itext/{projectId}/{notebookId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void createNotebookPdf(@PathVariable String projectId,
                                  @PathVariable String notebookId,
                                  HttpServletResponse response) throws IOException {
        String fileName = "report-" + SequenceIdUtil.buildFullId(projectId, notebookId) + ".pdf";
        setPdfCommonHeaders(response, fileName);
        iTextPrintService.generateNotebookPdf(projectId, notebookId, response.getOutputStream());
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/itext/{projectId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void createProjectPdf(@PathVariable String projectId,
                                 HttpServletResponse response) throws IOException {
        String fileName = "report-" + SequenceIdUtil.buildFullId(projectId) + ".pdf";
        setPdfCommonHeaders(response, fileName);
        iTextPrintService.generateProjectPdf(projectId, response.getOutputStream());
    }

    private void setPdfCommonHeaders(HttpServletResponse response, String fileName) {
        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename=" + fileName);
        response.setHeader("Access-Control-Allow-Headers", "Range");
        response.setHeader(
                "Access-Control-Expose-Headers",
                "Accept-Ranges, Content-Encoding, Content-Length, Content-Range"
        );
    }

}
