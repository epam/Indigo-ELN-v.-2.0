package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.model.TemplateDTO;
import com.epam.indigoeln.eln.model.TemplateDetailsDTO;
import com.epam.indigoeln.eln.model.TemplateRequest;
import com.epam.indigoeln.eln.model.TemplateTab;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class TemplateMapper extends AbstractMapper {

    private ObjectReader componentReader;
    private ObjectWriter componentWriter;

    @Inject
    void setObjectMapper(ObjectMapper objectMapper) {
        this.componentReader = objectMapper.readerFor(new TypeReference<List<TemplateTab>>() {
        });
        this.componentWriter = objectMapper.writerFor(new TypeReference<List<TemplateTab>>() {
        });
    }

    @IgnoreBaseFields
    public abstract TemplateEntity requestToTemplate(TemplateRequest template);

    public abstract TemplateDTO entityToDTO(TemplateEntity entity);

    public abstract TemplateDetailsDTO entityToDetailsDTO(TemplateEntity entity);

    @SneakyThrows
    protected String templateTabsToJSON(List<TemplateTab> templateTabs) {
        return componentWriter.writeValueAsString(templateTabs);
    }

    @SneakyThrows
    protected List<TemplateTab> templateTabsFromJSON(String json) {
        return componentReader.readValue(json);
    }
}
