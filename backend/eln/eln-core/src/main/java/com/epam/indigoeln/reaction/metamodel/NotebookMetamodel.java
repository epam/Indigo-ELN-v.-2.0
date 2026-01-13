package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.patch.NotebookPatch;

public class NotebookMetamodel {

    public static final Metamodel<NotebookSnapshot, NotebookPatch> INSTANCE = Metamodels.createMetamodel("Notebook", m -> {
        m.property("name", NotebookSnapshot::getName, NotebookSnapshot::setName, NotebookPatch::getName, NotebookPatch::setName);
        m.property("description", NotebookSnapshot::getDescription, NotebookSnapshot::setDescription, NotebookPatch::getDescription, NotebookPatch::setDescription);
    });
}
