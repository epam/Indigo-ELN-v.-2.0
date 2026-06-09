package com.epam.indigoeln.reports.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reports.api.ReportsAPI;
import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.epam.indigoeln.common.util.ContentDispositionUtil.generateContentDisposition;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.callWithSignificantFigures;

@Slf4j
@ApplicationScoped
public class ReportsService {

    private final ReadOnlyStreamingService readOnlyStreamingService;
    private final JasperReport jasperReport;
    private final byte[] logoImage;
    private final DateTimeFormatter dateTimeFormatter;

    @Inject
    public ReportsService(
            ReadOnlyStreamingService readOnlyStreamingService,
            @ConfigProperty(name = "report.timezone", defaultValue = "UTC") String timezone
    ) throws JRException {
        this.readOnlyStreamingService = readOnlyStreamingService;
        this.jasperReport = (JasperReport) JRLoader.loadObject(ReportsService.class.getResourceAsStream("/reports/ExperimentReport.jasper"));
        this.logoImage = ModelUtil.loadResource(ReportsService.class, "/reports/logo_new_blue.png");
        this.dateTimeFormatter = DateTimeFormatter
                .ofPattern("MMM d, u HH:mm:ss VV", Locale.ENGLISH)
                .withZone(ZoneId.of(timezone));
    }

    public Response generateExperimentReport(ReportsAPI.ExperimentReportDataDTO data) {
        byte[] bytes = doGenerateExperimentReport(List.of(data));
        return Response.ok(bytes)
                .header(HttpHeaders.CONTENT_DISPOSITION, generateContentDisposition(true, "Experiment " + data.getExperiment().getName() + ".pdf"))
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .build();
    }

    @SneakyThrows
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    byte[] doGenerateExperimentReport(List<ReportsAPI.ExperimentReportDataDTO> experiments) {
        int significantFigures = !experiments.isEmpty() ? experiments.getFirst().getExperiment().getModel().getSignificantFigures() : ExperimentModel.DEFAULT_SIGNIFICANT_FIGURES;
        return callWithSignificantFigures(significantFigures, () -> {
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(experiments);
            Map<String, Object> params = new HashMap<>();
            params.put("dateFormat", dateTimeFormatter);
            params.put("reportDate", ZonedDateTime.ofInstant(Instant.now(), dateTimeFormatter.getZone()));
            params.put("logoImage", new ByteArrayInputStream(logoImage));
            JasperPrint jasperPrint = JasperFillManager.getInstance(readOnlyStreamingService.getContext()).fill(jasperReport, params, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        });
    }
}
