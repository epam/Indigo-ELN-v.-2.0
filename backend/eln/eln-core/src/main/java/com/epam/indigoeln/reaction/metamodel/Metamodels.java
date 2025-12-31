package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.patch.*;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

class Metamodels {

    static <C, P> Metamodel<C, P> createMetamodel(String name, Consumer<Metamodel<C, P>> builder) {
        Metamodel<C, P> metamodel = new Metamodel<>(name);
        builder.accept(metamodel);
        return metamodel;
    }

    static <C extends ReactionSample, A extends Anchor, P extends AbstractReactionSamplePatch<A>> void buildReactionSampleMetamodel(Metamodel<C, P> m) {
        m.enteredValueProperty("density", ReactionSample::getDensity, ReactionSample::setDensity, AbstractReactionSamplePatch::getDensity, AbstractReactionSamplePatch::setDensity);
        m.enteredValueProperty("molarity", ReactionSample::getMolarity, ReactionSample::setMolarity, AbstractReactionSamplePatch::getMolarity, AbstractReactionSamplePatch::setMolarity);
        m.enteredValueProperty("volume", ReactionSample::getVolume, ReactionSample::setVolume, AbstractReactionSamplePatch::getVolume, AbstractReactionSamplePatch::setVolume);
        m.enteredValueProperty("purity", ReactionSample::getPurity, ReactionSample::setPurity, AbstractReactionSamplePatch::getPurity, AbstractReactionSamplePatch::setPurity);
        m.<@Nullable STRCodeSample>property("strCode", ReactionSample::getStrCode, ReactionSample::setStrCode, AbstractReactionSamplePatch::getStrCode, AbstractReactionSamplePatch::setStrCode);
        m.property("healthHazards", ReactionSample::getHealthHazards, ReactionSample::setHealthHazards, AbstractReactionSamplePatch::getHealthHazards, AbstractReactionSamplePatch::setHealthHazards);
    }

    static <C extends ReactionRow, A extends Anchor, P extends AbstractReactionRowPatch<A>> void buildReactionRowMetamodel(Metamodel<C, P> m) {
        m.property("compound", ReactionRow::getCompound, ReactionRow::setCompound, AbstractReactionRowPatch::getCompound, AbstractReactionRowPatch::setCompound);
        m.enteredValueProperty("eq", ReactionRow::getEq, ReactionRow::setEq, AbstractReactionRowPatch::getEq, AbstractReactionRowPatch::setEq);
    }
}
