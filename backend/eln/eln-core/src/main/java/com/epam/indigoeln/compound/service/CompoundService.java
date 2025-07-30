package com.epam.indigoeln.compound.service;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.*;
import com.epam.indigoeln.compound.repository.CompoundRepository;
import com.epam.indigoeln.compound.repository.SampleRepository;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.SaltCodeRef;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.service.calculator.MolWeightCalculator;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.fixed;

@Slf4j
@Transactional
@ApplicationScoped
public class CompoundService {

    private static final String[] NAME_PROPERTIES = {"PUBCHEM_IUPAC_TRADITIONAL_NAME", "PUBCHEM_IUPAC_SYSTEMATIC_NAME", "PUBCHEM_IUPAC_OPENEYE_NAME"};
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
    MolWeightCalculator molWeightCalculator;

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
            compound.setFormula(molecule.grossFormula());
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
                        fillCompoundFromIndigo(molecule, compound);
                        sampleRepository.persist(sample);
                    }
                    stats.processed++;
                });
        log.info("Loaded compounds from file: {}", stats);
        return stats;
    }

    public CompoundRef.Stored realCompoundRef(CompoundEntity compound) {
        return new CompoundRef.Stored(compound.getId(), compound.getName(), fixed(compound.getMolWeight(), MolWeightUnit.G_PER_MOL), compound.getMolFile(), compound.getFormula());
    }

    public CompoundRef.Virtual virtualCompoundRef(IndigoMolecule molecule, @Nullable DictionaryItemRef stereoisomerCode, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ) {
        CompoundEntity compound = findOrCreate(molecule, stereoisomerCode, saltCode, saltEQ);
        double molWeight = molWeightCalculator.calculateMolWeight(molecule.molfile(), saltCode, saltEQ);
        return new CompoundRef.Virtual(compound.getId(), molecule.molfile(), molecule.grossFormula(), molWeight);
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
        for (String property : NAME_PROPERTIES) {
            if (molecule.hasProperty(property)) {
                compound.setName(molecule.getProperty(property));
                break;
            }
        }
        // TODO if PubChem compoundID found, search in PubChem?
//        for (String property : COMPOUND_ID_PROPERTIES) {
//            if (molecule.hasProperty(property)) {
//                compound.setCompoundId(molecule.getProperty(property));
//                break;
//            }
//        }
    }

    public List<SampleDTO> findSamples(FindSamplesRequest request) {
        return sampleMapper.sampleToDTOList(sampleRepository.find(request));
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
        STRCodeCompound compoundStrCode;
        if (compound.getStrCode() != null) {
            compoundStrCode = STRCodeCompound.parse(compound.getStrCode());
        } else {
            log.debug("registerSample: compound has no source code: {}", compound);
            CompoundKey key = new CompoundKey(compound.getCanSmiles(), compound.getStereoisomerCode() != null ? compound.getStereoisomerCode().getId() : null, compound.getSaltCode() != null ? compound.getSaltCode().getId() : null, compound.getSaltEQ100());
            String strCodeWithoutSaltCode = compoundRepository.findSameSTRCodeByCompoundKeyWithoutSaltCode(key);
            log.debug("registerSample: strCodeWithoutSaltCode={}", strCodeWithoutSaltCode);
            int compoundCode = strCodeWithoutSaltCode != null
                    ? STRCodeCompound.parse(strCodeWithoutSaltCode).getCompoundCode()
                    : compoundRepository.getNextSTRCodeCompoundCode();
            compoundStrCode = new STRCodeCompound(compoundCode, compound.getSaltCode() != null ? Integer.parseInt(compound.getSaltCode().getCode()) : 0);
            compound.setStrCode(compoundStrCode.toString());
        }
        log.debug("registerSample: compoundStrCode={}", compoundStrCode);
        SampleEntity sample = new SampleEntity();
        String lastSampleStrCode = sampleRepository.getLastSampleStrCode(compoundStrCode.toString());
        int sampleStrCode = lastSampleStrCode != null
                ? STRCodeSample.parse(lastSampleStrCode).getSampleCode() + 1
                : 1;
        sample.setStrCode(new STRCodeSample(compoundStrCode.getCompoundCode(), compoundStrCode.getSaltCode(), sampleStrCode).toString());
        sample.setCompound(compound);
        compound.getSamples().add(sample);
        sampleRepository.persist(sample);
        return sample;
    }

    @Data
    public static class LoadStatistics {

        private int processed;
    }
}
