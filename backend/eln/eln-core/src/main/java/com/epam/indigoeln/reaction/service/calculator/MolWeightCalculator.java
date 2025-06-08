package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.config.IndigoAPI;
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
        log.debug("!!! calculateMolWeightWithoutSalt 1");
        IndigoObject molecule = indigo.loadMolecule(molFile);
        log.debug("!!! calculateMolWeightWithoutSalt 2");
        return molecule.molecularWeight();
    }

    public double calculateMolWeightWithSalt(String molFile, SaltCodeRef salt, double saltEQ) {
        // Входные данные:
        // molWeight - молвейт нарисованного (или выбранного по analyze RXN) базового вещества
        // moleculeCharge - заряд нарисованного или выбранного базового вещества
        // saltCharge - заряд выбранной соли
        // saltMolWeight - молвейт выбранной соли
        // saltEq задается двумя цифрами: отдельно количество оснований baseEQ и кислотных остатков acidEQ; можно выбирать из списка, если вариантов не очень много (1/3, 1/2, 1, 2, 3/2, 3)
        // ---
        // totalBaseWeight = molWeight * baseEQ
        //     - общий вес оснований
        //
        // totalBaseCharge = moleculeCharge * baseEQ
        //     - текущий общий заряд оснований
        //
        // expectedCharge = - (saltCharge * acidEQ)
        //     - ожидаемый общий заряд оснований (обратный к общему заряду кислотных остатков)
        //
        // addHydrogen = expectedCharge - totalBaseCharge
        //     - >0: добавить водороды, чтобы сделать основания положительными
        //     - <0: убрать водороды, чтобы сделать основания отрицительными (если кислота и основание перепутаны)
        //     - 0: ничего: либо юзер нарисовал уже ион с нужным зарядом, либо salt code нейтральный
        //
        // totalBaseWeightWithHydrogen = totalBaseWeight + addHydrogen * 1.008
        //      - вес с учетом добавленных/убранных водородов
        //
        // totalSaltWeight = saltMolWeight * acidEQ
        //     - общий вес кислотных остатков, в зависимости, сколько их там
        //
        // finalMolWeight = totalBaseWeightWithHydrogen + totalSaltWeight
        log.debug("!!! calculateMolWeightWithSalt 1");
        IndigoObject molecule = indigo.loadMolecule(molFile);
        log.debug("!!! calculateMolWeightWithSalt 2");
        double molWeight = molecule.molecularWeight();
        int moleculeCharge = 0;
        for (IndigoObject atom : molecule.iterateAtoms()) {
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
    }
}
