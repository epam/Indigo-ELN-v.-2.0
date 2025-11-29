package com.epam.indigoeln.compound.service;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.CompoundKey;
import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.compound.repository.CompoundRepository;
import com.epam.indigoeln.compound.repository.SampleRepository;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoRendererAPI;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.SaltCodeRef;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.service.calculator.MolWeightCalculator;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;
import static com.epam.indigoeln.reaction.model.units.EnteredValue.fixed;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class CompoundService {

//    private static final String[] NAME_PROPERTIES = {"PUBCHEM_IUPAC_TRADITIONAL_NAME", "PUBCHEM_IUPAC_SYSTEMATIC_NAME", "PUBCHEM_IUPAC_OPENEYE_NAME"};
    private static final String[] COMPOUND_ID_PROPERTIES = {"PUBCHEM_COMPOUND_CID"};

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

    public CompoundEntity findOrCreate(IndigoMolecule molecule, @Nullable DictionaryItemRef stereoisomerCode, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ) {
        String canSmiles = molecule.canonicalSmiles();
        CompoundKey key = new CompoundKey(canSmiles, stereoisomerCode != null ? stereoisomerCode.getId() : null, saltCode != null ? saltCode.getId() : null, saltEQ != null ? (int) (saltEQ * 100) : null);
        CompoundEntity compound = compoundRepository.findByCompoundKey(key);
        if (compound == null) {
            compound = new CompoundEntity();
            compound.setSource(CompoundSource.ELN);
            compound.setCanSmiles(canSmiles);
            compound.setStereoisomerCode(stereoisomerCode != null ? dictionaryService.get(stereoisomerCode.getId()) : null);
            compound.setSaltCode(saltCode != null ? dictionaryService.getSalt(saltCode.getId()) : null);
            compound.setSaltEQ100(saltEQ != null ? (int) (saltEQ * 100) : null);
            compound.setSource(CompoundSource.ELN);
            compound.setMolFile(molecule.molfile());
            compound.setMolWeight(molWeightCalculator.calculateMolWeight(molecule.molfile(), saltCode, saltEQ));
            compound.setExactMass(molWeightCalculator.calculateExactMass(molecule.molfile()));
            compound.setFormula(molecule.grossFormula());
            indigoRenderer.setRenderOptions("svg", 500, 200);
            byte[] buf = indigoRenderer.renderToBuffer(molecule);
            compound.setPicture(buf);
            compoundRepository.persist(compound);
            log.debug("new compound created: {}", compound);
        }
        return compound;
    }

    public LoadStatistics loadCompoundsFromFile(InputStream is) throws IOException {
        LoadStatistics stats = new LoadStatistics();
        StreamEx.of(readSDFFile(is))
                .map(indigo::loadMolecule)
                .forEach(molecule -> {
                    CompoundEntity compound = findOrCreate(molecule, null, null, null);
                    if (sampleRepository.findDefaultSample(compound.getId()) == null) {
                        SampleEntity sample = new SampleEntity();
                        compound.getSamples().add(sample);
                        sample.setCompound(compound);
                        updateDates(sample, userService.getCurrentUserEntity());
                        fillCompoundFromIndigo(molecule, compound);
                        sampleRepository.persist(sample);
                    }
                    stats.processed++;
                });
        log.info("Loaded compounds from file: {}", stats);
        return stats;
    }

    public CompoundRef.Stored realCompoundRef(CompoundEntity compound) {
        return new CompoundRef.Stored(compound.getId(), fixed(compound.getMolWeight(), MolWeightUnit.G_PER_MOL), compound.getExactMass(), compound.getFormula(), compound.getStrCode(), compound.getCasNumber());
    }

    public CompoundRef.Virtual virtualCompoundRef(IndigoMolecule molecule, @Nullable DictionaryItemRef stereoisomerCode, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ) {
        CompoundEntity compound = findOrCreate(molecule, stereoisomerCode, saltCode, saltEQ);
        return new CompoundRef.Virtual(compound.getId(), molecule.grossFormula(), stereoisomerCode, saltCode, saltEQ, EnteredValue.fixed(compound.getMolWeight(), MolWeightUnit.G_PER_MOL), compound.getExactMass(), compound.getCasNumber());
    }

    public CompoundRef.Unknown unknownCompoundRef() {
        return new CompoundRef.Unknown();
    }

    @SneakyThrows
    private List<String> readSDFFile(InputStream is) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
            while (true) {
                String line = rd.readLine();
                if (line == null || line.startsWith("$$$$")) {
                    if (!current.isEmpty()) {
                        result.add(current.toString());
                    }
                    if (line == null) {
                        break;
                    }
                    current.setLength(0);
                } else {
                    current.append(line).append('\n');
                }
            }
        }
        return result;
    }

    private void fillCompoundFromIndigo(IndigoMolecule molecule, CompoundEntity compound) {
        compound.setSource(CompoundSource.ELN);
        compound.setFormula(molecule.grossFormula());
        compound.setMolFile(molecule.molfile());
        compound.setMolWeight(molecule.molecularWeight());
//        for (String property : NAME_PROPERTIES) {
//            if (molecule.hasProperty(property)) {
//                compound.setName(molecule.getProperty(property));
//                break;
//            }
//        }
        // TODO if PubChem compoundID found, search in PubChem?
//        for (String property : COMPOUND_ID_PROPERTIES) {
//            if (molecule.hasProperty(property)) {
//                compound.setCompoundId(molecule.getProperty(property));
//                break;
//            }
//        }
    }

    public Response getCompoundPicture(UUID compoundID) {
        CompoundEntity compound = compoundRepository.get(compoundID);
        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(3_600 * 24 * 30);
        return Response.ok(compound.getPicture())
                .type("image/svg+xml")
                .cacheControl(cacheControl)
                .build();
    }

    public Page<SampleDTO> findSamples(FindSamplesRequest request, Paging paging) {
        return sampleRepository.find(request, paging);
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
        sample.setDensity(request.getDensity() != null ? request.getDensity().getValue() : null);
        sample.setMolarity(request.getMolarity() != null ? request.getMolarity().getValue() : null);
        sample.setMolarityUnit(request.getMolarity() != null ? request.getMolarity().getUnit() : null);
        sample.setPurity(request.getPurity());
        if (request.getHealthHazards() != null) {
            sample.getHealthHazards().addAll(dictionaryService.lookup(BuiltInDictionary.HEALTH_HAZARD.name(), request.getHealthHazards()));
        }
        sample.setCompoundState(dictionaryService.lookup(BuiltInDictionary.COMPONENT_STATE.name(), request.getCompoundState()));
        sample.setChemicalName(request.getChemicalName());
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
            compoundStrCode = new STRCodeCompound(compoundCode, compound.getSaltCode() != null ? Integer.parseInt(compound.getSaltCode().getCode()) : 0);
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

    @Data
    public static class LoadStatistics {

        private int processed;
    }
}
