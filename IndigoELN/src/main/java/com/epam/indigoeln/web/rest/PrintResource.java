package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.service.print.PhantomJsService;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.google.common.collect.ImmutableMap;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import java.io.*;
import java.util.Map;
import java.util.UUID;

import static com.epam.indigoeln.core.service.util.TempFileUtil.TEMP_FILE_PREFIX;

@Api
@RestController
@RequestMapping("/api/print")
public class PrintResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintResource.class);

    public static final String HEADER =
            "<table width=\"100%\" border=\"0\"><tr><td>Header</td><td align=\"right\">Some title</td></tr></table>";

    @Autowired
    private PhantomJsService phantomJsService;

    @ApiOperation(value = "Converts HTML printout to PDF.", produces = "application/json")
    @RequestMapping(method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> createPdf(
            @ApiParam("HTML printout") @RequestBody HtmlWrapper wrapper) throws FileNotFoundException {
        String fileName = String.format("%s.pdf", wrapper.getFileName());
        File file = FileUtils.getFile(FileUtils.getTempDirectory(), fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] screenshot = phantomJsService.takesScreenshot(wrapper.getHtml(), OutputType.BYTES);
            Image image = Image.getInstance(screenshot);
            image.scalePercent(75);
            Document document = new Document(new Rectangle(image.getScaledWidth(), image.getScaledHeight()), 0, 0, 0, 0);
            PdfWriter.getInstance(document, fileOutputStream);
            /** writer.setPageEvent(new HeaderFooter()); **/
            document.open();
            document.add(image);
            document.close();
        } catch (Exception e) {
            LOGGER.error("Error occurred while creating pdf file.", e);
        }
        return ResponseEntity.ok(ImmutableMap.of("fileName", fileName));
    }

    @ApiOperation(value = "Returns file content by it's name.", produces = "application/json")
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> download(
            @ApiParam("File name") @PathParam("fileName") String fileName
        ) {
        File file = FileUtils.getFile(FileUtils.getTempDirectory(), fileName);
        try {  //NOSONAR: spring will close stream, after it will send bytes to client
            InputStream is = new FileInputStream(file);
            HttpHeaders headers = HeaderUtil.createAttachmentDescription(fileName);
            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(is));
        } catch (Exception e) {
            throw new IndigoRuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    public static class HtmlWrapper {
        private String html;
        private String header;
        private String fileName;

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }

    public static class HeaderFooter extends PdfPageEventHelper {
        protected ElementList header;

        public HeaderFooter() throws IOException {
            header = XMLWorkerHelper.parseToElementList(HEADER, null);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                ColumnText ct = new ColumnText(writer.getDirectContent());
                ct.setSimpleColumn(new Rectangle(36, 832, 559, 810));
                for (Element e : header) {
                    ct.addElement(e);
                }
                ct.go();
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }
    }
}