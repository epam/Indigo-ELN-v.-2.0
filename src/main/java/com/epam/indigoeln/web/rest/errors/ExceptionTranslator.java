/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest.errors;

import com.epam.indigo.IndigoException;
import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.epam.indigoeln.core.service.exception.OperationNotAvailableException;
import com.epam.indigoeln.core.service.exception.PermissionIncorrectException;
import com.epam.indigoeln.core.service.exception.UriProcessingException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 */
@ControllerAdvice
public class ExceptionTranslator {

    @ExceptionHandler(ConcurrencyFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorDTO processConcurrencyError() {
        return new ErrorDTO(ErrorConstants.ERR_CONCURRENCY_FAILURE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO processValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        return processFieldErrors(fieldErrors);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO processJpaValidationError(ValidationException exception) {
        return (exception instanceof ConstraintViolationException)
                ? processConstraintViolationError((ConstraintViolationException) exception)
                : new ErrorDTO(ErrorConstants.ERR_VALIDATION, exception.getMessage());
    }

    private ErrorDTO processConstraintViolationError(ConstraintViolationException exception) {
        List<FieldErrorDTO> fieldErrors = exception.getConstraintViolations().stream().
                map(v -> new FieldErrorDTO(v.getRootBeanClass().getName(), v.getPropertyPath().toString(),
                        v.getMessage())).
                collect(Collectors.toList());
        return new ErrorDTO(ErrorConstants.ERR_VALIDATION, null, fieldErrors);
    }

    @ExceptionHandler(IndigoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO processJpaValidationError(IndigoException exception) {
        return new ErrorDTO(ErrorConstants.ERR_VALIDATION, exception.getMessage());
    }

    @ExceptionHandler(CustomParametrizedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ParametrizedErrorDTO processParametrizedValidationError(CustomParametrizedException ex) {
        return ex.getErrorDTO();
    }

    @ExceptionHandler(UriProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ParametrizedErrorDTO processUriProcessingError(UriProcessingException ex) {
        return ex.getErrorDTO();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorDTO processAccessDeniedException(AccessDeniedException e) {
        return new ErrorDTO(ErrorConstants.ERR_ACCESS_DENIED, e.getMessage());
    }

    @ExceptionHandler(OperationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ParametrizedErrorDTO operationDeniedException(OperationDeniedException ex) {
        return ex.getErrorDTO();
    }

    @ExceptionHandler(OperationNotAvailableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ParametrizedErrorDTO operationNotAvailableException(OperationNotAvailableException ex) {
        return ex.getErrorDTO();
    }

    @ExceptionHandler(PermissionIncorrectException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ParametrizedErrorDTO permissionIncorrectException(PermissionIncorrectException ex) {
        return ex.getErrorDTO();
    }

    private ErrorDTO processFieldErrors(List<FieldError> fieldErrors) {
        ErrorDTO dto = new ErrorDTO(ErrorConstants.ERR_VALIDATION);

        for (FieldError fieldError : fieldErrors) {
            dto.add(fieldError.getObjectName(), fieldError.getField(), fieldError.getCode());
        }

        return dto;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorDTO processMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return new ErrorDTO(ErrorConstants.ERR_METHOD_NOT_SUPPORTED, exception.getMessage());
    }

    @ExceptionHandler(IndigoRuntimeException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO processIndigoRuntimeException(IndigoRuntimeException exception) {
        return new ErrorDTO(ErrorConstants.ERR_URI_SYNTAX, exception.getMessage());
    }

    @ExceptionHandler(URISyntaxException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO processURISyntaxException(URISyntaxException exception) {
        return new ErrorDTO(ErrorConstants.ERR_URI_SYNTAX, exception.getMessage());
    }

    @ExceptionHandler(IOException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO processIOException(IOException exception) {
        return new ErrorDTO(ErrorConstants.ERR_IO, exception.getMessage());
    }


    /**
     * Default Error handler for Database Issues.
     * Database constraint violations and business logic issues, such as duplicate key exception, unique index etc.,
     * should be handed other way.
     *
     * @return Error
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO processDataAccessException() {
        return new ErrorDTO(ErrorConstants.ERR_DATABASE_ACCESS, "Internal Data Access error occurred");
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ResponseBody
    public ErrorDTO processFileLimitException(MultipartException e) {
        return new ErrorDTO(ErrorConstants.ERR_FILE_SIZE_LIMIT, e.getMessage());
    }
}
