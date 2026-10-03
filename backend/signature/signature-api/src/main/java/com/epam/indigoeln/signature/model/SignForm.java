package com.epam.indigoeln.signature.model;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;

@NoArgsConstructor
@SuppressWarnings("NotNullFieldNotInitialized")
public class SignForm {

    @NotNull
    @FormParam("keystore")
    private File keystore;

    @Getter
    @NotNull
    @FormParam("keystore")
    FileUpload upload;

    @Getter
    @NotNull
    @FormParam("password")
    String password;

    public SignForm(File keystore, String password) {
        this.keystore = keystore;
        this.password = password;
    }
}
