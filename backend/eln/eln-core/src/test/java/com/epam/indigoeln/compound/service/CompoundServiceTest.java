package com.epam.indigoeln.compound.service;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolarityUnit;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class CompoundServiceTest extends ELNBaseTest {

    @Inject
    IndigoAPI indigo;
    @Inject
    CompoundService compoundService;
    @Inject
    DictionaryService dictionaryService;

    SaltCodeRef saltCode;
    HealthHazardRef healthHazard;
    ComponentStateRef compoundState;
    CompoundRef.Virtual compound1;
    CompoundRef.Virtual compound2;
    STRCodeSample str1;
    UUID sampleID1;
    STRCodeSample str2;
    STRCodeSample strOtherCompound;
    STRCodeSample strOtherSaltCode;
    STRCodeSample strOtherSaltEQ;
    String experimentName = "11111111-1111";
    String experimentName2 = "11111111-2222";

    @Test
    @Order(-1000)
    void testInit() {
        saltCode = dictionaryService.<SaltCodeRef>getDictionary(BuiltInDictionary.SALT_CODE.name(), false).getFirst();
        healthHazard = dictionaryService.<HealthHazardRef>getDictionary(BuiltInDictionary.HEALTH_HAZARD.name(), false).getFirst();
        compoundState = dictionaryService.<ComponentStateRef>getDictionary(BuiltInDictionary.COMPONENT_STATE.name(), false).getFirst();
        IndigoReaction reaction = indigo.loadReaction(loadResource(getClass(), "/reaction.rxn"));
        Iterator<IndigoMolecule> it = reaction.products().iterator();
        IndigoMolecule molecule = it.next();
        compound1 = compoundService.virtualCompoundRef(molecule, null, null, null);
        molecule = it.next();
        compound2 = compoundService.virtualCompoundRef(molecule, null, null, null);
    }

    @Test
    @Order(100)
    void testRegisterSample() {
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(compound1)
                .withNbkBatchNumber(new NbkBatchNumber("00000000-0000", 1))
                .withDensity(EnteredValue.userEntered("10.00", DensityUnit.G_ML, 1))
                .withMolarity(EnteredValue.userEntered("20", MolarityUnit.MM, 1))
                .withPurity(new BigDecimal("50.0"))
                .withHealthHazards(List.of(healthHazard))
                .withCompoundState(compoundState)
                .withBatchComment("batch comment")
        );
        str1 = sample.getStrCode();
        sampleID1 = sample.getId();
        assertThat(str1.getSaltCode()).as(str1.toString()).isZero();
        assertThat(str1.getSampleCode()).as(str1.toString()).isPositive();
        NbkBatchNumber nbkBatchNumber = sample.getNbkBatchNumber();
        assertThat(nbkBatchNumber.getExperimentName()).isEqualTo("00000000-0000");
        assertThat(nbkBatchNumber.getOrdinal()).isEqualTo(1);
        assertThat(nbkBatchNumber.toString()).isEqualTo("00000000-0000-001");
        assertThat(sample.getDensity()).isEqualTo(new BigDecimal("10.00"));
        assertThat(sample.getMolarity()).isEqualTo(new BigDecimal("20"));
        assertThat(sample.getMolarityUnit()).isEqualTo(MolarityUnit.MM);
        assertThat(sample.getPurity()).isEqualTo(new BigDecimal("50.0"));
        assertThat(sample.getHealthHazards()).map(IdentifiableEntity::getId).containsExactly(healthHazard.getId());
        assertThat(sample.getCompoundState()).extracting(IdentifiableEntity::getId).isEqualTo(compoundState.getId());
        assertThat(sample.getBatchComment()).isEqualTo("batch comment");
    }

    @Test
    @Order(200)
    void testRegisterSample2() {
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(compound1));
        str2 = sample.getStrCode();
        assertThat(str2.getCompoundCode()).as(str2.toString()).isEqualTo(str1.getCompoundCode());
        assertThat(str2.getSaltCode()).as(str2.toString()).isZero();
        assertThat(str2.getSampleCode()).as(str2.toString()).isEqualTo(str1.getSampleCode() + 1);
    }

    @Test
    @Order(300)
    void testRegisterSampleForOtherCompound() {
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(compound2));
        strOtherCompound = sample.getStrCode();
        assertThat(strOtherCompound.getCompoundCode()).as(strOtherCompound.toString()).isNotEqualTo(str1.getCompoundCode());
        assertThat(strOtherCompound.getSaltCode()).as(strOtherCompound.toString()).isZero();
        assertThat(strOtherCompound.getSampleCode()).as(strOtherCompound.toString()).isPositive();
    }

    @Test
    @Order(400)
    @Transactional
    void testRegisterSampleForOtherSaltCode() {
        IndigoMolecule molecule = indigo.loadMolecule(compoundService.getCompound(compound1.getCompoundID()).getMolFile());
        compound1 = compoundService.virtualCompoundRef(molecule, null, saltCode, 1.0);
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(compound1));
        strOtherSaltCode = sample.getStrCode();
        assertThat(strOtherSaltCode.getCompoundCode()).as(strOtherSaltCode.toString()).isEqualTo(str1.getCompoundCode());
        assertThat(strOtherSaltCode.getSaltCode()).as(strOtherSaltCode.toString()).isEqualTo(Integer.parseInt(saltCode.getCode()));
        assertThat(strOtherSaltCode.getSampleCode()).as(strOtherSaltCode.toString()).isPositive();
    }

    @Test
    @Order(500)
    @Transactional
    void testRegisterSampleForOtherSaltEQ() {
        IndigoMolecule molecule = indigo.loadMolecule(compoundService.getCompound(compound1.getCompoundID()).getMolFile());
        compound1 = compoundService.virtualCompoundRef(molecule, null, saltCode, 2.0);
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(compound1));
        strOtherSaltEQ = sample.getStrCode();
        assertThat(strOtherSaltEQ.getCompoundCode()).as(strOtherSaltEQ.toString()).isEqualTo(str1.getCompoundCode());
        assertThat(strOtherSaltEQ.getSaltCode()).as(strOtherSaltEQ.toString()).isEqualTo(strOtherSaltCode.getSaltCode());
        assertThat(strOtherSaltEQ.getSampleCode()).as(strOtherSaltEQ.toString()).isGreaterThan(strOtherSaltCode.getSampleCode());
    }
}
