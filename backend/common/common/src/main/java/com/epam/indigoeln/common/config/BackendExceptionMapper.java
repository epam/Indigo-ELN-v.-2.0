package com.epam.indigoeln.common.config;

import com.epam.indigoeln.common.exception.UserFacingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.google.common.base.Throwables;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class BackendExceptionMapper {

    /**
     * A {@link UserFacingException} anywhere in the cause chain is returned with its own message and status.
     * Anything else is internal: its details are only logged, and the client gets a generic message with an error id.
     */
    @ServerExceptionMapper(priority = 0)
    public RestResponse<List<ErrorDTO>> toResponse(Exception exception) {
        return StreamEx.of(Throwables.getCausalChain(exception))
                .select(UserFacingException.class)
                .findFirst()
                .map(e -> {
                    log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage());
                    return buildResponse(e.getStatus(), new ErrorDTO(e.getMessage()));
                })
                .orElseGet(() -> internalError(exception));
    }

    @ServerExceptionMapper
    public RestResponse<List<ErrorDTO>> toResponse(MismatchedInputException exception) {
        String message = exception.getClass().getName() + ": " + exception.getMessage();
        log.error(message, exception);
        return buildResponse(Response.Status.BAD_REQUEST, new ErrorDTO(message, exception));
    }

    @ServerExceptionMapper
    public RestResponse<List<ErrorDTO>> toResponse(ConstraintViolationException exception) {
        boolean isReturnValue = false;
        for (Path.Node node : exception.getConstraintViolations().iterator().next().getPropertyPath()) {
            if (node.getKind() == ElementKind.RETURN_VALUE) {
                isReturnValue = true;
                break;
            }
        }
        if (isReturnValue) {
            return internalError(exception);
        }
        ErrorDTO[] errors = exception.getConstraintViolations().stream()
                .map(this::toErrorDTO)
                .toArray(ErrorDTO[]::new);
        log.warn("Validation errors:\n{}", StreamEx.of(errors).joining("\n"));
        return buildResponse(Response.Status.BAD_REQUEST, errors);
    }

    @ServerExceptionMapper
    public RestResponse<List<ErrorDTO>> toResponse(WebApplicationException exception) {
        if (exception.getResponse().getStatusInfo().getFamily() == Response.Status.Family.SERVER_ERROR) {
            return internalError(exception);
        }
        log.error("WebApplicationException", exception);
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        return buildResponse(exception.getResponse().getStatusInfo().toEnum(), new ErrorDTO(cause.getMessage()));
    }

    private RestResponse<List<ErrorDTO>> internalError(Throwable exception) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unhandled error, id {}", errorId, exception);
        return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, new ErrorDTO("Unexpected server error. Error ID: " + errorId));
    }

    @SneakyThrows
    private RestResponse<List<ErrorDTO>> buildResponse(Response.Status status, ErrorDTO... errors) {
        return RestResponse.ResponseBuilder.create(status, Arrays.asList(errors)).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }

    private ErrorDTO toErrorDTO(ConstraintViolation<?> v) {
        return new ErrorDTO(
                v.getRootBeanClass().getName(),
                v.getPropertyPath().toString(),
                v.getMessage(),
                v.getInvalidValue() != null ? v.getInvalidValue().toString() : null,
                null,
                v.getExecutableParameters() != null ? Arrays.toString(v.getExecutableParameters()) : null,
                v.getExecutableReturnValue() != null ? v.getExecutableReturnValue().toString() : null
        );
    }
}
