package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.patch.ACLEntryPatch;
import com.epam.indigoeln.reaction.model.patch.AttachmentPatch;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.DefaultDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.ExperimentRefDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.SetDiffHandler;

import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ExperimentMetamodel {

    public static final ModelProperty<ExperimentSnapshot, String, ExperimentPatch, String> TITLE = property("title",ExperimentSnapshot::getTitle, ExperimentSnapshot::setTitle, ExperimentPatch::getTitle, ExperimentPatch::setTitle);
    public static final ModelProperty<ExperimentSnapshot, ExperimentStatus, ExperimentPatch, ExperimentStatus> STATUS = property("status", ExperimentSnapshot::getStatus, ExperimentSnapshot::setStatus, ExperimentPatch::getStatus, ExperimentPatch::setStatus);
    public static final ModelProperty<ExperimentSnapshot, DictionaryItemRef, ExperimentPatch, DictionaryItemRef> THERAPEUTIC_AREA = property("therapeuticArea", ExperimentSnapshot::getTherapeuticArea, ExperimentSnapshot::setTherapeuticArea, ExperimentPatch::getTherapeuticArea, ExperimentPatch::setTherapeuticArea);
    public static final ModelProperty<ExperimentSnapshot, DictionaryItemRef, ExperimentPatch, DictionaryItemRef> PROJECT_CODE = property("projectCode", ExperimentSnapshot::getProjectCode, ExperimentSnapshot::setProjectCode, ExperimentPatch::getProjectCode, ExperimentPatch::setProjectCode);
    public static final ModelProperty<ExperimentSnapshot, String, ExperimentPatch, String> DESCRIPTION = property("description", ExperimentSnapshot::getDescription, ExperimentSnapshot::setDescription, ExperimentPatch::getDescription, ExperimentPatch::setDescription);
    public static final ModelProperty<ExperimentSnapshot, String, ExperimentPatch, String> LITERATURE = property("literature", ExperimentSnapshot::getLiterature, ExperimentSnapshot::setLiterature, ExperimentPatch::getLiterature, ExperimentPatch::setLiterature);
    public static final ModelProperty<ExperimentSnapshot, UserRef, ExperimentPatch, UserRef> BATCH_CREATOR = property("batchCreator", ExperimentSnapshot::getBatchCreator, ExperimentSnapshot::setBatchCreator, ExperimentPatch::getBatchCreator, ExperimentPatch::setBatchCreator);
    public static final ModelProperty<ExperimentSnapshot, Set<ExperimentRef>, ExperimentPatch, ?> LINKED_EXPERIMENTS = property("linkedExperiments", ExperimentSnapshot::getLinkedExperiments, ExperimentSnapshot::setLinkedExperiments, ExperimentPatch::getLinkedExperiments, ExperimentPatch::setLinkedExperiments, new SetDiffHandler<>(ExperimentRef::getId, ExperimentRefDiffHandler.INSTANCE));
    public static final ModelProperty<ExperimentSnapshot, Set<ExperimentRef>, ExperimentPatch, ?> CONTINUED_FROM = property("continuedFrom", ExperimentSnapshot::getContinuedFrom, ExperimentSnapshot::setContinuedFrom, ExperimentPatch::getContinuedFrom, ExperimentPatch::setContinuedFrom, new SetDiffHandler<>(ExperimentRef::getId, ExperimentRefDiffHandler.INSTANCE));
    public static final ModelProperty<ExperimentSnapshot, Set<ExperimentRef>, ExperimentPatch, ?> CONTINUED_TO = property("continuedTo", ExperimentSnapshot::getContinuedTo, ExperimentSnapshot::setContinuedTo, ExperimentPatch::getContinuedTo, ExperimentPatch::setContinuedTo, new SetDiffHandler<>(ExperimentRef::getId, ExperimentRefDiffHandler.INSTANCE));
    public static final ModelProperty<ExperimentSnapshot, Boolean, ExperimentPatch, Boolean> DELETED = property("deleted", ExperimentSnapshot::getDeleted, ExperimentSnapshot::setDeleted, ExperimentPatch::getDeleted, ExperimentPatch::setDeleted, DefaultDiffHandler.DEFAULT_FALSE_INSTANCE);
    public static final ModelProperty<ExperimentSnapshot, Set<AttachmentDTO>, ExperimentPatch, ?> ATTACHMENTS = property("attachments", ExperimentSnapshot::getAttachments, ExperimentSnapshot::setAttachments, ExperimentPatch::getAttachments, ExperimentPatch::setAttachments, new SetDiffHandler<>(AttachmentDTO::getId, new MetamodelDiffHandler<>(AttachmentMetamodel.INSTANCE, AttachmentPatch::new)));
    public static final ModelProperty<ExperimentSnapshot, Set<ACLDetailsEntryDTO>, ExperimentPatch, ?> ACL = property("acl", ExperimentSnapshot::getAcl, ExperimentSnapshot::setAcl, ExperimentPatch::getAcl, ExperimentPatch::setAcl, new SetDiffHandler<>(ACLDetailsEntryDTO::getUsername, new MetamodelDiffHandler<>(ACLEntryMetamodel.INSTANCE, ACLEntryPatch::new)));
    public static final ModelProperty<ExperimentSnapshot, ExperimentModel, ExperimentPatch, ExperimentModelPatch> MODEL = property("model", ExperimentSnapshot::getModel, ExperimentSnapshot::setModel, ExperimentPatch::getModel, ExperimentPatch::setModel, new MetamodelDiffHandler<>(ExperimentModelMetamodel.INSTANCE, ExperimentModelPatch::new));

    public static final Metamodel<ExperimentSnapshot, ExperimentPatch> INSTANCE = new Metamodel<>("Experiment", List.of(
            TITLE,
            STATUS,
            THERAPEUTIC_AREA,
            PROJECT_CODE,
            DESCRIPTION,
            LITERATURE,
            BATCH_CREATOR,
            LINKED_EXPERIMENTS,
            CONTINUED_FROM,
            CONTINUED_TO,
            DELETED,
            ATTACHMENTS,
            ACL,
            MODEL
    ));
}
