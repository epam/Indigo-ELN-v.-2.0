package com.epam.indigoeln.compound.service;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.common.model.units.MolWeightUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.repository.CompoundRepository;
import com.epam.indigoeln.compound.repository.SampleRepository;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.eln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.eln.indigowrapper.IndigoRendererAPI;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.SampleSource;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.GlobalSearchService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.CompoundKey;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.EnteredValue;
import com.epam.indigoeln.reaction.service.calculator.MolWeightCalculator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.MOL_WEIGHT_DECIMAL_PLACES;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class CompoundService {

    @Inject
    CompoundRepository compoundRepository;
    @Inject
    SampleRepository sampleRepository;
    @Inject
    SampleMapper sampleMapper;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    IndigoAPI indigo;
    @Inject
    IndigoRendererAPI indigoRenderer;
    @Inject
    MolWeightCalculator molWeightCalculator;
    @Inject
    UserService userService;
    @Inject
    GlobalSearchService globalSearchService;

    public CompoundEntity findOrCreate(IndigoMolecule molecule, @Nullable StereoisomerCodeRef stereoisomerCode, @Nullable SaltCodeRef saltCode, @Nullable Integer saltEQ100, @NotNull SampleSource source, @Nullable String compoundKey, @Nullable String chemicalName) {
        String canSmiles = molecule.canonicalSmiles();
        CompoundKey key = new CompoundKey(canSmiles, stereoisomerCode != null ? stereoisomerCode.getId() : null, saltCode != null ? saltCode.getId() : null, saltEQ100);
        CompoundEntity compound = compoundRepository.findByCompoundKey(key);
        boolean isNew = compound == null;
        if (isNew) {
            compound = new CompoundEntity();
        }
        compound.setCanSmiles(canSmiles);
        compound.setStereoisomerCode(dictionaryService.lookup(stereoisomerCode, true));
        compound.setSaltCode(dictionaryService.lookup(saltCode));
        compound.setSaltEQ100(saltEQ100);
        compound.setMolFile(molecule.molfile());
        compound.setMolWeight(molWeightCalculator.calculateMolWeight(molecule.molfile(), saltCode, compound.getSaltEQ()));
        compound.setExactMass(molWeightCalculator.calculateExactMass(molecule.molfile()));
        compound.setFormula(new MolFormula(molecule.molecularFormula()));
        indigoRenderer.setRenderOptions("svg", 300, 200);
        byte[] buf = indigoRenderer.renderToBuffer(molecule);
        compound.setPicture(buf);
        //noinspection ConstantValue
        if (compound.getSource() == null || compound.getSource() == SampleSource.ELN) {
            compound.setSource(source);
            compound.setCompoundKey(compoundKey);
        }
        if (compound.getChemicalName() == null) {
            compound.setChemicalName(chemicalName);
        }
        if (isNew) {
            compoundRepository.persist(compound);
            log.debug("new compound created: {}", compound);
        }
        return compound;
    }

    public CompoundEntity findOrCreate(String molfile, @Nullable StereoisomerCodeRef stereoisomerCode, @Nullable SaltCodeRef saltCode, @Nullable Integer saltEQ100, @NotNull SampleSource source, @Nullable String compoundKey, @Nullable String chemicalName) {
        IndigoMolecule molecule = indigo.loadMolecule(molfile);
        return findOrCreate(molecule, stereoisomerCode, saltCode, saltEQ100, source, compoundKey, chemicalName);
    }

    @Nullable
    public CompoundEntity findByCompoundKey(SampleSource source, String key) {
        return compoundRepository.findByCompoundKey(source, key);
    }

    public CompoundRef.Stored realCompoundRef(CompoundEntity compound) {
        return new CompoundRef.Stored(
                compound.getId(),
                dictionaryService.get(compound.getStereoisomerCode()),
                dictionaryService.get(compound.getSaltCode()),
                compound.getSaltEQ(),
                EnteredValue.fixedExact(compound.getMolWeight(), MOL_WEIGHT_DECIMAL_PLACES, MolWeightUnit.G_PER_MOL),
                EnteredValue.fixedExact(compound.getExactMass(), MOL_WEIGHT_DECIMAL_PLACES, NoUnit.NO_UNIT),
                compound.getFormula(),
                compound.getCompoundKey(),
                compound.getCasNumber(),
                calculateBatchMF(compound)
        );
    }

    public CompoundRef.Virtual virtualCompoundRef(IndigoMolecule molecule, @Nullable StereoisomerCodeRef stereoisomerCode, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ) {
        CompoundEntity compound = findOrCreate(molecule, stereoisomerCode, saltCode, saltEQ != null ? (int) (saltEQ * 100.0) : null, SampleSource.ELN, null, null);
        return virtualCompoundRef(compound);
    }

    public CompoundRef.Virtual virtualCompoundRef(CompoundEntity compound) {
        return new CompoundRef.Virtual(
                compound.getId(),
                compound.getFormula(),
                compound.getCompoundKey(),
                dictionaryService.get(compound.getStereoisomerCode()),
                dictionaryService.get(compound.getSaltCode()),
                compound.getSaltEQ(),
                EnteredValue.fixedExact(compound.getMolWeight(), MOL_WEIGHT_DECIMAL_PLACES, MolWeightUnit.G_PER_MOL),
                EnteredValue.fixedExact(compound.getExactMass(), MOL_WEIGHT_DECIMAL_PLACES, NoUnit.NO_UNIT),
                compound.getCasNumber(),
                calculateBatchMF(compound)
        );
    }

    public CompoundRef.Unknown unknownCompoundRef() {
        return new CompoundRef.Unknown();
    }

    public byte[] getCompoundPicture(UUID compoundID) {
        return compoundRepository.get(compoundID).getPicture();
    }

    public byte[] getExternalPicture(String inchi) {
        IndigoMolecule molecule = indigo.loadMolecule(inchi);
        indigoRenderer.setRenderOptions("svg", 300, 200);
        return indigoRenderer.renderToBuffer(molecule);
    }

    public CompoundEntity getCompound(UUID id) {
        return compoundRepository.get(id);
    }

    private String calculateBatchMF(CompoundEntity compound) {
        StringBuilder sb = new StringBuilder();
        String parentFormula = compound.getFormula().toHTMLString();
        sb.append(parentFormula);
        if (compound.getSaltCode() != null) {
            SaltCodeRef salt = dictionaryService.byId(compound.getSaltCode().getId());
            sb.append("&nbsp;*&nbsp;").append((compound.getSaltEQ())).append(" (").append(salt.getFormula()).append(")");
        }
        return sb.toString();
    }
}
