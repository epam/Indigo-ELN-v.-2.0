package com.epam.indigoeln.bingodb.service;

import com.epam.indigo.Bingo;
import com.epam.indigo.BingoObject;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.bingodb.config.IndigoProvider;
import com.epam.indigoeln.bingodb.domain.BingoStructure;
import com.epam.indigoeln.bingodb.exception.BingoDbException;
import lombok.Synchronized;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BingoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BingoService.class);

    private static final String PREFIX_MOL = "MOL";
    private static final String PREFIX_RXN = "RXN";

    private static final String ERROR_NOT_A_MOLECULE_OR_REACTION = "Not a molecule or reaction";
    private static final String ERROR_NOT_A_MOLECULE = "Not a molecule";
    private static final String ERROR_NOT_A_REACTION = "Not a reaction";

    private final IndigoProvider indigoProvider;

    private final Indigo moleculeIndigo;
    private final Indigo reactionIndigo;

    private final Bingo moleculeBingo;
    private final Bingo reactionBingo;

    @Autowired
    public BingoService(IndigoProvider indigoProvider,
                        Indigo moleculeIndigo,
                        Indigo reactionIndigo,
                        Bingo moleculeBingo,
                        Bingo reactionBingo) {
        this.indigoProvider = indigoProvider;
        this.moleculeIndigo = moleculeIndigo;
        this.reactionIndigo = reactionIndigo;
        this.moleculeBingo = moleculeBingo;
        this.reactionBingo = reactionBingo;
    }

    /* Structures */

    @Synchronized
    public BingoStructure getById(String id) {
        if (isMoleculeId(id)) {
            return new BingoStructure(id, moleculeBingo.getRecordById(getIntId(id)).molfile());
        }
        if (isReactionId(id)) {
            return new BingoStructure(id, reactionBingo.getRecordById(getIntId(id)).rxnfile());
        }
        return null;
    }

    @Synchronized
    public BingoStructure insert(String s) {
        if (isMolecule(s)) {
            return getById(createId(moleculeBingo.insert(moleculeIndigo.loadMolecule(s)), s));
        }
        if (isReaction(s)) {
            return getById(createId(reactionBingo.insert(reactionIndigo.loadReaction(s)), s));
        }
        throw new BingoDbException(ERROR_NOT_A_MOLECULE_OR_REACTION);
    }

    @Synchronized
    public BingoStructure update(String id, String s) {
        if (!isMolecule(s) && !isReaction(s)) {
            throw new BingoDbException(ERROR_NOT_A_MOLECULE_OR_REACTION);
        }
        delete(id);
        return insert(s);
    }

    @Synchronized
    public void delete(String id) {
        if (isMoleculeId(id)) {
            moleculeBingo.delete(getIntId(id));
        }
        if (isReactionId(id)) {
            reactionBingo.delete(getIntId(id));
        }
    }

    /* Search */

    @Synchronized
    public List<BingoStructure> searchMoleculeExact(String s, String options) {
        if (isMolecule(s)) {
            return result(moleculeBingo.searchExact(moleculeIndigo.loadMolecule(s), options), PREFIX_MOL);
        }
        throw new BingoDbException(ERROR_NOT_A_MOLECULE);
    }

    @Synchronized
    public List<BingoStructure> searchMoleculeSub(String s, String options) {
        if (isMolecule(s)) {
            return result(moleculeBingo.searchSub(moleculeIndigo.loadQueryMolecule(s), options), PREFIX_MOL);
        }
        throw new BingoDbException(ERROR_NOT_A_MOLECULE);
    }

    @Synchronized
    public List<BingoStructure> searchMoleculeSim(String s, Float min, Float max, String metric) {
        if (isMolecule(s)) {
            return result(moleculeBingo.searchSim(moleculeIndigo.loadMolecule(s), min, max, metric), PREFIX_MOL);
        }
        throw new BingoDbException(ERROR_NOT_A_MOLECULE);
    }

    @Synchronized
    public List<BingoStructure> searchMoleculeMolFormula(String molFormula, String options) {
        return result(moleculeBingo.searchMolFormula(molFormula, options), PREFIX_MOL);
    }

    @Synchronized
    public List<BingoStructure> searchReactionExact(String s, String options) {
        if (isReaction(s)) {
            return result(reactionBingo.searchExact(reactionIndigo.loadReaction(s), options), PREFIX_RXN);
        }
        if (isMolecule(s)) {
            List<BingoStructure> result = new ArrayList<>();

            IndigoObject rxn1 = reactionIndigo.createReaction();
            rxn1.addReactant(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchExact(rxn1, options), PREFIX_RXN));

            IndigoObject rxn2 = reactionIndigo.createReaction();
            rxn2.addProduct(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchExact(rxn2, options), PREFIX_RXN));

            return result;
        }
        throw new BingoDbException(ERROR_NOT_A_REACTION);
    }

    @Synchronized
    public List<BingoStructure> searchReactionSub(String s, String options) {
        if (isReaction(s)) {
            return result(reactionBingo.searchSub(reactionIndigo.loadQueryReaction(s), options), PREFIX_RXN);
        }
        if (isMolecule(s)) {
            List<BingoStructure> result = new ArrayList<>();

            IndigoObject rxn1 = reactionIndigo.createQueryReaction();
            rxn1.addReactant(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchSub(rxn1, options), PREFIX_RXN));

            IndigoObject rxn2 = reactionIndigo.createQueryReaction();
            rxn2.addProduct(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchSub(rxn2, options), PREFIX_RXN));

            return result;
        }
        throw new BingoDbException(ERROR_NOT_A_REACTION);
    }

    @Synchronized
    public List<BingoStructure> searchReactionSim(String s, Float min, Float max, String metric) {
        if (isReaction(s)) {
            return result(reactionBingo.searchSim(reactionIndigo.loadReaction(s), min, max, metric), PREFIX_RXN);
        }
        if (isMolecule(s)) {
            List<BingoStructure> result = new ArrayList<>();

            IndigoObject rxn1 = reactionIndigo.createReaction();
            rxn1.addReactant(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchSim(rxn1, min, max, metric), PREFIX_RXN));

            IndigoObject rxn2 = reactionIndigo.createReaction();
            rxn2.addProduct(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchSim(rxn2, min, max, metric), PREFIX_RXN));

            return result;
        }
        throw new BingoDbException(ERROR_NOT_A_REACTION);
    }

    /* Common */

    private List<BingoStructure> result(BingoObject o, String prefix) {
        List<BingoStructure> result = new ArrayList<>();

        while (o.next()) {
            result.add(getById(prefix + o.getCurrentId()));
        }

        return result;
    }

    private Integer getIntId(String id) {
        if (isMoleculeId(id)) {
            return Integer.valueOf(id.substring(id.indexOf(PREFIX_MOL) + PREFIX_MOL.length()));
        }
        if (isReactionId(id)) {
            return Integer.valueOf(id.substring(id.indexOf(PREFIX_RXN) + PREFIX_RXN.length()));
        }
        return Integer.valueOf(id);
    }

    private String createId(Integer id, String s) {
        if (isMolecule(s)) {
            return PREFIX_MOL + id;
        }
        if (isReaction(s)) {
            return PREFIX_RXN + id;
        }
        return Integer.toString(id);
    }

    private boolean isMoleculeId(String id) {
        return StringUtils.startsWith(id, PREFIX_MOL);
    }

    private boolean isReactionId(String id) {
        return StringUtils.startsWith(id, PREFIX_RXN);
    }

    private boolean isMolecule(String s) {
        try {
            indigoProvider.indigo().loadMolecule(s);
            return true;
        } catch (Exception e) {
            LOGGER.trace("isMolecule", e);
        }
        return false;
    }

    private boolean isReaction(String s) {
        try {
            indigoProvider.indigo().loadReaction(s);
            return true;
        } catch (Exception e) {
            LOGGER.trace("isReaction", e);
        }
        return false;
    }
}
