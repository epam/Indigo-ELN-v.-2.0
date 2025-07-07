package com.epam.indigoeln.compound.service;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.*;
import com.epam.indigoeln.compound.repository.CompoundRepository;
import com.epam.indigoeln.compound.repository.SampleRepository;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.reaction.model.CompoundRef;
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
import java.util.List;
import java.util.UUID;

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

    // TODO switch to CompoundKey
    public Pair<CompoundEntity, SampleEntity> findOrCreateByCanonicalSmiles(String canSmiles, IndigoMolecule indigoObject) {
        CompoundEntity compound = compoundRepository.findByCompoundKey(new CompoundKey(canSmiles, null, null, null), true);
        SampleEntity sample;
        if (compound == null) {
            compound = new CompoundEntity();
            compound.setCanSmiles(canSmiles);
            sample = new SampleEntity();
            compound.getSamples().add(sample);
            sample.setCompound(compound);
            fillCompoundFromIndigo(indigoObject, compound);
            compoundRepository.persist(compound);
            sampleRepository.persist(sample);
        } else {
            sample = sampleRepository.findDefaultSample(compound.getId());
        }
        return Pair.of(compound, sample); // TODO specify which sample to use
    }

    public LoadStatistics loadCompoundsFromFile(InputStream is) throws IOException {
        LoadStatistics stats = new LoadStatistics();
        StreamEx.of(readSDFFile(is))
                .map(indigo::loadMolecule)
                .forEach(molecule -> {
                    findOrCreateByCanonicalSmiles(molecule.canonicalSmiles(), molecule);
                    stats.processed++;
                });
        log.info("Loaded compounds from file: {}", stats);
        return stats;
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
            case CompoundRef.Virtual virtual -> findOrCreateCompound(virtual, indigo.loadMolecule(virtual.getMolFile()));
            case CompoundRef.Unknown unknown -> {
                throw new IllegalArgumentException("Cannot register a sample of an unknown compound");
            }
        };
        SampleEntity sample = new SampleEntity();
        STRCode compoundStrCode = STRCode.parse(compound.getStrCode());
        SampleEntity lastCompoundSample = sampleRepository.getLastSampleByStrCode(compound);
        int sampleStrCode = lastCompoundSample != null
                ? STRCode.parse(lastCompoundSample.getStrCode()).getSampleCode() + 1
                : 1;
        sample.setStrCode(new STRCode(compoundStrCode.getCompoundCode(), compoundStrCode.getSaltCode(), sampleStrCode).toString());
        sample.setCompound(compound);
        compound.getSamples().add(sample);
        sampleRepository.persist(sample);
        return sample;
    }

    private CompoundEntity findOrCreateCompound(CompoundRef.Virtual virtual, IndigoMolecule molecule) {
        CompoundKey key = new CompoundKey(molecule.canonicalSmiles()
                , virtual.getStereoisomerCode() != null ? virtual.getStereoisomerCode().getId() : null
                , virtual.getSaltCode() != null ? virtual.getSaltCode().getId() : null
                , virtual.getSaltEQ() != null ? (int) (virtual.getSaltEQ() * 100) : null
        );
        log.debug("findOrCreateCompound: key={}", key);
        CompoundEntity found = compoundRepository.findByCompoundKey(key, true);
        log.debug("findOrCreateCompound: found={}", found);
        if (found == null) {
            CompoundEntity foundIgnoringSaltCode = compoundRepository.findByCompoundKey(key, false);
            log.debug("findOrCreateCompound: foundIgnoringSaltCode={}", foundIgnoringSaltCode);
            int strCodeCompound = foundIgnoringSaltCode != null
                    ? STRCode.parse(foundIgnoringSaltCode.getStrCode()).getCompoundCode()
                    : compoundRepository.getNextSTRCodeCompoundCode();
            log.debug("findOrCreateCompound: strCodeCompound={}", strCodeCompound);
            found = new CompoundEntity();
            found.setSource(CompoundSource.ELN);
            found.setMolFile(virtual.getMolFile());
            found.setCanSmiles(key.getCanSmiles());
            DictionaryItemEntity stereoisomerCode = key.getStereoisomerCode() != null ? dictionaryService.get(key.getStereoisomerCode()) : null;
            found.setStereoisomerCode(stereoisomerCode);
            SaltCodeEntity saltCode = key.getSaltCode() != null ? dictionaryService.getSalt(key.getSaltCode()) : null;
            found.setSaltCode(saltCode);
            found.setSaltEQ100(key.getSaltEQ100());
            found.setFormula(virtual.getFormula());
            found.setMolWeight(virtual.getMolWeight().getValue());
            found.setStrCode(new STRCode(strCodeCompound, saltCode != null ? Integer.parseInt(saltCode.getCode()) : 0, null).toString());
            log.info("Registering new compound with STR code: {}", found.getStrCode());
            compoundRepository.persist(found);
        }
        return found;
    }

    @Data
    public static class LoadStatistics {

        private int processed;
    }
}
