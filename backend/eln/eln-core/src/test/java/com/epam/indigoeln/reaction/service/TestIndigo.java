package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.indigowrapper.IndigoRendererAPI;
import com.epam.indigoeln.reaction.util.CalculationReportBuilder;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class TestIndigo extends ELNBaseTest {

    @Inject
    IndigoAPI indigoAPI;
    @Inject
    IndigoRendererAPI indigoRendererAPI;

    @Test
    public void test() {
        indigoRendererAPI.setRenderOptions("svg", 500, 200);
        try (CalculationReportBuilder reportBuilder = new CalculationReportBuilder(new File("build/test-indigo.html"))) {
            IndigoReaction reaction = indigoAPI.loadReaction(ModelUtil.loadResource(getClass(), "/reaction-with-duplicates-updated.rxn"));
            byte[] content = indigoRendererAPI.renderToBuffer(reaction);
            reportBuilder.addHeader("", "Initial");
            reportBuilder.addPicture("mutation", content, "image/svg+xml");
            List<IndigoMolecule> reactants = StreamEx.of(reaction.reactants().iterator()).toList();
            List<IndigoMolecule> catalysts = StreamEx.of(reaction.catalysts().iterator()).toList();
            List<IndigoMolecule> products = StreamEx.of(reaction.products().iterator()).toList();
            for (int skipped = 0; skipped < reactants.size(); skipped++) {
                List<IndigoMolecule> reactants2 = new ArrayList<>(reactants);
                reactants2.remove(skipped);
                reportBuilder.addHeader("", "Remove reactant " + skipped);
                reportBuilder.addPicture("mutation", rebuildReactionRxnFile(reactants2, catalysts, products), "image/svg+xml");
            }
            for (int skipped = 0; skipped < catalysts.size(); skipped++) {
                List<IndigoMolecule> catalysts2 = new ArrayList<>(catalysts);
                catalysts2.remove(skipped);
                reportBuilder.addHeader("", "Remove catalyst " + skipped);
                reportBuilder.addPicture("mutation", rebuildReactionRxnFile(reactants, catalysts2, products), "image/svg+xml");
            }
            for (int skipped = 0; skipped < products.size(); skipped++) {
                List<IndigoMolecule> products2 = new ArrayList<>(products);
                products2.remove(skipped);
                reportBuilder.addHeader("", "Remove product " + skipped);
                reportBuilder.addPicture("mutation", rebuildReactionRxnFile(reactants, catalysts, products2), "image/svg+xml");
            }
        }
    }

    public byte[] rebuildReactionRxnFile(List<IndigoMolecule> reactants, List<IndigoMolecule> catalysts, List<IndigoMolecule> products) {
        IndigoReaction reaction = indigoAPI.createReaction();
        reactants.forEach(reaction::addReactant);
        catalysts.forEach(reaction::addCatalyst);
        products.forEach(reaction::addProduct);
        return indigoRendererAPI.renderToBuffer(reaction);
    }
}
