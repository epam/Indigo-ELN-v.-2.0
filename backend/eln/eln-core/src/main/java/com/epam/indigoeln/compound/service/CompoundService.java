package com.epam.indigoeln.compound.service;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.common.model.units.MolWeightUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.repository.CompoundRepository;
import com.epam.indigoeln.compound.repository.MarkedSampleRepository;
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

import java.math.BigDecimal;
import java.util.UUID;

import static com.epam.indigoeln.eln.model.BuiltInDictionary.SALT_CODE;
import static com.epam.indigoeln.eln.model.BuiltInDictionary.STEREOISOMER_CODE;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.MOL_WEIGHT_DECIMAL_PLACES;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class CompoundService {

    @Inject
    CompoundRepository compoundRepository;
    @Inject
    MarkedSampleRepository markedSampleRepository;
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

    public CompoundEntity findOrCreate(IndigoMolecule molecule, StereoisomerCodeRef stereoisomerCode, SaltCodeRef saltCode, @Nullable BigDecimal saltEQ, @NotNull SampleSource source, @Nullable String compoundKey, @Nullable String chemicalName) {
        String canSmiles = molecule.canonicalSmiles();
        CompoundKey key = new CompoundKey(canSmiles, stereoisomerCode.getId(), saltCode.getId(), saltEQ);
        CompoundEntity compound = compoundRepository.findByCompoundKey(key);
        boolean isNew = compound == null;
        if (isNew) {
            compound = new CompoundEntity(source, compoundKey, canSmiles, dictionaryService.lookup(stereoisomerCode), dictionaryService.lookup(saltCode), saltEQ);
            compound.setMolFile(molecule.molfile());
            compound.setMolWeight(molWeightCalculator.calculateMolWeight(molecule.molfile(), saltCode, compound.getSaltEQ()));
            compound.setExactMass(molWeightCalculator.calculateExactMass(molecule.molfile()));
            compound.setFormula(new MolFormula(molecule.molecularFormula()));
            indigoRenderer.setRenderOptions("svg", 300, 200);
            byte[] buf = indigoRenderer.renderToBuffer(molecule);
            compound.setPicture(buf);
        }
        if (compound.getSource() == SampleSource.VIRTUAL) {
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

    public CompoundEntity findOrCreate(IndigoMolecule molecule, @NotNull SampleSource source, @Nullable String compoundKey, @Nullable String chemicalName) {
        return findOrCreate(molecule, dictionaryService.getDefault(STEREOISOMER_CODE), dictionaryService.getDefault(SALT_CODE), null, source, compoundKey, chemicalName);
    }

    public CompoundEntity findOrCreate(String molfile, StereoisomerCodeRef stereoisomerCode, SaltCodeRef saltCode, @Nullable BigDecimal saltEQ, @NotNull SampleSource source, @Nullable String compoundKey, @Nullable String chemicalName) {
        IndigoMolecule molecule = indigo.loadMolecule(molfile);
        return findOrCreate(molecule, stereoisomerCode, saltCode, saltEQ, source, compoundKey, chemicalName);
    }

    public CompoundRef compoundRef(CompoundEntity compound) {
        return new CompoundRef(
                compound.getId(),
                dictionaryService.get(compound.getStereoisomerCode()),
                dictionaryService.get(compound.getSaltCode()),
                compound.getSaltEQ(),
                compound.getCompoundKey(),
                compound.getFormula(),
                EnteredValue.fixedExact(compound.getMolWeight(), MOL_WEIGHT_DECIMAL_PLACES, MolWeightUnit.G_PER_MOL),
                EnteredValue.fixedExact(compound.getExactMass(), MOL_WEIGHT_DECIMAL_PLACES, NoUnit.NO_UNIT),
                compound.getCasNumber()
        );
    }

    public CompoundRef compoundRef(IndigoMolecule molecule) {
        return compoundRef(findOrCreate(molecule, SampleSource.VIRTUAL, null, null));
    }

    public CompoundRef compoundRef(IndigoMolecule molecule, StereoisomerCodeRef stereoisomerCode, SaltCodeRef saltCode, @Nullable BigDecimal saltEQ) {
        CompoundEntity compound = findOrCreate(molecule, stereoisomerCode, saltCode, saltEQ, SampleSource.VIRTUAL, null, null);
        return compoundRef(compound);
    }

    public CompoundRef unknownCompoundRef() {
        return new CompoundRef(null, dictionaryService.getDefault(STEREOISOMER_CODE), dictionaryService.getDefault(SALT_CODE), null, null, null, null, null, null);
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
}
