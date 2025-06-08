package com.epam.indigoeln.common.config;

import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.common.exception.DictionaryNotFoundException;
import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class BackendExceptionMapper {

    @ServerExceptionMapper(priority = 0)
    public Response toResponse(Exception exception) {
        String message = exception.getClass().getName() + ": " + exception.getMessage();
        log.error(message, exception);
        return Response.serverError().entity(message).build();
    }

    @ServerExceptionMapper
    public Response toResponse(MismatchedInputException exception) {
        String message = exception.getClass().getName() + ": " + exception.getMessage();
        log.error(message, exception);
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    @ServerExceptionMapper
    public Response toResponse(ConstraintViolationException exception) {
        boolean isReturnValue = false;
        for (Path.Node node : exception.getConstraintViolations().iterator().next().getPropertyPath()) {
            if (node.getKind() == ElementKind.RETURN_VALUE) {
                isReturnValue = true;
                break;
            }
        }
        String message = "Validation failed:\n"
                + exception.getConstraintViolations().stream().map(this::format).collect(Collectors.joining());
        log.warn(message);
        return Response.status(isReturnValue ? Response.Status.INTERNAL_SERVER_ERROR : Response.Status.BAD_REQUEST).entity(message).build();
    }

    @ServerExceptionMapper
    public Response toResponse(AccessDeniedException exception) {
        log.warn(exception.getMessage());
        return Response.status(Response.Status.FORBIDDEN).entity(exception.getMessage()).build();
    }

    @ServerExceptionMapper
    public Response toResponse(EntityNotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return Response.status(Response.Status.FORBIDDEN).entity(exception.getMessage()).build();
    }

    @ServerExceptionMapper
    public Response toResponse(DictionaryNotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
    }

    @ServerExceptionMapper
    public Response toResponse(InvalidRequestException exception) {
        log.error(exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
    }

    private String format(ConstraintViolation<?> v) {
        String s = "\t-" + v.getRootBeanClass().getName() + "." + v.getPropertyPath() + ": " + v.getMessage();
        s += "\n\t\tInvalid value: " + v.getInvalidValue();
        if (v.getExecutableParameters() != null) {
            s += "\n\t\tParameters: " + Arrays.toString(v.getExecutableParameters());
        }
        if (v.getExecutableReturnValue() != null) {
            s += "\n\t\tReturn value: " + v.getExecutableReturnValue();
        }
        s += "\n\n";
        return s;
    }
}
