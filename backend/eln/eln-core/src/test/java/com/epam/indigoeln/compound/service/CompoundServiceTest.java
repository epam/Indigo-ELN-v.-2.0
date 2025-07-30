package com.epam.indigoeln.compound.service;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.STRCodeCompound;
import com.epam.indigoeln.compound.model.STRCodeSample;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.util.TestHelper;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.SaltCodeRef;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.common.util.ModelUtil.loadResourceAsStream;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@JwtSecurity
@TestSecurity(user = TestHelper.JOHN_USERNAME)
public class CompoundServiceTest extends BaseTest {

    @Inject
    IndigoAPI indigo;
    @Inject
    CompoundService compoundService;
    @Inject
    DictionaryService dictionaryService;

    SaltCodeRef saltCode;
    CompoundRef.Virtual compound1;
    CompoundRef.Virtual compound2;
    STRCodeSample str1;
    STRCodeSample str2;
    STRCodeSample strOtherCompound;
    STRCodeSample strOtherSaltCode;
    STRCodeSample strOtherSaltEQ;

    @Test
    @Order(-1000)
    void testInit() {
        List<DictionaryItemRef> saltCodes = dictionaryService.getSaltCodes();
        saltCode = dictionaryService.getSaltRef(saltCodes.getFirst().getId());
        IndigoReaction reaction = indigo.loadReaction(loadResource(getClass(), "/reaction.rxn"));
        Iterator<IndigoMolecule> it = reaction.products().iterator();
        IndigoMolecule molecule = it.next();
        compound1 = compoundService.virtualCompoundRef(molecule, null, null, null);
        molecule = it.next();
        compound2 = compoundService.virtualCompoundRef(molecule, null, null, null);
    }

//    @Test
    void testLoadCompounds() throws Exception {
        try (InputStream is = loadResourceAsStream(getClass(), "/Compound_000000001_000500000.1.sdf")) {
            var stats = compoundService.loadCompoundsFromFile(is);
            assertThat(stats.getProcessed()).isPositive();
        }
    }

    @Test
    @Order(100)
    void testRegisterSample() {
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(compound1));
        str1 = STRCodeSample.parse(sample.getStrCode());
        assertThat(str1.getSaltCode()).as(str1.toString()).isZero();
        assertThat(str1.getSampleCode()).as(str1.toString()).isPositive();
    }

    @Test
    @Order(200)
    void testRegisterSample2() {
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(compound1));
        str2 = STRCodeSample.parse(sample.getStrCode());
        assertThat(str2.getCompoundCode()).as(str2.toString()).isEqualTo(str1.getCompoundCode());
        assertThat(str2.getSaltCode()).as(str2.toString()).isZero();
        assertThat(str2.getSampleCode()).as(str2.toString()).isEqualTo(str1.getSampleCode() + 1);
    }

    @Test
    @Order(300)
    void testRegisterSampleForOtherCompound() {
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(compound2));
        strOtherCompound = STRCodeSample.parse(sample.getStrCode());
        assertThat(strOtherCompound.getCompoundCode()).as(strOtherCompound.toString()).isNotEqualTo(str1.getCompoundCode());
        assertThat(strOtherCompound.getSaltCode()).as(strOtherCompound.toString()).isZero();
        assertThat(strOtherCompound.getSampleCode()).as(strOtherCompound.toString()).isPositive();
    }

    @Test
    @Order(400)
    void testRegisterSampleForOtherSaltCode() {
        IndigoMolecule molecule = indigo.loadMolecule(compound1.getMolFile());
        compound1 = compoundService.virtualCompoundRef(molecule, null, saltCode, 1.0);
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(compound1));
        strOtherSaltCode = STRCodeSample.parse(sample.getStrCode());
        assertThat(strOtherSaltCode.getCompoundCode()).as(strOtherSaltCode.toString()).isEqualTo(str1.getCompoundCode());
        assertThat(strOtherSaltCode.getSaltCode()).as(strOtherSaltCode.toString()).isEqualTo(Integer.parseInt(saltCode.getCode()));
        assertThat(strOtherSaltCode.getSampleCode()).as(strOtherSaltCode.toString()).isPositive();
    }

    @Test
    @Order(500)
    void testRegisterSampleForOtherSaltEQ() {
        IndigoMolecule molecule = indigo.loadMolecule(compound1.getMolFile());
        compound1 = compoundService.virtualCompoundRef(molecule, null, saltCode, 2.0);
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(compound1));
        strOtherSaltEQ = STRCodeSample.parse(sample.getStrCode());
        assertThat(strOtherSaltEQ.getCompoundCode()).as(strOtherSaltEQ.toString()).isEqualTo(str1.getCompoundCode());
        assertThat(strOtherSaltEQ.getSaltCode()).as(strOtherSaltEQ.toString()).isEqualTo(strOtherSaltCode.getSaltCode());
        assertThat(strOtherSaltEQ.getSampleCode()).as(strOtherSaltEQ.toString()).isGreaterThan(strOtherSaltCode.getSampleCode());
    }
}
