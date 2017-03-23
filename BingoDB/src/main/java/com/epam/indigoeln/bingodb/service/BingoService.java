package com.epam.indigoeln.bingodb.service;

import com.epam.indigo.Bingo;
import com.epam.indigo.BingoObject;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.bingodb.config.IndigoProvider;
import com.epam.indigoeln.bingodb.domain.BingoStructure;
import org.apache.commons.lang.StringUtils;
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

    private final Object transaction = new Object();

    @Autowired
    private IndigoProvider indigoProvider;

    @Autowired
    private Indigo moleculeIndigo;

    @Autowired
    private Indigo reactionIndigo;

    @Autowired
    private Bingo moleculeBingo;

    @Autowired
    private Bingo reactionBingo;

    /* Structures */

    public BingoStructure getById(String id) {
        synchronized (transaction) {
            if (isMoleculeId(id)) {
                return new BingoStructure(id, moleculeBingo.getRecordById(getIntId(id)).molfile());
            }
            if (isReactionId(id)) {
                return new BingoStructure(id, reactionBingo.getRecordById(getIntId(id)).rxnfile());
            }
            return null;
        }
    }

    public BingoStructure insert(String s) {
        synchronized (transaction) {
            if (isMolecule(s)) {
                return getById(createId(moleculeBingo.insert(moleculeIndigo.loadMolecule(s)), s));
            }
            if (isReaction(s)) {
                return getById(createId(reactionBingo.insert(reactionIndigo.loadReaction(s)), s));
            }
            throw new RuntimeException("Not a molecule or reaction");
        }
    }

    public BingoStructure update(String id, String s) {
        synchronized (transaction) {
            if (!isMolecule(s) && !isReaction(s)) {
                throw new RuntimeException("Not a molecule or reaction");
            }
            delete(id);
            return insert(s);
        }
    }

    public void delete(String id) {
        synchronized (transaction) {
            if (isMoleculeId(id)) {
                moleculeBingo.delete(getIntId(id));
            }
            if (isReactionId(id)) {
                reactionBingo.delete(getIntId(id));
            }
        }
    }

    /* Search */

    public List<BingoStructure> searchMoleculeExact(String s, String options) {
        synchronized (transaction) {
            if (isMolecule(s)) {
                return result(moleculeBingo.searchExact(moleculeIndigo.loadMolecule(s), options), Prefix.MOL);
            }
            throw new RuntimeException("Not a molecule");
        }
    }

    public List<BingoStructure> searchMoleculeSub(String s, String options) {
        synchronized (transaction) {
            if (isMolecule(s)) {
                return result(moleculeBingo.searchSub(moleculeIndigo.loadQueryMolecule(s), options), Prefix.MOL);
            }
            throw new RuntimeException("Not a molecule");
        }
    }

    public List<BingoStructure> searchMoleculeSim(String s, Float min, Float max, String metric) {
        synchronized (transaction) {
            if (isMolecule(s)) {
                return result(moleculeBingo.searchSim(moleculeIndigo.loadMolecule(s), min, max, metric), Prefix.MOL);
            }
            throw new RuntimeException("Not a molecule");
        }
    }

    public List<BingoStructure> searchMoleculeMolFormula(String molFormula, String options) {
        synchronized (transaction) {
            return result(moleculeBingo.searchMolFormula(molFormula, options), Prefix.MOL);
        }
    }


    public List<BingoStructure> searchReactionExact(String s, String options) {
        synchronized (transaction) {
            if (isReaction(s)) {
                return result(reactionBingo.searchExact(reactionIndigo.loadReaction(s), options), Prefix.RXN);
            }
            if (isMolecule(s)) {
                ArrayList<BingoStructure> result = new ArrayList<BingoStructure>();

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
    }

    public List<BingoStructure> searchReactionSub(String s, String options) {
        synchronized (transaction) {
            if (isReaction(s)) {
                return result(reactionBingo.searchSub(reactionIndigo.loadQueryReaction(s), options), Prefix.RXN);
            }
            if (isMolecule(s)) {
                ArrayList<BingoStructure> result = new ArrayList<BingoStructure>();

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
    }

    public List<BingoStructure> searchReactionSim(String s, Float min, Float max, String metric) {
        synchronized (transaction) {
            if (isReaction(s)) {
                return result(reactionBingo.searchSim(reactionIndigo.loadReaction(s), min, max, metric), Prefix.RXN);
            }
            if (isMolecule(s)) {
                ArrayList<BingoStructure> result = new ArrayList<BingoStructure>();

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
    }

    public List<BingoStructure> searchReactionMolFormula(String molFormula, String options) {
        synchronized (transaction) {
            return result(reactionBingo.searchMolFormula(molFormula, options), Prefix.RXN);
        }
    }

    /* Common */

    private List<BingoStructure> result(BingoObject o, String prefix) {
        List<BingoStructure> result = new ArrayList<BingoStructure>();

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
