package com.epam.indigoeln.common.config;

import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.exception.IncorrectRevisionException;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
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

import java.rmi.ServerException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@ApplicationScoped
public class BackendExceptionMapper {

    @ServerExceptionMapper(priority = 0)
    public RestResponse<List<ErrorDTO>> toResponse(Exception exception) {
        String message = exception.getClass().getName() + ": " + exception.getMessage();
        log.error(message, exception);
        return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, new ErrorDTO(message, exception));
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
        ErrorDTO[] errors = exception.getConstraintViolations().stream()
                .map(this::toErrorDTO)
                .toArray(ErrorDTO[]::new);
        log.warn("Validation errors:\n{}", StreamEx.of(errors).joining("\n"));
        return buildResponse(
                isReturnValue ? Response.Status.INTERNAL_SERVER_ERROR : Response.Status.BAD_REQUEST,
                errors
        );
    }

    @ServerExceptionMapper
    public RestResponse<List<ErrorDTO>> toResponse(AccessDeniedException exception) {
        log.warn(exception.getMessage());
        return buildResponse(Response.Status.FORBIDDEN, new ErrorDTO(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<List<ErrorDTO>> toResponse(EntityNotFoundException exception) {
        log.error(exception.getMessage());
        return buildResponse(Response.Status.NOT_FOUND, new ErrorDTO(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<List<ErrorDTO>> toResponse(InvalidRequestException exception) {
        log.error(exception.getMessage());
        return buildResponse(Response.Status.BAD_REQUEST, new ErrorDTO(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<List<ErrorDTO>> toResponse(IncorrectRevisionException exception) {
        log.error(exception.getMessage());
        return buildResponse(Response.Status.CONFLICT, new ErrorDTO(exception.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<List<ErrorDTO>> toResponse(WebApplicationException exception) {
        log.error("WebApplicationException", exception);
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        return buildResponse(exception.getResponse().getStatusInfo().toEnum(), new ErrorDTO(cause.getMessage()));
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
