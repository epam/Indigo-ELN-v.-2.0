package com.epam.indigoeln.core.service.print.itext2.model.common;

import com.epam.indigoeln.web.rest.dto.FileDTO;

import java.util.List;

public class AttachmentsModel implements SectionModel {

    private List<FileDTO> files;

    public AttachmentsModel(List<FileDTO> files) {
        this.files = files;
    }

    public List<FileDTO> getFiles() {
        return files;
    }
}
