package com.epam.indigoeln.compound.service;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.CompoundSource;
import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.repository.CompoundRepository;
import com.epam.indigoeln.compound.repository.SampleRepository;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
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
    IndigoAPI indigo;

    public Pair<CompoundEntity, SampleEntity> findOrCreateByCanonicalSmiles(String canonicalSmiles, IndigoMolecule indigoObject) {
        CompoundEntity compound = compoundRepository.findByCanonicalSmiles(canonicalSmiles);
        SampleEntity sample;
        if (compound == null) {
            compound = new CompoundEntity();
            compound.setCanonicalSmiles(canonicalSmiles);
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
        indigo.withSession(indigoSession -> {
            StreamEx.of(readSDFFile(is))
                    .map(indigoSession::loadMolecule)
                    .forEach(molecule -> {
                        findOrCreateByCanonicalSmiles(molecule.canonicalSmiles(), molecule);
                        stats.processed++;
                    });
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

    @Data
    public static class LoadStatistics {

        private int processed;
    }
}
