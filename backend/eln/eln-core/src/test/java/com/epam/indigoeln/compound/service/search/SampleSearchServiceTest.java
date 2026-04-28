package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.compound.model.search.*;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.entity.SaltCodeInfo;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.ComponentStateRef;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.HealthHazardRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeSample;
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
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.compound.model.search.SearchCatalog.ELN;
import static com.epam.indigoeln.compound.model.search.SearchCatalog.MY_MATERIALS;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@JwtSecurity
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class SampleSearchServiceTest extends ELNBaseTest {

    @Inject
    IndigoAPI indigo;
    @Inject
    CompoundService compoundService;
    @Inject
    DictionaryService dictionaryService;

    SaltCodeInfo saltCode;
    HealthHazardRef healthHazard;
    ComponentStateRef compoundState;
    CompoundRef.Virtual compound1;
    CompoundRef.Virtual compound2;
    STRCodeSample str1;
    UUID sampleID1;
    STRCodeSample str2;
    STRCodeSample strOtherCompound;

    @Test
    @Order(Integer.MIN_VALUE)
    void init() {
        List<DictionaryItemRef> saltCodes = dictionaryService.getSaltCodes();
        saltCode = dictionaryService.getSaltInfo(saltCodes.getFirst().getId());
        DictionaryItemRef healthHazardRef = dictionaryService.getDictionary(BuiltInDictionary.HEALTH_HAZARD.name()).getFirst();
        healthHazard = new HealthHazardRef(healthHazardRef.getId(), healthHazardRef.getName());
        DictionaryItemRef compoundStateRef = dictionaryService.getDictionary(BuiltInDictionary.COMPONENT_STATE.name()).getFirst();
        compoundState = new ComponentStateRef(compoundStateRef.getId(), compoundStateRef.getName());
        IndigoReaction reaction = indigo.loadReaction(loadResource(getClass(), "/reaction.rxn"));
        Iterator<IndigoMolecule> it = reaction.products().iterator();
        IndigoMolecule molecule = it.next();
        compound1 = compoundService.virtualCompoundRef(molecule, null, null, null);
        molecule = it.next();
        compound2 = compoundService.virtualCompoundRef(molecule, null, null, null);

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
        sample = compoundService.registerSample(new SampleRegistrationRequest(compound1));
        str2 = sample.getStrCode();
        sample = compoundService.registerSample(new SampleRegistrationRequest(compound2));
        strOtherCompound = sample.getStrCode();
    }

    @Test
    void testQuickSearch() {
        SampleSearchResult found = compoundClient.search(request(ELN).withQuickSearch("\"" + strOtherCompound + "\""), null, null, 10);
        assertThat(found.items()).singleElement().returns(strOtherCompound, SampleDTO::getStrCode);
        assertThat(found.nextCatalog()).isNull();
        assertThat(found.nextAfter()).isNull();
        assertThat(found.totalItems()).isEqualTo(1L);
    }

    @Test
    void testSearchRequestValidation() {
        assertThatClientCall(() -> {
            compoundClient.search(request(ELN).withCompoundKey(new TextSearch.ExactSearch(null)), null, null, 10);
        }).isBadRequest("must not be null");
    }

    @Test
    void testAdvancedSearch() {
        SampleSearchResult found = compoundClient.search(request(ELN)
                .withNbkBatchNumber(new TextSearch.ExactSearch("00000000-0000-001"))
                .withMolecularFormula(new TextSearch.ExactSearch("C9 H8 O4"))
                .withMolWeight(new NumericSearch.Equals(180.0))
                .withCompoundState(compoundState)
                .withBatchComment(new TextSearch.ExactSearch("batch comment"))
                .withHealthHazards(healthHazard)
                , null, null, 10
        );
        assertThat(found.items()).singleElement()
                .returns(str1, SampleDTO::getStrCode);
    }

    @Test
    void testAdvancedSearchStartsWith() {
        SampleSearchResult found = compoundClient.search(request(ELN)
                .withNbkBatchNumber(new TextSearch.StartsWithSearch("00000000-0000-"))
                , null, null, 10
        );
        assertThat(found.items()).singleElement().returns(str1, SampleDTO::getStrCode);
    }

    @Test
    void testAdvancedSearchContains() {
        SampleSearchResult found = compoundClient.search(request(ELN)
                .withNbkBatchNumber(new TextSearch.ContainsSearch("-0000-"))
                , null, null, 10
        );
        assertThat(found.items()).singleElement().returns(str1, SampleDTO::getStrCode);
    }

    @Test
    void testAdvancedSearchEndsWith() {
        SampleSearchResult found = compoundClient.search(request(ELN)
                .withNbkBatchNumber(new TextSearch.ContainsSearch("-001"))
                , null, null, 10
        );
        assertThat(found.items()).singleElement().returns(str1, SampleDTO::getStrCode);
    }

    @Test
    @Order(610)
    void testAdvancedSearchBetween() {
        SampleSearchResult found = compoundClient.search(request(ELN)
                .withNbkBatchNumber(new TextSearch.BetweenSearch("00000000-0000-000", "00000000-0000-999"))
                , null, null, 10
        );
        assertThat(found.items()).singleElement().returns(str1, SampleDTO::getStrCode);
    }

    @Test
    void testAdvancedSearchGreaterThenOrEqual() {
        SampleSearchResult found = compoundClient.search(request(ELN)
                .withMolWeight(new NumericSearch.GreaterThanOrEqual(100.0))
                , null, null, 10
        );
        assertThat(found.items()).isNotEmpty();
    }

    @Test
    void testAdvancedSearchLessThenOrEqual() {
        SampleSearchResult found = compoundClient.search(request(ELN)
                .withMolWeight(new NumericSearch.LessThanOrEqual(200.0))
                , null, null, 10
        );
        assertThat(found.items()).isNotEmpty();
    }

    @Test
    void testPaginationAndTotalItems() {
        String molFile = new String(loadResource(getClass(), "/ring-substructure.mol"));
        SearchCatalog nextCatalog = null;
        String nextAfter = null;
        boolean hasNext = true;
        Long totalItemsReported = null;
        long totalItemsActual = 0;
        while (hasNext) {
            SampleSearchResult found = compoundClient.search(request(ELN).withStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, molFile))
                    , nextCatalog, nextAfter, 1
            );
            if (totalItemsReported == null) {
                totalItemsReported = found.totalItems();
            } else {
                assertThat(found.totalItems()).isEqualTo(totalItemsReported);
            }
            totalItemsActual += found.items().size();
            nextCatalog = found.nextCatalog();
            nextAfter = found.nextAfter();
            hasNext = found.hasNext();
        }
        assertThat(totalItemsActual).isEqualTo(totalItemsReported);
    }

    @Test
    void testSearchMyMaterials() {
        SampleSearchResult page = compoundClient.search(request(MY_MATERIALS), null, null, 10);
        assertThat(page.items()).isEmpty();

        compoundService.markSample(sampleID1, true);
        page = compoundClient.search(request(MY_MATERIALS), null, null, 10);
        assertThat(page.items()).singleElement()
                .returns(sampleID1, SampleDTO::getId)
                .returns(true, SampleDTO::isMarked);

        page = compoundClient.search(request(MY_MATERIALS).withQuickSearch(str1.toString()), null, null, 10);
        assertThat(page.items()).singleElement().returns(sampleID1, SampleDTO::getId);

        page = compoundClient.search(request(MY_MATERIALS).withQuickSearch("nosuchcompound"), null, null, 10);
        assertThat(page.items()).isEmpty();

        compoundService.markSample(sampleID1, false);
        page = compoundClient.search(request(MY_MATERIALS), null, null, 10);
        assertThat(page.items()).isEmpty();
    }

    private FindSamplesRequest request(SearchCatalog... catalogs) {
        return new FindSamplesRequest().withCatalogs(Set.of(catalogs));
    }
}
