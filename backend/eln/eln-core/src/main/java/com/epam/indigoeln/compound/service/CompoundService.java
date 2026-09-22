package com.epam.indigoeln.compound.service;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.common.model.units.MolWeightUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.repository.CompoundRepository;
import com.epam.indigoeln.compound.repository.SampleRepository;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.eln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.eln.indigowrapper.IndigoRendererAPI;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
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
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.MOL_WEIGHT_DECIMAL_PLACES;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class CompoundService {

    private static final String[] NAME_PROPERTIES = {"PUBCHEM_IUPAC_TRADITIONAL_NAME", "PUBCHEM_IUPAC_SYSTEMATIC_NAME", "PUBCHEM_IUPAC_OPENEYE_NAME"};

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

    public CompoundEntity findOrCreate(IndigoMolecule molecule, @Nullable StereoisomerCodeRef stereoisomerCode, @Nullable SaltCodeRef saltCode, @Nullable Integer saltEQ100, @NotNull SampleSource source, @Nullable String compoundKey) {
        String canSmiles = molecule.canonicalSmiles();
        CompoundKey key = new CompoundKey(canSmiles, stereoisomerCode != null ? stereoisomerCode.getId() : null, saltCode != null ? saltCode.getId() : null, saltEQ100);
        CompoundEntity compound = compoundRepository.findByCompoundKey(key);
        if (compound == null) {
            compound = new CompoundEntity();
            compound.setCanSmiles(canSmiles);
            compound.setStereoisomerCode(dictionaryService.lookup(stereoisomerCode, true));
            compound.setSaltCode(dictionaryService.lookup(saltCode));
            compound.setSaltEQ100(saltEQ100);
            compound.setSource(source);
            compound.setCompoundKey(compoundKey);
            compound.setMolFile(molecule.molfile());
            compound.setMolWeight(molWeightCalculator.calculateMolWeight(molecule.molfile(), saltCode, compound.getSaltEQ()));
            compound.setExactMass(molWeightCalculator.calculateExactMass(molecule.molfile()));
            compound.setFormula(new MolFormula(molecule.molecularFormula()));
            indigoRenderer.setRenderOptions("svg", 300, 200);
            byte[] buf = indigoRenderer.renderToBuffer(molecule);
            compound.setPicture(buf);
            compoundRepository.persist(compound);
            log.debug("new compound created: {}", compound);
        }
        return compound;
    }

    public List<UUID> loadCompoundsFromFile(Path file, boolean createSamples) {
        List<UUID> list = new ArrayList<>();
        for (IndigoMolecule molecule : indigo.iterateSDFile(file.toAbsolutePath().toString())) {
            Pair<SampleSource, @Nullable String> source = detectCompoundSource(molecule);
            CompoundEntity compound = findOrCreate(molecule, null, null, null, source.a(), source.b());
            updateCompoundProperties(molecule, compound);
            if (createSamples) {
                findOrCreateDefaultSample(compound);
            }
            list.add(compound.getId());
        }
        log.info("Loaded {} compounds from file", list.size());
        return list;
    }

    public SampleEntity findOrCreateDefaultSample(CompoundEntity compound) {
        SampleEntity sample = sampleRepository.findDefaultSample(compound.getId());
        if (sample == null) {
            sample = new SampleEntity();
            sample.setCompound(compound);
            sample.setSource(compound.getSource());
            updateDates(sample, userService.getCurrentUserEntity());
            sample.setSearchVector(globalSearchService.collectSampleSearchVector(sample));
            sampleRepository.persist(sample);
        }
        return sample;
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
        CompoundEntity compound = findOrCreate(molecule, stereoisomerCode, saltCode, saltEQ != null ? (int) (saltEQ * 100.0) : null, SampleSource.ELN, null);
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

    private Pair<SampleSource, @Nullable String> detectCompoundSource(IndigoMolecule molecule) {
        String cid = molecule.getProperties().get("PUBCHEM_COMPOUND_CID");
        if (cid != null) {
            return Pair.of(SampleSource.PUBCHEM, cid);
        }
        return Pair.of(SampleSource.ELN, null);
    }

    private void updateCompoundProperties(IndigoMolecule molecule, CompoundEntity compound) {
        Map<String, String> properties = molecule.getProperties();
        if (compound.getChemicalName() == null) {
            for (String property : NAME_PROPERTIES) {
                if (properties.containsKey(property)) {
                    compound.setChemicalName(properties.get(property));
                    break;
                }
            }
        }
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
        IndigoMolecule molecule = indigo.loadMolecule(request.getMolfile());
        CompoundEntity compound = findOrCreate(molecule
                , request.getStereoisomerCode() != null ? dictionaryService.<StereoisomerCodeRef>get(request.getStereoisomerCode()) : null
                , request.getSaltCode() != null ? dictionaryService.get(request.getSaltCode()) : null
                , request.getSaltEQ100()
                , SampleSource.ELN
                , null
        );
        SampleEntity sample = new SampleEntity();
        sample.setCompound(compound);
        sample.setSource(SampleSource.ELN);
        sample.setSampleKey(request.getNbkBatchNumber());
        sample.setNbkBatchNumber(NbkBatchNumber.parse(request.getNbkBatchNumber()));
        sample.setDensity(request.getDensity());
        sample.setMolarity(request.getMolarity());
        sample.setMolarityUnit(request.getMolarityUnit());
        sample.setPurity(request.getPurity());
        if (request.getHealthHazards() != null) {
            sample.setHealthHazards(StreamEx.of(request.getHealthHazards()).map(x -> dictionaryService.lookup(x)).toSet());
        }
        sample.setCompoundState(request.getCompoundState() != null ? dictionaryService.lookup(request.getCompoundState()) : null);
        sample.setBatchComment(request.getBatchComment());
        updateDates(sample, userService.getCurrentUserEntity());
        sample.setSearchVector(globalSearchService.collectSampleSearchVector(sample));
        sampleRepository.persist(sample);
        return sample;
    }

    public void updateSample(SampleEntity sample, Consumer<SampleEntity> consumer) {
        consumer.accept(sample);
        sample.setSearchVector(globalSearchService.collectSampleSearchVector(sample));
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
        String parentFormula = compound.getFormula().toHTMLString();
        sb.append(parentFormula);
        if (compound.getSaltCode() != null) {
            SaltCodeRef salt = dictionaryService.get(compound.getSaltCode().getId());
            sb.append("&nbsp;*&nbsp;").append((compound.getSaltEQ())).append(" (").append(salt.getFormula()).append(")");
        }
        return sb.toString();
    }
}
