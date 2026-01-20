package com.epam.indigoeln.common.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Throwables;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDTO {

    @JsonIgnore
    private String rootClass;

    @Nullable
    private String path;

    private String message;

    @Nullable
    private String value;

    @Nullable
    @JsonIgnore
    private Throwable exception;

    @Nullable
    @JsonIgnore
    private String executableParameters;

    @Nullable
    @JsonIgnore
    private String executableReturnValue;

    public ErrorDTO(String message) {
        this.message = message;
    }

    public ErrorDTO(String message, Throwable exception) {
        this.message = message;
    }

    @Nullable
    public String getStackTrace() {
        return exception != null ? Throwables.getStackTraceAsString(exception) : null;
    }
}
