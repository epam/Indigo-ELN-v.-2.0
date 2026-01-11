package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.patch.ACLEntryPatch;
import com.epam.indigoeln.reaction.model.patch.AttachmentPatch;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.SetDiffHandler;

public class ExperimentMetamodel {

    public static final Metamodel<ExperimentSnapshot, ExperimentPatch> INSTANCE = Metamodels.createMetamodel("Experiment", m -> {
        m.property("status", ExperimentSnapshot::getStatus, ExperimentSnapshot::setStatus, ExperimentPatch::getStatus, ExperimentPatch::setStatus);
        m.property("therapeuticArea", ExperimentSnapshot::getTherapeuticArea, ExperimentSnapshot::setTherapeuticArea, ExperimentPatch::getTherapeuticArea, ExperimentPatch::setTherapeuticArea);
        m.property("projectCode", ExperimentSnapshot::getProjectCode, ExperimentSnapshot::setProjectCode, ExperimentPatch::getProjectCode, ExperimentPatch::setProjectCode);
        m.property("description", ExperimentSnapshot::getDescription, ExperimentSnapshot::setDescription, ExperimentPatch::getDescription, ExperimentPatch::setDescription);
        m.property("deleted", ExperimentSnapshot::getDeleted, ExperimentSnapshot::setDeleted, ExperimentPatch::getDeleted, ExperimentPatch::setDeleted);
        m.property("attachments", ExperimentSnapshot::getAttachments, ExperimentSnapshot::setAttachments, ExperimentPatch::getAttachments, ExperimentPatch::setAttachments, new SetDiffHandler<>(AttachmentDTO::getId, new MetamodelDiffHandler<>(AttachmentMetamodel.INSTANCE, AttachmentPatch::new)));
        m.property("acl", ExperimentSnapshot::getAcl, ExperimentSnapshot::setAcl, ExperimentPatch::getAcl, ExperimentPatch::setAcl, new SetDiffHandler<>(ACLDetailsEntryDTO::getUsername, new MetamodelDiffHandler<>(ACLEntryMetamodel.INSTANCE, ACLEntryPatch::new)));
        m.property("model", ExperimentSnapshot::getModel, ExperimentSnapshot::setModel, ExperimentPatch::getModel, ExperimentPatch::setModel, new MetamodelDiffHandler<>(ExperimentModelMetamodel.INSTANCE, ExperimentModelPatch::new));
    });
}
