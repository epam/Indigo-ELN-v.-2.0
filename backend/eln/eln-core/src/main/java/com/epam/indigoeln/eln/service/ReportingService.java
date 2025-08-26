package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class ReportingService {

    @Inject
    ExperimentRepository experimentRepository;
//    @Inject
//    ReadOnlyStreamingService readOnlyStreamingService;

    public Response printReport(UUID experimentId) {
        byte[] bytes = generateExperimentReport(experimentId);
        return Response.ok(bytes)
                .header("Content-Disposition", "attachment; filename=\"Experiment " + experimentId + ".pdf\"")
                .header("Content-Type", "application/pdf")
                .build();
    }

    @DataAccess
    @Transactional
    public byte[] generateExperimentReport(UUID experimentId) {
        return generateReport(List.of(experimentRepository.loadForReport(experimentId)));
    }

    @SneakyThrows
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    byte[] generateReport(List<ExperimentEntity> experiments) {
//        JasperReport jasperReport = (JasperReport) readOnlyStreamingService.getResource("/reports/ExperimentReport.jasper");
        InputStream xa = ModelUtil.class.getResourceAsStream("/reports/ExperimentReport.jasper");
        InputStream xb = ModelUtil.class.getResourceAsStream("/reports/logo_new_blue.png");
        log.info("!!! /reports/ExperimentReport.jasper = " + xa);
        log.info("!!! /reports/logo_new_blue.png = " + xb);
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(ModelUtil.loadResourceAsStream(getClass(), "/reports/ExperimentReport.jasper"));
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(experiments);
        Map<String, Object> params = new HashMap<>();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
//                JasperFillManager.getInstance(readOnlyStreamingService.getContext()).fill(jasperReport, params, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
