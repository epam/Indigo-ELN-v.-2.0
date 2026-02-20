package com.epam.indigoeln.compound.service;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.*;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.entity.SaltCodeInfo;
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
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.common.util.ModelUtil.loadResourceAsStream;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@JwtSecurity
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class CompoundServiceTest extends ELNBaseTest {

    private static final Offset<Double> EPS = Offset.offset(0.0001);

    @Inject
    IndigoAPI indigo;
    @Inject
    CompoundService compoundService;
    @Inject
    DictionaryService dictionaryService;

    SaltCodeInfo saltCode;
    DictionaryItemRef healthHazard;
    DictionaryItemRef compoundState;
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
        List<DictionaryItemRef> saltCodes = dictionaryService.getSaltCodes();
        saltCode = dictionaryService.getSaltInfo(saltCodes.getFirst().getId());
        healthHazard = dictionaryService.getDictionary(BuiltInDictionary.HEALTH_HAZARD.name()).getFirst();
        compoundState = dictionaryService.getDictionary(BuiltInDictionary.COMPONENT_STATE.name()).getFirst();
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
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(compound1)
                .withNbkBatchNumber(new NbkBatchNumber("00000000-0000", 1))
                .withDensity(EnteredValue.userEntered("10.0", DensityUnit.G_ML, 1))
                .withMolarity(EnteredValue.userEntered("20.0", MolarityUnit.MM, 1))
                .withPurity(50.0)
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
        assertThat(sample.getDensity()).isEqualTo(10.0, EPS);
        assertThat(sample.getMolarity()).isEqualTo(20.0, EPS);
        assertThat(sample.getMolarityUnit()).isEqualTo(MolarityUnit.MM);
        assertThat(sample.getPurity()).isEqualTo(50.0, EPS);
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

    @Test
    @Order(600)
    void testQuickSearch() {
        Page<SampleDTO> found = compoundService.findSamples(new FindSamplesRequest().withQuickSearch("\"" + strOtherCompound + "\""), Paging.DEFAULT);
        assertThat(found.getItems()).singleElement().returns(strOtherCompound, SampleDTO::getStrCode);
    }

    @Test
    @Order(650)
    void testSearchRequestValidation() {
        assertThatClientCall(() -> {
            compoundClient.findSamples(new FindSamplesRequest()
                            .withCompoundKey(new TextSearch.ExactSearch(null))
                    , Paging.DEFAULT
            );
        }).isBadRequest("must not be null");
    }

    @Test
    @Order(610)
    void testAdvancedSearch() {
        Page<SampleDTO> found = compoundService.findSamples(new FindSamplesRequest()
                .withNbkBatchNumber(new TextSearch.ExactSearch("00000000-0000-001"))
                .withMolecularFormula(new TextSearch.ExactSearch("C9 H8 O4"))
                .withMolWeight(new NumericSearch.Equals(180.0))
                .withCompoundState(compoundState)
                .withBatchComment(new TextSearch.ExactSearch("batch comment"))
                .withHealthHazards(healthHazard)
                , Paging.DEFAULT
        );
        assertThat(found.getItems()).singleElement()
                .returns(str1, SampleDTO::getStrCode)
                .returns(false, SampleDTO::getMarked);
    }

    @Test
    @Order(610)
    void testAdvancedSearchStartsWith() {
        Page<SampleDTO> found = compoundService.findSamples(new FindSamplesRequest()
                .withNbkBatchNumber(new TextSearch.StartsWithSearch("00000000-0000-"))
                , Paging.DEFAULT
        );
        assertThat(found.getItems()).singleElement().returns(str1, SampleDTO::getStrCode);
    }

    @Test
    @Order(610)
    void testAdvancedSearchContains() {
        Page<SampleDTO> found = compoundService.findSamples(new FindSamplesRequest()
                .withNbkBatchNumber(new TextSearch.ContainsSearch("-0000-"))
                , Paging.DEFAULT
        );
        assertThat(found.getItems()).singleElement().returns(str1, SampleDTO::getStrCode);
    }

    @Test
    @Order(610)
    void testAdvancedSearchEndsWith() {
        Page<SampleDTO> found = compoundService.findSamples(new FindSamplesRequest()
                .withNbkBatchNumber(new TextSearch.ContainsSearch("-001"))
                , Paging.DEFAULT
        );
        assertThat(found.getItems()).singleElement().returns(str1, SampleDTO::getStrCode);
    }

    @Test
    @Order(610)
    void testAdvancedSearchBetween() {
        Page<SampleDTO> found = compoundService.findSamples(new FindSamplesRequest()
                .withNbkBatchNumber(new TextSearch.BetweenSearch("00000000-0000-000", "00000000-0000-999"))
                , Paging.DEFAULT
        );
        assertThat(found.getItems()).singleElement().returns(str1, SampleDTO::getStrCode);
    }

    @Test
    @Order(610)
    void testAdvancedSearchGreaterThenOrEqual() {
        Page<SampleDTO> found = compoundService.findSamples(new FindSamplesRequest()
                .withMolWeight(new NumericSearch.GreaterThanOrEqual(100.0))
                , Paging.DEFAULT
        );
        assertThat(found.getItems()).isNotEmpty();
    }

    @Test
    @Order(610)
    void testAdvancedSearchLessThenOrEqual() {
        Page<SampleDTO> found = compoundService.findSamples(new FindSamplesRequest()
                .withMolWeight(new NumericSearch.LessThanOrEqual(200.0))
                , Paging.DEFAULT
        );
        assertThat(found.getItems()).isNotEmpty();
    }

    @Test
    @Order(700)
    void testListMarkedSamplesBefore() {
        Page<SampleDTO> page = compoundService.findSamples(new FindSamplesRequest().withMarked(true), Paging.DEFAULT);
        assertThat(page.getItems()).isEmpty();
    }

    @Test
    @Order(701)
    void testMarkSample() {
        compoundService.markSample(sampleID1, true);
    }

    @Test
    @Order(702)
    void testListMarkedSamples() {
        Page<SampleDTO> page = compoundService.findSamples(new FindSamplesRequest().withMarked(true), Paging.DEFAULT);
        assertThat(page.getItems()).singleElement()
                .returns(sampleID1, SampleDTO::getId)
                .returns(true, SampleDTO::getMarked);
    }

    @Test
    @Order(703)
    void testListMarkedSamplesQuickSearch() {
        Page<SampleDTO> page = compoundService.findSamples(new FindSamplesRequest().withQuickSearch(str1.toString()).withMarked(true), Paging.DEFAULT);
        assertThat(page.getItems()).singleElement().returns(sampleID1, SampleDTO::getId);
    }

    @Test
    @Order(704)
    void testListMarkedSamplesQuickSearchNotFound() {
        Page<SampleDTO> page = compoundService.findSamples(new FindSamplesRequest().withQuickSearch("nosuchcompound").withMarked(true), Paging.DEFAULT);
        assertThat(page.getItems()).isEmpty();
    }

    @Test
    @Order(705)
    void testUnmarkSample() {
        compoundService.markSample(sampleID1, false);
        Page<SampleDTO> page = compoundService.findSamples(new FindSamplesRequest().withMarked(true), Paging.DEFAULT);
        assertThat(page.getItems()).isEmpty();
    }
}
