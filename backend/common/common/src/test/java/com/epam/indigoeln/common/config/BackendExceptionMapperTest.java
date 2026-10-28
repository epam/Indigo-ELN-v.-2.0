package com.epam.indigoeln.common.config;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BackendExceptionMapperTest {

    BackendExceptionMapper mapper = new BackendExceptionMapper();

    @Test
    void testInternalErrorHidesDetails() {
        RestResponse<List<ErrorDTO>> response = mapper.toResponse(new IllegalStateException("database detail"));
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getEntity()).singleElement().extracting(ErrorDTO::getMessage).asString()
                .startsWith("Unexpected server error. Error ID: ")
                .doesNotContain("database detail", "IllegalStateException");
    }

    @Test
    void testUserFacingErrorKeepsMessageAndStatus() {
        RestResponse<List<ErrorDTO>> response = mapper.toResponse(new EntityNotFoundException("Project", "123"));
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity()).singleElement().extracting(ErrorDTO::getMessage).isEqualTo("Project 123 not found");
    }

    @Test
    void testWrappedUserFacingError() {
        RestResponse<List<ErrorDTO>> response = mapper.toResponse(new RuntimeException("Failed to perform undo", new InvalidRequestException("Nothing to undo")));
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity()).singleElement().extracting(ErrorDTO::getMessage).isEqualTo("Nothing to undo");
    }
}
