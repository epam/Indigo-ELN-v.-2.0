package com.epam.indigoeln.reports.service;

import com.epam.indigoeln.reports.api.ReportsAPI;
import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.compilers.ReportClassFilter;
import net.sf.jasperreports.compilers.ReportExpressionEvaluationData;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRReportCompileData;
import net.sf.jasperreports.engine.util.JRClassLoader;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRResourcesUtil;
import net.sf.jasperreports.repo.ReportResource;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.setSignificantFigures;

@Slf4j
@ApplicationScoped
public class ReportsService {
    @ConfigProperty(name = "report.timezone", defaultValue="UTC")
    String timezone;

    @Inject
    ReadOnlyStreamingService readOnlyStreamingService;

    public Response generateExperimentReport(ReportsAPI.ExperimentReportDataDTO data) {
        byte[] bytes = doGenerateExperimentReport(List.of(data));
        return Response.ok(bytes)
                .header("Content-Disposition", "attachment; filename=\"Experiment " + data.getExperiment().getName() + ".pdf\"")
                .header("Content-Type", "application/pdf")
                .build();
    }

    @SneakyThrows
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    byte[] doGenerateExperimentReport(List<ReportsAPI.ExperimentReportDataDTO> experiments) {
        setSignificantFigures(10);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(experiments);
        Map<String, Object> params = new HashMap<>();

//        Add date formater to report params
        log.info("!!! timezone = " + timezone);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                .ofPattern("MMM d, u HH:mm:ss VV", Locale.ENGLISH)
                .withZone(ZoneId.of(timezone));
        params.put("dateFormat", dateTimeFormatter);

//        Add current date to the report params
        ZonedDateTime reportDate = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of(timezone));
        params.put("reportDate", reportDate);

//        JasperReport jasperReport = (JasperReport) readOnlyStreamingService.getResource("/reports/ExperimentReport.jasper", JasperPrint.class);
        InputStream xa = ReportsService.class.getResourceAsStream("/reports/ExperimentReport.jasper");
        log.info("!!! /reports/ExperimentReport.jasper = " + xa);
        InputStream xb = ReportsService.class.getResourceAsStream("/reports/logo_new_blue.png");
        log.info("!!! /reports/logo_new_blue.png = " + xb);
        params.put("logoImage", new ByteArrayInputStream(xb.readAllBytes()));
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(ReportsService.class.getResourceAsStream("/reports/ExperimentReport.jasper"));
        log.info("!!! jasperReport = " + jasperReport);
        JRReportCompileData compileData = (JRReportCompileData) jasperReport.getCompileData();
        log.info("!!! compileData = " + compileData);
        ReportExpressionEvaluationData mainCompileData = (ReportExpressionEvaluationData) compileData.getMainDatasetCompileData();
        log.info("!!! mainCompileData = " + mainCompileData);
        byte[] mainCompileDataBytes = (byte[]) mainCompileData.getCompileData();
        log.info("!!! mainCompileDataBytes.length = " + (mainCompileDataBytes != null ? mainCompileDataBytes.length : null));
//        JasperPrint jasperPrint = JasperFillManager.getInstance(readOnlyStreamingService.getContext()).fill(jasperReport, params, dataSource);

//        InputStream xb = ModelUtil.class.getResourceAsStream("/reports/logo_new_blue.png");
//        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(ModelUtil.loadResourceAsStream(getClass(), "/reports/ExperimentReport.jasper"));
//        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
//        return JasperExportManager.exportReportToPdf(jasperPrint);

//        !!! what type is compile data? try to load it manually
        Class<?> loadedClass = JRClassLoader.loadClassFromBytes(new ReportClassFilter(readOnlyStreamingService.getContext()), mainCompileData.getCompileName(), mainCompileDataBytes);
        log.info("!!! loadedClass = " + loadedClass + ", classloader=" + (loadedClass != null ? loadedClass.getClassLoader() : null));



        URL url = JRResourcesUtil.findClassLoaderResource("/reports/ExperimentReport.jasper", getClass().getClassLoader());
        log.info("!!! JRResourcesUtil.findClassLoaderResource = " + url);
        if (url != null) {
            log.info("!!! JRLoader.getInputStream = " + JRLoader.getInputStream(url));
        }
        log.info("!!! readOnlyStreamingService.getResource = " + readOnlyStreamingService.getResource("/reports/ExperimentReport.jasper", ReportResource.class));

        JasperPrint jasperPrint = JasperFillManager.getInstance(readOnlyStreamingService.getContext()).fill(jasperReport, params, dataSource);
//        JasperPrint jasperPrint = JasperFillManager.getInstance(readOnlyStreamingService.getContext()).fillFromRepo("/reports/ExperimentReport.jasper", params, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
