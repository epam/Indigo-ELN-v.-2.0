package com.epam.indigoeln.bingodb.web.rest.errors;

import com.epam.indigoeln.bingodb.web.rest.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleException(Exception e) {
        String message = "Error processing request: " + e.getMessage();
        LOGGER.error(message, e);
        return new ResponseEntity<>(new ResponseDTO(message), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
