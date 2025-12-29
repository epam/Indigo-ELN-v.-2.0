package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.patch.handler.Handlers;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Data
public class ExperimentSnapshot {

    public static void buildMetamodel(Metamodel<ExperimentSnapshot, ExperimentPatch> metamodel) {
        metamodel.setName("Experiment");
        metamodel.property("status", ExperimentSnapshot::getStatus, ExperimentSnapshot::setStatus, ExperimentPatch::getStatus, ExperimentPatch::setStatus);
        metamodel.property("therapeuticArea", ExperimentSnapshot::getTherapeuticArea, ExperimentSnapshot::setTherapeuticArea, ExperimentPatch::getTherapeuticArea, ExperimentPatch::setTherapeuticArea, null);
        metamodel.property("projectCode", ExperimentSnapshot::getProjectCode, ExperimentSnapshot::setProjectCode, ExperimentPatch::getProjectCode, ExperimentPatch::setProjectCode, null);
        metamodel.property("description", ExperimentSnapshot::getDescription, ExperimentSnapshot::setDescription, ExperimentPatch::getDescription, ExperimentPatch::setDescription);
        metamodel.property("deleted", ExperimentSnapshot::getDeleted, ExperimentSnapshot::setDeleted, ExperimentPatch::getDeleted, ExperimentPatch::setDeleted);
        metamodel.listProperty("attachments", ExperimentSnapshot::getAttachments, ExperimentSnapshot::setAttachments, ExperimentPatch::getAttachments, ExperimentPatch::setAttachments, null, Handlers.ATTACHMENT_SET);
        metamodel.listProperty("acl", ExperimentSnapshot::getAcl, ExperimentSnapshot::setAcl, ExperimentPatch::getAcl, ExperimentPatch::setAcl, null, Handlers.ACL_ENTRY_SET);
        metamodel.listProperty("model", ExperimentSnapshot::getModel, ExperimentSnapshot::setModel, ExperimentPatch::getModel, ExperimentPatch::setModel, null, Handlers.EXPERIMENT_MODEL);
    }

    private ExperimentStatus status;

    @Nullable
    private DictionaryItemRef therapeuticArea;

    @Nullable
    private DictionaryItemRef projectCode;

    @Nullable
    private String description;

    private Boolean deleted;

    @Nullable
    private Set<AttachmentDTO> attachments;

    @Nullable
    private Set<ACLEntryDTO> acl;

    @Nullable
    private ExperimentModel model;
}
