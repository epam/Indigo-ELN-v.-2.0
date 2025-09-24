package com.epam.indigoeln.integrationtests;

import com.epam.indigoeln.eln.api.MiscAPI;
import com.epam.indigoeln.eln.model.TotalCounts;
import com.epam.indigoeln.test.FeignUtil;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

//@ExtendWith(IntegrationEnvironmentResource.class)
//public class TestLambda {
//
//    @Test
//    void test() throws Exception {
//        AtomicReference<String> username = new AtomicReference<>("admin");
//        AtomicReference<String> authorization = new AtomicReference<>();
//        MiscAPI miscAPI = FeignUtil.buildFeignClient(new URI("http://localhost:28080"), MiscAPI.class, username, authorization);
//        TotalCounts totalCounts = miscAPI.getTotalCounts();
//        System.out.println(totalCounts);
//    }
//}
