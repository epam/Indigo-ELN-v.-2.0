package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.MiscAPI;
import com.epam.indigoeln.eln.model.MiscInfo;
import com.epam.indigoeln.eln.model.TotalCounts;
import com.epam.indigoeln.eln.service.ProjectService;
import com.epam.indigoeln.eln.service.SupportService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ContentDispositionUtil.generateContentDisposition;

@Slf4j
@Path(BaseAPI.BASE_PATH)
public class MiscResource implements MiscAPI {

    @Inject
    ProjectService projectService;
    @Inject
    SupportService supportService;

    @Override
    public MiscInfo getInfo() {
        MiscInfo info = new MiscInfo();
        info.setApplication("Indigo ELN");
        try {
            InetAddress[] addresses = InetAddress.getAllByName("localhost");
            info.setIpv6Support(StreamEx.of(addresses)
                    .select(Inet6Address.class)
                    .findAny().isPresent());
        } catch (Exception e) {
            log.error("Cannot detect IPv6 support", e);
        }
        return info;
    }

    @Override
    public @NotNull @Valid TotalCounts getTotalCounts() {
        return projectService.getTotalCounts();
    }

    @Override
    public Map<String, String> migrate() {
        return supportService.migrate();
    }

    @Override
    public Map<String, String> insertTestData() {
        return supportService.insertTestData();
    }

    @Override
    public Response generateExperimentDetailsReport(UUID experimentID) {
        Pair<String, byte[]> result = supportService.generateExperimentDetailsReport(experimentID);
        return Response.ok(result.b())
                .header(HttpHeaders.CONTENT_DISPOSITION, generateContentDisposition(true, result.a()))
                .build();
    }
}
