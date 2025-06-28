package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.reaction.model.SaltCodeRef;
import com.google.common.math.DoubleMath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;

@Slf4j
@ApplicationScoped
public class MolWeightCalculator {

    private static final double HYDROGEN_MASS = 1.00784;

    @Inject
    IndigoAPI indigo;

    public double calculateMolWeightWithoutSalt(String molFile) {
        return indigo.withSession(indigoSession -> {
            IndigoAPI.IndigoMolecule molecule = indigoSession.loadMolecule(molFile);
            return molecule.molecularWeight();
        });
    }

    public double calculateMolWeightWithSalt(String molFile, SaltCodeRef salt, double saltEQ) {
        // Input data:
        // molWeight - mol weight of user drawn (or selected by "Analyze RXN") main compound
        // moleculeCharge - electric charge of main compound
        // saltCharge - electric charge of selected salt code (from dictionary)
        // saltMolWeight - mol weight of selected salt code (from dictionary)
        // saltEq is either positive integer (meaning 1 main compound + saltEq copies of added compound) or 0.5 (meaning 2 copies of main compounds + 1 copy of added compound)
        // ---
        // totalBaseWeight = molWeight * baseEQ
        //     - total mol weight of main compound
        //
        // totalBaseCharge = moleculeCharge * baseEQ
        //     - total charge of main compound
        //
        // expectedCharge = - (saltCharge * acidEQ)
        //     - expected charge of main compound (negative to total charge of added compound)
        //
        // addHydrogen = expectedCharge - totalBaseCharge
        //     - >0: add hydrogen atoms to make main compound positively charged
        //     - <0: remove hydrogen atoms to make main compound negatively charged
        //     - 0: nothing: either user already drawn an ion with correct charge, or salt code is neutral
        //
        // totalBaseWeightWithHydrogen = totalBaseWeight + addHydrogen * 1.008
        //     - total mol weight of main compound, padded to expected charge
        //
        // totalSaltWeight = saltMolWeight * acidEQ
        //     - total mol weight of added compound
        //
        // finalMolWeight = totalBaseWeightWithHydrogen + totalSaltWeight
        return indigo.withSession(indigoSession -> {
            IndigoAPI.IndigoMolecule molecule = indigoSession.loadMolecule(molFile);
            double molWeight = molecule.molecularWeight();
            int moleculeCharge = 0;
            for (IndigoAPI.IndigoAtom atom : molecule.atoms()) {
                moleculeCharge += atom.charge();
            }
            int mainEQ, addEQ;
            if (DoubleMath.fuzzyEquals(saltEQ, 0.5, 0.0001)) {
                mainEQ = 2;
                addEQ = 1;
            } else if (DoubleMath.fuzzyEquals(saltEQ, Math.round(saltEQ), 0.0001)) {
                mainEQ = 1;
                addEQ = (int) Math.round(saltEQ);
                validate(addEQ > 0, "saltEQ must be positive");
            } else {
                throw new InvalidRequestException("saltEQ must be 0.5 or integer");
            }
            double totalBaseWeight = molWeight * mainEQ;
            int totalBaseCharge = moleculeCharge * mainEQ;
            int expectedCharge = -(salt.getCharge() * addEQ);
            int addHydrogen = expectedCharge - totalBaseCharge;
            // TODO validate if there are enough hydrogen to remove
            double totalBaseWeightWithHydrogen = totalBaseWeight + addHydrogen * HYDROGEN_MASS;
            double totalSaltWeight = salt.getMolWeight() * addEQ;
            return totalBaseWeightWithHydrogen + totalSaltWeight;
        });
    }
}
