package com.epam.indigoeln.compound.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class CompoundServiceTest {

    @Inject
    CompoundService compoundService;

//    @Test
    void testLoadCompounds() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/Compound_000000001_000500000.1.sdf")) {
            var stats = compoundService.loadCompoundsFromFile(is);
            assertThat(stats.getProcessed()).isPositive();
        }
    }
}
