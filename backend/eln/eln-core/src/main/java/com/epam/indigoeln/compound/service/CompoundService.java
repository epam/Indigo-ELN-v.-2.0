package com.epam.indigoeln.compound.service;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.CompoundKey;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.compound.repository.CompoundRepository;
import com.epam.indigoeln.compound.repository.SampleRepository;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.eln.util.MolFormulaFormatter;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoRendererAPI;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.service.calculator.MolWeightCalculator;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.epam.indigoeln.eln.util.ModelUtil.calculateCompoundKey;
import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;
import static com.epam.indigoeln.reaction.model.units.EnteredValue.fixed;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.MOL_WEIGHT_DECIMAL_PLACES;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.roundToDecimalPlaces;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class CompoundService {

    private static final String[] NAME_PROPERTIES = {"PUBCHEM_IUPAC_TRADITIONAL_NAME", "PUBCHEM_IUPAC_SYSTEMATIC_NAME", "PUBCHEM_IUPAC_OPENEYE_NAME"};
    private static final Map<String, CompoundExternalSource> COMPOUND_ID_PROPERTIES = Map.of(
            "PUBCHEM_COMPOUND_CID", CompoundExternalSource.PUBCHEM
    );

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

    public CompoundEntity findOrCreate(IndigoMolecule molecule, @Nullable StereoisomerCodeRef stereoisomerCode, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ, @Nullable Consumer<CompoundEntity> compoundConfigurer) {
        String canSmiles = molecule.canonicalSmiles();
        CompoundKey key = new CompoundKey(canSmiles, stereoisomerCode != null ? stereoisomerCode.getId() : null, saltCode != null ? saltCode.getId() : null, saltEQ != null ? (int) (saltEQ * 100) : null);
        CompoundEntity compound = compoundRepository.findByCompoundKey(key);
        if (compound == null) {
            compound = new CompoundEntity();
            if (compoundConfigurer != null) {
                compoundConfigurer.accept(compound);
            }
            compound.setCanSmiles(canSmiles);
            compound.setStereoisomerCode(dictionaryService.lookup(stereoisomerCode, true));
            compound.setSaltCode(dictionaryService.lookup(saltCode));
            compound.setSaltEQ(saltEQ);
            compound.setMolFile(molecule.molfile());
            compound.setMolWeight(molWeightCalculator.calculateMolWeight(molecule.molfile(), saltCode, saltEQ));
            compound.setExactMass(molWeightCalculator.calculateExactMass(molecule.molfile()));
            compound.setFormula(molecule.molecularFormula());
            compound.setCompoundKey(calculateCompoundKey(compound));
            indigoRenderer.setRenderOptions("svg", 300, 200);
            byte[] buf = indigoRenderer.renderToBuffer(molecule);
            compound.setPicture(buf);
            compoundRepository.persist(compound);
            log.debug("new compound created: {}", compound);
        }
        return compound;
    }

    public List<UUID> loadCompoundsFromFile(Path file, boolean createSamples) throws IOException {
        List<UUID> list = new ArrayList<>();
        for (IndigoMolecule molecule : indigo.iterateSDFile(file.toAbsolutePath().toString())) {
            CompoundEntity compound = findOrCreate(molecule, null, null, null, null);
            fillCompoundFromIndigo(molecule, compound);
            SampleEntity sample = createSamples ? findOrCreateDefaultSample(compound) : null;
            list.add(compound.getId());
        }
        log.info("Loaded {} compounds from file", list.size());
        return list;
    }

    public SampleEntity findOrCreateDefaultSample(CompoundEntity compound) {
        SampleEntity sample = sampleRepository.findDefaultSample(compound.getId());
        if (sample == null) {
            sample = new SampleEntity();
            compound.getSamples().add(sample);
            sample.setCompound(compound);
            updateDates(sample, userService.getCurrentUserEntity());
            sampleRepository.persist(sample);
        }
        return sample;
    }

    public CompoundRef.Stored realCompoundRef(CompoundEntity compound) {
        return new CompoundRef.Stored(
                compound.getId(),
                fixed(compound.getMolWeight(), MolWeightUnit.G_PER_MOL),
                fixed(compound.getExactMass(), NoUnit.NO_UNIT),
                MolFormulaFormatter.format(compound.getFormula()),
                compound.getCompoundKey(),
                compound.getCasNumber(),
                calculateBatchMF(compound)
        );
    }

    public CompoundRef.Virtual virtualCompoundRef(IndigoMolecule molecule, @Nullable StereoisomerCodeRef stereoisomerCode, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ) {
        CompoundEntity compound = findOrCreate(molecule, stereoisomerCode, saltCode, saltEQ, null);
        return virtualCompoundRef(compound);
    }

    public CompoundRef.Virtual virtualCompoundRef(CompoundEntity compound) {
        return new CompoundRef.Virtual(
                compound.getId(),
                MolFormulaFormatter.format(compound.getFormula()),
                compound.getCompoundKey(),
                dictionaryService.get(compound.getStereoisomerCode()),
                dictionaryService.get(compound.getSaltCode()),
                compound.getSaltEQ(),
                EnteredValue.fixed(compound.getMolWeight(), MolWeightUnit.G_PER_MOL),
                EnteredValue.fixed(compound.getExactMass(), NoUnit.NO_UNIT),
                compound.getCasNumber(),
                calculateBatchMF(compound)
        );
    }

    public CompoundRef.Unknown unknownCompoundRef() {
        return new CompoundRef.Unknown();
    }

    private void fillCompoundFromIndigo(IndigoMolecule molecule, CompoundEntity compound) {
        Map<String, String> properties = molecule.getProperties();
        compound.setFormula(molecule.molecularFormula());
        compound.setMolFile(molecule.molfile());
        compound.setMolWeight(roundToDecimalPlaces(molecule.molecularWeight(), MOL_WEIGHT_DECIMAL_PLACES));
        for (String property : NAME_PROPERTIES) {
            if (properties.containsKey(property)) {
                compound.setChemicalName(properties.get(property));
                break;
            }
        }
        COMPOUND_ID_PROPERTIES.forEach((property, source) -> {
            if (properties.containsKey(property)) {
                compound.setExternalSource(source);
                compound.setExternalNumber(properties.get(property));
            }
        });
    }

    public byte[] getCompoundPicture(UUID compoundID) {
        return compoundRepository.get(compoundID).getPicture();
    }

    public byte[] getExternalPicture(String inchi) {
        IndigoMolecule molecule = indigo.loadMolecule(inchi);
        indigoRenderer.setRenderOptions("svg", 300, 200);
        return indigoRenderer.renderToBuffer(molecule);
    }

    public SampleEntity getSample(UUID id) {
        return sampleRepository.get(id);
    }

    public CompoundEntity getCompound(UUID id) {
        return compoundRepository.get(id);
    }

    public SampleEntity registerSample(SampleRegistrationRequest request) {
        CompoundEntity compound = switch (request.getCompound()) {
            case CompoundRef.Stored stored -> compoundRepository.get(stored.getCompoundID());
            case CompoundRef.Virtual virtual -> compoundRepository.get(virtual.getCompoundID());
            case CompoundRef.Unknown unknown -> {
                throw new IllegalArgumentException("Cannot register a sample of an unknown compound");
            }
        };
        SampleEntity sample = new SampleEntity();
        sample.setCompound(compound);
        sample.setStrCode(generateStrCode(compound));
        sample.setNbkBatchNumber(request.getNbkBatchNumber());
        sample.setDensity(request.getDensity() != null ? request.getDensity().toBigDecimal() : null);
        sample.setMolarity(request.getMolarity() != null ? request.getMolarity().toBigDecimal() : null);
        sample.setMolarityUnit(request.getMolarity() != null ? request.getMolarity().getUnit() : null);
        sample.setPurity(request.getPurity());
        if (request.getHealthHazards() != null) {
            sample.getHealthHazards().addAll(dictionaryService.lookup(request.getHealthHazards()));
        }
        sample.setCompoundState(dictionaryService.lookup(request.getCompoundState()));
        sample.setBatchComment(request.getBatchComment());
        compound.getSamples().add(sample);
        updateDates(sample, userService.getCurrentUserEntity());
        sampleRepository.persist(sample);
        return sample;
    }

    private STRCodeSample generateStrCode(CompoundEntity compound) {
        STRCodeCompound compoundStrCode;
        if (compound.getStrCode() != null) {
            compoundStrCode = compound.getStrCode();
        } else {
            log.debug("registerSample: compound has no source code: {}", compound);
            CompoundKey key = new CompoundKey(compound.getCanSmiles(), compound.getStereoisomerCode() != null ? compound.getStereoisomerCode().getId() : null, compound.getSaltCode() != null ? compound.getSaltCode().getId() : null, compound.getSaltEQ100());
            STRCodeCompound strCodeWithoutSaltCode = compoundRepository.findSameSTRCodeByCompoundKeyWithoutSaltCode(key);
            log.debug("registerSample: strCodeWithoutSaltCode={}", strCodeWithoutSaltCode);
            int compoundCode = strCodeWithoutSaltCode != null
                    ? strCodeWithoutSaltCode.getCompoundCode()
                    : compoundRepository.getNextSTRCodeCompoundCode();
            SaltCodeRef saltCode = dictionaryService.get(compound.getSaltCode());
            compoundStrCode = new STRCodeCompound(compoundCode, saltCode != null ? Integer.parseInt(saltCode.getCode()) : 0);
            compound.setStrCode(compoundStrCode);
        }
        log.debug("registerSample: compoundStrCode={}", compoundStrCode);
        STRCodeSample lastSampleStrCode = sampleRepository.getLastSampleStrCode(compoundStrCode.toString());
        int sampleStrCode = lastSampleStrCode != null
                ? lastSampleStrCode.getSampleCode() + 1
                : 1;
        return new STRCodeSample(compoundStrCode.getCompoundCode(), compoundStrCode.getSaltCode(), sampleStrCode);
    }

    public SampleDTO markSample(UUID sampleID, boolean mark) {
        SampleEntity sample = sampleRepository.get(sampleID);
        if (mark) {
            sample.getMarkedBy().add(userService.getCurrentUserEntity());
        } else {
            sample.getMarkedBy().remove(userService.getCurrentUserEntity());
        }
        sample.setMarked(mark);
        return sampleMapper.sampleToDTO(sample);
    }

    private String calculateBatchMF(CompoundEntity compound) {
        StringBuilder sb = new StringBuilder();
        String parentFormula = MolFormulaFormatter.format(compound.getFormula());
        sb.append(parentFormula);
        if (compound.getSaltCode() != null) {
            SaltCodeRef salt = dictionaryService.get(compound.getSaltCode().getId());
            sb.append("&nbsp;*&nbsp;").append((compound.getSaltEQ())).append(" (").append(salt.getFormula()).append(")");
        }
        return sb.toString();
    }
}
