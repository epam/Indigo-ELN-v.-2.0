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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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

@RestController
@RequestMapping("/api/print")
public class PrintResource {

    @Autowired
    private PhantomJsService phantomJsService;

    @RequestMapping(method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> createPdf(@RequestBody HtmlWrapper wrapper) throws FileNotFoundException {
        File file = null;
        String fileName = String.format("%s%s.pdf", TEMP_FILE_PREFIX, UUID.randomUUID().toString());
        FileOutputStream fileOutputStream = null;
        try {
            file = FileUtils.getFile(FileUtils.getTempDirectory(), fileName);
            fileOutputStream = new FileOutputStream(file);
            byte[] screenshot = phantomJsService.takesScreenshot(wrapper.getHtml(), OutputType.BYTES);
            Image image = Image.getInstance(screenshot);
            image.scalePercent(75);
            Document document = new Document(new Rectangle(image.getScaledWidth(), image.getScaledHeight()), 0, 0, 0, 0);
//            PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);
//            writer.setPageEvent(new HeaderFooter());
            document.open();
            document.add(image);
            document.close();
        } catch (Exception e) {
            FileUtils.deleteQuietly(file);
        } finally {
            if (fileOutputStream != null) {
                IOUtils.closeQuietly(fileOutputStream);
            }
        }
        return ResponseEntity.ok(ImmutableMap.of("fileName", fileName));
    }

    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> download(@PathParam("fileName") String fileName) {
        File file = FileUtils.getFile(FileUtils.getTempDirectory(), fileName);
        try (InputStream is = new FileInputStream(file)) {
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
    }

    public static final String HEADER =
            "<table width=\"100%\" border=\"0\"><tr><td>Header</td><td align=\"right\">Some title</td></tr></table>";

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