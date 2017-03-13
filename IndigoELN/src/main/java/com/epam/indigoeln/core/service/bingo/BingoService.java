package com.epam.indigoeln.core.service.bingo;

import com.epam.indigo.Bingo;
import com.epam.indigo.BingoObject;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.config.bingo.IndigoProvider;
import com.epam.indigoeln.core.service.calculation.CalculationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BingoService {

    private static final Logger log = LoggerFactory.getLogger(BingoService.class);

    private static final String MOL = "MOL";
    private static final String RXN = "RXN";

    private final Object transaction = new Object();

    @Autowired
    private IndigoProvider indigoProvider;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private Indigo moleculeIndigo;

    @Autowired
    private Indigo reactionIndigo;

    @Autowired
    private Bingo moleculeBingo;

    @Autowired
    private Bingo reactionBingo;

    /* Common */

    public Optional<String> getById(String id) {
        try {
            synchronized (transaction) {
                Integer intId = getId(id);
                if (intId != null) {
                    if (StringUtils.startsWith(id, MOL)) {
                        return Optional.of(moleculeBingo.getRecordById(intId).molfile());
                    }
                    if (StringUtils.startsWith(id, RXN)) {
                        return Optional.of(moleculeBingo.getRecordById(intId).rxnfile());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Cannot find by id=" + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<String> insert(String s) {
        try {
            synchronized (transaction) {
                if (calculationService.isMolecule(s)) {
                    return Optional.of(MOL + moleculeBingo.insert(moleculeIndigo.loadMolecule(s)));
                }
                if (calculationService.isReaction(s)) {
                    return Optional.of(RXN + reactionBingo.insert(reactionIndigo.loadReaction(s)));
                }
            }
        } catch (Exception e) {
            log.warn("Cannot insert: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<String> update(String id, String s) {
        try {
            synchronized (transaction) {
                delete(id);
                return insert(s);
            }
        } catch (Exception e) {
            log.warn("Cannot update by id=" + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    public void delete(String id) {
        try {
            synchronized (transaction) {
                Integer intId = getId(id);
                if (intId != null) {
                    if (StringUtils.startsWith(id, MOL)) {
                        moleculeBingo.delete(intId);
                    }
                    if (StringUtils.startsWith(id, RXN)) {
                        reactionBingo.delete(intId);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Cannot delete by id=" + id + ": " + e.getMessage());
        }
    }

    private Integer getId(String id) {
        if (StringUtils.startsWith(id, MOL)) {
            return Integer.valueOf(id.substring(id.indexOf(MOL) + MOL.length()));
        }
        if (StringUtils.startsWith(id, RXN)) {
            return Integer.valueOf(id.substring(id.indexOf(RXN) + RXN.length()));
        }
        return null;
    }

    private List<String> result(BingoObject object, String prefix) {
        List<String> result = new ArrayList<>();

        while (object.next()) {
            result.add(prefix + object.getCurrentId());
        }

        return result;
    }

    /* Molecule */

    public List<String> searchMoleculeExact(String molecule, String options) {
        try {
            synchronized (transaction) {
                return result(moleculeBingo.searchExact(moleculeIndigo.loadMolecule(molecule), options), MOL);
            }
        } catch (Exception e) {
            log.warn("Cannot search exact molecule: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchMoleculeSub(String molecule, String options) {
        try {
            synchronized (transaction) {
                return result(moleculeBingo.searchSub(moleculeIndigo.loadQueryMolecule(molecule), options), MOL);
            }
        } catch (Exception e) {
            log.warn("Cannot search sub molecule: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchMoleculeSim(String molecule, Float min, Float max, String metric) {
        try {
            synchronized (transaction) {
                return result(moleculeBingo.searchSim(moleculeIndigo.loadMolecule(molecule), min, max, metric), MOL);
            }
        } catch (Exception e) {
            log.warn("Cannot search sim molecule: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchMoleculeMolFormula(String formula, String options) {
        try {
            synchronized (transaction) {
                return result(moleculeBingo.searchMolFormula(formula, options), MOL);
            }
        } catch (Exception e) {
            log.warn("Cannot search molformula molecule: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    /* Reaction */

    public List<String> searchReactionExact(String reaction, String options) {
        try {
            synchronized (transaction) {
                if (calculationService.isReaction(reaction)) {
                    return result(reactionBingo.searchExact(reactionIndigo.loadReaction(reaction), options), RXN);
                } else {
                    List<String> result = new ArrayList<>();

                    IndigoObject rxn = reactionIndigo.createReaction();
                    rxn.addReactant(reactionIndigo.loadMolecule(reaction));

                    result.addAll(result(reactionBingo.searchExact(rxn, options), RXN));

                    rxn = reactionIndigo.createReaction();
                    rxn.addProduct(reactionIndigo.loadMolecule(reaction));

                    result.addAll(result(reactionBingo.searchExact(rxn, options), RXN));

                    return result;
                }
            }
        } catch (Exception e) {
            log.warn("Cannot search exact reaction: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchReactionSub(String reaction, String options) {
        try {
            synchronized (transaction) {
                if (calculationService.isReaction(reaction)) {
                    return result(reactionBingo.searchSub(reactionIndigo.loadQueryReaction(reaction), options), RXN);
                } else {
                    List<String> result = new ArrayList<>();

                    IndigoObject rxn = reactionIndigo.createQueryReaction();
                    rxn.addReactant(reactionIndigo.loadMolecule(reaction));

                    result.addAll(result(reactionBingo.searchSub(rxn, options), RXN));

                    rxn = reactionIndigo.createReaction();
                    rxn.addProduct(reactionIndigo.loadMolecule(reaction));

                    result.addAll(result(reactionBingo.searchSub(rxn, options), RXN));

                    return result;
                }
            }
        } catch (Exception e) {
            log.warn("Cannot search sub reaction: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchReactionSim(String reaction, Float min, Float max, String metric) {
        try {
            synchronized (transaction) {
                if (calculationService.isReaction(reaction)) {
                    return result(reactionBingo.searchSim(reactionIndigo.loadReaction(reaction), min, max, metric), RXN);
                } else {
                    List<String> result = new ArrayList<>();

                    IndigoObject rxn = reactionIndigo.createReaction();
                    rxn.addReactant(reactionIndigo.loadMolecule(reaction));

                    result.addAll(result(reactionBingo.searchSim(rxn, min, max, metric), RXN));

                    rxn = reactionIndigo.createReaction();
                    rxn.addProduct(reactionIndigo.loadMolecule(reaction));

                    result.addAll(result(reactionBingo.searchSim(rxn, min, max, metric), RXN));

                    return result;
                }
            }
        } catch (Exception e) {
            log.warn("Cannot search sim reaction: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchReactionMolFormula(String formula, String options) {
        try {
            synchronized (transaction) {
                return result(reactionBingo.searchMolFormula(formula, options), RXN);
            }
        } catch (Exception e) {
            log.warn("Cannot search molformula reaction: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
