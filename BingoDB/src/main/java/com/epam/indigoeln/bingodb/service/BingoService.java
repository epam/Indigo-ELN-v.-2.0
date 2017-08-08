package com.epam.indigoeln.bingodb.service;

import com.epam.indigo.Bingo;
import com.epam.indigo.BingoObject;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.bingodb.config.IndigoProvider;
import com.epam.indigoeln.bingodb.domain.BingoStructure;
import lombok.Synchronized;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BingoService {

    private static class Prefix {
        static final String MOL = "MOL";
        static final String RXN = "RXN";
    }

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
        throw new RuntimeException("Not a molecule or reaction");
    }

    @Synchronized
    public BingoStructure update(String id, String s) {
        if (!isMolecule(s) && !isReaction(s)) {
            throw new RuntimeException("Not a molecule or reaction");
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
            return result(moleculeBingo.searchExact(moleculeIndigo.loadMolecule(s), options), Prefix.MOL);
        }
        throw new RuntimeException("Not a molecule");
    }

    @Synchronized
    public List<BingoStructure> searchMoleculeSub(String s, String options) {
        if (isMolecule(s)) {
            return result(moleculeBingo.searchSub(moleculeIndigo.loadQueryMolecule(s), options), Prefix.MOL);
        }
        throw new RuntimeException("Not a molecule");
    }

    @Synchronized
    public List<BingoStructure> searchMoleculeSim(String s, Float min, Float max, String metric) {
        if (isMolecule(s)) {
            return result(moleculeBingo.searchSim(moleculeIndigo.loadMolecule(s), min, max, metric), Prefix.MOL);
        }
        throw new RuntimeException("Not a molecule");
    }

    @Synchronized
    public List<BingoStructure> searchMoleculeMolFormula(String molFormula, String options) {
        return result(moleculeBingo.searchMolFormula(molFormula, options), Prefix.MOL);
    }

    @Synchronized
    public List<BingoStructure> searchReactionExact(String s, String options) {
        if (isReaction(s)) {
            return result(reactionBingo.searchExact(reactionIndigo.loadReaction(s), options), Prefix.RXN);
        }
        if (isMolecule(s)) {
            List<BingoStructure> result = new ArrayList<>();

            IndigoObject rxn1 = reactionIndigo.createReaction();
            rxn1.addReactant(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchExact(rxn1, options), Prefix.RXN));

            IndigoObject rxn2 = reactionIndigo.createReaction();
            rxn2.addProduct(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchExact(rxn2, options), Prefix.RXN));

            return result;
        }
        throw new RuntimeException("Not a reaction");
    }

    @Synchronized
    public List<BingoStructure> searchReactionSub(String s, String options) {
        if (isReaction(s)) {
            return result(reactionBingo.searchSub(reactionIndigo.loadQueryReaction(s), options), Prefix.RXN);
        }
        if (isMolecule(s)) {
            List<BingoStructure> result = new ArrayList<>();

            IndigoObject rxn1 = reactionIndigo.createQueryReaction();
            rxn1.addReactant(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchSub(rxn1, options), Prefix.RXN));

            IndigoObject rxn2 = reactionIndigo.createQueryReaction();
            rxn2.addProduct(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchSub(rxn2, options), Prefix.RXN));

            return result;
        }
        throw new RuntimeException("Not a reaction");
    }

    @Synchronized
    public List<BingoStructure> searchReactionSim(String s, Float min, Float max, String metric) {
        if (isReaction(s)) {
            return result(reactionBingo.searchSim(reactionIndigo.loadReaction(s), min, max, metric), Prefix.RXN);
        }
        if (isMolecule(s)) {
            List<BingoStructure> result = new ArrayList<>();

            IndigoObject rxn1 = reactionIndigo.createReaction();
            rxn1.addReactant(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchSim(rxn1, min, max, metric), Prefix.RXN));

            IndigoObject rxn2 = reactionIndigo.createReaction();
            rxn2.addProduct(reactionIndigo.loadMolecule(s));
            result.addAll(result(reactionBingo.searchSim(rxn2, min, max, metric), Prefix.RXN));

            return result;
        }
        throw new RuntimeException("Not a reaction");
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
            return Integer.valueOf(id.substring(id.indexOf(Prefix.MOL) + Prefix.MOL.length()));
        }
        if (isReactionId(id)) {
            return Integer.valueOf(id.substring(id.indexOf(Prefix.RXN) + Prefix.RXN.length()));
        }
        return Integer.valueOf(id);
    }

    private String createId(Integer id, String s) {
        if (isMolecule(s)) {
            return Prefix.MOL + id;
        }
        if (isReaction(s)) {
            return Prefix.RXN + id;
        }
        return Integer.toString(id);
    }

    private boolean isMoleculeId(String id) {
        return StringUtils.startsWith(id, Prefix.MOL);
    }

    private boolean isReactionId(String id) {
        return StringUtils.startsWith(id, Prefix.RXN);
    }

    private boolean isMolecule(String s) {
        try {
            indigoProvider.indigo().loadMolecule(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isReaction(String s) {
        try {
            indigoProvider.indigo().loadReaction(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
