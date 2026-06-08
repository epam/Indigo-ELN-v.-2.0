package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;

import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ExperimentMetamodel {

    public static final ModelProperty<ExperimentSnapshot, String> TITLE = property("title",ExperimentSnapshot::getTitle, ExperimentSnapshot::setTitle);
    public static final ModelProperty<ExperimentSnapshot, ExperimentStatus> STATUS = property("status", ExperimentSnapshot::getStatus, ExperimentSnapshot::setStatus);
    public static final ModelProperty<ExperimentSnapshot, TherapeuticAreaRef> THERAPEUTIC_AREA = property("therapeuticArea", ExperimentSnapshot::getTherapeuticArea, ExperimentSnapshot::setTherapeuticArea);
    public static final ModelProperty<ExperimentSnapshot, ProjectCodeRef> PROJECT_CODE = property("projectCode", ExperimentSnapshot::getProjectCode, ExperimentSnapshot::setProjectCode);
    public static final ModelProperty<ExperimentSnapshot, String> DESCRIPTION = property("description", ExperimentSnapshot::getDescription, ExperimentSnapshot::setDescription);
    public static final ModelProperty<ExperimentSnapshot, String> LITERATURE = property("literature", ExperimentSnapshot::getLiterature, ExperimentSnapshot::setLiterature);
    public static final ModelProperty<ExperimentSnapshot, UserRef> BATCH_CREATOR = property("batchCreator", ExperimentSnapshot::getBatchCreator, ExperimentSnapshot::setBatchCreator);
    public static final ModelProperty<ExperimentSnapshot, Set<ExperimentRef>> LINKED_EXPERIMENTS = property("linkedExperiments", ExperimentSnapshot::getLinkedExperiments, ExperimentSnapshot::setLinkedExperiments);
    public static final ModelProperty<ExperimentSnapshot, Set<ExperimentRef>> CONTINUED_FROM = property("continuedFrom", ExperimentSnapshot::getContinuedFrom, ExperimentSnapshot::setContinuedFrom);
    public static final ModelProperty<ExperimentSnapshot, Set<ExperimentRef>> CONTINUED_TO = property("continuedTo", ExperimentSnapshot::getContinuedTo, ExperimentSnapshot::setContinuedTo);
    public static final ModelProperty<ExperimentSnapshot, Boolean> DELETED = property("deleted", ExperimentSnapshot::isDeleted, ExperimentSnapshot::setDeleted);
    public static final ModelProperty<ExperimentSnapshot, Set<AttachmentDTO>> ATTACHMENTS = property("attachments", ExperimentSnapshot::getAttachments, ExperimentSnapshot::setAttachments);
    public static final ModelProperty<ExperimentSnapshot, Set<ACLEntryDTO>> ACL = property("acl", ExperimentSnapshot::getAcl, ExperimentSnapshot::setAcl);
    public static final ModelProperty<ExperimentSnapshot, ExperimentModel> MODEL = property("model", ExperimentSnapshot::getModel, ExperimentSnapshot::setModel, ExperimentModelMetamodel.INSTANCE);

    public static final Metamodel<ExperimentSnapshot> INSTANCE = new Metamodel<>("Experiment", List.of(
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
