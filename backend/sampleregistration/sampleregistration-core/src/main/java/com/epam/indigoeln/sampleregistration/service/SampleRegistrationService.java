package com.epam.indigoeln.sampleregistration.service;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.common.util.SearchVector;
import com.epam.indigoeln.eln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.eln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.eln.indigowrapper.IndigoRendererAPI;
import com.epam.indigoeln.reaction.model.CompoundKey;
import com.epam.indigoeln.sampleregistration.entity.SRSCompoundEntity;
import com.epam.indigoeln.sampleregistration.entity.SRSSampleEntity;
import com.epam.indigoeln.sampleregistration.model.STRCodeCompound;
import com.epam.indigoeln.sampleregistration.model.STRCodeSample;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationRequest;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationResponse;
import com.epam.indigoeln.sampleregistration.repository.SRSCompoundRepository;
import com.epam.indigoeln.sampleregistration.repository.SRSSampleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Transactional
@ApplicationScoped
public class SampleRegistrationService {

    private static final String[] NAME_PROPERTIES = {"PUBCHEM_IUPAC_TRADITIONAL_NAME", "PUBCHEM_IUPAC_SYSTEMATIC_NAME", "PUBCHEM_IUPAC_OPENEYE_NAME"};

    @Inject
    EntityManager em;
    @Inject
    SRSCompoundRepository compoundRepository;
    @Inject
    SRSSampleRepository sampleRepository;
    @Inject
    IndigoAPI indigo;
    @Inject
    IndigoRendererAPI indigoRenderer;

    public SampleRegistrationResponse registerSample(SampleRegistrationRequest request) {
        IndigoMolecule molecule = indigo.loadMolecule(request.getMolfile());
        SRSCompoundEntity compound = findOrCreate(molecule, request.getStereoisomerCode(), request.getSaltCode(), request.getSaltCodeNumeric(), request.getSaltEQ100(), request.getMolWeight(), request.getExactMass(), request.getChemicalName(), request.getCasNumber());
        SRSSampleEntity sample = new SRSSampleEntity();
        sample.setCreatedAt(Instant.now());
        sample.setCompound(compound);
        sample.setStrCode(generateStrCode(compound));
        sample.setNbkBatchNumber(request.getNbkBatchNumber());
        sample.setDensity(request.getDensity());
        sample.setMolarity(request.getMolarity());
        sample.setMolarityUnit(request.getMolarityUnit());
        sample.setPurity(request.getPurity());
        if (request.getHealthHazards() != null) {
            sample.setHealthHazards(request.getHealthHazards().toArray(UUID[]::new));
        }
        sample.setCompoundState(request.getCompoundState());
        sample.setBatchComment(request.getBatchComment());
        sample.setSearchVector(collectSampleSearchVector(sample));
        sampleRepository.persist(sample);
        return new SampleRegistrationResponse(sample.getStrCode(), sample.getId());
    }

    public int loadCompoundsFromFile(Path file) {
        int inserted = 0;
        for (IndigoMolecule molecule : indigo.iterateSDFile(file.toAbsolutePath().toString())) {
            String chemicalName = ModelUtil.getAny(molecule.getProperties(), NAME_PROPERTIES);
            SRSCompoundEntity compound = findOrCreate(molecule, null, null, null, null, null, null, chemicalName, null);
            SRSSampleEntity sample = new SRSSampleEntity();
            sample.setCompound(compound);
            sample.setCreatedAt(Instant.now());
            sample.setStrCode(generateStrCode(compound));
            sample.setSearchVector(collectSampleSearchVector(sample));
            sampleRepository.persist(sample);
            inserted++;
        }
        log.info("Loaded {} compounds from file", inserted);
        return inserted;
    }

    private SRSCompoundEntity findOrCreate(IndigoMolecule molecule, @Nullable UUID stereoisomerCode, @Nullable UUID saltCode, @Nullable Integer saltCodeNumeric, @Nullable Integer saltCodeEQ100, @Nullable Double molWeight, @Nullable Double exactMass, @Nullable String chemicalName, @Nullable String casNumber) {
        CompoundKey compoundKey = new CompoundKey(molecule.canonicalSmiles(), stereoisomerCode, saltCode, saltCodeEQ100);
        SRSCompoundEntity compound = compoundRepository.findByCompoundKey(compoundKey);
        boolean isNew = compound == null;
        if (isNew) {
            compound = new SRSCompoundEntity();
            compound.setCanSmiles(compoundKey.getCanSmiles());
            compound.setStereoisomerCode(compoundKey.getStereoisomerCode());
            compound.setSaltCode(compoundKey.getSaltCode());
            compound.setSaltEQ100(compoundKey.getSaltEQ100());
            compound.setStrCode(generateStrCodeCompound(compoundKey, saltCodeNumeric));

            compound.setMolFile(molecule.molfile());
            compound.setMolWeight(molWeight != null ? molWeight : molecule.molecularWeight());
            compound.setExactMass(exactMass != null ? exactMass : molecule.monoisotopicMass());
            compound.setFormula(new MolFormula(molecule.molecularFormula()));

            indigoRenderer.setRenderOptions("svg", 300, 200);
            byte[] buf = indigoRenderer.renderToBuffer(molecule);
            compound.setPicture(buf);
        }
        if (compound.getChemicalName() == null) {
            compound.setChemicalName(chemicalName);
        }
        if (compound.getCasNumber() == null) {
            compound.setCasNumber(casNumber);
        }
        if (isNew) {
            compoundRepository.persist(compound);
        }
        return compound;
    }

    private STRCodeCompound generateStrCodeCompound(CompoundKey compoundKey, @Nullable Integer saltCodeNumeric) {
        STRCodeCompound strCodeWithoutSaltCode = compoundRepository.findSameSTRCodeByCompoundKeyWithoutSaltCode(compoundKey);
        log.debug("getNextSTRCodeCompound: strCodeWithoutSaltCode={}", strCodeWithoutSaltCode);
        int compoundCode = strCodeWithoutSaltCode != null
                ? strCodeWithoutSaltCode.getCompoundCode()
                : compoundRepository.getNextSTRCodeCompoundCode();
        return new STRCodeCompound(compoundCode, saltCodeNumeric != null ? saltCodeNumeric : 0);
    }

    private STRCodeSample generateStrCode(SRSCompoundEntity compound) {
        STRCodeSample lastSampleStrCode = sampleRepository.getLastSampleStrCode(compound.getStrCode().toString());
        int sampleStrCode = lastSampleStrCode != null
                ? lastSampleStrCode.getSampleCode() + 1
                : 1;
        return new STRCodeSample(compound.getStrCode().getCompoundCode(), compound.getStrCode().getSaltCode(), sampleStrCode);
    }

    private SearchVector collectSampleSearchVector(SRSSampleEntity sample) {
        SRSCompoundEntity c = sample.getCompound();
        SearchVector.Builder sv = new SearchVector.Builder()
                .aIdentifier(sample.getStrCode())
                .aIdentifier(sample.getNbkBatchNumber())
                .b(c.getChemicalName());
        return sv.build();
    }
}
